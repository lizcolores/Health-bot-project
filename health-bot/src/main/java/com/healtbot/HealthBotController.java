package com.healtbot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.healtbot.model.CardiovascularRiskRequest;
import com.healtbot.model.FeaturesRequest;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@RestController
@EnableSwagger2
@Configuration
public class HealthBotController {

    @Value("#{${healthbot.summary.age} ?: null}")
    private Map<String, Float> summaryAge;

    @Value("#{${healthbot.summary.physical.activity} ?: null}")
    private Map<String, Float> summaryPhysicalActivity;

    @Value("#{${healthbot.summary.gender} ?: null}")
    private Map<String, Float> summaryGender;

    @Value("#{${healthbot.summary.smoke} ?: null}")
    private Map<String, Float> summarySmoke;

    @Value("#{${healthbot.summary.drink} ?: null}")
    private Map<String, Float> summaryDrink;

    @Value("#{${healthbot.summary.weight} ?: null}")
    private Map<String, Float> summaryWeight;

    @Value("#{${healthbot.summary.height} ?: null}")
    private Map<String, Float> summaryHeight;

    @Value("#{${healthbot.summary.phi} ?: null}")
    private Map<String, Float> summaryPhi;

    @Value("#{${healthbot.summary.plo} ?: null}")
    private Map<String, Float> summaryPlo;

    @Value("#{${healthbot.summary.cholesterol} ?: null}")
    private Map<String, Float> summaryCholesterol;

    @Value("#{${healthbot.summary.glucose} ?: null}")
    private Map<String, Float> summaryGlucose;

    @Value("#{${healthbot.summary.bmi} ?: null}")
    private Map<String, Float> summaryBmi;

    @Value("#{${healthbot.summary.risk} ?: null}")
    private Map<String, Float> summaryRisk;

    @Value("${healthbot.summary.text.font.default.size:null}")
    private Float fontDefaultSize;

    @Value("${cardio.sight.endpoint}")
    String cardioSightEndPoint;

    @Autowired
    ResourceLoader resourceLoader;

    private Log logger = LogFactory.getLog(getClass());

    @PostMapping("/getCardiovascularRisk")
    public ResponseEntity<byte[]> getCardiovascularRisk(@RequestBody FeaturesRequest features) {

        // ['age', 'gender', 'ap_hi', 'ap_lo', 'cholesterol', 'gluc', 'smoke', 'alco',
        // 'active', 'newvalues_height', 'newvalues_weight','New_values_BMI']
        // Variable - 'ap_hi' or Systolic blood pressure : Represents the systolic blood
        // pressure status
        // Variable - 'ap_lo' or Diastolic blood pressure : Represents the diastolic
        // blood pressure status
        CardiovascularRiskRequest cardiovascularRiskRequest;

        cardiovascularRiskRequest = new CardiovascularRiskRequest();
        cardiovascularRiskRequest.setAge(getAge(features.getFeatures().get(0)));
        cardiovascularRiskRequest.setGender(getGender(features.getFeatures().get(1)));
        cardiovascularRiskRequest.setPHi(features.getFeatures().get(2).toString());
        cardiovascularRiskRequest.setPLo(features.getFeatures().get(3).toString());
        cardiovascularRiskRequest.setCholesterol(getName(features.getFeatures().get(4)));
        cardiovascularRiskRequest.setGlucose(getName(features.getFeatures().get(5)));
        cardiovascularRiskRequest.setSmoke(getSmoke(features.getFeatures().get(6)));
        cardiovascularRiskRequest.setDrink(getExpression(features.getFeatures().get(7)));
        cardiovascularRiskRequest.setPhysicalActivity(getExpression(features.getFeatures().get(8)));
        cardiovascularRiskRequest.setHeight(features.getFeatures().get(9).toString());
        cardiovascularRiskRequest.setWeight(features.getFeatures().get(10).toString());

        features.getFeatures().add(getBMI(features.getFeatures().get(9), features.getFeatures().get(10)));
        cardiovascularRiskRequest.setRisk(getPredictResult(features.getFeatures()));
        cardiovascularRiskRequest.setBmi(features.getFeatures().get(11).toString());
        return generatePdf(cardiovascularRiskRequest);
    }

    protected void addText(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float x,
            float y) {
        try {
            if (text != null && x > 0 && y > 0) {
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(text);
                contentStream.endText();
            }
        } catch (Exception e) {
            logger.error("----Unable to add text: " + text, e);
        }
    }

    private ResponseEntity<byte[]> generatePdf(CardiovascularRiskRequest cardiovascularRiskRequest) {
        PDPage page;
        PDFont defaultFont;
        PDDocument document;
        byte[] receiptBody;
        InputStream templateStream;
        ResponseEntity<byte[]> returnValue;
        ByteArrayOutputStream outputStream;
        String templateDocumentPath;
        PDPageContentStream contentStream;

        document = null;
        outputStream = null;
        returnValue = ResponseEntity.notFound().build();
        try {

            defaultFont = PDType1Font.HELVETICA;
            templateDocumentPath = "classpath:templates/summary.pdf";

            templateStream = resourceLoader.getResource(templateDocumentPath).getInputStream();
            document = PDDocument.load(templateStream);

            page = document.getPage(0);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false, true);

            addText(contentStream, cardiovascularRiskRequest.getAge(), defaultFont, fontDefaultSize,
                    summaryAge.get("x"), summaryAge.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getPhysicalActivity(), defaultFont, fontDefaultSize,
                    summaryPhysicalActivity.get("x"), summaryPhysicalActivity.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getGender(), defaultFont, fontDefaultSize,
                    summaryGender.get("x"), summaryGender.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getSmoke(), defaultFont, fontDefaultSize,
                    summarySmoke.get("x"), summarySmoke.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getDrink(), defaultFont, fontDefaultSize,
                    summaryDrink.get("x"), summaryDrink.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getWeight(), defaultFont, fontDefaultSize,
                    summaryWeight.get("x"), summaryWeight.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getHeight(), defaultFont, fontDefaultSize,
                    summaryHeight.get("x"), summaryHeight.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getPHi(), defaultFont, fontDefaultSize,
                    summaryPhi.get("x"), summaryPhi.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getPLo(), defaultFont, fontDefaultSize,
                    summaryPlo.get("x"), summaryPlo.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getCholesterol(), defaultFont, fontDefaultSize,
                    summaryCholesterol.get("x"), summaryCholesterol.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getGlucose(), defaultFont, fontDefaultSize,
                    summaryGlucose.get("x"), summaryGlucose.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getBmi(), defaultFont, fontDefaultSize,
                    summaryBmi.get("x"), summaryBmi.get("y"));
            addText(contentStream, cardiovascularRiskRequest.getRisk(), defaultFont, fontDefaultSize,
                    summaryRisk.get("x"), summaryRisk.get("y"));
            contentStream.close();

            outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();
            receiptBody = outputStream.toByteArray();
            if (receiptBody != null) {
                returnValue = ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"health-bot-summary.pdf\"")
                        .body(receiptBody);
            }

        } catch (IOException e) {
            logger.error("----Unable to generate template", e);
        }

        return returnValue;
    }

    private String getPredictResult(List<Integer> values) {
        String returnValue;
        ResponseEntity<JsonNode> predictValue;
        RestTemplate restTemplate;
        HttpHeaders httpHeaders;
        FeaturesRequest featuresRequest;

        predictValue = null;
        restTemplate = new RestTemplate();
        httpHeaders = new HttpHeaders();
        returnValue = "NIVEL DE RIESGO BAJO";
        featuresRequest = new FeaturesRequest();

        featuresRequest.setFeatures(values);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        predictValue = restTemplate.exchange(cardioSightEndPoint, HttpMethod.POST,
                new HttpEntity<>(featuresRequest, httpHeaders), JsonNode.class);

        if (new BigDecimal(predictValue.getBody().get("prediction").asDouble())
                .compareTo(new BigDecimal(0.475193)) >= 0) {
            returnValue = "NIVEL DE RIESGO ALTO";
        }

        return returnValue;
    }

    private String getAge(Integer age) {
        return String.valueOf(age / 365);
    }

    private String getGender(Integer gender) {
        switch (gender) {
        case 0:
            return "SIN GÉNERO";
        case 1:
            return "FEMENINO";
        case 2:
            return "MASCULINO";
        default:
            break;
        }
        return null;
    }

    private String getName(Integer value) {
        switch (value) {
        case 1:
            return "NORMAL";
        case 2:
            return "ARRIVAL DE LO NORMAL";
        case 3:
            return "MUY ARRIBA DE LO NORMAL";
        default:
            break;
        }
        return null;
    }

    private Integer getBMI(Integer height, Integer weight) {
        return (int) (weight / ((height * 2) / 100));
    }

    private String getSmoke(Integer smoke) {
        switch (smoke) {
        case 0:
            return "NO";
        case 1:
            return "SI";
        default:
            break;
        }
        return null;
    }

    private String getExpression(Integer value) {
        switch (value) {
        case 0:
            return "NO";
        case 1:
            return "SI";
        case 3:
            return "NO MUY SEGUIDO";
        default:
            break;
        }
        return null;
    }
}
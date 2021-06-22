package com.healtbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CardiovascularRiskRequest {
    private String age;
    private String physicalActivity;
    private String gender;
    private String smoke;
    private String drink;
    private String weight;
    private String height;
    private String pHi;
    private String pLo;
    private String cholesterol;
    private String glucose;
    private String risk;
    private String bmi;
}

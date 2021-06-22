import joblib
import flask
from flask import jsonify
import os

app = flask.Flask(__name__)
port = int(os.getenv("PORT", 9099))

#inputs = [18393,2,110.00,80.00,3,1,1,0,1,168,62,23]

def loadmodel():
    RFM = joblib.load(open("rf_random.pkl","rb"))
    return RFM

@app.route('/predict', methods=['POST'])
def predict():  
    RandomForestModel = loadmodel()
    features = flask.request.get_json(force=True)['features']
    prediction = RandomForestModel.predict_proba([features])[:,1]
    return jsonify(
        prediction=prediction[0]
    )
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=port)
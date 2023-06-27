package com.example.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.logging.Handler;

public class Accelerometer implements SensorEventListener {

    com.example.myapplication.MainActivity mainActivity;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Context context;
    private TextView respiratoryRateLabel;

    String TAG = "SensorAccelerometer Class";

    float timestamp;
    public float respiratoryRate = 0;

    float ts = 0.0f;
    int tmp = 0;

    public ArrayList<Double> zAxisValueArray = new ArrayList<Double>();
    public ArrayList<Double> yAxisValueArray = new ArrayList<Double>();
    public ArrayList<Double> xAxisValueArray = new ArrayList<Double>();

    public Accelerometer(Context context, float timestamp, TextView t) {
        this.timestamp = timestamp;
        this.context = context;
        this.respiratoryRateLabel = t;
        initialiseSensor();

    }


    public void initialiseSensor(){
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i(TAG, "initialiseSensor: "+accelerometer.getReportingMode());
        sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor(){
        sensorManager.unregisterListener(this);
        //Toast.makeText(context, "Sensor Stopped..", Toast.LENGTH_SHORT).show();
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int timeLimit = 45;

    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (tmp == 0) {
                tmp += 1;
                ts = sensorEvent.timestamp * 1.0f / 1000000000.0f;
            }

            int i = (int) (ts - (sensorEvent.timestamp * 1.0f / 1000000000.0f));

            if (-i <= timeLimit) {
                double xVal = sensorEvent.values[0];
                double yVal = sensorEvent.values[1];
                double zVal = sensorEvent.values[2];

                xAxisValueArray.add(xVal);
                yAxisValueArray.add(yVal);
                zAxisValueArray.add(zVal);
            }
            if (-i == timeLimit) {
                this.unregisterSensor();
                getRespRate(zAxisValueArray);
            }
        }
    }

    public void getRespRate(ArrayList<Double> zAxisValueArray) {
        int windowSize = 30;
        int slidingSize = 5;
        int length = zAxisValueArray.size();
        ArrayList<Float> zValsSmooth = new ArrayList<>();
        for (int i = length % windowSize; i + windowSize < length; i += slidingSize) {
            int sum = 0;
            for (int j = i; j < windowSize + i; j++) {
                sum += zAxisValueArray.get(j);
            }
            zValsSmooth.add((float) sum / windowSize);
        }

        ArrayList<Float> zValsZeroes = new ArrayList<>();
        for (int i = 1; i < zValsSmooth.size(); i++) {
            zValsZeroes.add(zValsSmooth.get(i) - zValsSmooth.get(i - 1));
        }
        for (int i = 1; i < zValsZeroes.size(); i++) {
            if (zValsZeroes.get(i) == 0 || (zValsZeroes.get(i - 1) > 0 && zValsZeroes.get(i) < 0) || (zValsZeroes.get(i - 1) < 0 && zValsZeroes.get(i) > 0)) {
                respiratoryRate += 1;
            }
        }
        respiratoryRateLabel.setText((respiratoryRate * 30 / timeLimit) + "");

    }

}
package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;


public class SymptomsRatingActivity extends AppCompatActivity {

    String TAG = "SymptomActivity";
    com.example.myapplication.MainActivity mainActivity;

    Button btnUploadSymptoms;
    com.example.myapplication.UserInfoDatabase userInfoDatabase;
    String respiratoryRate;
    String heartRate;
    String[] symptomsList = {"Feeling tired", "Shortness of Breath", "Cough", "Loss of Smell or Taste", "Muscle Ache", "Fever", "Nausea", "Headache", "Diarrhea", "Soar Throat"};
    String selectedSymptom = "";
    Spinner spinnerSymptoms;
    RatingBar ratings;
    Intent newIntent;
    int sympIndex = 0;

    int symptomRatings[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_rating);
        Log.i(TAG, "on create function");

        userInfoDatabase = new com.example.myapplication.UserInfoDatabase(getApplicationContext());

        newIntent = getIntent();
        if (newIntent.hasExtra("RESPRATEVAL")) {
            heartRate = newIntent.getStringExtra("RESPRATEVAL");
        }
        if (newIntent.hasExtra("HRTRATEVAL")) {
            respiratoryRate = newIntent.getStringExtra("HRTRATEVAL");
        }


        btnUploadSymptoms = (Button) findViewById(R.id.btnUploadSymptoms);
        spinnerSymptoms = (Spinner) findViewById(R.id.spinnerSymptoms);

        ratings = (RatingBar) findViewById(R.id.ratingBar);
        ratings.setStepSize((float) 1.0);

        ratings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 1) {
                    float ratingVals = ratings.getRating();
                    symptomRatings[sympIndex] = (int) ratingVals;
                    Toast.makeText(SymptomsRatingActivity.this, " " + ratingVals, Toast.LENGTH_LONG).show();

                }
                return false;
            }
        });


        spinnerSymptoms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ratings.setRating(0);
                selectedSymptom = symptomsList[i];
                sympIndex = i;
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> spin_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, symptomsList);
        spinnerSymptoms.setAdapter(spin_adapter);

        btnUploadSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btn function");
                Log.i(TAG, "onClickSymptomACtivity: " + respiratoryRate + "      " + heartRate);
                userInfoDatabase.setHeartRateValue(heartRate);
                userInfoDatabase.setRespiratoryRateValue(respiratoryRate);
                userInfoDatabase.insertIntoDB(symptomRatings);

            }
        });


    }
}
package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    TextView textViewRespRate;
    TextView textViewHeartRate;
    Button btnHeartRate;
    Button btnRespiratoryRate;
    UserInfoDatabase userInfoDB;
    String hearRateValue;

    private final int PHONE_STATE = 1;
    private static final int VID_CAP = 101;

    public String heartRate = "0";
    public String respRate = "0";

    String TAG = "MainActivity";
    Context context;

    float ts = 0.0f;
    int tmp = 0;

    float timestamp;
    Intent newIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if( ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }

        showPhoneStatePermission();
        newIntent = getIntent();

        textViewHeartRate = (TextView) findViewById(R.id.textViewHeartRate);
        hearRateValue = "";


        findViewById(R.id.btnHeartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: startRec");

                Intent intent = new Intent(MainActivity.this, HeartRateView.class);
                startActivityForResult(intent, 1234);
            }
        });

        findViewById(R.id.btnRespiratoryRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Respiratory rate is now being recorded.", Toast.LENGTH_LONG).show();
                timestamp = (float) (System.currentTimeMillis() / 1000.0);
                Accelerometer accelerometer = new Accelerometer(view.getContext(), timestamp, textViewRespRate);
            }
        });

        textViewRespRate = (TextView) findViewById(R.id.textViewRespRate);

        findViewById(R.id.btnSymptoms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "btnSymptoms");
                openSymptomsActivity();
            }
        });

        findViewById(R.id.btnUploadSigns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInfoDB = new UserInfoDatabase(getApplicationContext());
                userInfoDB.onCreate(userInfoDB.getWritableDatabase());
                userInfoDB.setHeartRateValue(respRate);
                userInfoDB.setRespiratoryRateValue(heartRate);
            }
        });


    }


    @Override
    protected void onPause() {
        super.onPause();
        Accelerometer scc = new Accelerometer(getApplicationContext(), timestamp, textViewRespRate);
        scc.unregisterSensor();

    }


    public void openSymptomsActivity() {
        Intent intent = new Intent(this, SymptomsRatingActivity.class);
        intent.putExtra("HRTRATEVAL", hearRateValue);
        intent.putExtra("RESPRATEVAL", textViewRespRate.getText().toString());
        startActivity(intent);
    }


    private boolean hasCamera() {
        Log.i(TAG, "hasCamera: ");
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VID_CAP) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video has been saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == 1234 && resultCode == 7777) {

            if (data.hasExtra("HRTRATEVAL")) {

                hearRateValue = data.getStringExtra("HRTRATEVAL");
                Toast.makeText(this, "Value : " + hearRateValue, Toast.LENGTH_LONG).show();
                textViewHeartRate.setText(hearRateValue);

            }

        }
    }

    private void requestCameraPermission()
    {
        if( ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        101);

            }

        }
    }


    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showExplanation("Permission Needed", "Rationale", Manifest.permission.WRITE_EXTERNAL_STORAGE, PHONE_STATE);
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PHONE_STATE);
            }
        } else {
            // Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExplanation(String title, String message, final String permission, final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

}

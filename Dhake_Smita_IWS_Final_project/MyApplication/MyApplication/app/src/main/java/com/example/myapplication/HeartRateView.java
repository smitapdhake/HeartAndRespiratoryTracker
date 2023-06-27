package com.example.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;


public class HeartRateView extends AppCompatActivity {

    private static final String TAG = "MainActivity2";
    private TextureView textureView;
    private String cameraId;
    protected CameraDevice camDevice;
    protected CameraCaptureSession camCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private static final int REQ_CAM = 1;

    private Handler bkgrndHandler;
    private HandlerThread bkgrndThread;

    public static int heartRateBPM;
    private int currRollAvg;
    private int prevRollAvg;
    private int finalLastRollAvg;
    private long [] timeArray;
    private int numCaptures = 0;
    private int numBeats = 0;
    HeartRateView activity;
    Intent returnIntent;
    public int time_interval = 45;
    TextView textureViewInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_view);

        textureView =  findViewById(R.id.texture1);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        timeArray = new long [time_interval];
        textureViewInfoTextView = (TextView)findViewById(R.id.textureViewInfoTextView);
        activity = this;
        returnIntent = new Intent();


    }
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureUpdated");
            Bitmap currentBitmap = textureView.getBitmap();
            int bitmapWidth = currentBitmap.getWidth();
            int bitmapHeight = currentBitmap.getHeight();
            int[] pixels = new int[bitmapHeight * bitmapWidth];
            currentBitmap.getPixels(pixels, 0, bitmapWidth, bitmapWidth / 2, bitmapHeight / 2, bitmapWidth / 20, bitmapHeight / 20);
            int sum = 0;
            for (int pixelIndex = 0; pixelIndex < bitmapHeight * bitmapWidth; pixelIndex++) {
                int redMeasurement = (pixels[pixelIndex] >> 16) & 0xFF;
                sum = sum + redMeasurement;
            }

            if (numCaptures == 20) {
                currRollAvg = sum;
            }
            else if (numCaptures > 20 && numCaptures < 49) {
                currRollAvg = (currRollAvg *(numCaptures - 20) + sum) / (numCaptures - 19);
            }

            else if (numCaptures >= 49) {
                currRollAvg = (currRollAvg *29 + sum) / 30;
                if (prevRollAvg > currRollAvg && prevRollAvg > finalLastRollAvg && numBeats < time_interval) {
                    timeArray[numBeats] = System.currentTimeMillis();
                    numBeats++;
                    if (numBeats == time_interval) {
                        calculateBeatsPerMinute();
                    }
                }
            }
            numCaptures++;
            finalLastRollAvg = prevRollAvg;
            prevRollAvg = currRollAvg;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int bitmapWidth, int bitmapHeight) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int bitmapWidth, int bitmapHeight) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
    };


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onError(CameraDevice camera, int error) {
            if (camDevice != null)
                camDevice.close();
            camDevice = null;
        }

        @Override
        public void onOpened(CameraDevice camera) {
            Log.e(TAG, "onOpened");
            camDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camDevice.close();
        }
    };
    protected void startBackgroundThread() {
        bkgrndThread = new HandlerThread("Camera Background");
        bkgrndThread.start();
        bkgrndHandler = new Handler(bkgrndThread.getLooper());
    }
    // onPause
    protected void stopBackgroundThread() {
        bkgrndThread.quitSafely();
        try {
            bkgrndThread.join();
            bkgrndThread = null;
            bkgrndHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HeartRateView.this, new String[]{Manifest.permission.CAMERA}, REQ_CAM);
                return;
            }
            manager.openCamera(cameraId, stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }
    protected void updatePreview() {
        if (null == camDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        try {
            camCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, bkgrndHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != camDevice) {
            camDevice.close();
            camDevice = null;
        }
    }
    private void calculateBeatsPerMinute() {
        int med;
        long [] timedist = new long [time_interval - 1];
        for (int i = 0; i < time_interval - 1; i++) {
            timedist[i] = timeArray[i + 1] - timeArray[i];
        }
        Arrays.sort(timedist);
        med = (int) timedist[timedist.length / 2];
        heartRateBPM = 60000 / med;
        addToDatabase();

    }


    private void addToDatabase()
    {
        textureViewInfoTextView.setText("Heart Rate = "+ heartRateBPM +" BPM");
        returnIntent.putExtra("HRTRATEVAL", heartRateBPM +"");
        activity.setResult(7777, returnIntent);
        Toast.makeText(HeartRateView.this, "Heart Rate Value recorded, Go back to upload the values ", Toast.LENGTH_LONG).show();
        Toast.makeText(HeartRateView.this, " " + heartRateBPM, Toast.LENGTH_LONG).show();


    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = camDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            camDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == camDevice) {
                        return;
                    }
                    camCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(HeartRateView.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_CAM) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(HeartRateView.this, "Camera permission needed!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


}

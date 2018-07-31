package com.dikamjitborah.hobarb.parkingfeescalculator;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class TimerStart extends AppCompatActivity {


    DatabaseReference databaseReference;

    TextView textView;
    Long startTime = 0L, timeinMS = 0L, updateTime = 0L, timeSwapBuff = 0L;
    Handler handler = new Handler();


    SurfaceView cameraPreview;
    BarcodeDetector barcodeDetector;
    Camera camera;
    CameraSource cameraSource;

    final int Requestid = 1001;
    Button b3;
    static int flag = 0;

    AlertDialog.Builder builder;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Requestid: {
                try {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_start);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        textView = findViewById(R.id.textViewTimexx);
        builder = new AlertDialog.Builder(this);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                timeinMS = SystemClock.uptimeMillis() - startTime;
                updateTime = timeSwapBuff + timeinMS;
                int secs = (int) (updateTime / 1000);
                int mins = secs / 60;
                int hrs = mins / 60;
                secs %= 60;
                int ms = (int) (updateTime / 100);

                textView.setText("" + String.format("%02d", hrs) + ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
                handler.postDelayed(this, 0);
            }
        };

        startTime = SystemClock.uptimeMillis();
        handler.postDelayed(runnable, 0);


        cameraPreview = findViewById(R.id.surfaceView3);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(640, 480).build();
        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TimerStart.this, new String[]{Manifest.permission.CAMERA}, Requestid);
                    return;
                }
                try {
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();

            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                if (qrcodes.size() != 0) {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            // Toast.makeText(getApplicationContext(), "ffff", Toast.LENGTH_SHORT).show();

                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
                            vibrator.vibrate(200);

                            timeSwapBuff += timeinMS;
                            handler.removeCallbacks(runnable);

                            //databaseReference.child("IMEI").setValue(IMEI);
                            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            String device_id = tm.getDeviceId();
                            databaseReference.child(device_id).push().setValue(textView.getText());
                            builder.setMessage("Vehicle parked for: " + textView.getText());
                            builder.setNeutralButton("PAY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                   /* Intent intent = new Intent();
                                         intent.setAction(Intent.ACTION_MAIN);
                                         intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                         intent.setComponent(new ComponentName());*/
                                }
                            });
                            AlertDialog alert = builder.create();
                            //Setting the title manually
                            alert.setTitle("PARKING TIME");
                            alert.show();


                        }
                    });
                }
            }
        });



    }



   /* public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return deviceUniqueIdentifier;
            }
            deviceUniqueIdentifier = tm.getDeviceId(); }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length())
        {
            deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID); }
        return deviceUniqueIdentifier; }
*/

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}

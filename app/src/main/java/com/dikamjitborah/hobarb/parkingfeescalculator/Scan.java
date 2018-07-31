package com.dikamjitborah.hobarb.parkingfeescalculator;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class Scan extends AppCompatActivity implements ZXingScannerView.ResultHandler {



    static int flag = 0;
    private ZXingScannerView scannerView;
    private static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA)== PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(), "Opening Camera", Toast.LENGTH_SHORT).show();
            }
            else
            {
                requestPermission();

            }
        }
        else
        {
            if(scannerView == null)
            {
                scannerView = new ZXingScannerView(this);
                setContentView(scannerView);
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();

        }

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permission[], int grantResults[])
    {
        switch(requestCode){
            case REQUEST_CAMERA:
                if(grantResults.length >0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted)
                    {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }

        }
    }

    @Override
    public void onResume() {

        super.onResume();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(scannerView == null)
            {
                scannerView = new ZXingScannerView(this);
                setContentView(scannerView);
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }
        else
        {
            requestPermission();
            if(scannerView == null)
            {
                scannerView = new ZXingScannerView(this);
                setContentView(scannerView);
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();


        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        scannerView.stopCamera();
    }


    @Override
    public void handleResult(Result result) {



        //Toast.makeText(getApplicationContext(), "ggg", Toast.LENGTH_SHORT).show();
       Intent intent = new Intent(Scan.this, TimerStart.class);
        startActivity(intent);
    }


}

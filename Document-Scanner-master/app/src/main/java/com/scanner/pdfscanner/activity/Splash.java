package com.scanner.pdfscanner.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.scanner.pdfscanner.R;

import java.util.ArrayList;
import java.util.Map;

public class Splash extends AppCompatActivity {
    private static boolean splashLoaded = false;
    public static ArrayList<Bitmap> duplicatemodded=new ArrayList<>();;
    public static ArrayList<Bitmap> originalbmps=new ArrayList<>(), resources=new ArrayList<>(), modded=new ArrayList<>();
    public  static  ArrayList<Map<Integer, PointF>> pointsF=new ArrayList<>();
    public static String fileType = "pdf";
    public static int compressionRatio =100;
    private static final int FILE_PATH_RETURN_REQUEST_CODE = 10;

    public static String filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        modded = new ArrayList<>();
        pointsF = new ArrayList<>();
        //duplicatemodded = new ArrayList<>();
        resources = new ArrayList<>();
        originalbmps =new ArrayList<>();
        Intent intent = getIntent();
        try {
           fileType = (String) intent.getExtras().get("fileType"); // possible values are -> "jpg", "pdf"
           compressionRatio = Integer.parseInt((String) intent.getExtras().get("compressionRatio")); // possible values are -> 0 to 100
        } catch (Throwable e) {
            fileType= "pdf";
            e.printStackTrace();
        }
        Intent goToMainActivity = new Intent(Splash.this, MyHomeAcitivty.class);
      //  goToMainActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(goToMainActivity, FILE_PATH_RETURN_REQUEST_CODE);
    }

    //Capturing the result from filterfragment.
   /* public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PATH_RETURN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String filePath = data.getExtras().get("filePath").toString();
                Intent intent = new Intent();
                intent.putExtra("filePath", filePath);
                setResult(FILE_PATH_RETURN_REQUEST_CODE, intent);
                finish();
                System.exit(0);

            }
        }
    }*/

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
       // if (requestCode == FILE_PATH_RETURN_REQUEST_CODE) {
        //    if (resultCode == RESULT_OK) {
                String filePath = Splash.filePath;
                Intent intent = new Intent();
                intent.putExtra("filePath", filePath);
                setResult(FILE_PATH_RETURN_REQUEST_CODE, intent);
                finish();
                System.exit(0);

      //      }
      //  }
    }
}

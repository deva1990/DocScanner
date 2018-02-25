package com.scanner.pdfscanner.activity;

import android.graphics.Bitmap;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by USER on 12/23/2016.
 */

public class StorageClass
{
    public static ArrayList<Bitmap> duplicatemodded;
    public static ArrayList<Bitmap> originalbmps, resources, modded;
    public  static  ArrayList<Map<Integer, PointF>> pointsF;
    public void Storage()
    {
        modded = new ArrayList<>();
        pointsF = new ArrayList<>();
        duplicatemodded = new ArrayList<>();
        resources = new ArrayList<>();
        originalbmps =new ArrayList<>();
    }

}

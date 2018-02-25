/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Zillow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.scanner.pdfscanner.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.scanlibrary.PolygonView;
import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.activity.MyHomeAcitivty;
import com.scanner.pdfscanner.activity.Splash;
import com.scanner.pdfscanner.interfaces.ScanListener;
import com.scanner.pdfscanner.main.ScannerEngine;
import com.scanner.pdfscanner.utils.AppUtility;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CropFragment extends BaseFragment {

    private static Bitmap bitmap;

    private ScanListener scanListener;

    @Bind(R.id.sourceImageView)
    ImageView sourceImageView;

    @Bind(R.id.sourceFrame)
    FrameLayout sourceFrame;

    @Bind(R.id.polygonView)
    PolygonView polygonView;

    @Bind(R.id.progress)
    ProgressBar progressBar;

    private ProgressDialog progressDialog;
    private  FilterFragment previewFragment;
   public static Bitmap getBitmap;
    static Bitmap  tempBitmap,scaledBitmap;
    Map<Integer, PointF> point;
    public static CropFragment newInstance(Bitmap bitmap) {

        CropFragment.bitmap = bitmap;
        CropFragment fragment = new CropFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_crop, container, false);
        ButterKnife.bind(this, view);
        if (bitmap.getWidth() > bitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setVisibility(View.VISIBLE);
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    setBitmap(bitmap);

                }
            }

        });
        // File outputFile = AppUtility.getOutputMediaFile(Const.FOLDERS.CROP_IMAGE_PATH, System.currentTimeMillis() + ".jpg");

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ScanListener) {
            scanListener = (ScanListener) activity;
        } else {
            throw new RuntimeException(activity.getClass().getName() + " must implement " + ScanListener.class.getName());
        }
    }

    @OnClick(R.id.ok_ib)
    public void onOKClicked(View view) {
        //Splash.pointsF = new ArrayList<>();
        Splash.pointsF.add(point);
       // Splash.duplicatemodded = new ArrayList<>();
       // Splash.duplicatemodded.add(scaledBitmap);
        Splash.resources.add(scaledBitmap);
    applyCrop();
    }

    @OnClick(R.id.rotate_right_ib)
    public void onRotateRightClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        scanListener.onRotateRightClicked();
    }

    @OnClick(R.id.rotate_left_ib)
    public void onRotateLeftClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        scanListener.onRotateLeftClicked();
    }

    @OnClick(R.id.back_ib)
    public void onBackButtonClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage("Do you want to clear all the scanned documents and start from beginning?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent i = new Intent(getActivity(),MyHomeAcitivty.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                getActivity().finish();

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();


    }

    public void applyCrop() {
        Map<Integer, PointF> points = polygonView.getPoints();
        if (isScanPointsValid(points)) {
            new ScanAsyncTask(points).execute();
        } else {
            showErrorDialog();
        }
    }


    public void setBitmap(Bitmap original) {
        bitmap = original;
        int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        int pad = (int) getResources().getDimension(com.scanlibrary.R.dimen.scanPadding);

        if (scaledBitmap!= null && ! scaledBitmap.isRecycled()) {
            scaledBitmap.recycle();
            scaledBitmap = null;
            System.gc();
        }
        scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
       // int angleToRotate = getRoatationAngle(getActivity(), Camera.CameraInfo.CAMERA_FACING_FRONT);
        // Solve image inverting problem
        //angleToRotate = angleToRotate + 180;
      // scaledBitmap = rotate(scaledBitmap,angleToRotate);

        if (scaledBitmap.getWidth() > scaledBitmap.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            scaledBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        }
        sourceImageView.setImageBitmap(scaledBitmap);
       sourceImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        if (tempBitmap!= null && ! tempBitmap.isRecycled()) {
            tempBitmap.recycle();
            tempBitmap = null;
            System.gc();
        }


         tempBitmap = ((BitmapDrawable) sourceImageView.getDrawable()).getBitmap();

        point = getEdgePoints(tempBitmap);

        polygonView.setPoints(point);



        polygonView.setVisibility(View.VISIBLE);

        int padding = (int) getResources().getDimension(com.scanlibrary.R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 3 * padding, tempBitmap.getHeight() + 3 * padding);//mdevarajan
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
       // new ScanAsyncTask(pointFs).execute();



    }



    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        Map<Integer, PointF> orderedPoints = orderedValidEdgePoints(tempBitmap, pointFs);
        return orderedPoints;
    }

    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        float[] points = ScannerEngine.getInstance().getPoints(tempBitmap);
        float x1 = points[0];
        float x2 = points[1];
        float x3 = points[2];
        float x4 = points[3];

        float y1 = points[4];
        float y2 = points[5];
        float y3 = points[6];
        float y4 = points[7];

        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x1, y1));
        pointFs.add(new PointF(x2, y2));
        pointFs.add(new PointF(x3, y3));
        pointFs.add(new PointF(x4, y4));
        return pointFs;
    }

    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        //
   return outlinePoints;


    }

    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
      // if (!polygonView.isValidShape(orderedPoints)) { mdevarajan
            orderedPoints = getOutlinePoints(tempBitmap);
     //  }
        return orderedPoints;
    }

    private void showErrorDialog() {
        AppUtility.showErrorDialog(getActivity(), getString(R.string.cantCrop));
    }

    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;
        Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
        Bitmap _bitmap = ScannerEngine.getInstance().getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
        return _bitmap;
    }

    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private Map<Integer, PointF> points;

        public ScanAsyncTask(Map<Integer, PointF> points) {
            this.points = points;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(com.scanlibrary.R.string.scanning));
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return getScannedBitmap(bitmap, points);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            dismissDialog();
            //getBitmap = bitmap;
            scanListener.onOkButtonClicked(bitmap);

        }
    }

    protected void showProgressDialog(String message) {
        progressDialog = ProgressDialog.show(getActivity(),"",message);
        progressDialog.setCancelable(false);
    }

    protected void dismissDialog() {
        progressDialog.dismiss();
    }

}

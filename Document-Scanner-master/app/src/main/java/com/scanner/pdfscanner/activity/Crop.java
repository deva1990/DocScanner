package com.scanner.pdfscanner.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.scanlibrary.PolygonView;
import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.activity.adapters.CustomPagerAdapter;
import com.scanner.pdfscanner.fragment.FilterFragment;
import com.scanner.pdfscanner.main.ScannerEngine;
import com.scanner.pdfscanner.utils.AppUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Crop extends AppCompatActivity {
@Bind(R.id.polygonView)
    PolygonView polygonView;
    @Bind(R.id.sourceImageView)
    ImageView imageView;
    Map<Integer, PointF> pointsF;
    @Bind(R.id.sourceFrame)
    FrameLayout sourceFrame;
    static  int pos;
    private ProgressDialog progressDialog;
    private FilterFragment filterFragment;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop2);
        ButterKnife.bind(this);

      pos = (int) getIntent().getExtras().get("postion");
        pointsF = Splash.pointsF.get(pos);
        // Bitmap orignal = Splash.duplicatemodded.get(pos);
        Bitmap orignal = Splash.resources.get(pos);
       // Bitmap scaledBitmap = scaledBitmap(orignal, sourceFrame.getWidth(), sourceFrame.getHeight());

     //  Bitmap scaledBitmap = scaledBitmap(orignal, sourceFrame.getWidth(), sourceFrame.getHeight());


        imageView.setImageBitmap(orignal);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        Map<Integer, PointF> point = getEdgePoints(tempBitmap);

        polygonView.setPoints(pointsF);
        polygonView.setVisibility(View.VISIBLE);


        int padding = (int) getResources().getDimension(com.scanlibrary.R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);//mdevarajan
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);

        //progressBar.setVisibility(View.GONE);

       // polygonView.setPoints(pointsF);



    }
    @OnClick(R.id.ok_ib)
    public void OnClick(View v)
    {
        applyCrop();
    }

    @OnClick(R.id.back_ib)
    public void OnBackClick(View v)
    {
      finish();
    }



    public void applyCrop() {
        Map<Integer, PointF> points = polygonView.getPoints();
        if (isScanPointsValid(points)) {
            new ScanAsyncTask(points).execute();
        } else {
            showErrorDialog();
        }
    }
    private void showErrorDialog() {
        AppUtility.showErrorDialog(getApplicationContext(), getString(R.string.cantCrop));
        /*AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Oops!");
        builder.setMessage(R.string.cantCrop);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();*/
    }
    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }

    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private Map<Integer, PointF> points;

        public ScanAsyncTask(Map<Integer, PointF> points) {
            this.points = points;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(Void... params) {
           // Bitmap bitmap = Splash.duplicatemodded.get(pos);
            Bitmap bitmap = Splash.resources.get(pos);
            return getScannedBitmap(bitmap, points);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //dismissDialog(getA);
            //getBitmap = bitmap;

            Splash.modded.set(pos,bitmap);
            Splash.resources.set(pos,bitmap);
            CustomPagerAdapter adapter = new CustomPagerAdapter(getApplicationContext(),Splash.modded);
            FilterFragment.pager.setAdapter(adapter);
            FilterFragment.pager.setCurrentItem(pos);
            //adapter.notifyItemRemoved(pos);
            //adapter.notifyDataSetChanged();
            //CustomPagerAdapter.imageView.setImageBitmap(bitmap);

            finish();

           // scanListener.onOkButtonClicked(bitmap);

        }
    }
    protected void showProgressDialog(String message) {
        progressDialog = ProgressDialog.show(getApplicationContext(),"",message);
        progressDialog.setCancelable(false);
    }
    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        float xRatio = (float) original.getWidth() / imageView.getWidth();
        float yRatio = (float) original.getHeight() / imageView.getHeight();

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
    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
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
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }
}

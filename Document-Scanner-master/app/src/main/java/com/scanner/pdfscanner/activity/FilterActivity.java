package com.scanner.pdfscanner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.activity.adapters.CustomPagerAdapter;
import com.scanner.pdfscanner.db.models.NoteGroup;
import com.scanner.pdfscanner.fragment.FilterFragment;
import com.scanner.pdfscanner.interfaces.PhotoSavedListener;
import com.scanner.pdfscanner.interfaces.ScanListener;
import com.scanner.pdfscanner.main.Const;
import com.scanner.pdfscanner.utils.AppUtility;
import com.scanner.pdfscanner.utils.SavingBitmapTask;

import java.io.File;

public class FilterActivity extends BaseScannerActivity implements ScanListener{

    private FilterFragment previewFragment;
    CustomPagerAdapter adapter;
    Bitmap bitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXTRAS.FROM_CAMERA, false)) {
            setTitle(R.string.lbl_take_another);
        }

    }

    protected void loadPhoto() {

        progressBar.setVisibility(View.VISIBLE);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected Void doInBackground(Void... params) {
                //BaseScannerActivity.this.bitmap = Compressor.getDefault(BaseScannerActivity.this).compressToBitmap(new File(path)); // mdevarajan
                bitmap = decodeFile(new File(path)); ///original
                //BaseScannerActivity.this.bitmap = getCompressedBitmap(path);
           //     bitmap = BitmapFactory.decodeFile(path);
                return null;
            }
            @Override
            protected void onPostExecute(Void dummy) {
                showPhoto(bitmap);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    @Override
    protected void showPhoto(Bitmap bitmap) {
        if (previewFragment == null) {
            previewFragment = FilterFragment.newInstance(bitmap);
            setFragment(previewFragment, FilterFragment.class.getSimpleName());
        } else {
            previewFragment.setBitmap(bitmap);
        }
    }

    @Override
    public void onRotateLeftClicked() {

    }

    @Override
    public void onRotateRightClicked() {

    }

    @Override
    public void onBackClicked() {

    }


    @Override
    public void onBackPressed() {

        //  progressBar.setVisibility(View.VISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to clear all the scanned documents and start from beginning?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                if (FilterActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Splash.modded.clear();
                    Splash.duplicatemodded.clear();
                    Splash.originalbmps.clear();
                    Intent i = new Intent(FilterActivity.this, Splash.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    FilterActivity.this.finish();
                } else {
                    Intent i = new Intent(FilterActivity.this, Splash.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    FilterActivity.this.finish();
                }
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

    @Override
    public void onOkButtonClicked(Bitmap bitmap) {
        File outputFile = AppUtility.getOutputMediaFile(Const.FOLDERS.CROP_IMAGE_PATH, System.currentTimeMillis() + ".jpg");
        if(outputFile!=null)
            new SavingBitmapTask(getNoteGroupFromIntent(), bitmap, outputFile.getAbsolutePath(), new PhotoSavedListener() {
                @Override
                public void photoSaved(String path, String name) {
                    if(previewFragment!=null)
                        previewFragment.hideProgressBar();
                }

                @Override
                public void onNoteGroupSaved(NoteGroup noteGroup) {
                    //openNoteGroupActivity(noteGroup);
                }
            }).execute();
    }

    @Override
    public void OnClickFinesh() {
        this.finish();
        System.exit(0);
    }

}

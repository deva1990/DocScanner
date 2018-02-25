package com.scanner.pdfscanner.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;

import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.db.models.NoteGroup;
import com.scanner.pdfscanner.fragment.CropFragment;
import com.scanner.pdfscanner.fragment.FilterFragment;
import com.scanner.pdfscanner.interfaces.PhotoSavedListener;
import com.scanner.pdfscanner.interfaces.ScanListener;
import com.scanner.pdfscanner.main.Const;
import com.scanner.pdfscanner.manager.ImageManager;
import com.scanner.pdfscanner.utils.AppUtility;

import org.parceler.Parcels;

import java.io.File;

public class ScannerActivity extends BaseScannerActivity implements ScanListener {

    private CropFragment cropFragment;
    private FilterFragment previewFragment;
    Bitmap bitmap ;


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

                //bitmap = decodeFile(new File(path)); ///original
                //BaseScannerActivity.this.bitmap = getCompressedBitmap(path);
                bitmap = BitmapFactory.decodeFile(path);
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
        if (cropFragment == null) {
            cropFragment = CropFragment.newInstance(bitmap);
            setFragment(cropFragment, CropFragment.class.getSimpleName());
        } else {
            cropFragment.setBitmap(bitmap);
        }

    }

    private void openFilterActivity(String path, String name) {

        Intent intent = new Intent(this, FilterActivity.class);
        intent.putExtra(BaseScannerActivity.EXTRAS.PATH, path);
        intent.putExtra(BaseScannerActivity.EXTRAS.NAME, name);
        intent.putExtra(BaseScannerActivity.EXTRAS.FROM_CAMERA, false);
       // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_FORWARD_RESULT);//Carry forwarding the result to Splash
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        NoteGroup noteGroup = getNoteGroupFromIntent();
        if(noteGroup!=null)
            intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(noteGroup));
        //startActivityForResult(intent,5);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        finish();

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


                if (ScannerActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Splash.modded.clear();
                    Splash.duplicatemodded.clear();
                    Splash.originalbmps.clear();
                    Intent i = new Intent(ScannerActivity.this, Splash.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    ScannerActivity.this.finish();
                } else {
                    Intent i = new Intent(ScannerActivity.this, Splash.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    ScannerActivity.this.finish();
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
    public void onRotateLeftClicked() {
        rotatePhoto(-90);
    }

    @Override
    public void onRotateRightClicked() {
        rotatePhoto(90);
    }

    @Override
    public void onBackClicked() {
        onBackPressed();
        MyHomeAcitivty.modded.clear();
        finish();
    }

    @Override
    public void onOkButtonClicked(final Bitmap croppedBitmap) {
      /*  File outputFile = AppUtility.getOutputMediaFile(Const.FOLDERS.CROP_IMAGE_PATH, System.currentTimeMillis() + ".jpg");
        FileOutputStream out=null;
        try {
            out = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

        openFilterActivity(outputFile.getAbsolutePath(), name);*/

        File outputFile = AppUtility.getOutputMediaFile(Const.FOLDERS.CROP_IMAGE_PATH, System.currentTimeMillis() + ".jpg");
        if(outputFile!=null) {
            ImageManager.i.cropBitmap(outputFile.getAbsolutePath(), croppedBitmap, new PhotoSavedListener() {

                @Override
                public void photoSaved(String path, String name) {
                    openFilterActivity(path, name);
                }

                @Override
                public void onNoteGroupSaved(NoteGroup noteGroup) {

                }
            });
        }
    }



    @Override
    public void OnClickFinesh() {

    }
}

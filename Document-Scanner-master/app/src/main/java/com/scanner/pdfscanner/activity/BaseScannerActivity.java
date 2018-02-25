package com.scanner.pdfscanner.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.db.models.NoteGroup;
import com.scanner.pdfscanner.interfaces.PhotoSavedListener;
import com.scanner.pdfscanner.utils.RotatePhotoTask;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class BaseScannerActivity extends BaseActivity {

    protected String path;
    protected String name;
    protected Bitmap bitmap;

    @Bind(R.id.progress)
    protected View progressBar;
    private NoteGroup mNoteGroup;
    File actualimage = null;
    File originalimage;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showActionBar();
        showBack();
        setContentView(R.layout.activity_base_photo);
        ButterKnife.bind(this);

        if (getIntent().hasExtra(EXTRAS.PATH)) {
            path = getIntent().getStringExtra(EXTRAS.PATH);
        } else {
            throw new RuntimeException("There is no path to image in extras");
        }
        if (getIntent().hasExtra(EXTRAS.NAME)) {
            name = getIntent().getStringExtra(EXTRAS.NAME);
        } else {
            throw new RuntimeException("There is no image name in extras");
        }

        mNoteGroup = Parcels.unwrap(getIntent().getParcelableExtra(NoteGroup.class.getSimpleName()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bitmap == null || bitmap.isRecycled()) {
            loadPhoto();
        }
    }

    protected NoteGroup getNoteGroupFromIntent() {
        return mNoteGroup;
    }

    protected abstract void showPhoto(Bitmap bitmap);

    protected void rotatePhoto(float angle) {

        new RotatePhotoTask(path, angle, new PhotoSavedListener() {
            @Override
            public void photoSaved(String path, String name) {

                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    showPhoto(bitmap);
                    setResult(EXTRAS.RESULT_EDITED, setIntentData());
                }
            }

            @Override
            public void onNoteGroupSaved(NoteGroup noteGroup) {

            }
        }).execute();
    }

    protected void deletePhoto() {
        setResult(EXTRAS.RESULT_DELETED, setIntentData());
        finish();
    }

    protected void loadPhoto() {

        progressBar.setVisibility(View.VISIBLE);
      /*  if(BaseScannerActivity.this.bitmap!=null){
            BaseScannerActivity.this.bitmap.recycle();
            System.gc();
        }*/
        // DisplayMetrics metrics = new DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //  ImageManager.i.loadPhoto(path, metrics.widthPixels, metrics.heightPixels, loadingTarget);
        // File f = Compressor.getDefault(this).compressToFile(new File(path));

      /*  BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);*/
        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
        compressedImageBitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);*/


        /*progressBar.setVisibility(View.VISIBLE);
        BaseScannerActivity.this.bitmap = Compressor.getDefault(this).compressToBitmap(new File(path));
        showPhoto(BaseScannerActivity.this.bitmap);
        progressBar.setVisibility(View.GONE);
        */
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }


            @Override
            protected Void doInBackground(Void... params) {
               //BaseScannerActivity.this.bitmap = Compressor.getDefault(BaseScannerActivity.this).compressToBitmap(new File(path)); // mdevarajan

                  BaseScannerActivity.this.bitmap = decodeFile(new File(path)); ///original
                //BaseScannerActivity.this.bitmap = getCompressedBitmap(path);
              //  BaseScannerActivity.this.bitmap = BitmapFactory.decodeFile(path);
                return null;
            }

            @Override
            protected void onPostExecute(Void dummy) {
                //  if (null != theBitmap) {
                // The full bitmap should be available here
                //  image.setImageBitmap(theBitmap);
                //Log.d(TAG, "Image loaded");
                // };
                showPhoto(BaseScannerActivity.this.bitmap);
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    public  Bitmap getCompressedBitmap(String imagePath) {
        float maxHeight = 1920.0f;
        float maxWidth = 1080.0f;
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);

        byte[] byteArray = out.toByteArray();

        Bitmap updatedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        return updatedBitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }
        return inSampleSize;
    }

    // Decodes image and scales it to reduce memory consumption -- mdevarajan
    public Bitmap decodeFile(File f) { //-- mdevarajan
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=650;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    //target to save
   /* private  Target getTarget(final String path){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        File file = new File(path);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();
                BaseScannerActivity.this.bitmap = bitmap;
                showPhoto(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }*/

    /*private SimpleTarget target = new SimpleTarget<Bitmap>(800,500) {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
            // do something with the bitmap
            // for demonstration purposes, let's just set it to an ImageView
            BaseScannerActivity.this.bitmap = bitmap;
            showPhoto(bitmap);
        }
    };*/

    public void setFragment(Fragment fragment, String name) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_content, fragment, name);
       // fragmentTransaction.replace(R.id.fragment_content, fragment, name);
       // fragmentTransaction.addToBackStack(name);
        fragmentTransaction.commit();
    }

   /* private Target loadingTarget = new Target() {

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            *//*ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,0,outputStream);
            Bitmap decodedMap = BitmapFactory.decodeStream(new ByteArrayInputStream(outputStream.toByteArray()));*//*
            progressBar.setVisibility(View.GONE);
            BaseScannerActivity.this.bitmap = bitmap;
            showPhoto(bitmap);

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            recycle();
            progressBar.setVisibility(View.GONE);
            bitmap = null;
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            progressBar.setVisibility(View.VISIBLE);
        }

        *//**
         * Recycle bitmap to free memory
         *//*
        private void recycle() {
            if (BaseScannerActivity.this.bitmap != null && !BaseScannerActivity.this.bitmap.isRecycled()) {
                BaseScannerActivity.this.bitmap.recycle();
                BaseScannerActivity.this.bitmap = null;
                System.gc();
            }
        }

    };*/

    protected Intent setIntentData() {
        return setIntentData(null);
    }

    protected Intent setIntentData(Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }
        intent.putExtra(EXTRAS.PATH, path);
        intent.putExtra(EXTRAS.NAME, name);
        return intent;
    }

    public static final class EXTRAS {

        public static final String PATH = "path";

        public static final String NAME = "name";

        public static final String FROM_CAMERA = "from_camera";

        public static final int REQUEST_PHOTO_EDIT = 7338;

        public static final int RESULT_EDITED = 338;

        public static final int RESULT_DELETED = 3583;

        public static final String CROPPED_PATH = "CROPPED_PATH";

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}

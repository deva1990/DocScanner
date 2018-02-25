package com.scanner.pdfscanner.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.scanner.pdfscanner.interfaces.Initializer;
import com.scanner.pdfscanner.interfaces.PhotoSavedListener;
import com.scanner.pdfscanner.interfaces.StorageCallback;
import com.scanner.pdfscanner.utils.CropPhotoTask;
import com.scanner.pdfscanner.utils.ManagedTarget;
import com.scanner.pdfscanner.utils.RotatePhotoTask;
import com.scanner.pdfscanner.utils.ScaleTransformation;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import com.squareup.picasso.Target;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public enum ImageManager implements Initializer, StorageCallback {
    i;

    private Context context;
    private Picasso picasso;

    private HashSet<ManagedTarget> targets;
    private Map<String, WeakReference<Bitmap>> bitmapMap;

    @Override
    public void init(Context context) {
        this.context = context;
        this.picasso = Picasso.with(context);
        bitmapMap = new HashMap<>();
        targets = new HashSet<>();
    }

    public void loadPhoto(String path, int width, int height, Target target) {
        File photo = !TextUtils.isEmpty(path) ? new File(path) : null;
        if (path == null) {
            target.onBitmapFailed(null);
        }
        Bitmap bitmap = getBitmap(path);
        if (bitmap != null && !bitmap.isRecycled()) {
            target.onBitmapLoaded(bitmap, Picasso.LoadedFrom.MEMORY);
        } else {
            ManagedTarget managedTarget = new ManagedTarget(target, path, this);
            Picasso.with(context)
                    .load(photo)
                   .config(Bitmap.Config.ARGB_8888)
                    //.resize(width, height)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .transform(new ScaleTransformation(width, height))
                    .into(target);
          /*  Glide.with(context).load(photo)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(target);

            Glide.with(context)
            .load(Uri.fromFile(new File(path)))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>(500 , 800) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            Toast.makeText(context,"loaded",Toast.LENGTH_SHORT).show();
                        }
                    });*/


        }
    }



    public void cropBitmap(String path, Bitmap croppedBitmap, PhotoSavedListener callback) {
       // setBitmap(path, croppedBitmap);
        new CropPhotoTask(path, croppedBitmap, callback).execute();
    }

    public Bitmap rotatePhoto(String path, float angle) {
        Bitmap bitmap = getBitmap(path);
        if (bitmap != null && !bitmap.isRecycled()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        setBitmap(path, bitmap);
        new RotatePhotoTask(path, angle, null).execute();

        return bitmap;
    }

    public Bitmap getBitmap(String path) {
        return bitmapMap.get(path) != null ? bitmapMap.get(path).get() : null;
    }

    @Override
    public void clear() {
        synchronized (bitmapMap) {
            for (WeakReference<Bitmap> reference : bitmapMap.values()) {
                if (reference != null) {
                    Bitmap bitmap = reference.get();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
            }
            bitmapMap.clear();
        }
        PicassoTools.clearCache(picasso);
    }

    @Override
    public void setBitmap(String path, Bitmap bitmap) {
     //   bitmapMap.put(path, new WeakReference<>(bitmap));
    }

    @Override
    public void addTarget(ManagedTarget target) {
        removeTarget(target);
        targets.add(target);
    }

    @Override
    public void removeTarget(ManagedTarget target) {
        if (targets.contains(target)) {
            targets.remove(target);
        }
    }

}

package com.scanner.pdfscanner.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.activity.adapters.CustomPagerAdapter;
import com.scanner.pdfscanner.db.models.NoteGroup;
import com.scanner.pdfscanner.interfaces.ScanListener;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ViewPagerActivity extends BaseScannerActivity implements ScanListener {

    private ViewPagerFragment previewFragment;
    CustomPagerAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getIntent().getBooleanExtra(EXTRAS.FROM_CAMERA, false)) {
        //  setTitle(R.string.lbl_take_another);
        //}
    }

    @Override
    protected void showPhoto(Bitmap bitmap) {
     //   MyHomeAcitivty.modded.add(bitmap);
        //adapter = new CustomPagerAdapter(getApplicationContext(),MyHomeActivity.modded);

        if (previewFragment == null) {
            previewFragment = ViewPagerFragment.newInstance(bitmap);
            setFragment(previewFragment, ViewPagerFragment.class.getSimpleName());
        } else {
            previewFragment.setBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXTRAS.REQUEST_PHOTO_EDIT) {
            if (resultCode == EXTRAS.RESULT_EDITED) {
                setResult(EXTRAS.RESULT_EDITED, setIntentData());
                loadPhoto();
            }
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
        onBackPressed();
        finish();
    }

    @Override
    public void onOkButtonClicked(Bitmap bitmap) {

    }



    @Override
    public void OnClickFinesh() {
        new PDFAsyncTask().execute();
    }

    private void openNoteGroupActivity(NoteGroup noteGroup) {
        Intent intent = new Intent(this, NoteGroupActivity.class);
        intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(noteGroup));
        startActivity(intent);
        finish();
    }
    private void openFilterActivity(String path, String name) {
        Intent intent = new Intent(this, ViewPagerActivity.class);
        intent.putExtra(BaseScannerActivity.EXTRAS.PATH, path);
        intent.putExtra(BaseScannerActivity.EXTRAS.NAME, name);
        intent.putExtra(BaseScannerActivity.EXTRAS.FROM_CAMERA, false);
        NoteGroup noteGroup = getNoteGroupFromIntent();
        if(noteGroup!=null)
            intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(noteGroup));

        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        finish();
    }

    private class PDFAsyncTask extends AsyncTask<Void, Void, Void>
    {
        ProgressDialog pdLoading = new ProgressDialog(ViewPagerActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("Saving as PDF...");
            if(pdLoading != null)
                pdLoading.dismiss();
            pdLoading.show();
        }
        @Override
        protected Void doInBackground(Void... params) {
            File parentfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/DocumentScanner/");
            int count;
            if(parentfile.exists()) {
                count = parentfile.listFiles().length;
            }
            else {
                count = 0;
            }
            String s = "New Doc " + (count+1);
            convertit(MyHomeAcitivty.modded, s);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pdLoading.dismiss();
        }

    }
    private void convertit(ArrayList<Bitmap> bitmaps, String s) {
        try
        {
            Document document = new Document();
            String dirpath=android.os.Environment.getExternalStorageDirectory().toString();
            File wallpaperDirectory = new File(dirpath+"/DocumentScanner/" + s);
            // have the object build the directory structure, if needed.
            if(!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            PdfWriter.getInstance(document,new FileOutputStream(dirpath+"/DocumentScanner/" + s + "/" + s + ".pdf"));
            document.open();
            Bitmap bitmap;
            OutputStream outStream = null;
            // BitmapDrawable bmp = (BitmapDrawable)imageView.getDrawable();
            Log.d("Creating PDF", String.valueOf(bitmaps.size()));
            for(int i = 0; i < bitmaps.size(); i++) {
                bitmap = bitmaps.get(i);
                File outputfile = new File(dirpath+"/DocumentScanner/" + s + "/Image" + (i+1) + ".png");
                outStream = new FileOutputStream(outputfile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
                if(bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                    Image img = Image.getInstance(stream.toByteArray());
                    // addImage(document);
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                            - document.rightMargin() - 0) / img.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                    img.scalePercent(scaler);
                    img.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
                    //img.setAlignment(Image.LEFT| Image.TEXTWRAP);

                    document.add(img);
                }
            }
            document.close();
            Intent resultIntent = new Intent();
            resultIntent.setData(Uri.parse(dirpath+"/DocumentScanner/" + s + "/" + s + ".pdf"));
            setResult(2, resultIntent);
            MyHomeAcitivty.modded.clear();
            finish();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

package com.scanner.pdfscanner.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.activity.Crop;
import com.scanner.pdfscanner.activity.MyHomeAcitivty;
import com.scanner.pdfscanner.activity.Splash;
import com.scanner.pdfscanner.activity.ViewPagerFragment;
import com.scanner.pdfscanner.activity.adapters.CustomPagerAdapter;
import com.scanner.pdfscanner.interfaces.ScanListener;
import com.scanner.pdfscanner.main.ScannerEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterFragment extends BaseFragment {

    private Bitmap original;
    ViewPagerFragment viewPagerFragment;
    @Bind(R.id.photo)
    ImageView imageView;

    private ScanListener scanListener;

    @Bind(R.id.original_ib)
    TextView originalTextView;

    @Bind(R.id.magic_ib)
    TextView magicTextView;

    @Bind(R.id.gray_mode_ib)
    TextView grayTextView;

    @Bind(R.id.bw_mode_ib)
    TextView bwTextView;
    @Bind(R.id.addimage)
    ImageView addimage;

    @Bind(R.id.progress)
    ProgressBar progressBar;


    public static ViewPager pager;
    @Bind(R.id.popup)
    ImageView popup;
    private Bitmap transformed;
    static CustomPagerAdapter adapter;

    public static String session = "still";
    public static int postion = 0;
    static String retake = "noretake";
    static int repos;
    static int applycolor = 0;


    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    private Uri fileUri; // file url to store image/video

    FragmentPagerAdapter fragmentPagerAdapter;

    private static FilterFragment fragment;

    public static FilterFragment newInstance(Bitmap bitmap) {
        if (fragment != null) {
            fragment = new FilterFragment();
            fragment.original = bitmap;
        }else {
            fragment = new FilterFragment();
            fragment.original = bitmap;
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_layout, container, false);
        ButterKnife.bind(this, view);
        pager = (ViewPager) view.findViewById(R.id.pager);
        try {
            if (Splash.fileType.equals("jpg")) {
                addimage.setVisibility(View.GONE);
            } else {
                addimage.setVisibility(View.VISIBLE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (retake.equals("retake")) {
            Splash.modded.add(repos, original);
            Splash.resources.add(repos, original);
            Splash.duplicatemodded.add(repos, original);
            adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
            pager.setAdapter(adapter);
            pager.setCurrentItem(repos);
        } else {
            Splash.modded.add(original);
           // Splash.resources.add(original); //mdevarajan
            adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
            pager.setAdapter(adapter);
            pager.setCurrentItem(Splash.modded.size() - 1);
        }
        //pager.setOffscreenPageLimit(100);
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (original != null && !original.isRecycled()) {
            //imageView.setImageBitmap(original);
            this.transformed = original;
        } else {
            // imageView.setImageResource(R.drawable.no_image);

        }
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

    public void setBitmap(Bitmap bitmap) {
        this.original = bitmap;
        //Splash.modded.add(bitmap1);
        //imageView.setImageBitmap(bitmap);
    }

    @OnClick(R.id.crop)
    public void CropMenu(final View v) {
        Intent i = new Intent(getActivity(), Crop.class);
        int pos = pager.getCurrentItem();
        i.putExtra("postion", pos);
        startActivity(i);
    }

    @OnClick(R.id.popup)
    public void showMenu(final View v) {
        final PopupMenu popup = new PopupMenu(getActivity(), v);

        // This activity implements OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_rotateleft:
                        // archive(item);
                        Bitmap res1 = Splash.modded.get(pager.getCurrentItem());
                        Bitmap res2 = Splash.resources.get(pager.getCurrentItem());
                        int tmpl = pager.getCurrentItem();
                        Splash.resources.set(pager.getCurrentItem(), rotateBitmap(res2, -90));
                        Splash.modded.set(pager.getCurrentItem(), rotateBitmap(res1, -90));
                        adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
                        pager.setAdapter(adapter);
                        pager.setCurrentItem(tmpl);
                        // adapter.notifyDataSetChanged();
                        return true;
                    case R.id.action_rotateright:
                        //delete(item);
                        Bitmap res3 = Splash.modded.get(pager.getCurrentItem());
                        Bitmap res4 = Splash.resources.get(pager.getCurrentItem());
                        int tmp2 = pager.getCurrentItem();
                        Splash.resources.set(pager.getCurrentItem(), rotateBitmap(res4, 90));
                        Splash.modded.set(pager.getCurrentItem(), rotateBitmap(res3, 90));
                        adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
                        pager.setAdapter(adapter);
                        pager.setCurrentItem(tmp2);
                        return true;
                    case R.id.action_delete:
                        //delete(item);
                        //
                        Splash.resources.remove(pager.getCurrentItem());
                        int tmpp = pager.getCurrentItem();
                        // Splash.originalbmps.remove(pager.getCurrentItem());
                        Splash.modded.remove(pager.getCurrentItem());
                        //Splash.resources.remove(pager.getCurrentItem());
                        adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
                        pager.setAdapter(adapter);
                        pager.setCurrentItem(tmpp - 1);
                        Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_LONG).show();
                        /// Splash.modded.remove(pos);


                        return true;
                    case R.id.action_retake:
                        try {
                            retake = "retake";
                            repos = pager.getCurrentItem();
                            if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                                Splash.resources.remove(repos);
                                Splash.modded.remove(repos);
                                Splash.duplicatemodded.remove(repos);
                                Intent i = new Intent(getActivity(), MyHomeAcitivty.class);
                                startActivity(i);
                            } else {
                                Splash.resources.remove(repos);
                                Splash.modded.remove(repos);
                                Splash.duplicatemodded.remove(repos);
                                Intent i = new Intent(getActivity(), MyHomeAcitivty.class);
                                //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "something wrong...please try again", Toast.LENGTH_SHORT).show();
                            getActivity().recreate();
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.menu_main);
        popup.show();
    }

    @OnClick(R.id.ok_ib)
    public void onOKClicked(View view) {
        //  progressBar.setVisibility(View.VISIBLE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage("All the documents are captured? Are you sure want to create PDF?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                new PDFAsyncTask().execute();
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

    @OnClick(R.id.addimage)
    public void onAddClicked(View view) {
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Intent i = new Intent(getActivity(), MyHomeAcitivty.class);
            startActivity(i);
        } else {
            Intent i = new Intent(getActivity(), MyHomeAcitivty.class);
            startActivity(i);
        }
    }


    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }


    @OnClick(R.id.back_ib)
    public void onBackButtonClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setMessage("Do you want to clear all the scanned documents and start from beginning?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Splash.modded.clear();
                    Splash.duplicatemodded.clear();
                    Splash.originalbmps.clear();
                    Intent i = new Intent(getActivity(), Splash.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    getActivity().finish();
                } else {
                    Intent i = new Intent(getActivity(), Splash.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    getActivity().finish();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @OnClick(R.id.original_ib)
    public void onOrginalModeClicked(View view) {
        originalTextView.setSelected(true);
        magicTextView.setSelected(false);
        grayTextView.setSelected(false);
        bwTextView.setSelected(false);
        int pos = pager.getCurrentItem();
        Bitmap bitmap = Splash.resources.get(pager.getCurrentItem());
        transformed = bitmap;
        Splash.modded.set(pager.getCurrentItem(), transformed);
        ImageView imageView = (ImageView) pager.getChildAt(0).findViewById(R.id.imageView); //mdevarajan
        //ImageView imageView = (ImageView) pager.getChildAt(pager.getCurrentItem()).findViewById(R.id.imageView);
        imageView.setImageBitmap(transformed);
        adapter = new CustomPagerAdapter(getActivity(), Splash.modded); //mdevarajan
        pager.setAdapter(adapter);
        pager.setCurrentItem(pos);
        applycolor = 0;
    }

    @OnClick(R.id.magic_ib)
    public void onMagicModeClicked(View view) {
        progressBar.setVisibility(View.VISIBLE);
        originalTextView.setSelected(false);
        magicTextView.setSelected(true);
        grayTextView.setSelected(false);
        bwTextView.setSelected(false);
        int pos = pager.getCurrentItem();
        double value = 70;
        Bitmap transformed = adjustedContrast(Splash.resources.get(pager.getCurrentItem()), value);
        Splash.modded.set(pos, transformed);
        applycolor = 1;
        ImageView imageView = (ImageView) pager.getChildAt(0).findViewById(R.id.imageView); //mdevarajan
     //   ImageView imageView = (ImageView) pager.getChildAt(pager.getCurrentItem()).findViewById(R.id.imageView);
        imageView.setImageBitmap(transformed);
        adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
        pager.setAdapter(adapter);
        pager.setCurrentItem(pos);
    }

    private Bitmap adjustedContrast(Bitmap src, double value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create a mutable empty bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // create a canvas so that we can draw the bmOut Bitmap from source bitmap
        Canvas c = new Canvas();
        c.setBitmap(bmOut);

        // draw bitmap to bmOut from src bitmap so we can modify it
        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));


        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int) (((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (R < 0) {
                    R = 0;
                } else if (R > 255) {
                    R = 255;
                }

                G = Color.green(pixel);
                G = (int) (((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (G < 0) {
                    G = 0;
                } else if (G > 255) {
                    G = 255;
                }

                B = Color.blue(pixel);
                B = (int) (((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if (B < 0) {
                    B = 0;
                } else if (B > 255) {
                    B = 255;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        progressBar.setVisibility(View.GONE);
        return bmOut;
    }

    @OnClick(R.id.gray_mode_ib)
    public void onGrayModeClicked(View view) {
        originalTextView.setSelected(false);
        magicTextView.setSelected(false);
        grayTextView.setSelected(true);
        bwTextView.setSelected(false);
        int pos = pager.getCurrentItem();
        Bitmap transformed = ScannerEngine.getInstance().getGrayBitmap(Splash.resources.get(pager.getCurrentItem()));
        Splash.modded.set(pos, transformed);
        applycolor = 1;
        ImageView imageView = (ImageView) pager.getChildAt(0).findViewById(R.id.imageView); //mdevarajan
     //   ImageView imageView = (ImageView) pager.getChildAt(pager.getCurrentItem()).findViewById(R.id.imageView);
        imageView.setImageBitmap(transformed);
        adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
        pager.setAdapter(adapter);
        pager.setCurrentItem(pos);
    }

    @OnClick(R.id.bw_mode_ib)
    public void onBWModeClicked(View view) {
        originalTextView.setSelected(false);
        magicTextView.setSelected(false);
        grayTextView.setSelected(false);
        bwTextView.setSelected(true);
        int pos = pager.getCurrentItem();
        Bitmap transformed = new ScannerEngine().getBWBitmap(Splash.resources.get(pager.getCurrentItem()));
        Splash.modded.set(pos, transformed);
        applycolor = 1;
        ImageView imageView = (ImageView) pager.getChildAt(0).findViewById(R.id.imageView); //mdevarajan
        //ImageView imageView = (ImageView)pager.getAdapter().getItem(pager.getCurrentItem()).findViewById(R.id.imageView);
        imageView.setImageBitmap(transformed);
        adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
        pager.setAdapter(adapter);
        pager.setCurrentItem(pos);
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        if (bitmap != null && !bitmap.isRecycled()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            return bitmap;
        }
        return null;
    }

    private void convertit(ArrayList<Bitmap> bitmaps, String s) {

        try {
            Document document = new Document();
            String dirpath = android.os.Environment.getExternalStorageDirectory().toString();
            File wallpaperDirectory = new File(dirpath + "/DocumentScanner/" + s);
            // have the object build the directory structure, if needed.
            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dirpath + "/DocumentScanner/" + s + "/" + s + "_original.pdf"));
            // writer.setPdfVersion(PdfWriter.PDF_VERSION_1_5);
            //  writer.setFullCompression();
            document.open();
            Bitmap bitmap;
            Bitmap temp;
            OutputStream outStream = null;
            // BitmapDrawable bmp = (BitmapDrawable)imageView.getDrawable();
            Log.d("Creating PDF", String.valueOf(bitmaps.size()));
            for (int i = 0; i < bitmaps.size(); i++) {
                bitmap = bitmaps.get(i);
              //  bitmap = doBrightness(bitmap,50);
                //bitmap = doGamma(bitmap,0.6,0.6,0.6);
               // bitmap = sharpen(bitmap,80);
                //temp = getCompressedBitmap(bitmap);
                //bitmap = temp;
                File outputfile = new File(dirpath + "/DocumentScanner/" + s + "/Image" + (i + 1) + ".png");
                outStream = new FileOutputStream(outputfile);
                File actualimage = new File(getImageUri(getActivity(), bitmap, (FileOutputStream) outStream));
                //   File actualimage =  new File(outputfile.getAbsolutePath());
                try {
                  //  bitmap = getCompressedBitmap(outputfile.getAbsolutePath());
                    // File f= Compressor.getDefault(getActivity()).compressToFile(actualimage);
                    // bitmap= Compressor.getDefault(getActivity()).compressToBitmap(f);
                } catch (Throwable e) {
                    Bitmap compressedImage = BitmapFactory.decodeFile(outputfile.getAbsolutePath());
                    //bitmap = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                    bitmap = compressedImage;
                }

                // = new File(getImageUri(getActivity(),bitmap,outStream));
                //  Bitmap compressedImageBitmap = Compressor.getDefault(getActivity()).compressToBitmap(actualimage);
                // bitmap = compressedImageBitmap;
                // compressedImageBitmap.compress(Bitmap.CompressFormat.PNG, 3, outStream);


                outStream.flush();
                outStream.close();

                if (bitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    // bitmap = getCompressedBitmap(bitmap);
                    // temp = adjustedContrast(bitmap,50);
                    // bitmap = temp;
                    // bitmap.compress(Bitmap.CompressFormat.PNG, 88, stream);
                       bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    //File actualimage = new File(getImageUri(getActivity(),bitmap,outStream));
                    //Bitmap compressedImageBitmap = Compressor.getDefault(getActivity()).compressToBitmap(actualimage);
                    //bitmap = compressedImageBitmap;
                    Image img = Image.getInstance(stream.toByteArray());
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                            - document.rightMargin() - 0) / img.getWidth()) * 90; // 0 means you have no indentation. If you have any, change it.
                    img.scalePercent(scaler);
                    img.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
                    //img.setAlignment(Image.LEFT| Image.TEXTWRAP);
                  //  document.setPageSize(img);
                    document.add(img);
                }
            }
            document.close();
            //new CompressPDF().manipulatePdf(dirpath+"/DocumentScanner/" + s + "/" + s + "_original.pdf", dirpath+"/DocumentScanner/" + s + "/" + s + "_compressed.pdf");
             Splash.modded.clear();

          /*  adapter = new CustomPagerAdapter(getActivity(), Splash.modded);
            pager.setAdapter(adapter);*/
            //document.close();
            try {

                if (Splash.fileType.equals("jpg")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(dirpath + "/DocumentScanner/" + s + "/Image1" + ".jpg"));
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_FORWARD_RESULT|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("filePath", Uri.parse(dirpath + "/DocumentScanner/" + s + "/Image1" + ".jpg"));
                    getActivity().setResult(getActivity().RESULT_OK, intent);
                    // getActivity().startActivity(intent);
                    getActivity().finish();
                } else {
                    //Old code - starts
                    Intent target = new Intent();
                    //target.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    target.setData(Uri.parse(dirpath + "/DocumentScanner/" + s + "/" + s + "_original.pdf"));
                    target.putExtra("filePath", Uri.parse(dirpath + "/DocumentScanner/" + s + "/" + s + "_original.pdf"));
                    Splash.filePath = Uri.parse(dirpath + "/DocumentScanner/" + s + "/" + s + "_original.pdf").toString();
                    getActivity().setResult(getActivity().RESULT_OK, target);
                    getActivity().finish();
//                    FragmentManager fm = getFragmentManager();
//                    FragmentTransaction ft = fm.beginTransaction();
//                    ft.remove(this);
//                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
//                    ft.commit();

                    /*FragmentManager fm = getActivity().getSupportFragmentManager();
                    Log.d("count",new Integer(fm.getBackStackEntryCount()).toString());
                    for (int i = 0; i < fm.getFragments().size(); ++i) {
                        fm.popBackStack();
                    }
                    */
                    /*((FilterActivity)getActivity()).finish();*/
                    //Old code ends

                }


            } catch (Throwable e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getImageUri(Context inContext, Bitmap inImage, FileOutputStream outStream) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 40, outStream);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return path;
    }

   /* public static Bitmap sharpen(Bitmap src, double weight) {
        double[][] SharpConfig = new double[][] {
                { 0 , -2    , 0  },
                { -2, weight, -2 },
                { 0 , -2    , 0  }
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(SharpConfig);
        convMatrix.Factor = weight - 8;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }*/
/*
    public static Bitmap doGamma(Bitmap src, double red, double green, double blue) {
        // create output image
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        // color information
        int A, R, G, B;
        int pixel;
        // constant value curve
        final int    MAX_SIZE = 256;
        final double MAX_VALUE_DBL = 255.0;
        final int    MAX_VALUE_INT = 255;
        final double REVERSE = 1.0;

        // gamma arrays
        int[] gammaR = new int[MAX_SIZE];
        int[] gammaG = new int[MAX_SIZE];
        int[] gammaB = new int[MAX_SIZE];

        // setting values for every gamma channels
        for(int i = 0; i < MAX_SIZE; ++i) {
            gammaR[i] = (int)Math.min(MAX_VALUE_INT,
                    (int)((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / red)) + 0.5));
            gammaG[i] = (int)Math.min(MAX_VALUE_INT,
                    (int)((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / green)) + 0.5));
            gammaB[i] = (int)Math.min(MAX_VALUE_INT,
                    (int)((MAX_VALUE_DBL * Math.pow(i / MAX_VALUE_DBL, REVERSE / blue)) + 0.5));
        }

        // apply gamma table
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // look up gamma
                R = gammaR[Color.red(pixel)];
                G = gammaG[Color.green(pixel)];
                B = gammaB[Color.blue(pixel)];
                // set new color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public static Bitmap doBrightness(Bitmap src, int value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += value;
                if(R > 255) { R = 255; }
                else if(R < 0) { R = 0; }

                G += value;
                if(G > 255) { G = 255; }
                else if(G < 0) { G = 0; }

                B += value;
                if(B > 255) { B = 255; }
                else if(B < 0) { B = 0; }

                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }*/

    private class PDFAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog pdLoading = new ProgressDialog(getActivity());

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            try {
                if (Splash.fileType.equals("jpg")) {
                    pdLoading.setMessage("Saving as JPG...");
                } else {
                    pdLoading.setMessage("Saving as PDF...");
                }
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); // Make the user not touchable
                if (pdLoading != null)
                    pdLoading.dismiss();
                pdLoading.show();
                pdLoading.setCancelable(false); // Added to keep the progress bar
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Void doInBackground(Void... params) {
            File parentfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DocumentScanner/");
            int count;
            if (parentfile.exists()) {
                count = parentfile.listFiles().length;
            } else {
                count = 0;
            }
            String s = "New Doc " + (count + 1);
            // String s = "New Doc";
            convertit(Splash.modded, s);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //   super.onPostExecute(result);

            //this method will be running on UI thread

            pdLoading.dismiss();
            getActivity().finish();
        }

    }


    //test

    public Bitmap getCompressedBitmap(String imagePath) {
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

   /* @Override
    public void onBackPressed(){
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            getActivity().onBackPressed();
        }
    }*/

}

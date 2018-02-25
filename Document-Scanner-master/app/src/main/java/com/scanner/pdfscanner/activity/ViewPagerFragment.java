package com.scanner.pdfscanner.activity;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.activity.adapters.CustomPagerAdapter;
import com.scanner.pdfscanner.fragment.BaseFragment;
import com.scanner.pdfscanner.interfaces.ScanListener;
import com.scanner.pdfscanner.views.PinchImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewPagerFragment extends BaseFragment {
    private Bitmap original;

    @Bind(R.id.photo)
    PinchImageView imageView;
    @Bind(R.id.progress)
    ProgressBar progressBar;
    @Bind(R.id.pager)
    ViewPager pager;
    @Bind(R.id.addimage)
    ImageView addimage;
    private ScanListener scanListener;

    private Bitmap transformed;
    CustomPagerAdapter adapter;
    public static ViewPagerFragment newInstance(Bitmap bitmap) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        fragment.original = bitmap;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    View view =  inflater.inflate(R.layout.fragment_view_pager, container, false);
        ButterKnife.bind(this, view);
        this.transformed = original;
       // adapter = new CustomPagerAdapter(getActivity(), MyHomeAcitivty.modded);
        //pager.setAdapter(adapter);

        return  view;
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (original != null && !original.isRecycled()) {
            imageView.setImageBitmap(original);
            this.transformed = original;
        } else {
            imageView.setImageResource(R.drawable.no_image);
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
    @OnClick(R.id.back_ib)
    public void onBackButtonClicked(View view) {
        scanListener.onBackClicked();
        getActivity().finish();
    }

    @OnClick(R.id.ok_ib)
    public void onOKClicked(View view) {
        scanListener.OnClickFinesh();


        File picturefile =   getOutputMediaFile();
        if (picturefile == null) {

            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(picturefile);
            transformed.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

    }
    private static File getOutputMediaFile() {
        // make a new file directory inside the "sdcard" folder
        File mediaStorageDir = new File("/sdcard/", "DocumentStore");

        // if this "JCGCamera folder does not exist
        if (!mediaStorageDir.exists()) {
            // if you cannot make this folder return
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        int i = (int) (new Date().getTime()/1000);
        System.out.println("Integer : " + i);
        String name = String.valueOf(i);
        System.out.println("Time : " + name);



        File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + name + ".jpg");
        System.out.println("mediaFile : " + mediaFile);



        return mediaFile;
    }
    @OnClick(R.id.addimage)
    public void onAddImageClicked(View v)
    {

        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        CameraActivity.startCameraFromLocation(startingLocation, getActivity(), null);
        getActivity().finish();
        //overridePendingTransition(0, 0);
    }
    public void setBitmap(Bitmap bitmap) {
        this.original = bitmap;
         imageView.setImageBitmap(bitmap);
    }

    public void hideProgressBar()
    {
        progressBar.setVisibility(View.GONE);
    }
}

package com.scanner.pdfscanner.activity.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.scanner.pdfscanner.R;

import java.util.ArrayList;

public class CustomPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<Bitmap> mResources;
   public static ImageView imageView;
    public CustomPagerAdapter(Context context, ArrayList<Bitmap> mResources) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mResources = mResources;
    }

    public CustomPagerAdapter(Context applicationContext) {
    }

    public void updatepostion(Bitmap bitmap)
    {

        imageView.setImageBitmap(bitmap);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

      imageView  = (ImageView) itemView.findViewById(R.id.imageView);
        //Bitmap transformed = adjustedContrast(mResources.get(position),25);
        imageView.setImageBitmap(mResources.get(position));

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }


    public ImageView getImageView(){
        return imageView;
    }

    @Override
    public int getItemPosition(Object object) {
        // Causes adapter to reload all Fragments when
        // notifyDataSetChanged is called
        return POSITION_NONE;
    }

}
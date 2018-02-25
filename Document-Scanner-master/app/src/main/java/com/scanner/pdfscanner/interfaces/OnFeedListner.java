package com.scanner.pdfscanner.interfaces;

/**
 * Created by USER on 12/7/2016.
 */

public interface OnFeedListner {

    void onOrignalClick(int pos);
    void onMagicClick(int pos);
    void onGrayClick(int pos);
    void onBMClick(int pos);
    void onRotateRight(int pos);
    void onRotateLeft(int pos);
    void onBackClick();
    void onCropClick(int pos);
    void onAddimageClick();
    void onFineshClick();

}

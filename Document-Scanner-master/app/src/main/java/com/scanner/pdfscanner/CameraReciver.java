package com.scanner.pdfscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

/**
 * Created by USER on 12/21/2016.
 */

public class CameraReciver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.i("INFO", "Enter BroadcastReceiver");


        Cursor cursor = context.getContentResolver().query(intent.getData(),
                null, null, null, null);
        cursor.moveToFirst();
        String image_path = cursor.getString(cursor.getColumnIndex("_data"));



    }
}

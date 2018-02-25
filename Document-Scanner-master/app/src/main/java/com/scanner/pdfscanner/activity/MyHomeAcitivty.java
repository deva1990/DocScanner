package com.scanner.pdfscanner.activity;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.scanner.pdfscanner.R;
import com.scanner.pdfscanner.activity.adapters.DocsAdapter;
import com.scanner.pdfscanner.db.models.NoteGroup;
import com.scanner.pdfscanner.interfaces.PhotoSavedListener;
import com.scanner.pdfscanner.utils.TransformAndSaveTask;
import com.scanner.pdfscanner.views.DocItem;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MyHomeAcitivty extends BaseActivity {
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private RecyclerView rvDocs;
    private Uri fileUri1, selectedImage;
    ArrayList<DocItem> iPostParams;
    private DocsAdapter adapter;
    private NavigationView navigationView;
    PhotoSavedListener photoSavedListener;
    private static final int REQUEST_CAMERA_PERMISSIONS = 931;
    private static final int CAPTURE_MEDIA = 368;

    private Activity activity;

    public static ArrayList<Map<Integer, PointF>> pointsF;
    private NoteGroup mNoteGroup;
    public static ArrayList<Bitmap> modded;
    public static String fileType = "jpg";
    /* private static final int PERMISSION_REQUEST_CODE = 1;
     String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.CAMERA};
 */

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Hello Camera";

    private Uri fileUri; // file url to store image/video

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_my_home);
        modded = new ArrayList<>();
        pointsF = new ArrayList<>();
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_SMS, Manifest.permission.CAMERA};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }





      /*  toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initNavigationDrawer();
        getSupportActionBar().setHomeButtonEnabled(true);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        rvDocs = (RecyclerView) findViewById(R.id.rvDocs);
        iPostParams = new ArrayList<DocItem>();

        Filewalker fw = new Filewalker();
        String dirpath=android.os.Environment.getExternalStorageDirectory().toString();
        File reader = new File(dirpath, "DocumentScanner");
        fw.walk(reader);

        adapter = new DocsAdapter(getApplicationContext(), iPostParams);
        rvDocs.setAdapter(adapter);
        rvDocs.setLayoutManager(new GridLayoutManager(this, 2));
        View v = null;*/
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            // will close the app if the device does't have camera
            finish();
        }

     /*   if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

            captureImage();

        } else if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            int[] startingLocation = new int[2];
            linearLayout.getLocationOnScreen(startingLocation);
            startingLocation[0] += linearLayout.getWidth() / 2;
            CameraActivity.startCameraFromLocation(startingLocation, this, null);
            overridePendingTransition(0, 0);
            finish();
           *//* captureImage();*//*
        } else {

            captureImage();

        }*/

          captureImage();
       /* Intent target = new Intent();
        // target.setAction(Intent.ACTION_SEND);
       // target.setData(Uri.parse(dirpath+"/DocumentScanner/" + s + "/" + s + "_compressed.pdf"));
        target.putExtra("filePath","test");
        setResult(1,target);
        finish();*/


    }


    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        //finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public Uri getOutputMediaFileUri(int type) {


        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return FileProvider.getUriForFile(getApplicationContext(), "com.scanner.pdfscanner.fileprovider", getOutputMediaFile(type));
        } else {
            return Uri.fromFile(getOutputMediaFile(type));
        }


    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = null;
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            }
        }

        // String path1 = data.getStringExtra(BaseScannerActivity.EXTRAS.PATH);
        // PhotoUtil.deletePhoto(path1);
          /*  if(data!=null){
                //Bundle bundle = data.getExtras();
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);

                File file = new File(picturePath);

                cursor.close();
                openScannerActivity(picturePath, file.getName());


            }*/


    }

    private void previewCapturedImage() {
        try {
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            // options.inSampleSize = 20; // mdevarajan- commented this line

            //  final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
            //          options);

            File file = new File(String.valueOf(fileUri));
            String filename = file.getName();
            openScannerActivity(fileUri.getPath(), filename);

            //imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    public void onCameraClicked(View view) {
        int[] startingLocation = new int[2];
        view.getLocationOnScreen(startingLocation);
        startingLocation[0] += view.getWidth() / 2;
        CameraActivity.startCameraFromLocation(startingLocation, this, null);
        overridePendingTransition(0, 0);
    }

    public void onCameraClicked1() {
        final View v = null;
        int[] startingLocation = new int[2];
        v.getLocationOnScreen(startingLocation);
        startingLocation[0] += v.getWidth() / 2;
        CameraActivity.startCameraFromLocation(startingLocation, this, null);
        overridePendingTransition(0, 0);
    }

   /* @Override
    public void photoSaved(String path, String name) {
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra(BaseScannerActivity.EXTRAS.PATH, path);
        intent.putExtra(BaseScannerActivity.EXTRAS.NAME, name);
        intent.putExtra(BaseScannerActivity.EXTRAS.FROM_CAMERA, true);
        if(mNoteGroup!=null)
            intent.putExtra(NoteGroup.class.getSimpleName(), Parcels.wrap(mNoteGroup));

        //startActivityForResult(intent, BaseScannerActivity.EXTRAS.REQUEST_PHOTO_EDIT);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        finish();
    }*/

    /*@Override
    public void onNoteGroupSaved(NoteGroup noteGroup) {

    }*/

    public class Filewalker {

        public void walk(File root) {
            iPostParams = new ArrayList<>();
            DocItem postemail = new DocItem("Dummy Doc", "12/09/16", null);
//            iPostParams.add(postemail);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
            String formattedDate = df.format(c.getTime());
            System.out.println("Current time => " + formattedDate);

            File[] list = root.listFiles();
            if (list != null) {
                for (File f : list) {
                    if (f.isDirectory() && !(f.getName().equals("DoumentStore"))) {
                        Log.d("", "Dir: " + f.getAbsoluteFile());
                        postemail = null;
                        Bitmap b = null;
                        File file = new File(android.os.Environment.getExternalStorageDirectory(), "/DocumentScanner/" + f.getName() + ".jpg");
                        try {
                            b = BitmapFactory.decodeStream(new FileInputStream(file));
                            System.out.println("File name" + file);
                            System.out.println("bitmap" + b);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        postemail = new DocItem(f.getName().toString(), formattedDate, b);
                        iPostParams.add(postemail);
                        // walk(f);
                    } else {
                        Log.d("", "File: " + f.getAbsoluteFile());
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //navigationView.setCheckedItem(0);
       /* Filewalker fw = new Filewalker();
        String dirpath=android.os.Environment.getExternalStorageDirectory().toString();
        File reader = new File(dirpath, "DocumentScanner");
        fw.walk(reader);


        adapter = new DocsAdapter(getApplicationContext(), iPostParams);
        rvDocs.setAdapter(adapter);
        rvDocs.setLayoutManager(new GridLayoutManager(this, 2));*/
    }


    private class GalleryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            openMediaContent();
        }

    }

    public void openMediaContent() {
       /* Intent photoPickerIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoPickerIntent, 0x201);*/
    }

    public void openCamera() {


        Intent in = new Intent(MyHomeAcitivty.this, CameraActivity.class);
        startActivity(in);

    }


    private void openScannerActivity(String path, String name) {
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra(BaseScannerActivity.EXTRAS.PATH, path);
        intent.putExtra(BaseScannerActivity.EXTRAS.NAME, name);
        intent.putExtra(BaseScannerActivity.EXTRAS.FROM_CAMERA, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT); //Carry forwarding the result to Splash
        startActivity(intent);
        // startActivityForResult(intent, BaseScannerActivity.EXTRAS.REQUEST_PHOTO_EDIT);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        finish();
    }

    private void saveTransformedImage(final String path, final String name) {
        Target loadingTarget = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                new TransformAndSaveTask(mNoteGroup, name, bitmap, new PhotoSavedListener() {
                    String croppedPath = "";

                    @Override
                    public void photoSaved(String path, String name) {
//                        Toast.makeText(CameraActivity.this, "Photo " + name + " saved1", Toast.LENGTH_SHORT).show();
                        croppedPath = path;
                    }

                    @Override
                    public void onNoteGroupSaved(NoteGroup noteGroup) {

                    }
                }).execute();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        };

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //ImageManager.i.loadPhoto(path, metrics.widthPixels, metrics.heightPixels, loadingTarget);

    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public void initNavigationDrawer() {

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                switch (id) {
                    case R.id.home:

                        drawerLayout.closeDrawers();
                        break;
                    case R.id.settings:
                        openMediaContent();//this will open the gallery
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.docs:
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.logout://exit the app
                        finish();

                }
                return true;
            }
        });
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
/*
    private boolean checkPermission() {


        int result = ContextCompat.checkSelfPermission(HomeActivity.this, String.valueOf(PERMISSIONS));
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, String.valueOf(PERMISSIONS))) {
            Toast.makeText(HomeActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }*/

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


}
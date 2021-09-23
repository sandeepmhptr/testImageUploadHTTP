package com.example.testimageuploadhttp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.CheckBox;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import butterknife.BindView;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, UploadStatusDelegate {
    private static final int REQUEST = 112;
    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;


    //private AndroidPermissions mPermissions;
    private Button buttonChoose;
    private Button buttonUpload;
    private Button buttonDownload;
    private ImageView imageView;
    private EditText editText;
    private EditText imageName;
    private EditText imagePath;
    //private CheckBox displayNotification;
    private CheckBox autoDeleteUploadedFiles;
    //private CheckBox autoClearOnSuccess;
    private CheckBox fixedLengthStreamingMode;
    //private CheckBox useUtf8;

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;

    //Bitmap to get image from gallery
    private Bitmap bitmap;

    //Uri to store the image uri
    private Uri filePath;

    private String path;

    private  String USER_AGENT ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Requesting storage permission
        //requestStoragePermission();

        //Initializing views
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonDownload = (Button) findViewById(R.id.buttonDownload);
        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editTextName);
        imageName = (EditText) findViewById(R.id.editImageName);
        imagePath = (EditText) findViewById(R.id.editImagePath);

        //displayNotification = (CheckBox)findViewById(R.id.displayNotification);
        autoDeleteUploadedFiles = (CheckBox)findViewById(R.id.autoDeleteUploadedFiles);
        //autoClearOnSuccess = (CheckBox)findViewById(R.id.autoClearOnSuccess);
        fixedLengthStreamingMode = (CheckBox)findViewById(R.id.fixedLengthStreamingMode);
        //useUtf8 = (CheckBox)findViewById(R.id.useUtf8);

        //Setting clicklistener
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        buttonDownload.setOnClickListener(this);
        USER_AGENT = getString(R.string.app_name) + "/" + BuildConfig.VERSION_NAME;
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST:
            case STORAGE_PERMISSION_CODE:
            default: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                    //Toast.makeText(this, "The app was  allowed to read your store.", Toast.LENGTH_LONG).show();
                } else {
                    //Toast.makeText(this, "The app was not allowed to read your store.", Toast.LENGTH_LONG).show();
                }
            }
        }


    }

    @Override
    public void onClick(View v) {
        if (v == buttonChoose) {
            showFileChooser();
        }
        if (v == buttonDownload) {
            Intent inte = new Intent(MainActivity.this,
                    DownloadActivity.class);
            startActivity(inte);
            //invoke the SecondActivity.
            finish();
        }
        if (v == buttonUpload) {
            path = getPath(filePath);
            imagePath.setText(path);
            showImage();
            if (Build.VERSION.SDK_INT >= 23) {
                String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.FOREGROUND_SERVICE};
                if (!hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions((Activity) this, PERMISSIONS, REQUEST );
                    imagePath.setText("SDK>23,has no permission");
                    uploadMultipart();

                } else {
                    //do here
                    imagePath.setText("SDK>23,has no permission");
                    uploadMultipart();

                }
            } else {
                //do here
                imagePath.setText("SDK<23");
                uploadMultipart();

            }

        }
    }

    private boolean hasPermissions(MainActivity mainActivity, String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        imageName.setText("editText.getText().toString().trim()");
        imagePath.setText(editText.getText().toString().trim());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

        }
    }

    private void uploadMultipart() {
        String name = editText.getText().toString().trim();
        imageName.setText(name);

        try {
            MultipartUploadRequest req = new MultipartUploadRequest(this, Constants.UPLOAD_URL)
                    .addFileToUpload(path, "image")
                    .addParameter("name", name) //Adding text parameter to the request
                    .setNotificationConfig(getNotificationConfig(name))
                    .setCustomUserAgent(USER_AGENT)
                    .setAutoDeleteFilesAfterSuccessfulUpload(autoDeleteUploadedFiles.isChecked())
                    .setUsesFixedLengthStreamingMode(fixedLengthStreamingMode.isChecked())
                    .setMaxRetries(3);

            String uploadID = req.setDelegate(this).startUpload();
        } catch (FileNotFoundException exc) {
            //showToast(exc.getMessage());
        } catch (IllegalArgumentException exc) {
            //showToast("Missing some arguments. " + exc.getMessage());
        } catch (MalformedURLException exc) {
            //showToast(exc.getMessage());
        }

    }

    private UploadNotificationConfig getNotificationConfig(String filename) {
        //if (!displayNotification.isChecked()) return null;

        return new UploadNotificationConfig()
                //.setIcon(R.drawable.ic_upload)
                //.setCompletedIcon(R.drawable.ic_upload_success)
                //.setErrorIcon(R.drawable.ic_upload_error)
                //.setTitle(filename)
                //.setInProgressMessage("getString(R.string.uploading)")
                //.setCompletedMessage("getString(R.string.upload_success)")
                //.setErrorMessage("getString(R.string.upload_error)")
                //.setAutoClearOnSuccess(autoClearOnSuccess.isChecked())
                //.setClickIntent(new Intent(this, MainActivity.class))
                //.setClearOnAction(true)
                .setRingToneEnabled(true);
    }

    private String getPath(Uri uri) {
        return getPath4(uri);
    }

    private String getPath1(Uri uri) {
        File file = new File(uri.getPath());//create path from uri
        String path = file.getPath();
        return path;
    }
    private String getPath2(Uri uri) {
        File file = new File(uri.getPath());//create path from uri
        final String[] split = file.getPath().split(":");//split the path.
        String path = split[1];
        return path;
    }
    private String getPath3(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = this.getContentResolver().query(uri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private String getPath4(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
    private void showImage(){
        show(2);
    }
    private void show(int i){
        switch(i)
        {
            case 1:
                showImgFromBitmap();
                break;
            case 2:
                showImgFromFilePath();
                break;
            default:
                showImgFromBitmap();
                break;
        }
    }
    private void showImgFromBitmap() {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showImgFromFilePath() {
        Bitmap image = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(image);
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        editText.setText("progress");
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
        editText.setText("error");
        Writer writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        String s = writer.toString();
        editText.setText(s);
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        editText.setText(serverResponse.getBodyAsString());
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        editText.setText("cancelled");
    }

    public class Constants {
        public static final String UPLOAD_URL = "https://swulj.000webhostapp.com/upload.php";
        public static final String TEST_URL = "https://swulj.000webhostapp.com/test.php";
        public static final String IMAGES_URL = "https://swulj.000webhostapp.com/getImages.php";
    }
}
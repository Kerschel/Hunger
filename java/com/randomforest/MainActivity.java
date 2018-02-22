package com.example.sachinrajkumar.randomforest;



import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.Trace;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//
//import com.app.androidkt.tensorf.util.ImageUtils;
//import com.app.androidkt.tensorf.util.Recognition;
//import com.example.sachinrajkumar.randomforest.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private static final int PERMISSIONS_REQUEST = 1;

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "graph.pb";
    private static final String LABEL_FILE = "labels.txt";
    private static final boolean MAINTAIN_ASPECT = true;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final float TEXT_SIZE_DIP = 10;
    private Handler handler;
    private HandlerThread handlerThread;
    private Integer sensorOrientation;
    private int previewWidth = 0;
    private int previewHeight = 0;
    private byte[][] yuvBytes;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap;
    private boolean computing = false;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private TextView resultsView;
    //  private BorderedText borderedText;
    private long lastProcessingTimeMs;
    public ImageView image;
    private TextView mTextMessage;
    public static final int SELECT_IMAGE = 234;
    private TensorFlowImageClassifier classifier;
    public LocationManager manage;
    double latti;
    double longi;
    private Uri filePath;
    private StorageReference mStorageRef;
    public ImageButton imgBtn;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    DatabaseReference mConditionRef = myRef.child("condition");


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,0);
                    mTextMessage.setText(R.string.title_notifications);


//                    mTextMessage.setText(R.string.title_home);
                    return true;
//                case R.id.navigation_dashboard:
//                    intent = new Intent(Intent.ACTION_PICK);
//                    intent.setType("image/*");
//                    startActivityForResult(intent,1889);

//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;

                case R.id.navigation_dashboard:
                    Intent myIntent = new Intent(MainActivity.this,Map.class);
                    MainActivity.this.startActivity(myIntent);
                    return true;

                case R.id.navigation_notifications:
//                    Intent myIntent = new Intent(MainActivity.this,Map.class);
//                    MainActivity.this.startActivity(myIntent);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        Bitmap bitmap = null;
        Bitmap bitmap2 = null;
        Uri selectedImage;
//        setContentView(R.layout.activity_camera);
        if(requestCode == 1889){
           System.out.println( "Gallery type" + data.getData());
            selectedImage = data.getData();
            try {
                bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                bitmap = (Bitmap) data.getExtras().get("data");
                System.out.println("GALLERY IMAGE : "+ bitmap);
                image.setImageBitmap(bitmap2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            selectedImage = data.getData();
            bitmap = (Bitmap) data.getExtras().get("data");
            System.out.println(bitmap.getHeight() +" bitmap withd" +bitmap.getWidth() + "Config "+bitmap.getConfig());
            System.out.println( "Camera type:" +data.getData());
            System.out.println("Camera IMAGE : "+ bitmap);
            image.setImageBitmap(bitmap);

        }
        classifier = new TensorFlowImageClassifier(getResources().getAssets(), MODEL_FILE, LABEL_FILE, INPUT_SIZE, IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME);
        final List<Recognition> results = classifier.recognizeImage(bitmap);
        System.out.println("Result is :"+ results);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("dd-MMM-yyyy-hh:mm");
        String datetime = dateformat.format(c.getTime());
        if(results.size() !=0){
//            String condition = results.get(0).toString().split(" ")[0];
            String condition = results.get(0).toString().split(" ")[0];

            System.out.println("Condition:" + condition);
            final User user = new User(condition,datetime,latti,longi);
            String key = database.getReference().push().getKey();
            myRef.child(key).setValue(user);
            mTextMessage.setText(results.toString());
            uploadFile(selectedImage,key);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        image = (ImageView) findViewById(R.id.imagecam);
        imgBtn = (ImageButton) findViewById(R.id.cameraBtn);
        imgBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);

            }
        });

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        manage  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();


    }

    public void getLocation() {
        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            Location location = manage.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            System.out.println("Latitudesas" + latti);
            if (location != null){
                latti = location.getLatitude();
                longi = location.getLongitude();
                System.out.println("Latitude" + latti);
            }
        }

    }


    private void uploadFile(Uri image,String key){
        if (image != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            StorageReference riversRef = mStorageRef.child("images/"+key+".jpg");

            riversRef.putFile(image)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded",Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage(((int) progress) + "% Uploaded...");
                }
            });
        }
    }





}


package com.example.friends.opennings;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;
import com.example.friends.utility.NetworkChangeListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ImageOpen extends AppCompatActivity {
    private ImageView img,down;

    NetworkChangeListener networkChangeListener=new NetworkChangeListener();

    @Override
    protected void onStart() {
        IntentFilter intentFilter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,intentFilter);
        super.onStart();

    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_image_open);
        img=(ImageView) findViewById(R.id.opened_image);
        down=(ImageView) findViewById(R.id.download_image);
        String uri= String.valueOf(getIntent().getExtras().get("uri"));
        Picasso.get().load(uri).placeholder(R.drawable.home).into(img);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bmp= ((BitmapDrawable)img.getDrawable()).getBitmap();
                String time=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
                File path= Environment.getExternalStorageDirectory();
                File dir=new File(path+"/DCIM/Dark");
                dir.mkdirs();
                File file=new File(dir,time+".PNG");
                OutputStream outputStream;
                try {
                    outputStream=new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                    outputStream.flush();
                    outputStream.close();
                    Toast.makeText(ImageOpen.this, "Saved", Toast.LENGTH_SHORT).show();


                }catch (Exception e){

                }

            }
        });



    }
}
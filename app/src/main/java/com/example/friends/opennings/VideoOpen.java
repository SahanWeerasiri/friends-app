package com.example.friends.opennings;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;
import com.example.friends.utility.NetworkChangeListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class VideoOpen extends AppCompatActivity {
    private VideoView vid;
    private ImageView down;

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
        setContentView(R.layout.activity_video_open);
        vid=(VideoView) findViewById(R.id.opened_video);
        down=(ImageView) findViewById(R.id.download_video);
        String uri= String.valueOf(getIntent().getExtras().get("uri"));

        vid.setVideoURI(Uri.parse(uri));
        MediaController mc=new MediaController(VideoOpen.this);
        mc.setAnchorView(vid);
        vid.setMediaController(mc);

        vid.start();

        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String time=new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());

                DownloadManager.Request request=new DownloadManager.Request(Uri.parse(uri));
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setTitle("Download");
                request.setDescription("Downloading...");
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM,"/Dark/"+time+".MP4");

                DownloadManager manager=(DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);

            }
        });



    }
}
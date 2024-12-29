package com.example.friends;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class notifications extends Application {
    String CHANEL_ID="Dark MSG";
    String user_name="",url="";
    private static String sender,receiver,mid;

    private static int id=1;
    Context context;
    PendingIntent newIntent;
    private static NotificationChannel channel;
    private static NotificationManagerCompat notificationManager;
    public notifications(Context context){
        this.context=context;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel=new NotificationChannel(CHANEL_ID,"Dark MSG", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Dark Msg Notifications");
            channel.enableVibration(true);
            NotificationManager manager=context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
    }
    public void Notify(String title, String msg,String mid,String sender,String receiver){
        if(mid!=null){
            this.mid=mid;
        }
        if(sender!=null){
            this.sender=sender;
        }
        if(receiver!=null){
            this.receiver=receiver;
        }
         newIntent=PendingIntent.getActivity(context,0,new Intent(context, MainActivity.class),PendingIntent.FLAG_MUTABLE);
        FirebaseDatabase.getInstance().getReference().child("Users").child(sender).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.getKey().equals("user_name")){
                        user_name=ds.getValue().toString();
                    }
                    if(ds.getKey().equals("user_image")){
                        url=ds.getValue().toString();
                    }
                }
                /*Intent chats=new Intent(context, ChatDetails.class);
                chats.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                chats.putExtra("user_id",sender);
                chats.putExtra("user_name",user_name);
                chats.putExtra("user_image",url);
                chats.putExtra("read","YES");
                 newIntent=PendingIntent.getActivity(context,0,chats,PendingIntent.FLAG_MUTABLE);*/

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




       /* PendingIntent pendingIntent=PendingIntent.getActivity(context,0,new Intent(context, MainActivity.class),PendingIntent.FLAG_MUTABLE);
        Intent broadcast=new Intent(context, NotificationReceiver.class);
        broadcast.putExtra("sender",this.sender);
        broadcast.putExtra("receiver",this.receiver);
        broadcast.putExtra("msg_id",this.mid);

        PendingIntent actionIntent=PendingIntent.getBroadcast(context,0,broadcast,PendingIntent.FLAG_MUTABLE);*/

        notificationManager= NotificationManagerCompat.from(context);
        FirebaseDatabase.getInstance().getReference().child("Users").child(sender).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    if(ds.getKey().equals("user_name")){
                        user_name=ds.getValue().toString();
                    }
                }

                @SuppressLint("NotificationTrampoline") Notification notification=new NotificationCompat.Builder(context,CHANEL_ID)
                        .setSmallIcon(R.drawable.batman64)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setContentTitle(title + user_name)
                        .setContentText(msg)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(newIntent )

                        .setOnlyAlertOnce(true)
                       // .addAction(R.drawable.batman64,"Read",actionIntent)
                        .setAutoCancel(true)

                        .build();
                notificationManager.notify(id,notification);
                id++;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}

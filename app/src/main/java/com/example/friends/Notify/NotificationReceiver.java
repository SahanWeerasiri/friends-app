package com.example.friends.Notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.friends.ChatDetails;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class NotificationReceiver extends BroadcastReceiver {
    String username="",url="";
    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            String sender=intent.getStringExtra("sender");
            String mid=intent.getStringExtra("msg_id");
            String receiver=intent.getStringExtra("receiver");
            Toast.makeText(context, receiver+sender, Toast.LENGTH_SHORT).show();
            HashMap<String,Object> map=new HashMap<>();
            map.put("seen","true");
            if(sender.isEmpty() ||  receiver.isEmpty()){

            }else{
                FirebaseDatabase.getInstance().getReference().child("Chats").child(receiver+sender).child(mid).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(sender).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    if(ds.getKey().equals("user_name")){
                                        username=ds.getValue().toString();
                                    }
                                    if(ds.getKey().equals("user_image")){
                                        url=ds.getValue().toString();
                                    }
                                }
                                Intent chats=new Intent(context, ChatDetails.class);
                                chats.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                chats.putExtra("user_id",sender);
                                chats.putExtra("user_name",username);
                                chats.putExtra("user_image",url);
                                chats.putExtra("read","YES");
                                context.startActivity(chats);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }


        }catch (Exception e){

        }




    }
}

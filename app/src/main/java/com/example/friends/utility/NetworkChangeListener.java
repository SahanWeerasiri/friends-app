package com.example.friends.utility;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class NetworkChangeListener extends BroadcastReceiver {
    static String ONLINE="ONLINE",OFFLINE="OFFLINE";


    @Override
    public void onReceive(Context context, Intent intent) {
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        if(!Connections.isConnected(context)){

            new AlertDialog.Builder(context).setMessage("No Connection").create().show();

        }else{
            HashMap<String,Object> map=new HashMap<>();
            map.put("ONLINE",ONLINE);
            firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid()).updateChildren(map);

        }
    }
}

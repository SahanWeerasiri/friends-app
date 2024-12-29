package com.example.friends;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.friends.Adapter.fragment_adapter;
import com.example.friends.ModelClasses.Chats;
import com.example.friends.databinding.ActivityMainBinding;
import com.example.friends.opennings.UpdateApp;
import com.example.friends.service.MyService;
import com.example.friends.utility.NetworkChangeListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    ArrayList<String> friends=new ArrayList<>();
    ArrayList<String> updatedUsers=new ArrayList<>();

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInOptions gso;
    private FirebaseDatabase database;
    static String SEEN="true",UNSEEN="false",ONLINE="ONLINE",OFFLINE="OFFLINE";
    static boolean ok=false,update_alert=false;

    NetworkChangeListener networkChangeListener=new NetworkChangeListener();

    @Override
    protected void onStart() {
        IntentFilter intentFilter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,intentFilter);
        Intent intent=new Intent(MainActivity.this, MyService.class);
        startService(intent);
        super.onStart();

    }



    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        HashMap<String,Object> map=new HashMap<>();
        map.put("ONLINE",OFFLINE);
        database.getReference().child("Users").child(firebaseAuth.getUid()).updateChildren(map);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HashMap<String,Object> map=new HashMap<>();
        map.put("ONLINE",OFFLINE);
        database.getReference().child("Users").child(firebaseAuth.getUid()).updateChildren(map);
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        moveTaskToBack(false);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.SEND_SMS,
                        android.Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE
                }, PackageManager.PERMISSION_GRANTED);

        firebaseAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        database.getReference().child("Update").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String state="false",date="";
                updatedUsers.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.getKey().equals("Update"))
                        state="true";
                    else if(ds.getKey().equals("date"))
                        date=ds.getValue().toString();
                    else{
                        updatedUsers.add(ds.getKey().toString());
                    }
                }
                boolean isUserUpdated=false;
                for(int i=0;i<updatedUsers.size();i++){
                    if(updatedUsers.get(i).equals(firebaseAuth.getUid())){
                        isUserUpdated=true;
                        break;
                    }else{
                        isUserUpdated=false;
                    }
                }
                if(state.equals("true") && !isUserUpdated){
                    new AlertDialog.Builder(MainActivity.this)
                            .setCancelable(false)
                            .setMessage("Update is required. Do you want download the update ? ( "+date+" )")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UpdateApp updateApp=new UpdateApp(MainActivity.this);
                                    updateApp.downloadUpdate();
                                    HashMap<String ,Object> map_up=new HashMap<>();
                                    map_up.put(firebaseAuth.getUid(),"Updated");
                                    database.getReference().child("Update").updateChildren(map_up);

                                    dialog.cancel();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("Thank you !!!")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            }).setCancelable(false).create().show();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .create().show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        HashMap<String,Object> map=new HashMap<>();
        map.put("ONLINE",ONLINE);
        database.getReference().child("Users").child(firebaseAuth.getUid()).updateChildren(map);

        database.getReference().child("Users").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friends.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.getKey().equals("background") ){
                        if(ds.getValue()!=null){
                            Picasso.get().load(ds.getValue().toString()).fit().placeholder(R.drawable.home).into(binding.homeImage);
                        }
                    }
                    if(ds.getKey().equals("Friends")){
                        for(DataSnapshot ds2:ds.getChildren()){
                            friends.add(ds2.getKey());

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient= GoogleSignIn.getClient(MainActivity.this,gso);

        binding.viewPager.setAdapter(new fragment_adapter(getSupportFragmentManager(),friends));
        binding.tabs.setupWithViewPager(binding.viewPager);
        newMessages();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.setting) {
            startActivity(new Intent(MainActivity.this, setting.class));
        } else if (itemId == R.id.group_chat) {
            startActivity(new Intent(MainActivity.this, groupChat.class));
        } else if (itemId == R.id.log_out) {
            firebaseAuth.signOut();
            GoogleSignIn.getClient(MainActivity.this, gso).signOut();
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    public void newMessages(){
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        database.getReference().child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds!=null){
                        for(DataSnapshot ds1:ds.getChildren()){
                            if(ds1!=null){
                                Chats chats=ds1.getValue(Chats.class);
                                if(chats.getReceiver().equals(firebaseAuth.getUid()) && chats.getSeen().equals(UNSEEN)){
                                    notifications note=new notifications(MainActivity.this);
                                    note.Notify("New Messages from ",chats.getMsg(),ds1.getKey(),ds1.child("sender").getValue().toString(),ds1.child("receiver").getValue().toString());

                                }
                            }}

                    }
                }
                ok=true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*if(true){try {
            database.getReference().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren()){
                        if(ds.getKey().equals("Update") && ds.getValue(String.class).equals("false")){
                            storage.getReference().child("App").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    notifications note = new notifications(MainActivity.this);
                                    note.Notify("New Update",uri.toString(),"","","");
                                    update_alert=true;
                                }
                            });


                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        }*/


    }
}
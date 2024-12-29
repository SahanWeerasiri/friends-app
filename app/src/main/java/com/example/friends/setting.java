package com.example.friends;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.databinding.ActivitySettingBinding;
import com.example.friends.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class setting extends AppCompatActivity {
ActivitySettingBinding binding;
FirebaseAuth auth;
FirebaseDatabase database;
FirebaseStorage storage;
static String ONLINE="ONLINE",OFFLINE="OFFLINE";
    SmsManager msg;

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
        HashMap<String,Object> map=new HashMap<>();
        map.put("ONLINE",OFFLINE);
        database.getReference().child("Users").child(auth.getUid()).updateChildren(map);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        database.getReference().child("Users").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.getKey().equals("background") ){
                        if(ds.getValue()!=null){
                            Picasso.get().load(ds.getValue().toString()).fit().placeholder(R.drawable.home).into(binding.settingBackImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewUI();
        binding.Aboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog d=new Dialog(setting.this);
                d.setCancelable(true);
                d.setTitle("About Us");
                d.setContentView(R.layout.aboutus);
                Picasso.get().load(R.drawable.batman64).placeholder(R.drawable.batman64).into((ImageView) d.findViewById(R.id.about_us_img));
                d.show();
            }
        });

        binding.help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog d=new Dialog(setting.this);
                d.setCancelable(true);
                d.setTitle("HELP");
                d.setContentView(R.layout.aboutus);
                TextView title=(TextView)d.findViewById(R.id.about_us_title);
                title.setText("Help");
                TextView content=(TextView)d.findViewById(R.id.about_us_content);
                content.setText("call : 0765820661 for help");
                Picasso.get().load(R.drawable.info).placeholder(R.drawable.batman64).into((ImageView) d.findViewById(R.id.about_us_img));
                d.show();
            }
        });

        binding.invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg=SmsManager.getDefault();
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI),111);

            }
        });

        binding.settingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(setting.this,MainActivity.class));
            }
        });

        binding.addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent img=new Intent();
                img.setAction(Intent.ACTION_GET_CONTENT);
                img.setType("image/*");
                startActivityForResult(img,25);
            }
        });

        binding.Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,Object> map=new HashMap<>();
                map.put("user_name",binding.txtNewUser.getText().toString());
                map.put("about",binding.txtNewAbout.getText().toString());
                map.put("number",binding.txtUserNumber.getText().toString());
                database.getReference().child("Users").child(auth.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(setting.this, "Updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        binding.privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(setting.this,privacy_setting.class));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getData()!=null && requestCode==25){
            Uri uri=data.getData();
            binding.SettingImg.setImageURI(uri);
            storage.getReference().child("Profile").child(auth.getUid()).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storage.getReference().child("Profile").child(auth.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(auth.getUid()).child("user_image").setValue(uri.toString());
                        }
                    });
                }
            });

        }
        if(data.getData()!=null && requestCode==111){
            Uri uri1=data.getData();
            Cursor cursor=getContentResolver().query(uri1,null,null,null,null);
            String num="";
            if(cursor.moveToFirst()){
                int phoneIndex=cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                 num=cursor.getString(phoneIndex);
                msg.sendTextMessage(num,null,"Dark Msg",null,null);
                String finalNum = num;
                storage.getReference().child("App").child("app-debug.apk").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(setting.this, uri.toString(), Toast.LENGTH_SHORT).show();
                        msg.sendTextMessage(finalNum,null,uri.toString(),null,null);
                    }});
                msg.sendTextMessage(num,null,"get app via above link",null,null);
            }




        }
    }
    public void viewUI(){
        database.getReference().child("Users").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){

                    try{
                        if(ds.getKey().equals("number")){
                            binding.txtUserNumber.setText(ds.getValue().toString());
                        }
                    }catch (Exception e){
                        binding.txtUserNumber.setText("");
                    }

                    try {
                        if(ds.getKey().equals("about")){
                            binding.txtNewAbout.setText(ds.getValue().toString());
                        }
                        if(ds.getKey().equals("user_image")){
                            Picasso.get().load(ds.getValue().toString()).placeholder(R.drawable.batman64).into(binding.SettingImg);
                        }
                        if(ds.getKey().equals("user_name")){
                            binding.txtNewUser.setText(ds.getValue().toString());
                        }



                    }catch (Exception e){
                        binding.txtNewAbout.setText("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
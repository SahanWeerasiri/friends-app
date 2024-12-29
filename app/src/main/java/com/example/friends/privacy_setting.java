package com.example.friends;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.databinding.ActivityPrivacySettingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class privacy_setting extends AppCompatActivity {
    ListView setting;
    ActivityPrivacySettingBinding binding;
    final String[] items={"Change Background"};
    final String[] sub_items={"change it as you wish"};
    ProgressDialog pd;
    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPrivacySettingBinding.inflate(getLayoutInflater());
        getSupportActionBar().hide();
        setContentView(binding.getRoot());
        binding.privacySettingBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(privacy_setting.this, com.example.friends.setting.class));
            }
        });
        pd= new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setTitle("Updating Background");
        pd.setCancelable(false);

        setting=(ListView) findViewById(R.id.setting_list);
        setting.setAdapter(new ArrayAdapter<String>(privacy_setting.this,R.layout.setting_lists,items));
        setting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent img=new Intent();
                img.setAction(Intent.ACTION_GET_CONTENT);
                img.setType("image/*");
                startActivityForResult(img,50);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getData()!=null && requestCode==50){

            try {
                pd.show();
                FirebaseStorage.getInstance().getReference().child("background").child(FirebaseAuth.getInstance().getUid()).putFile(data.getData()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        FirebaseStorage.getInstance().getReference().child("background").child(FirebaseAuth.getInstance().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("background").setValue(uri.toString());
                                pd.cancel();
                                Toast.makeText(privacy_setting.this,"Updated",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });


            } catch (Exception e) {
                Toast.makeText(this, "Not Updated. Check your Connection.", Toast.LENGTH_SHORT).show();
            }


        }
    }
}
package com.example.friends;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.friends.Adapter.chat_adapter;
import com.example.friends.ModelClasses.Chats;
import com.example.friends.databinding.ActivityGroupChatBinding;
import com.example.friends.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class groupChat extends AppCompatActivity {
    ActivityGroupChatBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog progressDialog;
    FirebaseUser firebaseUser;
    PdfDocument pdfDocument;
    Document document;
    FirebaseStorage storage;
    String sender,senderID,receiver,receiverID,receiverImg;
    ArrayList<Chats> list;
    String mid;
    HashMap<String,String>user_map=new HashMap<>();

    static String  msg_type="msg",img_type="img",vid_type="video",date,time,ONLINE="ONLINE",OFFLINE="OFFLINE";
    static int go_to_images=30,go_to_video=40;
    static String SEEN="true",UNSEEN="false";

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
        firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid()).updateChildren(map);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        list=new ArrayList<>();
        getSupportActionBar().hide();

        progressDialog=new ProgressDialog(groupChat.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setMessage("Please wait...");
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        storage=FirebaseStorage.getInstance();
        senderID=firebaseAuth.getUid();

        firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.getKey().equals("background") ){
                        if(ds.getValue()!=null){
                            Picasso.get().load(ds.getValue().toString()).fit().placeholder(R.drawable.home).into(binding.groupChatBackImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        firebaseDatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    user_map.put(ds.getKey(),ds.child("user_name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewChats();

        binding.backChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(groupChat.this,MainActivity.class));
            }
        });

        binding.addAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(groupChat.this).setCancelable(false)
                        .setPositiveButton("Image", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent img=new Intent();
                                img.setAction(Intent.ACTION_GET_CONTENT);
                                img.setType("image/*");
                                startActivityForResult(img,go_to_images);
                            }
                        })
                        .setNegativeButton("Video", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent vid=new Intent();
                                vid.setAction(Intent.ACTION_GET_CONTENT);
                                vid.setType("video/*");
                                startActivityForResult(vid,go_to_video);
                            }
                        }).setTitle("Media")
                        .create().show();


            }
        });

        binding.sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timestamp();
                Chats chats=new Chats(binding.txtMsg.getText().toString(), time,date,senderID,"Sample",UNSEEN,msg_type);
                chats.setGroup("group");
                firebaseDatabase.getReference().child("Group").push().setValue(chats).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                                    binding.txtMsg.setText("");
                                    viewChats();

                        }
                    }
                });
            }
        });

        binding.menuChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(groupChat.this,v);
                popupMenu.inflate(R.menu.groupchatmenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.clearChats){
                            clearChats().show();
                        }else if(item.getItemId()==R.id.exportChats){
                            exportChats();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }
    public AlertDialog clearChats(){
        AlertDialog d=new AlertDialog.Builder(groupChat.this).setMessage("Do you want to delete all the chats ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setCancelable(false)
                .create();
        return  d;
    }

    private void exportChats() {
        firebaseDatabase.getReference().child("Group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File f=new File(path,"Group Chat.pdf");
                PdfWriter writer=new PdfWriter(f);
                pdfDocument=new PdfDocument(writer);
                document=new Document(pdfDocument);
                document.add(new Paragraph("Group Chat Log").setBold().setUnderline().setTextAlignment(TextAlignment.CENTER));
                for(DataSnapshot ds:snapshot.getChildren()){
                    Chats chats3=ds.getValue(Chats.class);

                        if(chats3.getSender().equals(firebaseAuth.getUid())){
                            document.add(new Paragraph("Me :"));
                        }else{
                            document.add(new Paragraph(user_map.get(chats3.getSender())+" :"));
                        }
                        if(chats3.getType().equals(img_type)){
                            document.add(new Paragraph("IMAGE"));
                        }else if(chats3.getType().equals(vid_type)){
                            document.add(new Paragraph("VIDEO"));
                        }else{
                            document.add(new Paragraph(chats3.getMsg()));
                        }
                        document.add(new Paragraph(chats3.getDate()+"_"+chats3.getTime()));
                        document.add(new Paragraph("----------------------------"));


                }
                document.close();
                    Toast.makeText(groupChat.this,"OK",Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(groupChat.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void timestamp(){
        long t = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("h:m:s a");
        time = sdf.format(t);
        t = new Date().getTime();
        sdf = new SimpleDateFormat("yyyy/MM/dd");
        date = sdf.format(t);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            timestamp();
            if(data.getData()!=null && requestCode==go_to_images) {
                uploadAndSend(data.getData(),img_type);
            }else if(data.getData()!=null && requestCode==go_to_video){
                uploadAndSend(data.getData(),vid_type);
            }
        }catch (Exception e){
            Toast.makeText(this, "Select media to send", Toast.LENGTH_SHORT).show();
        }
    }
    public void uploadAndSend(Uri uri,String type){
        storage.getReference().child("Group").child(senderID).child(date + time).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.show();
                storage.getReference().child("Group").child(senderID).child(date + time).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Chats chats = new Chats(uri.toString(), time, date, senderID, "sample", "false", type);
                        chats.setGroup("group");
                        firebaseDatabase.getReference().child("Group").push().setValue(chats).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    binding.txtMsg.setText("");
                                    progressDialog.cancel();
                                }
                            }
                        });
                    }
                });
            }
        });
    }
    private void viewChats(){
        list=new ArrayList<>();
        chat_adapter adapter=new chat_adapter(list,groupChat.this );
        binding.chatDetailRecycler.setAdapter(adapter);

        LinearLayoutManager llm=new LinearLayoutManager(groupChat.this);
        binding.chatDetailRecycler.setLayoutManager(llm);

        firebaseDatabase.getReference().child("Group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Chats chats=ds.getValue(Chats.class);
                    chats.setMsgID(ds.getKey());
                    chats.setGroup("group");
                    list.add(chats);
                }
                adapter.notifyDataSetChanged();
                binding.chatDetailRecycler.smoothScrollToPosition(binding.chatDetailRecycler.getAdapter().getItemCount());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
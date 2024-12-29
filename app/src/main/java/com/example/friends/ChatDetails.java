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
import com.example.friends.databinding.ActivityChatDetailsBinding;
import com.example.friends.ModelClasses.Chats;
import com.example.friends.databinding.ActivityChatDetailsBinding;
import com.example.friends.utility.NetworkChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatDetails extends AppCompatActivity  {

    ActivityChatDetailsBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage storage;
    PdfDocument pdfDocument;
    Document document;
    ProgressDialog progressDialog;
    DatabaseReference ChatReference,UserReference,SenderRoom,ReceiverRoom;
    FirebaseUser firebaseUser;
    String sender,senderID,receiver,receiverID,receiverImg,date,time,number;
    String unread="NO";
    ArrayList<Chats> list;

    static String ONLINE="ONLINE",OFFLINE="OFFLINE";
    static int go_to_images=30,go_to_video=40;
    static String  msg_type="msg",img_type="img",vid_type="video";
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
        binding=ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        list=new ArrayList<>();
        getSupportActionBar().hide();

        progressDialog=new ProgressDialog(ChatDetails.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("");
        progressDialog.setMessage("Please wait...");

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        storage=FirebaseStorage.getInstance();
        ChatReference=firebaseDatabase.getReference().child("Chats");
        UserReference=firebaseDatabase.getReference().child("Users");

        senderID=firebaseAuth.getUid();
        receiverID=getIntent().getStringExtra("user_id");
        receiverImg=getIntent().getStringExtra("user_image");
        receiver=getIntent().getStringExtra("user_name");
        unread=getIntent().getStringExtra("read");
        number=getIntent().getStringExtra("number");

        SenderRoom=ChatReference.child(senderID+receiverID);
        ReceiverRoom=ChatReference.child(receiverID+senderID);

        createBackground();
        updateReadings();
        viewChats();

        try {
            binding.txtChatName.setText(receiver);
            Picasso.get().load(receiverImg).placeholder(R.drawable.batman64).into(binding.imgChat);
        }catch (Exception e){

        }

        binding.backChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatDetails.this,MainActivity.class));
            }
        });

        binding.addAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              sendAttachments().show();
            }
        });

        binding.sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeStamp();
                Chats chats=new Chats(binding.txtMsg.getText().toString(), time,date,senderID,receiverID,"false",msg_type);
                ReceiverRoom.push().setValue(chats).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Chats chats2=new Chats(binding.txtMsg.getText().toString(), time,date,senderID,receiverID,"true",msg_type);
                            SenderRoom.push().setValue(chats2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    binding.txtMsg.setText("");
                                    viewChats();
                                }
                            });
                        }
                        }
                });
            }
        });

        binding.menuChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(ChatDetails.this,v);
                popupMenu.inflate(R.menu.chatmenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.clearChats){
                            clearChats().show();
                        }else if(item.getItemId()==R.id.remove_friend){
                            removeFriend().show();
                        }else if(item.getItemId()==R.id.exportChats){
                            exportChats();
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });

        binding.videoChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatDetails.this, VideoCall.class)
                        .putExtra("second",receiverID)
                        .putExtra("user",firebaseAuth.getUid()));
            }
        });

        binding.callChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(!number.equals("NO")){
                        NormalCalls normalCalls=new NormalCalls(number);
                        startActivity(normalCalls.call());
                    }else{
                        Toast.makeText(ChatDetails.this,"Number is not assigned yet to profile",Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e){
                    Toast.makeText(ChatDetails.this,"Number is not assigned yet to profile",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void exportChats() {
        firebaseDatabase.getReference().child("Chats").child(senderID+receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{String path= String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                    File f=new File(path,receiver+".pdf");
                    if(f.setWritable(true)){
                        PdfWriter writer=new PdfWriter(f);
                        pdfDocument=new PdfDocument(writer);

                        document=new Document(pdfDocument);


                        document.add(new Paragraph("Chat Log - "+receiver).setBold().setUnderline().setTextAlignment(TextAlignment.CENTER));
                        for(DataSnapshot ds:snapshot.getChildren()){
                            Chats chats3=ds.getValue(Chats.class);
                            if(chats3.getSender().equals(firebaseAuth.getUid())){
                                document.add(new Paragraph("Me :"));
                            }else{
                                document.add(new Paragraph(receiver+" :"));
                            }

                            if(chats3.getType().equals(img_type)){
                                document.add(new Paragraph("IMAGE"));
                            }else if(chats3.getType().equals(vid_type)){
                                document.add(new Paragraph("VIDEO"));
                            }else{
                                Paragraph paragraph=new Paragraph();
                                Text text=new Text(chats3.getMsg());

                                paragraph.add(text);
                                document.add(paragraph.setTextAlignment(TextAlignment.JUSTIFIED));
                            }
                            document.add(new Paragraph(chats3.getDate()+"_"+chats3.getTime()));
                            document.add(new Paragraph("----------------------------"));
                        }
                        document.close();
                        Toast.makeText(ChatDetails.this,"Exported",Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e){
                    Toast.makeText(ChatDetails.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public byte[] getBytes(InputStream stream)throws IOException{
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        int size=1024;
        byte[] buffer=new byte[size];
        int len=0;
        while ((len=stream.read(buffer))!=-1){
            bos.write(buffer,0,len);
        }
        return bos.toByteArray();
    }

    public void timeStamp(){
        long t=new Date().getTime();
        SimpleDateFormat sdf=new SimpleDateFormat("h:m:s a");
        time=sdf.format(t);
        t=new Date().getTime();
        sdf=new SimpleDateFormat("yyyy/MM/dd");
        date=sdf.format(t);
    }
    private AlertDialog sendAttachments() {
        AlertDialog d=new AlertDialog.Builder(ChatDetails.this).setCancelable(false)
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
                .create();
        return d;
    }

    private void updateReadings() {
        if(unread.equals("YES")){
            unread="NO";
            HashMap<String,Object> map=new HashMap<>();
            map.put("seen","true");
            ChatReference.child(senderID+receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren()){
                        if(ds!=null){
                            Chats chats=ds.getValue(Chats.class);
                            if(chats.getSeen().equals("false")){
                                ChatReference.child(senderID+receiverID).child(ds.getKey()).updateChildren(map);
                            }
                        }
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
           });
        }
    }

    private void createBackground() {
        UserReference.child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.getKey().equals("background") ){
                        if(ds.getValue()!=null){
                            Picasso.get().load(ds.getValue().toString()).fit().placeholder(R.drawable.home).into(binding.chatBackImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public AlertDialog clearChats(){
        AlertDialog d=new AlertDialog.Builder(ChatDetails.this).setMessage("Do you want to delete all the chats ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SenderRoom.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ChatDetails.this,"Cleared",Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }
                            }
                        });
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
    public AlertDialog removeFriend(){
        AlertDialog d=new AlertDialog.Builder(ChatDetails.this).setTitle("Do you want to remove ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserReference.child(senderID).child("Friends").child(receiverID).removeValue();
                        UserReference.child(receiverID).child("Friends").child(senderID).removeValue();
                        dialog.cancel();
                        startActivity(new Intent(ChatDetails.this,MainActivity.class));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        return d;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            timeStamp();
            if(data.getData()!=null && requestCode==go_to_images) {
                uploadMediaAndSend(data.getData(),img_type,date,time);
            }else if(data.getData()!=null && requestCode==go_to_video){
                uploadMediaAndSend(data.getData(),vid_type,date,time);
            }
        }catch (Exception e){
            Toast.makeText(this, "Select Media File to send", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadMediaAndSend(Uri uri,String type,String date,String time){
        storage.getReference().child("Chats").child(senderID + receiverID).child(date + time).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if(type.equals(img_type))
                    progressDialog.setTitle("Uploading Image");
                else
                    progressDialog.setTitle("Uploading Video");
                progressDialog.show();
                storage.getReference().child("Chats").child(senderID + receiverID).child(date + time).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Chats chats = new Chats(uri.toString(), time, date, senderID, receiverID, "false", type);
                        ReceiverRoom.push().setValue(chats).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Chats chats2 = new Chats(uri.toString(), time, date, senderID, receiverID, "true", type);
                                    SenderRoom.push().setValue(chats2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            binding.txtMsg.setText("");
                                            progressDialog.cancel();
                                        }
                                    });

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
        chat_adapter adapter=new chat_adapter(list,ChatDetails.this);
        binding.chatDetailRecycler.setAdapter(adapter);

        LinearLayoutManager llm=new LinearLayoutManager(ChatDetails.this);
        binding.chatDetailRecycler.setLayoutManager(llm);

        firebaseDatabase.getReference().child("Chats").child(senderID+receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    Chats chats3=ds.getValue(Chats.class);
                    chats3.setMsgID(ds.getKey());
                    list.add(chats3);
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
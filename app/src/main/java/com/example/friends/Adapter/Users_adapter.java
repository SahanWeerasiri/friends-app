package com.example.friends.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friends.ChatDetails;
import com.example.friends.NormalCalls;
import com.example.friends.R;
import com.example.friends.ModelClasses.User;
import com.example.friends.VideoCall;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users_adapter extends RecyclerView.Adapter{
   ArrayList<User> list;
   FirebaseAuth firebaseAuth;
    static String FRIEND="YES",NOT_FRIEND="NO",VIDEO="VIDEO";
    FirebaseDatabase firebaseDatabase;
   Context context;
    String msg="Last Message",type;
    static String  msg_type="msg",img_type="img",vid_type="video",ONLINE="ONLINE",OFFLINE="OFFLINE";

    static int FRIEND_TYPE=1,ALL_TYPE=2,VIDEO_TYPE=3;

    String time="Never";

    public Users_adapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==FRIEND_TYPE){
            View view=LayoutInflater.from(context).inflate(R.layout.sample,parent,false);
            return new ViewHolder(view);
        }else if(viewType==ALL_TYPE){
            View view=LayoutInflater.from(context).inflate(R.layout.allusersseen,parent,false);
            return new AllViewHolder(view);
        }else{
            View view=LayoutInflater.from(context).inflate(R.layout.videouser,parent,false);
            return new VideoViewHolder(view);
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user=list.get(position);
        Uri uri=Uri.parse(user.getUser_image());

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();

        if(holder.getClass()==ViewHolder.class){
            ViewHolder viewHolder=(ViewHolder) holder;
            Picasso.get().load(uri).placeholder(R.drawable.batman64).into(viewHolder.circleImageView);
            viewHolder.txt_name.setText(user.getUser_name());
            try{
                if(user.getONLINE().equals(ONLINE)){
                    viewHolder.txt_online.setText(ONLINE);
                    viewHolder.txt_online.setTextColor((Color.GREEN));
                }else{
                    viewHolder.txt_online.setText(OFFLINE);
                    viewHolder.txt_online.setTextColor((Color.RED));
                }
            }catch (Exception e ){
            }
            firebaseDatabase.getReference().child("Chats").child(firebaseAuth.getUid()+user.getUser_id()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren()){
                        msg=ds.child("msg").getValue().toString();
                        type=ds.child("type").getValue().toString();
                        time=ds.child("time").getValue().toString();
                    }
                    viewHolder.txt_time.setText(time);
                    viewHolder.txt_msg.setText(msg);
                    try {
                        if(type.equals(msg_type)){
                            viewHolder.txt_msg.setText(msg);
                        }else if(type.equals(img_type)){
                            viewHolder.txt_msg.setText("IMAGE");
                        }else if(type.equals(vid_type)){
                            viewHolder.txt_msg.setText("VIDEO");
                        }
                    }catch (Exception e){
                        viewHolder.txt_msg.setText(msg);
                    }
                    time="Never";msg="Last Message";type="";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.itemView.findViewById(R.id.touch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent chats=new Intent(context, ChatDetails.class);
                    chats.putExtra("user_id",user.getUser_id());
                    chats.putExtra("user_name",user.getUser_name());
                    chats.putExtra("user_image",user.getUser_image());
                    chats.putExtra("read","YES");
                    try{
                        chats.putExtra("number",user.getNumber());
                    }catch (Exception e){
                        chats.putExtra("number","NO");
                    }

                    context.startActivity(chats);
                }
            });

            holder.itemView.findViewById(R.id.sample_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog d=new Dialog(v.getContext());
                    d.setContentView(R.layout.userpopup);
                    d.setCancelable(true);
                    TextView name=d.findViewById(R.id.pop_name);
                    TextView num=d.findViewById(R.id.pop_number);
                    ImageView img=d.findViewById(R.id.pop_image);
                    ImageView call=d.findViewById(R.id.pop_call);
                    ImageView video=d.findViewById(R.id.pop_video);
                    Picasso.get().load(user.getUser_image()).placeholder(R.drawable.batman64).into(img);
                    name.setText(user.getUser_name());
                    num.setText(user.getAbout());
                    video.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            context.startActivity(new Intent(context, VideoCall.class)
                                    .putExtra("second",user.getUser_id())
                                    .putExtra("user",firebaseAuth.getUid()));
                        }
                    });
                    call.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try{
                                NormalCalls normalCalls=new NormalCalls(user.getNumber());
                                context.startActivity(normalCalls.call());
                            }catch (Exception e){
                                Toast.makeText(context,"Number is not assigned yet to profile",Toast.LENGTH_SHORT).show();
                            }


                        }
                    });
                    d.show();
                }
            });
        }else if(holder.getClass()==AllViewHolder.class){
            AllViewHolder viewHolder=(AllViewHolder) holder;
            Picasso.get().load(uri).placeholder(R.drawable.batman64).into(viewHolder.circleImageView);
            viewHolder.txt_name.setText(user.getUser_name());
            viewHolder.btn_add.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid()).child("Friends").child(user.getUser_id()).setValue("YES");
                    firebaseDatabase.getReference().child("Users").child(user.getUser_id()).child("Friends").child(firebaseAuth.getUid()).setValue("YES");
                    viewHolder.btn_add.setEnabled(false);
                    viewHolder.btn_add.setBackgroundResource(R.color.grey_light);
                }
            });
        }else{
            VideoViewHolder viewHolder=(VideoViewHolder) holder;
            Picasso.get().load(uri).placeholder(R.drawable.batman64).into(viewHolder.circleImageView);
            viewHolder.txt_name.setText(user.getUser_name());
            viewHolder.txt_state.setText("ONLINE");
            viewHolder.answer.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, VideoCall.class)
                            .putExtra("second",user.getUser_id())
                            .putExtra("user",firebaseAuth.getUid()));
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        User u=list.get(position);
        if(u.getType().equals(FRIEND)){
            return FRIEND_TYPE;
        }else if(u.getType().equals(NOT_FRIEND)){
            return ALL_TYPE;
        }else{
            return VIDEO_TYPE;
        }
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView txt_name,txt_msg,txt_time,txt_online;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView=(CircleImageView) itemView.findViewById(R.id.sample_image);
            txt_name=(TextView) itemView.findViewById(R.id.sample_name);
            txt_msg=(TextView) itemView.findViewById(R.id.sample_last_msg);
            txt_time=(TextView) itemView.findViewById(R.id.sample_last_time);
            txt_online=(TextView) itemView.findViewById(R.id.sample_online);
        }
    }
    public  class AllViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView txt_name;
        Button btn_add;
        public AllViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView=(CircleImageView) itemView.findViewById(R.id.sample_image);
            txt_name=(TextView) itemView.findViewById(R.id.sample_name);
            btn_add=(Button) itemView.findViewById(R.id.add_friend);
        }
    }

    public  class VideoViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView txt_name,txt_state;
        Button answer;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView=(CircleImageView) itemView.findViewById(R.id.video_image);
            txt_name=(TextView) itemView.findViewById(R.id.video_user_name);
            txt_state=(TextView) itemView.findViewById(R.id.video_user_state);
            answer=(Button) itemView.findViewById(R.id.video_call_answer);
        }
    }

    }


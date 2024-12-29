package com.example.friends.Adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friends.ModelClasses.Chats;
import com.example.friends.R;
import com.example.friends.opennings.ImageOpen;
import com.example.friends.opennings.VideoOpen;
import com.example.friends.ModelClasses.Chats;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class chat_adapter extends RecyclerView.Adapter{
    static String  msg_type="msg",img_type="img",video_type="video";

    ArrayList<Chats> list;
    Context context;

    static SenderVIDEOViewHolder p_svh,n_svh;
    static ReceiverVIDEOViewHolder p_rvh,n_rvh;

    int SENDER_MSG=1;
    int RECEIVER_MSG=2;
    int SENDER_IMG=4;
    int RECEIVER_IMG=5;
    int SENDER_VID=6;
    int RECEIVER_VID=7;


    String current_date,user_id;

    public chat_adapter(ArrayList<Chats> list, Context context) {
        this.list = list;
        this.context = context;
        user_id=FirebaseAuth.getInstance().getUid();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==SENDER_MSG){
            View view=LayoutInflater.from(context).inflate(R.layout.send_bubble,parent,false);
            return new SenderViewHolder(view);
        }else if(viewType==RECEIVER_MSG){
            View view=LayoutInflater.from(context).inflate(R.layout.receive_buble,parent,false);
            return new ReceiverViewHolder(view);
        }else if(viewType==RECEIVER_IMG){
            View view=LayoutInflater.from(context).inflate(R.layout.receive_imag_buble,parent,false);
            return new ReceiverIMAGEViewHolder(view);
        }else if(viewType==RECEIVER_VID){
            View view=LayoutInflater.from(context).inflate(R.layout.receive_vid_buble,parent,false);
            return new ReceiverVIDEOViewHolder(view);
        }else if(viewType==SENDER_VID){
            View view=LayoutInflater.from(context).inflate(R.layout.send_vid_bubble,parent,false);
            return new SenderVIDEOViewHolder(view);
        }else{
            View view=LayoutInflater.from(context).inflate(R.layout.send_imag_bubble,parent,false);
            return new SenderIMAGEViewHolder(view);
        }

    }


    @Override
    public int getItemViewType(int position) {
        Chats chats=list.get(position);
        String sender=chats.getSender();
        String type=chats.getType();
        if(sender.equals(user_id) ){
            if( type.equals(msg_type)){
                return SENDER_MSG;
            }else if(type.equals(video_type)){
                return SENDER_VID;
            }else if(type.equals(img_type)){
                return SENDER_IMG;
            }else{
                return SENDER_MSG;
            }

        }else {
            if( type.equals(msg_type)){
                return RECEIVER_MSG;
            }else if(type.equals(video_type)){
                return RECEIVER_VID;
            }else if(type.equals(img_type)){
                return RECEIVER_IMG;
            }else{
                return RECEIVER_MSG;
            }
        }
    }

    public AlertDialog deleteDialogBuilder(Chats chats){
        DatabaseReference groupChat=FirebaseDatabase.getInstance().getReference().child("Group");
        DatabaseReference normalChat=FirebaseDatabase.getInstance().getReference().child("Chats");
        AlertDialog alertDialog=new AlertDialog.Builder(context).setMessage("Do you want to delete ?")
                .setTitle("Delete")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        normalChat.child(user_id + chats.getReceiver())
                        .child(chats.getMsgID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    dialog.cancel();
                                } else {
                                    Toast.makeText(context, "Not Deleted. Check your Connection", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        groupChat.child(chats.getMsgID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    dialog.cancel();
                                } else {
                                    Toast.makeText(context, "Not Deleted. Check your Connection", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }


                }).setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setCancelable(false).create();
        return alertDialog;
    }
    public void createAndStartIntents(Chats chats,Class destinationClass){
        Intent intent=new Intent(context, destinationClass);
        intent.putExtra("uri",chats.getMsg());
        intent.putExtra("class",chats.getClass());
        context.startActivity(intent);
    }
    public String receiverUserNamePrinter(Chats chats){
        final String[] receiver = {""};
        DatabaseReference userReference=FirebaseDatabase.getInstance().getReference().child("Users");
        if(chats.getGroup()!=null){
            userReference.child(chats.getSender()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren()){
                        if(ds.getKey().equals("user_name")){
                            receiver[0] =ds.getValue().toString();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        return receiver[0];
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Chats chats=list.get(position);

        if(holder.getClass()==SenderViewHolder.class){
            SenderViewHolder viewHolder=((SenderViewHolder)holder);

            viewHolder.txt_date.setText(chats.getDate());
            viewHolder.txt_msg.setText(list.get(position).getMsg());
            viewHolder.txt_time.setText(list.get(position).getTime());

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String use=chats.getSender();
                    if(user_id.equals(use)) {
                        deleteDialogBuilder(chats).show();
                    }
                    return true;
                }
            });
        }else if(holder.getClass()==ReceiverViewHolder.class){
            ReceiverViewHolder viewHolder=(ReceiverViewHolder)holder;
            viewHolder.txt_date.setText(chats.getDate());
            viewHolder.txt_msg.setText(chats.getMsg());
            viewHolder.txt_time.setText(chats.getTime());
            viewHolder.txt_sender.setText(receiverUserNamePrinter(chats));
        }else if(holder.getClass()==SenderIMAGEViewHolder.class){
            SenderIMAGEViewHolder viewHolder=((SenderIMAGEViewHolder)holder);

            viewHolder.txt_date.setText(chats.getDate());
            Picasso.get().load(chats.getMsg()).placeholder(R.drawable.home).into(viewHolder.img_view);
            viewHolder.txt_time.setText(list.get(position).getTime());

            viewHolder.img_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createAndStartIntents(chats,ImageOpen.class);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String use=chats.getSender();
                    if(user_id.equals(use)) {
                        deleteDialogBuilder(chats).show();
                    }
                    return true;
                }

            });
        }else if(holder.getClass()==ReceiverIMAGEViewHolder.class){
            ReceiverIMAGEViewHolder viewHolder=((ReceiverIMAGEViewHolder)holder);

            viewHolder.txt_date.setText(current_date);
            viewHolder.txt_time.setText(chats.getTime());
            viewHolder.txt_sender.setText(receiverUserNamePrinter(chats));

            Picasso.get().load(chats.getMsg()).placeholder(R.drawable.home).into(viewHolder.img_view);

            viewHolder.img_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createAndStartIntents(chats,ImageOpen.class);
                }
            });
        }else if(holder.getClass()==SenderVIDEOViewHolder.class){
            SenderVIDEOViewHolder viewHolder=((SenderVIDEOViewHolder)holder);

            viewHolder.txt_date.setText(chats.getDate());
            viewHolder.txt_time.setText(chats.getTime());
            VideoView videoView=viewHolder.vid_view;

            playVideo(videoView,chats);

            viewHolder.vid_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createAndStartIntents(chats,VideoOpen.class);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteDialogBuilder(chats);
                    return true;

                }

            });

        }else if(holder.getClass()==ReceiverVIDEOViewHolder.class){
            ReceiverVIDEOViewHolder viewHolder=((ReceiverVIDEOViewHolder)holder);

            viewHolder.txt_date.setText(chats.getDate());
            VideoView videoView=viewHolder.vid_view;
            viewHolder.txt_time.setText(chats.getTime());
            viewHolder.txt_sender.setText(receiverUserNamePrinter(chats));

            playVideo(videoView,chats);

            viewHolder.vid_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createAndStartIntents(chats,VideoOpen.class);
                }
            });

        }
    }

    public void playVideo(VideoView videoView,Chats chats){
        MediaController mc=new MediaController(context);
        mc.setAnchorView(videoView);
        mc.setMediaPlayer(videoView);
        mc.hide();
        videoView.setMediaController(mc);
        videoView.setVideoURI(Uri.parse(chats.getMsg()));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                try{
                    mp.setVolume(0f,0f);
                    mp.setLooping(true);
                    mp.start();
                }catch (Exception e){
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView txt_time,txt_msg,txt_sender,txt_date;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_msg=(TextView) itemView.findViewById(R.id.receive_message);
            txt_time=(TextView) itemView.findViewById(R.id.receive_time);
            txt_sender=(TextView) itemView.findViewById(R.id.sender);
            txt_date=(TextView) itemView.findViewById(R.id.receive_date);

        }
    }
    public  class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView txt_time,txt_msg,txt_date;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_msg=(TextView) itemView.findViewById(R.id.send_message);
            txt_time=(TextView) itemView.findViewById(R.id.send_time);
            txt_date=(TextView) itemView.findViewById(R.id.send_date);
        }
    }
    public  class ReceiverIMAGEViewHolder extends RecyclerView.ViewHolder {
        TextView txt_time,txt_sender,txt_date;
        ImageView img_view;
        public ReceiverIMAGEViewHolder(@NonNull View itemView) {
            super(itemView);
            img_view=(ImageView) itemView.findViewById(R.id.receive_image);
            txt_time=(TextView) itemView.findViewById(R.id.receive_time);
            txt_sender=(TextView) itemView.findViewById(R.id.sender);
            txt_date=(TextView) itemView.findViewById(R.id.receive_date);

        }
    }
    public  class SenderIMAGEViewHolder extends RecyclerView.ViewHolder {
        TextView txt_time,txt_date;
        ImageView img_view;
        public SenderIMAGEViewHolder(@NonNull View itemView) {
            super(itemView);
            img_view=(ImageView) itemView.findViewById(R.id.send_image);
            txt_time=(TextView) itemView.findViewById(R.id.send_time);
            txt_date=(TextView) itemView.findViewById(R.id.send_date);
        }
    }
    public  class ReceiverVIDEOViewHolder extends RecyclerView.ViewHolder {
        TextView txt_time,txt_sender,txt_date;
        VideoView vid_view;
        public ReceiverVIDEOViewHolder(@NonNull View itemView) {
            super(itemView);
            vid_view=(VideoView) itemView.findViewById(R.id.receive_video);
            txt_time=(TextView) itemView.findViewById(R.id.receive_time);
            txt_sender=(TextView) itemView.findViewById(R.id.sender);
            txt_date=(TextView) itemView.findViewById(R.id.receive_date);


        }
    }
    public  class SenderVIDEOViewHolder extends RecyclerView.ViewHolder {
        TextView txt_time,txt_date;
        VideoView vid_view;
        public SenderVIDEOViewHolder(@NonNull View itemView) {
            super(itemView);
            vid_view=(VideoView) itemView.findViewById(R.id.send_video);
            txt_time=(TextView) itemView.findViewById(R.id.send_time);
            txt_date=(TextView) itemView.findViewById(R.id.send_date);
        }
    }

}


package com.example.friends.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friends.Adapter.Users_adapter;
import com.example.friends.AllUsers;
import com.example.friends.ModelClasses.User;
import com.example.friends.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class chatFragment extends Fragment {
    RecyclerView recyclerView;
    FirebaseDatabase firebaseDatabase;
    ArrayList<User> list=new ArrayList<>();
    static String FRIEND="YES",NOT_FRIEND="NO";
    ArrayList<String> friends=new ArrayList<>();
    FirebaseAuth firebaseAuth;
    String firebaseUser;
    FragmentChatBinding fragmentChatBinding;
    String msg="Last Message",time="Never";

    public chatFragment(ArrayList<String >friends){
        this.friends=friends;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser().getUid();


        fragmentChatBinding=FragmentChatBinding.inflate(inflater,container,false);
        list=new ArrayList<>();
        Users_adapter users_adapter=new Users_adapter(list,getContext());
        fragmentChatBinding.chatRecycle.setAdapter(users_adapter);

        LinearLayoutManager llm=new LinearLayoutManager(getContext());
        fragmentChatBinding.chatRecycle.setLayoutManager(llm);

        fragmentChatBinding.sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SeeUsers=new Intent(getContext(), AllUsers.class);
                SeeUsers.putExtra("Friends",friends);
                startActivity(SeeUsers);
            }
        });




        firebaseDatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(!firebaseUser.equals(ds.getKey()) && friends!=null){
                        for(int i=0;i<friends.size();i++){
                            if(ds.getKey().toString().equals(friends.get(i))){
                                User user=ds.getValue(User.class);
                                user.setUser_id(ds.getKey());
                                user.setType(FRIEND);
                                list.add(user);
                            }

                        }

                    }
                }
                users_adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return fragmentChatBinding.getRoot();
    }
}
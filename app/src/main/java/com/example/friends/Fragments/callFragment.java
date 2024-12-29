package com.example.friends.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friends.Adapter.Users_adapter;
import com.example.friends.ModelClasses.User;
import com.example.friends.databinding.FragmentCallBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class callFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseDatabase firebaseDatabase;
    ArrayList<User> list=new ArrayList<>();
    ArrayList<String> online=new ArrayList<>();
    FirebaseAuth firebaseAuth;
    String firebaseUser;
    FragmentCallBinding binding;

    static String FRIEND="YES",NOT_FRIEND="NO",VIDEO_TYPE="VIDEO";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser().getUid();


        binding=FragmentCallBinding.inflate(inflater,container,false);
        list=new ArrayList<>();
        Users_adapter users_adapter=new Users_adapter(list,getContext());
        binding.videoRecycle.setAdapter(users_adapter);

        LinearLayoutManager llm=new LinearLayoutManager(getContext());
        binding.videoRecycle.setLayoutManager(llm);

        firebaseDatabase.getReference().child("VideoCalls").child(firebaseUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                online.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.getValue().toString().equals("ONLINE")){
                        online.add(ds.getKey());

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
                list.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    for(int i=0;i<online.size();i++){
                            if(ds.getKey().toString().equals(online.get(i))){
                                User user=ds.getValue(User.class);
                                user.setUser_id(ds.getKey());
                                user.setType(VIDEO_TYPE);
                                list.add(user);
                            }

                        }

                    }
                users_adapter.notifyDataSetChanged();
                }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return binding.getRoot();
    }
}
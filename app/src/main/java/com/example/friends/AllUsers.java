package com.example.friends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friends.Adapter.Users_adapter;
import com.example.friends.databinding.ActivityAllUsersBinding;
import com.example.friends.ModelClasses.User;
import com.example.friends.databinding.ActivityAllUsersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllUsers extends AppCompatActivity {
    RecyclerView recyclerView;
    FirebaseDatabase firebaseDatabase;
    ArrayList<User> list;
    ArrayList<String> friends;
    FirebaseAuth firebaseAuth;
    String firebaseUser;
    static String FRIEND="YES",NOT_FRIEND="NO";
    ActivityAllUsersBinding allUsersBinding;
    String msg="Last Message",time="Never";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allUsersBinding= ActivityAllUsersBinding.inflate(getLayoutInflater());
        setContentView(allUsersBinding.getRoot());
        getSupportActionBar().setTitle("Users");
        friends= (ArrayList<String>) getIntent().getExtras().get("Friends");

        firebaseDatabase= FirebaseDatabase.getInstance();
        firebaseAuth= FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser().getUid();


        list=new ArrayList<>();
        Users_adapter users_adapter=new Users_adapter(list,AllUsers.this);
        allUsersBinding.friendsSeeRecycle.setAdapter(users_adapter);

        LinearLayoutManager llm=new LinearLayoutManager(AllUsers.this);
        allUsersBinding.friendsSeeRecycle.setLayoutManager(llm);

        firebaseDatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                boolean isFriend=false;
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(!firebaseUser.equals(ds.getKey())){
                        for(int i=0;i<friends.size();i++){
                            if(friends.get(i).equals(ds.getKey())){
                                isFriend=true;
                                break;
                            }
                        }
                        if(!isFriend){
                            User user=ds.getValue(User.class);
                            user.setUser_id(ds.getKey());
                            user.setType(NOT_FRIEND);
                            list.add(user);
                        }
                        isFriend=false;

                    }
                }
                users_adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
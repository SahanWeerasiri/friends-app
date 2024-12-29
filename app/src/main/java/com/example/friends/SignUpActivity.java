package com.example.friends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.ModelClasses.User;
import com.example.friends.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();

        progressDialog=new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle("Creating an Account");
        progressDialog.setMessage("We are creating your account. Please Wait...");

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.txtEmailSignUp.getText().toString().isEmpty() || binding.txtPasswordSignUp.getText().toString().isEmpty() || binding.txtUserNameSignUp.getText().toString().isEmpty()){
                    Toast.makeText(SignUpActivity.this, "Enter Credentials", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.show();
                    firebaseAuth.createUserWithEmailAndPassword(binding.txtEmailSignUp.getText().toString(),binding.txtPasswordSignUp.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.cancel();
                                    if(task.isSuccessful()){
                                        Toast.makeText(SignUpActivity.this,"Sign Up Successful",Toast.LENGTH_SHORT).show();
                                        User user=new User(binding.txtUserNameSignUp.getText().toString(),binding.txtEmailSignUp.getText().toString(),binding.txtPasswordSignUp.getText().toString());
                                        String id=task.getResult().getUser().getUid();
                                        firebaseDatabase.getReference().child("Users").child(id).setValue(user);
                                    }else{
                                        Toast.makeText(SignUpActivity.this,"This Email is already reserved. Try with Another",Toast.LENGTH_SHORT).show();
                                    }
                                    binding.txtUserNameSignUp.setText("");
                                    binding.txtPasswordSignUp.setText("");
                                    binding.txtEmailSignUp.setText("");
                                }
                            });

                }
            }
        });
        binding.txtGotoSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
            }
        });
    }
}
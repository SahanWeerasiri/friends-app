package com.example.friends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.ModelClasses.User;
import com.example.friends.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient= GoogleSignIn.getClient(SignInActivity.this,gso);



        progressDialog=new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Validating your account. Please Wait...");

        if(firebaseAuth.getCurrentUser()!=null){
            Intent home=new Intent(SignInActivity.this,MainActivity.class);
            startActivity(home);
        }



        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.txtPasswordSignIn.getText().toString().isEmpty() || binding.txtEmailSignIn.getText().toString().isEmpty()){
                    Toast.makeText(SignInActivity.this, "Enter Credentials", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.show();
                    firebaseAuth.signInWithEmailAndPassword(binding.txtEmailSignIn.getText().toString(),binding.txtPasswordSignIn.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.cancel();
                                    if(task.isSuccessful()){
                                        Toast.makeText(SignInActivity.this,"Sign in Successful",Toast.LENGTH_SHORT).show();
                                        String id=task.getResult().getUser().getUid();
                                        Intent home=new Intent(SignInActivity.this,MainActivity.class);
                                        startActivity(home);
                                    }else{
                                        Toast.makeText(SignInActivity.this,"Email or password are incorrect. Try Again",Toast.LENGTH_SHORT).show();
                                    }
                                    binding.txtPasswordSignIn.setText("");
                                    binding.txtEmailSignIn.setText("");
                                }
                            });

                }
            }
        });
        binding.txtGotoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,SignUpActivity.class));
            }
        });
        binding.btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn();
            }
        });

    }
    private void signIn(){
        Intent signInIntent=googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,REQ_ONE_TAP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressDialog.show();

        switch (requestCode) {
            case REQ_ONE_TAP:
                Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account=task.getResult(ApiException.class);
                    firebaseAuthentication(account.getIdToken());
               } catch (ApiException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;

    }
}
private void firebaseAuthentication(String token){
        AuthCredential authCredential=GoogleAuthProvider.getCredential(token,null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Toast.makeText(SignInActivity.this, "Sign in Successful", Toast.LENGTH_SHORT).show();
                    FirebaseUser user=firebaseAuth.getCurrentUser();
                    User users=new User();
                    users.setUser_id(user.getUid());
                    users.setUser_name(user.getDisplayName());
                    users.setUser_image(user.getPhotoUrl().toString());
                    firebaseDatabase.getReference().child("Users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isExisted=false;
                            for(DataSnapshot ds:snapshot.getChildren()){
                                if(ds.getKey().equals(user.getUid())){
                                    isExisted=true;
                                    break;
                                }else{
                                    isExisted=false;
                                }

                            }
                            if(!isExisted){
                                firebaseDatabase.getReference().child("Users").child(users.getUser_id()).setValue(users);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    progressDialog.cancel();
                    Intent home=new Intent(SignInActivity.this,MainActivity.class);

                    startActivity(home);
                }
            }
        });
}
}
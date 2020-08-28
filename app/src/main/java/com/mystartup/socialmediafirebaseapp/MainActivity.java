package com.mystartup.socialmediafirebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private EditText userName,emailId,password;
    private Button signUp,login ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = findViewById(R.id.edit_user_name);
        emailId = findViewById(R.id.edit_email_id);
        password = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        signUp = findViewById(R.id.signUp);
        login = findViewById(R.id.login);
        signUp.setOnClickListener(this);
        login.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.signUp:
                mAuth.createUserWithEmailAndPassword(emailId.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this,"Signed up",Toast.LENGTH_LONG).show();
                                    FirebaseDatabase.getInstance().getReference().child("my_users").child(task.getResult().getUser().getUid()).child("User_Name").setValue(userName.getText().toString());
                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(userName.getText().toString())
                                            .build();
                                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(userProfileChangeRequest)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                               if(task.isSuccessful()){
                                                   Toast.makeText(MainActivity.this,"Huraay",Toast.LENGTH_LONG).show();
                                               }
                                                }
                                            });

                                } else {
                                    Toast.makeText(MainActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    } }
                        });
                break;
            case R.id.login:
                mAuth.signInWithEmailAndPassword(emailId.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    switchToSocialMediaActivity();
                                    Toast.makeText(MainActivity.this,"Logged in",Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(MainActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    }
                            }
                        });
                break;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            switchToSocialMediaActivity();
        }
    }

    private void switchToSocialMediaActivity(){
        Intent intent  = new Intent(MainActivity.this,SocialMediaActivity.class);
        startActivity(intent);
    }


}
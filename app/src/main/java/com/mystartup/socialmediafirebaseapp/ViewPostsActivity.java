package com.mystartup.socialmediafirebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {

    private ListView postListView;
    private TextView postDescription;
    private ImageView postsImage;
    private FirebaseAuth mAuth;
    private ArrayList<String> mArrayList;
    private ArrayAdapter mArrayAdapter;
    private ArrayList<DataSnapshot> dataSnap;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);
        postListView = findViewById(R.id.postsListView);
        postDescription = findViewById(R.id.postDescription);
        postDescription.setText("Click on the users to see post");
        postsImage = findViewById(R.id.imagePosts);
        mAuth = FirebaseAuth.getInstance();
        dataSnap =new ArrayList<>();
        postListView.setOnItemClickListener(this);
        postListView.setOnItemLongClickListener(this);
        mArrayList = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter(ViewPostsActivity.this,android.R.layout.simple_list_item_1,mArrayList);
        postListView.setAdapter(mArrayAdapter);
        FirebaseDatabase.getInstance().getReference().child("my_users").child(mAuth.getCurrentUser().getUid()).child("Received posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                dataSnap.add(snapshot);
                mArrayList.add(snapshot.child("fromWhom").getValue()+"");
                mArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                int  i =0;
                for(DataSnapshot snapshot1:dataSnap){
                    if(snapshot.getKey()==snapshot1.getKey()){
                        dataSnap.remove(i);
                        mArrayList.remove(i);
                    }
                    i++;
                    mArrayAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        postDescription.setText(dataSnap.get(i).child("Description").getValue().toString());
        url =(String) dataSnap.get(i).child("imageLink").getValue();
        Picasso.get().load(url).into(postsImage);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        FirebaseStorage.getInstance().getReference().child("My_Images").child(dataSnap.get(i).child("imageIdentifier").getValue().toString()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    postsImage.setImageResource(R.drawable.postimage);
                    postDescription.setText("Click on the user to see post");
                    Toast.makeText(ViewPostsActivity.this,"Scuccessfully deleted from storage",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(ViewPostsActivity.this,"Unsuccessful",Toast.LENGTH_LONG).show();
                }
            }
        });
        FirebaseDatabase.getInstance().getReference().child("my_users").child(mAuth.getCurrentUser().getUid()).child("Received posts").child(dataSnap.get(i).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ViewPostsActivity.this,"Successfully removed from database",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(ViewPostsActivity.this,"Unsuccessful in removing database",Toast.LENGTH_LONG).show();

                }
            }
        });

        return true;
    }
}
package com.mystartup.socialmediafirebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
public class SocialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mFirebaseAuth;
    private Button createImagePost;
    private ImageView loadImageView;
    private EditText description;
    private ListView listView;
    private ArrayList<String>mArrayList;
    private ArrayAdapter mArrayAdapter;
    private Bitmap mBitmap;
    private String imageIdentifier;
    private ArrayList<String> uid;
    private String imageLink;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);
        mFirebaseAuth = FirebaseAuth.getInstance();
        createImagePost = findViewById(R.id.postImage);
        loadImageView = findViewById(R.id.loadImage);
        description = findViewById(R.id.description);
        listView = findViewById(R.id.list_view);
        mArrayList = new ArrayList<>();
        uid = new ArrayList<>();
        mArrayAdapter = new ArrayAdapter(SocialMediaActivity.this,android.R.layout.simple_list_item_1,mArrayList);
        listView.setAdapter(mArrayAdapter);
        loadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadImage();
                 }
        });
        createImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImageToServer();
            }
        });
        listView.setOnItemClickListener(this);
        FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String users = (String)snapshot.child("User_Name").getValue();
                mArrayList.add(users);
                mArrayAdapter.notifyDataSetChanged();
                uid.add(snapshot.getKey());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                mFirebaseAuth.signOut();
                break;
            case R.id.viewPost:
                Intent intent = new Intent(SocialMediaActivity.this,ViewPostsActivity.class);
                startActivity(intent);
                break;
        }

                return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2000 && resultCode== RESULT_OK){
            if(data!=null) {
                Uri uri = data.getData();
                if(Build.VERSION.SDK_INT>28) {
                    try {
                        mBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri), new ImageDecoder.OnHeaderDecodedListener() {
                            @Override
                            public void onHeaderDecoded(@NonNull ImageDecoder imageDecoder, @NonNull ImageDecoder.ImageInfo imageInfo, @NonNull ImageDecoder.Source source) {
                            }});
                        loadImageView.setImageBitmap(mBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        loadImageView.setImageBitmap(mBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadImage();
    }

    private void uploadImageToServer() {
        if (mBitmap != null) {
            loadImageView.setDrawingCacheEnabled(true);
            loadImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) loadImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            imageIdentifier = UUID.randomUUID() + ".jpeg";

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("My_Images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(SocialMediaActivity.this,"Uploaded",Toast.LENGTH_LONG).show();

                 taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                     @Override
                     public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()) {
                            imageLink = task.getResult().toString();
                        }
                        }
                 });

                    }
            });
        }

    }

    private void loadImage(){
        if (Build.VERSION.SDK_INT > 23) {
            if (ContextCompat.checkSelfPermission(SocialMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2000);
            }
        }
        else {
            Intent intent  = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2000);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HashMap<String,String>hashMap =  new HashMap<>();
        hashMap.put("fromWhom",mFirebaseAuth.getCurrentUser().getDisplayName());
        hashMap.put("Description",description.getText().toString());
        hashMap.put("imageIdentifier",imageIdentifier);
        hashMap.put("imageLink",imageLink);
        FirebaseDatabase.getInstance().getReference().child("my_users").child(uid.get(i)).child("Received posts").push().setValue(hashMap);


    }
}

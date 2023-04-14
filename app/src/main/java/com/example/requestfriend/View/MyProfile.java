package com.example.requestfriend.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.requestfriend.Models.Users;
import com.example.requestfriend.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfile extends AppCompatActivity {
    private static final int MY_REQUEST_CODE = 77;
    private CircleImageView civMyAvatar;
    private ImageButton btnUpdateAvatar;
    private TextView tvMyUserName, tvMyEmail;
    private Toolbar toolbarMyProfile;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference;
    StorageReference mStorageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        setControl();
        setEvent();
    }

    private void setControl() {
        civMyAvatar = findViewById(R.id.civMyAvatar);
        btnUpdateAvatar = findViewById(R.id.btnUpdateAvatar);
        tvMyUserName = findViewById(R.id.tvMyUserName);
        tvMyEmail = findViewById(R.id.tvMyEmail);
        toolbarMyProfile = findViewById(R.id.toolbar_Myprofile);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
//        mStorageReference = FirebaseStorage.getInstance().getReference().child("profilePic").child(mAuth.getUid());
    }

    private void setEvent() {
        getMyProfile();
        actionToolbar();
        btnUpdateAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    private void actionToolbar() {
        setSupportActionBar(toolbarMyProfile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Hồ sơ cá nhân");
        toolbarMyProfile.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    private void getMyProfile() {
        mUserReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Users users = snapshot.getValue(Users.class);
                    if (users != null) {
                        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.default_avatar).into(civMyAvatar);
                        tvMyUserName.setText(users.getUserName());
                        tvMyEmail.setText(users.getEmail());
                    } else {
                        Toast.makeText(MyProfile.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                    }

                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, MY_REQUEST_CODE);
    }
    //Xử lý kết quả trả về từ hành động startActivityForResult

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.getData() != null) {
                Uri uri = data.getData();
                civMyAvatar.setImageURI(uri);
                final StorageReference mStorageReference = FirebaseStorage.getInstance().getReference().child("profilePic").child(mAuth.getUid());
                mStorageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mUserReference.child(mAuth.getUid()).child("profilePic").setValue(uri.toString());
                            }
                        });
                    }
                });
            }
        }
    }
}
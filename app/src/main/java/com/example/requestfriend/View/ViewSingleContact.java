package com.example.requestfriend.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.requestfriend.Models.Users;
import com.example.requestfriend.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewSingleContact extends AppCompatActivity {
    Toolbar toolbar_singleContact;
    CircleImageView civAvatarSingleContact;
    TextView tvUserNameSingleContact, tvEmailSingleContact;
    Button btnSendFriendRequest, btnCancelSendFriendRequest;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference, mFriendReference, mRequestReference;
    String userID, userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_contact);
        setControl();
        setEvent();
    }

    private void setControl() {
        toolbar_singleContact = findViewById(R.id.toolbar_singleContact);
        civAvatarSingleContact = findViewById(R.id.civAvatarSingleContact);
        tvUserNameSingleContact = findViewById(R.id.tvUserNameSingleContact);
        tvEmailSingleContact = findViewById(R.id.tvEmailSingleContact);
        btnSendFriendRequest = findViewById(R.id.btnSendFriendRequest);
        btnCancelSendFriendRequest = findViewById(R.id.btnCancelSendFriendRequest);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        mRequestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        userID = getIntent().getStringExtra("userID");

    }

    private void setEvent() {
        setActionToolBar();
        loadInformationUser();
    }

    private void setActionToolBar() {
        setSupportActionBar(toolbar_singleContact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Hồ sơ người dùng");
        toolbar_singleContact.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadInformationUser() {
        mUserReference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.default_avatar);
                    userName = user.getUserName();
                    tvUserNameSingleContact.setText(user.getUserName());
                    tvEmailSingleContact.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
package com.example.requestfriend.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyboardShortcutGroup;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.requestfriend.Models.Users;
import com.example.requestfriend.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewSingleContact extends AppCompatActivity {
    Toolbar toolbar_singleContact;
    CircleImageView civAvatarSingleContact;
    TextView tvUserNameSingleContact, tvEmailSingleContact;
    Button btnSendFriendRequest, btnCancelSendFriendRequest;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference, mFriendReference, mRequestReference;
    StorageReference mStorageReference;
    String userID, userName, userAvatarURL, userEmail, currentState = "nothing_happen";
    String myID, myUserName, myEmail, myAvatarURL;

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
        mStorageReference = FirebaseStorage.getInstance().getReference().child("profilePic").child("default_avatar.png");
        userID = getIntent().getStringExtra("userID");

    }

    private void setEvent() {
        setActionToolBar();
        loadInformationUser();
        loadMyProfile();
        checkRequestFriendExistance(userID);
        btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendActionFriendRequest(userID);
            }
        });

        btnCancelSendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDeclineFriendRequest(userID);
            }
        });
    }


    private void setActionToolBar() {
        setSupportActionBar(toolbar_singleContact);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
                    if (user != null) {
                        userAvatarURL = user.getProfilePic();
                        userName = user.getUserName();
                        userEmail = user.getEmail();
                        Picasso.get().load(userAvatarURL).placeholder(R.drawable.default_avatar);
                        tvUserNameSingleContact.setText(userName);
                        tvEmailSingleContact.setText(userEmail);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyProfile() {
        mUserReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    if (users != null) {
                        myID = mUser.getUid();
                        if (users.getProfilePic() != null) {
                            myAvatarURL = users.getProfilePic().trim();
                        } else {
                            mStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    myAvatarURL = uri.toString();
                                }
                            });
                        }
                        myUserName = users.getUserName().trim();
                        myEmail = users.getEmail().trim();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendActionFriendRequest(String userID) {
        if (currentState.equals("nothing_happen")) {
            HashMap hashMap = new HashMap();
            hashMap.put("status", "pending");
            mRequestReference.child(mUser.getUid()).child(userID).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        HashMap mHashMap = new HashMap();
                        mHashMap.put("status", "wait_confirm");
                        mHashMap.put("userName", myUserName);
                        mHashMap.put("profilePic", myAvatarURL);
                        mHashMap.put("userID", myID);
                        mRequestReference.child(userID).child(mUser.getUid()).updateChildren(mHashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewSingleContact.this, "Bạn đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                                    currentState = "i_sent_pending";
                                    btnSendFriendRequest.setText(R.string.button_cancel_send);
                                } else {
                                    Toast.makeText(ViewSingleContact.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
        if (currentState.equals("i_sent_pending") || currentState.equals("i_sent_decline")) {
            mRequestReference.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mRequestReference.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewSingleContact.this, "Bạn đã hủy yêu cầu kết bạn", Toast.LENGTH_SHORT).show();
                                    currentState = "nothing_happen";
                                    btnSendFriendRequest.setText(R.string.button_send_friend_request);
                                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(ViewSingleContact.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
        if (currentState.equals("he_sent_pending")) {
            mRequestReference.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mRequestReference.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    HashMap mHashMap = new HashMap();
                                    mHashMap.put("status", "friend");
                                    mHashMap.put("userID", userID);
                                    mHashMap.put("userName", userName);
                                    mHashMap.put("profilePic", userAvatarURL);
                                    mHashMap.put("email", userEmail);

                                    //Thông tin của bản thân sẽ lưu trong node của bạn bè
                                    HashMap mHashMap1 = new HashMap();
                                    mHashMap1.put("status", "friend");
                                    mHashMap1.put("userID", myID);
                                    mHashMap1.put("userName", myUserName);
                                    mHashMap1.put("profilePic", myAvatarURL);
                                    mHashMap1.put("email", myEmail);
                                    mFriendReference.child(myID).child(userID).updateChildren(mHashMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if (task.isSuccessful()) {
                                                mFriendReference.child(userID).child(myID).updateChildren(mHashMap1).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        Toast.makeText(ViewSingleContact.this, "Các bạn đã là bạn bè", Toast.LENGTH_SHORT).show();
                                                        currentState = "friend";
                                                        btnSendFriendRequest.setText(R.string.button_unfriend);
                                                        btnCancelSendFriendRequest.setVisibility(View.GONE);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }

                }
            });
        }
        if (currentState.equals("friend")) {
            openConfirmUnfriendDialog(Gravity.CENTER);
        }
    }

    private void checkRequestFriendExistance(String userID) {
        mFriendReference.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friend";
                    btnSendFriendRequest.setText(R.string.button_unfriend);
                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mFriendReference.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentState = "friend";
                    btnSendFriendRequest.setText(R.string.button_unfriend);
                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRequestReference.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "i_sent_pending";
                        btnSendFriendRequest.setText(R.string.button_cancel_send);
                        btnCancelSendFriendRequest.setVisibility(View.GONE);
                    }
                    if (snapshot.child("status").getValue().toString().equals("decline")) {
                        currentState = "i_sent_decline";
                        btnSendFriendRequest.setText(R.string.button_cancel_send);
                        btnCancelSendFriendRequest.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRequestReference.child(mUser.getUid()).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("status").getValue().toString().equals("wait_confirm")) {
                        mRequestReference.child(userID).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    if (snapshot.child("status").getValue().toString().equals("pending")) {
                                        currentState = "he_sent_pending";
                                        btnSendFriendRequest.setText(R.string.button_accept_friend_request);
                                        btnCancelSendFriendRequest.setText(R.string.button_decline);
                                        btnCancelSendFriendRequest.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (currentState.equals("nothing_happen")) {
            currentState = "nothing_happen";
            btnSendFriendRequest.setText(R.string.button_send_friend_request);
            btnCancelSendFriendRequest.setVisibility(View.GONE);
        }
    }

    private void sendDeclineFriendRequest(String userID) {
        if (currentState.equals("he_sent_pending")) {
            mRequestReference.child(userID).child(mUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener() {

                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        mRequestReference.child(mUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewSingleContact.this, "Đã từ chối kết bạn", Toast.LENGTH_SHORT).show();
                                    currentState = "nothing_happen";
                                    btnSendFriendRequest.setText(R.string.button_send_friend_request);
                                    btnCancelSendFriendRequest.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void openConfirmUnfriendDialog(int gravity) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_unfriend_dialog);
        Window window = (Window) dialog.getWindow();
        if (window == null) {
            return;
        } else {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            window.setAttributes(windowAttributes);

            if (Gravity.CENTER == gravity) {
                dialog.setCancelable(true);
            } else {
                dialog.setCancelable(false);
            }
            Button btnConfirm = dialog.findViewById(R.id.btnConfirmUnfriend);
            Button btnCancelConfirm = dialog.findViewById(R.id.btnCancelConfirmUnfriend);

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    deleteFriend(userID);
                }
            });
            btnCancelConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    private void deleteFriend(String userID) {
        mFriendReference.child(myID).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFriendReference.child(userID).child(myID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ViewSingleContact.this, "Đã hủy kết bạn thành công", Toast.LENGTH_SHORT).show();
                            currentState = "nothing_happen";
                            btnSendFriendRequest.setText(R.string.button_send_friend_request);
                            btnCancelSendFriendRequest.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }
}
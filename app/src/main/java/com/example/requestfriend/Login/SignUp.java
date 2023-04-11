package com.example.requestfriend.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.requestfriend.Models.Users;
import com.example.requestfriend.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    EditText edtUserName, edtEmail, edtPassword, edtConfirmPassword;
    TextView tvClickToSignIn;
    Button btnSignUp;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setControl();
        setEvent();
    }

    private void setControl() {
        edtUserName = findViewById(R.id.edtUserName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        tvClickToSignIn = findViewById(R.id.tvClickToSignIn);
        btnSignUp = findViewById(R.id.btnSignUp);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setEvent() {

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog dialog = new ProgressDialog(SignUp.this);
                dialog.setTitle("Đăng ký");
                dialog.setMessage("Đang tạo tài khoản cho bạn");
                if (edtUserName.getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Không được để trống Tên người dùng", Toast.LENGTH_SHORT).show();
                } else if (edtEmail.getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Không được để trống Email", Toast.LENGTH_SHORT).show();
                } else if (edtPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Không được để trống Mật khẩu", Toast.LENGTH_SHORT).show();
                } else if (edtConfirmPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SignUp.this, "Không được để trống Xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
                } else if (!edtConfirmPassword.getText().toString().trim().equals(edtPassword.getText().toString().trim())) {
                    Toast.makeText(SignUp.this, "Không được để trống Email", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                    mAuth.fetchSignInMethodsForEmail(edtEmail.getText().toString().trim()).
                            addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        SignInMethodQueryResult result = task.getResult();
                                        boolean emailExists = result.getSignInMethods().size() > 0;
                                        if (emailExists) {
                                            dialog.dismiss();
                                            Toast.makeText(SignUp.this, "Email đã được sử dụng, vui lòng sử dụng email khác", Toast.LENGTH_SHORT).show();
                                        } else {
                                            mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString().trim(), edtPassword.getText().toString().trim())
                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            dialog.dismiss();
                                                            if (task.isSuccessful()) {
                                                                String userID = task.getResult().getUser().getUid();
                                                                Users users = new Users(edtUserName.getText().toString().trim(), edtEmail.getText().toString().trim(), edtPassword.getText().toString().trim(), userID, "Offline");
                                                                mUserReference.child(userID).setValue(users);
                                                                Toast.makeText(SignUp.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(SignUp.this, SignIn.class);
                                                                startActivity(intent);
                                                            } else {
                                                                Toast.makeText(SignUp.this, "Lỗi khi kiểm tra email", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            });
                }
            }
        });
        tvClickToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvClickToSignIn.clearFocus();
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
            }
        });
    }
}
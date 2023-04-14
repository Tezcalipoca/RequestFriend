package com.example.requestfriend.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.requestfriend.MainActivity;
import com.example.requestfriend.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SignIn extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    TextView tvClickToSignUp;
    Button btnSignIn;
    SharedPreferences sharedPreferences;
    CheckBox cbRememberPassword;
    ProgressDialog dialog;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setControl();
        setEvent();
    }

    private void setControl() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tvClickToSignUp = findViewById(R.id.tvClickToSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);
        cbRememberPassword = findViewById(R.id.cbRememberPassword);

        sharedPreferences = getSharedPreferences("Password", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        dialog = new ProgressDialog(SignIn.this);
        dialog.setTitle("Đăng nhập");
        dialog.setMessage("Đang xác thực, vui lòng đợi!");
    }

    private void setEvent() {
        readPassword();
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtEmail.getText().toString().isEmpty()) {
                    Toast.makeText(SignIn.this, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show();
                } else if (edtPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SignIn.this, "Vui lòng nhập Mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    signInAction();
                }
            }
        });

        tvClickToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    private void signInAction() {
        dialog.show();
        mAuth.fetchSignInMethodsForEmail(edtEmail.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = result.getSignInMethods();
                            if (signInMethods != null && signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                mAuth.signInWithEmailAndPassword(edtEmail.getText().toString().trim(), edtPassword.getText().toString().trim())
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(SignIn.this, MainActivity.class);
                                                    startActivity(intent);
                                                    Toast.makeText(SignIn.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                                    if (cbRememberPassword.isChecked()) {
                                                        writePassword();
                                                    } else {
                                                        removeSharedPreferences();
                                                    }
                                                } else {
                                                    dialog.dismiss();
                                                    Toast.makeText(SignIn.this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(SignIn.this, "Email không tồn tại trên hệ thống", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            dialog.dismiss();
                            Toast.makeText(SignIn.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void writePassword() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Email",edtEmail.getText().toString().trim());
        editor.putString("Password",edtPassword.getText().toString().trim());
        editor.putBoolean("Checked", true);
        editor.commit();
    }
    private void readPassword() {
        edtEmail.setText(sharedPreferences.getString("Email", ""));
        edtPassword.setText(sharedPreferences.getString("Password",""));
        cbRememberPassword.setChecked(sharedPreferences.getBoolean("Checked", false));
    }

    private void removeSharedPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.remove("Checked");
        editor.commit();
    }

}
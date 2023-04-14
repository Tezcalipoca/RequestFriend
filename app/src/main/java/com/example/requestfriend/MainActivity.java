package com.example.requestfriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.requestfriend.Fragment.FriendFragment;
import com.example.requestfriend.Fragment.ListUserFragment;
import com.example.requestfriend.Login.SignIn;
import com.example.requestfriend.Models.Users;
import com.example.requestfriend.View.MyProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int FRAGMENT_FRIEND = 1;
    private static final int FRAGMENT_LIST_USER = 2;
    private int currentFragment = FRAGMENT_FRIEND;
    private int backPressCount = 0;
    private boolean doubleBackToExitPressedOnce = false;

    Toolbar toolbar;
    BottomNavigationView mBottomNavigationView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference, mRequestReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setControl();
        setEvent();
    }

    private void setControl() {
        toolbar = findViewById(R.id.toolbarMain);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void setEvent() {
        loadHeaderNavigation();  // Tải thông tin cho HeaderNavigation
        setActionDrawerToggle(); //Xử lý cho DrawerToggle
        actionToolbar(); //Xử lý cho Toolbar
        actionToolbarHeaderNavigation(); //Xử lý cho Toolbar
        replaceFragment(new FriendFragment());
        setTitleToolBar();
        bottomNavigation();
        actionNavigationDrawer();
    }

    private void loadHeaderNavigation() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        View headerNavigation = navigationView.getHeaderView(0);
        CircleImageView nav_header_userPhoto = (CircleImageView) headerNavigation.findViewById(R.id.nav_header_userPhoto);
        TextView nav_header_userName = (TextView) headerNavigation.findViewById(R.id.nav_header_userName);
        TextView nav_header_userEmail = (TextView) headerNavigation.findViewById(R.id.nav_header_userEmail);
        headerNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyProfile.class);
                startActivity(intent);
            }
        });

        mUserReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.default_avatar).into(nav_header_userPhoto);
                    nav_header_userName.setText(user.getUserName());
                    nav_header_userEmail.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setActionDrawerToggle() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void actionToolbarHeaderNavigation() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void actionToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.icon_menu_navigation);
    }

    private void actionNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_friend);
        mBottomNavigationView.getMenu().findItem(R.id.action_friends).setChecked(true);
    }

    private void bottomNavigation() {
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_friends:
                        openFriendFragment();
                        navigationView.setCheckedItem(R.id.nav_friend);
                        break;
                    case R.id.action_contacts:
                        openContactFragment();
                        navigationView.setCheckedItem(R.id.nav_contact);
                        break;
                }
                setTitleToolBar();
                return true;
            }
        });
    }

    private void openFriendFragment() {
        if (currentFragment != FRAGMENT_FRIEND) {
            replaceFragment(new FriendFragment());
            currentFragment = FRAGMENT_FRIEND;
        }
    }

    private void openContactFragment() {
        if (currentFragment != FRAGMENT_LIST_USER) {
            replaceFragment(new ListUserFragment());
            currentFragment = FRAGMENT_LIST_USER;
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_view, fragment);
        transaction.commit();
    }

    private void setTitleToolBar() {
        String title = "";
        switch (currentFragment) {
            case FRAGMENT_FRIEND:
                title = getString(R.string.action_friends);
                break;
            case FRAGMENT_LIST_USER:
                title = getString(R.string.action_contacts);
                break;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    Handler mHandler = new Handler();
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
            backPressCount = 0;
        }
    };

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                finishAffinity();
                super.onBackPressed();
                return;
            }

            doubleBackToExitPressedOnce = true;
            backPressCount++;

            if (backPressCount == 1) {
                Toast.makeText(this, "Nhấn back lần nữa để thoát", Toast.LENGTH_SHORT).show();
            } else if (backPressCount == 2) {
                Toast.makeText(this, "Thoát ứng dụng", Toast.LENGTH_SHORT).show();
                finishAffinity();
                return;
            }
            mHandler.postDelayed(mRunnable, 2000);
        }


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_friend) {
            openFriendFragment();
            mBottomNavigationView.getMenu().findItem(R.id.action_friends).setChecked(true);
        } else if (id == R.id.nav_contact) {
            openContactFragment();
            mBottomNavigationView.getMenu().findItem(R.id.action_contacts).setChecked(true);
        } else if (id == R.id.nav_logout) {
            openDialogConfirmLogout(Gravity.CENTER);
        }
        setTitleToolBar();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void openDialogConfirmLogout(int gravity) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_dialog);
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
            Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
            Button btnCancelConfirm = dialog.findViewById(R.id.btnCancelConfirm);

            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Xóa FCM_TOKEN
                    if (mUser != null) {
                        mUserReference.child(mUser.getUid()).child("fcmToken").removeValue();
                    }
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, SignIn.class);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
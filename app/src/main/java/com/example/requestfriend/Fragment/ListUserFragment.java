package com.example.requestfriend.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.requestfriend.Adapter.ContactAdapter;
import com.example.requestfriend.Models.Users;
import com.example.requestfriend.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ListUserFragment extends Fragment {
    RecyclerView rvListContact;
    androidx.appcompat.widget.SearchView action_searchUser;
    ArrayList<Users> listUsers = new ArrayList<>();
    List<Users> tempUsers = new ArrayList<>();
    ContactAdapter listUsersAdapter;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserReference, mFriendReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_user, container, false);
        setControl(view);
        setEvent();
        return view;

    }

    private void setControl(View view) {
        rvListContact = view.findViewById(R.id.rvListContact);
        action_searchUser = view.findViewById(R.id.action_searchUser);
        action_searchUser.clearFocus();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        // Khởi tạo adapter
        listUsersAdapter = new ContactAdapter(getContext(), listUsers);
        rvListContact.setAdapter(listUsersAdapter);
        // Tạo ngăn cách giữa 2 đối tượng
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        rvListContact.addItemDecoration(itemDecoration);
        /* Khởi tạo một LinearLayout và gán vào RecycleView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvListContact.setLayoutManager(layoutManager);
    }

    private void setEvent() {
        loadUsers();
        action_searchUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                listUsersAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listUsersAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void loadUsers() {
        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tempUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (mUser != null && users != null && !mUser.getEmail().equals(users.getEmail())) {
                        tempUsers.add(users);
                    }
                }
                mFriendReference.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        listUsers.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String friendID = dataSnapshot.getKey();

                                for (int i = 0; i < tempUsers.size(); i++) {
                                    Users users = tempUsers.get(i);

                                    if (users.getUserID().equals(friendID)) {
                                        tempUsers.remove(users);
                                        break;
                                    }
                                }
                            }
                        }
                        listUsers.clear();
                        listUsers.addAll(tempUsers);
                        listUsersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
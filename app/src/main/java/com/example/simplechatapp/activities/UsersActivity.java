package com.example.simplechatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatapp.R;
import com.example.simplechatapp.adapters.UserAdapter;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
public class UsersActivity extends AppCompatActivity
        implements UserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private ArrayList<User> usersList;
    private UserAdapter adapter;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    private Button btnlogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersList = new ArrayList<>();
        adapter = new UserAdapter(usersList, this);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUsers();

        btnlogout = findViewById(R.id.btnlogout);
        btnlogout.setOnClickListener(v -> logout());

    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                String currentUid = auth.getCurrentUser().getUid();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    /*if (user != null && !user.uid.equals(currentUid)) {
                        usersList.add(user);
                    }*/
                    if (user != null) {
                        usersList.add(user);
                    }

                }
                Toast.makeText(UsersActivity.this,
                        "Users found: " + snapshot.getChildrenCount(),
                        Toast.LENGTH_SHORT).show();

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

   @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userId", user.uid);
        intent.putExtra("userName", user.name);
        startActivity(intent);
    }

    private void logout() {
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(auth.getCurrentUser().getUid())
                .child("status")
                .setValue("offline");

        auth.signOut();

        startActivity(new Intent(UsersActivity.this, LoginActivity.class));
        finish();
    }

}

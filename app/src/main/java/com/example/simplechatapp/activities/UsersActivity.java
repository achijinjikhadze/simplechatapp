package com.example.simplechatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

public class UsersActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<User> usersList;
    private UserAdapter adapter;
    private Button btnLogout;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        EdgeToEdge.enable(this);

        listView = findViewById(R.id.listViewUsers);
        btnLogout = findViewById(R.id.btnlogout);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersList = new ArrayList<>();
        adapter = new UserAdapter(this, usersList, user -> openChat(user));
        listView.setAdapter(adapter);

        loadUsers();

        btnLogout.setOnClickListener(v -> logout());
    }

    //userebis listi
    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            //roca data sheicvleba
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usersList.clear(); //dublirebis asacileblad listis gasuftaveba
                String currentUid = auth.getCurrentUser().getUid();

                //listis loopi
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null && !user.uid.equals(currentUid)) { //axlandeli useri ar chans
                        usersList.add(user);
                    }
                }

                Toast.makeText(UsersActivity.this,
                        "მომხმარებლები: " + usersList.size(),
                        Toast.LENGTH_SHORT).show();

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UsersActivity.this,
                        "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //chatis intenti
    private void openChat(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("userId", user.uid);
        intent.putExtra("userName", user.name);
        startActivity(intent);
    }

    //gamosvla
    private void logout() {
        usersRef.child(auth.getCurrentUser().getUid())
                .child("status")
                .setValue("offline");

        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

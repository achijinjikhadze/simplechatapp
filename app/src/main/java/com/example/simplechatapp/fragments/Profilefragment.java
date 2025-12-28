package com.example.simplechatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.simplechatapp.R;
import com.example.simplechatapp.activities.ChatActivity;
import com.example.simplechatapp.activities.FirstActivity;
import com.example.simplechatapp.activities.LoginActivity;
import com.example.simplechatapp.activities.UsersActivity;
import com.example.simplechatapp.adapters.UserAdapter;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Profilefragment extends Fragment {

    private Button btnlgout;
    private TextView usr;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profilefr, container, false);

        btnlgout = view.findViewById(R.id.btnlogout);
        usr=view.findViewById(R.id.userid);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadCurrentUser();



        btnlgout.setOnClickListener(v -> logout());

        return view;

    }

    private void loadCurrentUser() {
        String currentUid = auth.getCurrentUser().getUid();

        usersRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            usr.setText(user.name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(),
                                "Failed to load user",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void logout() {
        usersRef.child(auth.getCurrentUser().getUid())
                .child("status")
                .setValue("offline");

        auth.signOut();
        startActivity(new Intent(getActivity(), FirstActivity.class));
        getActivity().finish();
    }
}

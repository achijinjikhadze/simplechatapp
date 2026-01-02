package com.example.simplechatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.simplechatapp.R;
import com.example.simplechatapp.activities.ChatActivity;
import com.example.simplechatapp.activities.LoginActivity;
import com.example.simplechatapp.adapters.UserAdapter;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Chatusersfragment extends Fragment {

    private ListView listView;
    private ArrayList<User> usersList;
    private UserAdapter adapter;
    private Button btnLogout;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.chatusersfr, container, false);

        listView = view.findViewById(R.id.listViewUsers);


        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersList = new ArrayList<>();
        adapter = new UserAdapter(getContext(), usersList, user -> openChat(user));
        listView.setAdapter(adapter);

        loadUsers();



        return view;
    }

    //userebis listi
    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            //roca data sheicvleba
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear(); ; //dublirebis asacileblad listis gasuftaveba
                String currentUid = auth.getCurrentUser().getUid();

                //listis loopi
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null && !user.uid.equals(currentUid)) { //axlandeli useri ar chans
                        usersList.add(user);
                    }
                }

                adapter.notifyDataSetChanged();

                /*Toast.makeText(getContext(),
                        "მომხმარებლები: " + usersList.size(),
                        Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //chatis intenti
    private void openChat(User user) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("userId", user.uid);
        intent.putExtra("userName", user.name);
        startActivity(intent);
    }

    /*gamosvla
    private void logout() {
        usersRef.child(auth.getCurrentUser().getUid())
                .child("status")
                .setValue("offline");

        auth.signOut();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    } */
}

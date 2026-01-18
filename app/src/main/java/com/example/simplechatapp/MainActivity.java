package com.example.simplechatapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

//import com.example.simplechatapp.activities.UsersActivity;
import com.example.simplechatapp.adapters.UserAdapter;
import com.example.simplechatapp.fragments.ChatsFragment;
import com.example.simplechatapp.fragments.Chatusersfragment;
import com.example.simplechatapp.fragments.Notificationsfragment;
import com.example.simplechatapp.fragments.Profilefragment;
import com.example.simplechatapp.fragments.UsersFragment;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<User> usersList;
    private UserAdapter adapter;
    private Button btnLogout;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;


    private Button regbtn, logbtn, usrbtn;
    ImageButton home, chat, user, notf, c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

         auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        listView = findViewById(R.id.listViewUsers);

        usersList = new ArrayList<>();
        adapter = new UserAdapter(this, usersList, null);
        listView.setAdapter(adapter);

        loadUsers();


      //navigation butonebi
        home   = findViewById(R.id.home);
        chat   = findViewById(R.id.chat);
        user  = findViewById(R.id.user);
        notf = findViewById(R.id.notf);
        c= findViewById(R.id.c);


        //gadava im fragemntze romelsac daacher

        loadFragment(new ChatsFragment());
        user.setSelected(false);
        home.setSelected(false);
        chat.setSelected(true);
        notf.setSelected(false);
        c.setSelected(false);

        user.setOnClickListener(v -> {
            user.setSelected(true);
            home.setSelected(false);
            chat.setSelected(false);
            notf.setSelected(false);
            c.setSelected(false);
            loadFragment(new Profilefragment());
        });


        chat.setOnClickListener(v ->{
                    user.setSelected(false);
                    home.setSelected(false);
                    chat.setSelected(true);
            notf.setSelected(false);
                    c.setSelected(false);
                loadFragment(new ChatsFragment());
                });

        home.setOnClickListener(v ->{
            user.setSelected(false);
            home.setSelected(true);
            chat.setSelected(false);
            c.setSelected(false);
            loadFragment(new Chatusersfragment());
        });

        notf.setOnClickListener(v ->{
            user.setSelected(false);
            home.setSelected(false);
            chat.setSelected(false);
            notf.setSelected(true);
            c.setSelected(false);
            loadFragment(new Notificationsfragment());
        });

        c.setOnClickListener(v ->{
            user.setSelected(false);
            home.setSelected(false);
            chat.setSelected(false);
            notf.setSelected(false);
            c.setSelected(true);
            loadFragment(new UsersFragment());
        });

       /* getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();*/


    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

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

            }
        });
    }
}


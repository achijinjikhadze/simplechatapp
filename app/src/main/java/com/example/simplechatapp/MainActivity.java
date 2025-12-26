package com.example.simplechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.simplechatapp.activities.LoginActivity;
import com.example.simplechatapp.activities.RegisterActivity;
import com.example.simplechatapp.activities.UsersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Button regbtn, logbtn, usrbtn;
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


        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        Toast.makeText(this, "Firebase Connected", Toast.LENGTH_SHORT).show();

        regbtn = findViewById(R.id.btngoreg);
        logbtn=findViewById(R.id.btngolog);

        usrbtn=findViewById(R.id.btnusr);

        // Example: switch fragment
        Fragment selectedFragment = null;

      /*  switch (buttonId) { //sachiro
            //case R.id.chati:
                //selectedFragment = new ChatFragment();
               // break;
            case R.id.user:
                selectedFragment = new UsersFragment();
                break;
        }*/

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit();


        regbtn.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        logbtn.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        usrbtn.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(intent);
        });

    }
}
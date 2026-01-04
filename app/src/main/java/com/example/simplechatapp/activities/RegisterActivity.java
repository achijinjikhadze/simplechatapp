package com.example.simplechatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatapp.R;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText etname, etemail, etpassword, etpassword2, etsurname;
    private Button etregister;
    private TextView oldacc;

    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EdgeToEdge.enable(this);

        auth = FirebaseAuth.getInstance();

        oldacc=findViewById(R.id.oldacc);
        etname = findViewById(R.id.etname);
        etsurname =findViewById(R.id.etsurname);
        etemail = findViewById(R.id.etemail);
        etpassword = findViewById(R.id.etpassword);
        etpassword2 = findViewById(R.id.etpassword2);
        etregister = findViewById(R.id.btnregister);
        //String etbio="";


        etregister.setOnClickListener(v -> {
            String pass1 = etpassword.getText().toString();
            String pass2 = etpassword2.getText().toString();

            if (!pass1.equals(pass2)) {
                Toast.makeText(RegisterActivity.this, "პაროლები არ ემთხვევა, სცადე თავიდან", Toast.LENGTH_SHORT).show();
            } else {
                registerUser();
            }
        });



        oldacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        //ukan gamosvla firstactivtize
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Intent intent = new Intent(RegisterActivity.this, FirstActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void registerUser() {
        String name = etname.getText().toString().trim();
        String surname= etsurname.getText().toString().trim();
        String email = etemail.getText().toString().trim();
        String password = etpassword.getText().toString().trim();
        String imageurl = "https://i.ibb.co/cc3bQ1Qk/user.jpg";
        String coverurl = "https://i.ibb.co/cc3bQ1Qk/user.jpg";
        String bio="";


        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        /*firebase register*/
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String uid = auth.getCurrentUser().getUid();
                        User user = new User(uid, name, surname, email, bio, "online", imageurl, coverurl, 0 ,0);

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(uid)
                                .setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(this,
                                                "Registration successful",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(this,
                                                "Database error",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.simplechatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.example.simplechatapp.Forgpas;
import com.example.simplechatapp.MainActivity;
import com.example.simplechatapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView newacc, logforg;
    private Button btnLogin, btnGoRegister;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EdgeToEdge.enable(this);


        newacc=findViewById(R.id.newacc);
        auth = FirebaseAuth.getInstance();

       if (auth.getCurrentUser() != null) {
            goToUsers();
            return;
        }

        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        btnLogin = findViewById(R.id.btnlogin);
        logforg = findViewById(R.id.forgetpas);

        btnLogin.setOnClickListener(v -> loginUser());


        newacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        logforg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, new Forgpas())
                        .commit();
            }
        });


        //ukan gamosvla firstactivtize
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                Intent intent = new Intent(LoginActivity.this, FirstActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //tu yvela field ar chaiwera
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "შეავსე ყველა", Toast.LENGTH_SHORT).show();
            return;
        }

        //shesvla
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "წარმატებით შეხვედი", Toast.LENGTH_SHORT).show();
                        goToUsers();
                    } else {
                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //shesvlis mere userebis listi
    private void goToUsers() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }






}

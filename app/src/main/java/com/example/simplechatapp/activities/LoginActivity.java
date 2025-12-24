package com.example.simplechatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoRegister;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

      /* if (auth.getCurrentUser() != null) {
            goToUsers();
            return;
        }*/

        etEmail = findViewById(R.id.etemail);
        etPassword = findViewById(R.id.etpassword);
        btnLogin = findViewById(R.id.btnlogin);
        btnGoRegister = findViewById(R.id.btngoreg);

        btnLogin.setOnClickListener(v -> loginUser());
        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
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
        Intent intent = new Intent(this, UsersActivity.class);
        startActivity(intent);
        finish();
    }
}

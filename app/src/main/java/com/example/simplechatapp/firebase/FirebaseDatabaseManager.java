package com.example.simplechatapp.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseManager {
    public static DatabaseReference root =
            FirebaseDatabase.getInstance().getReference();
}


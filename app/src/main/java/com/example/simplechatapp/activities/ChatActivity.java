package com.example.simplechatapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatapp.R;
import com.example.simplechatapp.adapters.MessageAdapter;
import com.example.simplechatapp.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;

    private FirebaseAuth auth;
    private DatabaseReference chatsRef;

    private ArrayList<Message> messageList;
    private MessageAdapter adapter;

    private String receiverId, receiverName, senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        auth = FirebaseAuth.getInstance();
        senderId = auth.getCurrentUser().getUid();

        // Get receiver info from intent
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");

        setTitle(receiverName);

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, senderId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String chatId = senderId + "_" + receiverId;
        long timestamp = System.currentTimeMillis();

        Message message = new Message(senderId, receiverId, text, timestamp, false);

        chatsRef.child(chatId).push().setValue(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        etMessage.setText("");
                    } else {
                        Toast.makeText(ChatActivity.this, "Failed to send", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        String chatId1 = senderId + "_" + receiverId;
        String chatId2 = receiverId + "_" + senderId;



        chatsRef.child(chatId1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    messageList.add(msg);

                    if (msg.receiverId.equals(senderId) && !msg.seen) {
                        ds.getRef().child("seen").setValue(true);
                    }
                }
                recyclerView.scrollToPosition(messageList.size() - 1);
                adapter.notifyDataSetChanged();


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });



        chatsRef.child(chatId2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (!messageList.contains(msg)) {
                        messageList.add(msg);
                    }
                }
                recyclerView.scrollToPosition(messageList.size() - 1);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


    }
}

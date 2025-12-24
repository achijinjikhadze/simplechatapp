package com.example.simplechatapp.activities;

import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    private ListView listView;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnSend2;

    private FirebaseAuth auth;
    private DatabaseReference chatsRef;

    private ArrayList<Message> messageList;
    private MessageAdapter adapter;

    private String receiverId, receiverName, senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listView = findViewById(R.id.listViewMessages);
        etMessage = findViewById(R.id.etMessage);
        //btnSend = findViewById(R.id.btnSend);
        btnSend2 = findViewById(R.id.sendbtn);


        auth = FirebaseAuth.getInstance();
        senderId = auth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");

        setTitle(receiverName);

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, senderId);
        listView.setAdapter(adapter);

        //btnSend.setOnClickListener(v -> sendMessage());
        btnSend2.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        String chatId = senderId + "_" + receiverId; //unikaluri chatid (marto 1 arsebobs)
        long timestamp = System.currentTimeMillis();

        //axali mesiji
        Message message = new Message(senderId, receiverId, text, timestamp, false);

        chatsRef.child(chatId).push().setValue(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        etMessage.setText("");
                    } else {
                        Toast.makeText(ChatActivity.this, "ვერ გაიგზავნა", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        //firebasedan orive mxaris wamogeba mesagebis sachveneblad
        String chatId1 = senderId + "_" + receiverId;
        String chatId2 = receiverId + "_" + senderId;

        //chveni mesijebi
        chatsRef.child(chatId1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear(); //dzveli messijebis washla

                //forit gadayola mesijebze
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    messageList.add(msg);

                    //tunaxa - tu current useri aris mimgebi
                    if (msg.receiverId.equals(senderId) && !msg.seen) {
                        ds.getRef().child("seen").setValue(true);
                    }
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(messageList.size() - 1); //bolo mesijze chasvla
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });

        //meore useris mesijebi
        chatsRef.child(chatId2).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (!messageList.contains(msg)) {
                        messageList.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                listView.setSelection(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

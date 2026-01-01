package com.example.simplechatapp.activities;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.simplechatapp.MainActivity;
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
    private TextView usrname;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnSend2, back, camera, photo, voice, icshow;

    private FirebaseAuth auth;
    private DatabaseReference chatsRef;

    private ArrayList<Message> messageList;
    private MessageAdapter adapter;

    private String receiverId, receiverName, senderId;

    private ImageView ivProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        listView = findViewById(R.id.listViewMessages);
        etMessage = findViewById(R.id.etMessage);
        //btnSend = findViewById(R.id.btnSend);
        btnSend2 = findViewById(R.id.sendbtn);
        back=findViewById(R.id.bchat);
        camera = findViewById(R.id.camerabtn);
         photo = findViewById(R.id.photobtn);
         voice = findViewById(R.id.voicebtn);
         icshow = findViewById(R.id.icshow);



        auth = FirebaseAuth.getInstance();
        senderId = auth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");

        usrname=findViewById(R.id.tvUserName);
        usrname.setText(receiverName);

        setTitle(receiverName);

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, senderId);
        listView.setAdapter(adapter);

        ivProfile = findViewById(R.id.profileimg);
        loadReceiverProfile();

        //btnSend.setOnClickListener(v -> sendMessage());
        btnSend2.setOnClickListener(v -> sendMessage());

        loadMessages();

        //typingis gadideba
        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) etMessage.getLayoutParams();

            if (hasFocus) {

                camera.setVisibility(View.GONE);
                photo.setVisibility(View.GONE);
                voice.setVisibility(View.GONE);
                icshow.setVisibility(View.VISIBLE);

                params.weight = 3;
                listView.post(() -> listView.setSelection(adapter.getCount() - 1));
            }
        });

        //iconebish chveneba
        icshow.setOnClickListener(v -> {
            animatemv();

            camera.setVisibility(View.VISIBLE);
            photo.setVisibility(View.VISIBLE);
            voice.setVisibility(View.VISIBLE);

            icshow.setVisibility(View.GONE);
        });

        //iconebis damalva
        etMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LinearLayout.LayoutParams params =
                        (LinearLayout.LayoutParams) etMessage.getLayoutParams();

                animatemv();

                camera.setVisibility(View.GONE);
                photo.setVisibility(View.GONE);
                voice.setVisibility(View.GONE);
                icshow.setVisibility(View.VISIBLE);

                params.weight = 3;
                etMessage.setLayoutParams(params);
                return false;
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });


    }

    private void animatemv() {
        TransitionManager.beginDelayedTransition(
                findViewById(R.id.bottomLayout),
                new AutoTransition().setDuration(150)
        );
    }


    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;


        String chatId = senderId.compareTo(receiverId) < 0 ?
                senderId + "_" + receiverId :
                receiverId + "_" + senderId;   //unikaluri chatid (marto 1 arsebobs)

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
        String chatId = senderId.compareTo(receiverId) < 0 ?
                senderId + "_" + receiverId :
                receiverId + "_" + senderId;

        chatsRef.child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
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
                listView.setSelection(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


   /* private void loadMessages() {
        //firebasedan orive mxaris wamogeba mesagebis sachveneblad
        String chatId1 = senderId + "_" + receiverId;
        String chatId2 = receiverId + "_" + senderId;

        //chveni mesijebi
        chatsRef.child(chatId1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //messageList.clear();dzveli messijebis washla

                //forit gadayola mesijebze
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message msg = ds.getValue(Message.class);
                    if (!messageList.contains(msg)) {
                        messageList.add(msg);
                    }

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
    }*/

    private void loadReceiverProfile() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(receiverId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int placeholder = R.drawable.user;

                if (snapshot.exists()) {
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {

                        Glide.with(ChatActivity.this)
                                .load(imageUrl)
                                .placeholder(placeholder)
                                .error(placeholder)
                                .circleCrop()
                                .into(ivProfile);
                        return;
                    }
                }

                Glide.with(ChatActivity.this)
                        .load(placeholder)
                        .circleCrop()
                        .into(ivProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }




}

package com.example.simplechatapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Base64;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.simplechatapp.MainActivity;
import com.example.simplechatapp.R;
import com.example.simplechatapp.adapters.MessageAdapter;
import com.example.simplechatapp.models.Message;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private ActivityResultLauncher<String> permissionLauncher, camerapermissionalauncher;
    private ListView listView;
    private TextView usrname, status;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnSend2, back, camera, photo, voice, icshow, msdel;

    private FirebaseAuth auth;
    private DatabaseReference chatsRef, chatindexref;

    private ArrayList<Message> messageList;
    private MessageAdapter adapter;

    private String receiverId, receiverName, senderId;

    private ImageView ivProfile;
    private LinearLayout chatuser;

    private final String IMGBB_API_KEY = "4fe848465e0b2932cba123e0b46e8c2f";
    private ActivityResultLauncher<String> imagePicker;

    // img
    private String uploadedImageUrl = "";
    private boolean imagePicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        listView = findViewById(R.id.listViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend2 = findViewById(R.id.sendbtn);
        back = findViewById(R.id.bchat);
        camera = findViewById(R.id.camerabtn);
        photo = findViewById(R.id.photobtn);
        voice = findViewById(R.id.voicebtn);
        icshow = findViewById(R.id.icshow);
        chatuser = findViewById(R.id.chatuser);



        auth = FirebaseAuth.getInstance();
        senderId = auth.getCurrentUser().getUid();
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("userName");

        usrname = findViewById(R.id.tvUserName);
        usrname.setText(receiverName);
        status = findViewById(R.id.tvStatus);
        loadReceiverStatus();

        setTitle(receiverName);

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        chatindexref = FirebaseDatabase.getInstance().getReference("chatIndex");

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(this, messageList, senderId, receiverId);
        listView.setAdapter(adapter);

        ivProfile = findViewById(R.id.profileimg);
        loadReceiverProfile();

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::uploadImageToImgbb
        );

        btnSend2.setOnClickListener(v -> sendMessage());

        loadMessages();

        chatuser.setOnClickListener(v -> openprof());

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
        etMessage.setOnTouchListener((v, event) -> {
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
        });

        back.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // galeriis gaxsna
       // photo.setOnClickListener(v -> imagePicker.launch("image/*"));
        //uflebis ageba
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> { //tu aviget ufleba gaixsnas galerea
                    if (isGranted) {

                            imagePicker.launch("image/*");
                    } else {
                        Toast.makeText(ChatActivity.this,
                                "გალერიის უფლება უარყოფილია",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        //uflebis ageba cameraze:
         camerapermissionalauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "კამერის უფლება უარყოფილია", Toast.LENGTH_SHORT).show();
                    }
                });


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    camerapermissionalauncher.launch(Manifest.permission.CAMERA);
                }
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String permission;
                //ufleba androidis versiis mixedvit
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permission = Manifest.permission.READ_MEDIA_IMAGES;
                } else {
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                }
                //tu gvaq ufleba gavxsnat galerea
                if (ContextCompat.checkSelfPermission(ChatActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
                        imagePicker.launch("image/*");


                } else { //tu ar gvaq vikitxot nebartva
                    permissionLauncher.launch(permission);
                }
            }
        });
    }

    //profilis gaxsna/
    private void openprof() {
        Intent intent = new Intent(this, Profileactivity.class);
        intent.putExtra("userId", receiverId);
        intent.putExtra("userName", receiverName);
        startActivity(intent);
    }


    //cameris gaxsna da atbirtva
    private Uri photoUri;
    private ActivityResultLauncher<Uri> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    uploadImageToImgbb(photoUri);
                }
            });
    private void openCamera() {
        File photoFile = new File(getCacheDir(), "camera_photo.jpg");
        photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
        takePhotoLauncher.launch(photoUri);
    }

    private void animatemv() {
        TransitionManager.beginDelayedTransition(
                findViewById(R.id.bottomLayout),
                new AutoTransition().setDuration(150)
        );
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        String imageUrl = "";

        // tu surati aviirchiet
        if (imagePicked) {
            imageUrl = uploadedImageUrl;  //suratis linki
        }

        // tu carielia ar gaigzavnos
        if (TextUtils.isEmpty(text) && TextUtils.isEmpty(imageUrl)) return;


        String chatId = senderId.compareTo(receiverId) < 0 ?
                senderId + "_" + receiverId :
                receiverId + "_" + senderId;   //unikaluri chatid (marto 1 arsebobs) firebase datashi

        long timestamp = System.currentTimeMillis(); //dro

        // axali mesijis objecti
        Message message = new Message(senderId, receiverId, text, imageUrl, timestamp, false);

        // firebaseshi ativrtva
        chatsRef.child(chatId).push().setValue(message) //databasereference
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                         //chatindex cxrilshi davamatot es userebis
                       // chatindexref.child(senderId).child(receiverId).setValue(true);
                        //  chatindexref.child(receiverId).child(senderId).setValue(true);

                        long timestamplast = System.currentTimeMillis();


                        chatindexref.child(senderId).child(receiverId).child("exists").setValue(true);
                        chatindexref.child(senderId).child(receiverId).child("timestamp").setValue(timestamplast);


                        chatindexref.child(receiverId).child(senderId).child("exists").setValue(true);
                        chatindexref.child(receiverId).child(senderId).child("timestamp").setValue(timestamplast);


                        etMessage.setText("");
                        imagePicked = false;
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
                    msg.key = ds.getKey();
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

    //tavze useris gamochena
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

    //suratis serverze atvirtva
    private void uploadImageToImgbb(Uri uri) {
        if (uri == null) return;

        try {
            //content privaideridan fotos gadmotana da serveristvis gamzadbea
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            //serveris ..
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", encodedImage)
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "ვერ აიტვრიტა: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        if (!response.isSuccessful() || response.body() == null) {
                            runOnUiThread(() -> Toast.makeText(ChatActivity.this, "ვერ აიტვირთა", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        JSONObject data = obj.getJSONObject("data");
                        uploadedImageUrl = data.getString("display_url");

                        imagePicked = true;
                        sendMessage();

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "ვერ აიტვირთა: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(ChatActivity.this, "შეცდომა: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //onlinea tu offline
    private void loadReceiverStatus() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(receiverId);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                String st = s.child("status").getValue(String.class);

                if ("online".equals(st)) {
                    status.setText("online");
                    status.setTextColor(getColor(R.color.green));
                } else {
                    status.setText("offline");
                    status.setTextColor(getColor(R.color.grey));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

}

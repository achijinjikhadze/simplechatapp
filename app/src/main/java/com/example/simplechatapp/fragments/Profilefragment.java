package com.example.simplechatapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.simplechatapp.R;
import com.example.simplechatapp.activities.FirstActivity;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Profilefragment extends Fragment {

    private ImageView profileImage;
    private TextView usr, emailTextView, statusTextView;
    private Button btnlgout;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private ActivityResultLauncher<String> imagePicker;

    private final String IMGBB_API_KEY = "4fe848465e0b2932cba123e0b46e8c2f";
    private final String defaultimg = "https://i.ibb.co/6RQWmmKC/5af442d9906e.jpg";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profilefr, container, false);

        profileImage = view.findViewById(R.id.profileImage);
        usr = view.findViewById(R.id.userid);
        emailTextView = view.findViewById(R.id.emailTextView);
        btnlgout = view.findViewById(R.id.btnlogout);

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::uploadImageToImgbb
        );

        profileImage.setOnClickListener(v -> imagePicker.launch("image/*"));

        loadCurrentUser();

        btnlgout.setOnClickListener(v -> logout());

        return view;
    }

    private void loadCurrentUser() {
        String uid = auth.getCurrentUser().getUid();

        usersRef.child(uid).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    usr.setText(user.name);
                    emailTextView.setText(user.email);

                    //profilis foto
                    String imageUrl = (user.imageUrl != null && !user.imageUrl.isEmpty()) ? user.imageUrl : defaultimg;

                    Log.d("ProfileFragment", "Profile Image URL: " + imageUrl);

                    Glide.with(requireContext())
                            .load(imageUrl)

                            .placeholder(R.drawable.user)
                            .error(R.drawable.user)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //profilis fotos dayeneba serveridan url
    private void uploadImageToImgbb(Uri uri) {
        if (uri == null) return;

        try {

            profileImage.setImageURI(uri);

            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("key", IMGBB_API_KEY)
                    .add("image", encodedImage)
                    .build();

            Request request = new Request.Builder()
                    .url("https://i.ibb.co/cc3bQ1Qk/user.jpg")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        if (!response.isSuccessful() || response.body() == null) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }

                        String json = response.body().string();
                        org.json.JSONObject obj = new org.json.JSONObject(json);
                        org.json.JSONObject data = obj.getJSONObject("data");
                        String imageUrl = data.getString("display_url");

                        Log.d("ProfileFragment", "Uploaded Image URL: " + imageUrl);

                        String uid = auth.getCurrentUser().getUid();
                        usersRef.child(uid).child("imageUrl").setValue(imageUrl);

                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Image error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void logout() {
        usersRef.child(auth.getCurrentUser().getUid())
                .child("status")
                .setValue("offline");

        auth.signOut();
        startActivity(new Intent(getActivity(), FirstActivity.class));
        getActivity().finish();
    }
}

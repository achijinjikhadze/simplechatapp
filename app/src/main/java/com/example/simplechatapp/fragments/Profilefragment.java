package com.example.simplechatapp.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.simplechatapp.MainActivity;
import com.example.simplechatapp.R;
import com.example.simplechatapp.activities.FirstActivity;
import com.example.simplechatapp.activities.FullScreenImageActivity;
import com.example.simplechatapp.activities.Profileactivity;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Profilefragment extends Fragment {

    private static final int REQUEST_GALLERY_PERMISSION = 101;
    private boolean isCoverImage = false;
    private ActivityResultLauncher<String> permissionLauncher;


    private ImageView profileimg, coverimg;
    private TextView usr, emailTextView,bioview,  folcnt1, folcnt2;;
    private LinearLayout logbtnt,editimgt, editcov, edtname;
    private Button btnlgout;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private ActivityResultLauncher<String> imagePicker;
    private ActivityResultLauncher<String> coverimgpicker;
    private ImageButton logbtn;
    private String userId;

    private final String IMGBB_API_KEY = "4fe848465e0b2932cba123e0b46e8c2f";
    private final String defaultimg = "https://i.ibb.co/ymhV9WVb/2clrlogo2.png";
    private final String defaultimg2 ="https://i.ibb.co/N2y52dMT/Cover.png";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

       // EdgeToEdge.enable(requireActivity());

        View view = inflater.inflate(R.layout.profilefr, container, false);


        profileimg = view.findViewById(R.id.profileImage);
        coverimg = view.findViewById(R.id.coverimg);

        usr = view.findViewById(R.id.userid);
        emailTextView = view.findViewById(R.id.emailTextView);
        bioview=view.findViewById(R.id.bio);
        folcnt1=view.findViewById(R.id.folwnum1);
        folcnt2=view.findViewById(R.id.folwnum2);

        logbtn=view.findViewById(R.id.logbtn);
        logbtnt=view.findViewById(R.id.logbtnt);
        editimgt=view.findViewById(R.id.editimgt);
        edtname=view.findViewById(R.id.editname);
        editcov = view.findViewById(R.id.editcov);


        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");




        //userId = getArguments() != null ? getArguments().getString("userID") : auth.getCurrentUser().getUid();


        //prifilis da coveris fotostvis
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::uploadImageToImgbb
        );

        coverimgpicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::uploadCoverImageToImgbb
        );


        editimgt.setOnClickListener(v -> {
            isCoverImage = false;
            openGalleryWithPermission(); //uflebis ageba
        });

        editcov.setOnClickListener(v -> {
            isCoverImage = true;
            openGalleryWithPermission();
        });



        loadCurrentUser();

        logbtnt.setOnClickListener(v -> logout());

        //saxelis shecvlis dialog
        edtname.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.editname, null);
            EditText input = dialogView.findViewById(R.id.editUsername);
            EditText input2 = dialogView.findViewById(R.id.editsurname);
            EditText input3=dialogView.findViewById(R.id.editbio);

            //sruli saxeli
            String fullName = usr.getText().toString();
            String[] nameParts = fullName.split(" "); //gayofa
            input.setText(nameParts[0]);
            if (nameParts.length > 1) {
                input2.setText(nameParts[1]);
            }
            input3.setText(bioview.getText().toString());
            Button btnSave = dialogView.findViewById(R.id.btnSave);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create();

            //shecvlilis shenaxva
            btnSave.setOnClickListener(vi -> {
                String newName = input.getText().toString().trim();
                String newSurname = input2.getText().toString().trim();
                String newBio = input3.getText().toString().trim();

                if (!newName.isEmpty() ) { //&& !newSurname.isEmpty()

                    String uid = auth.getCurrentUser().getUid();
                    usersRef.child(uid).child("name").setValue(newName);
                    usersRef.child(uid).child("bio").setValue(newBio);
                    usersRef.child(uid).child("surname").setValue(newSurname)
                            .addOnSuccessListener(aVoid -> {
                                usr.setText(newName + " " + newSurname);
                                Toast.makeText(getContext(), "სახელი შეიცვალა", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "ვერ შეიცვალა", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getContext(), "ჩაწერე სახელი", Toast.LENGTH_SHORT).show();
                }
            });

            btnCancel.setOnClickListener(vi -> dialog.dismiss());

            dialog.show();
        });


        //uflebis ageba
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> { //tu aviget ufleba gaixsnas galerea
                    if (isGranted) {
                        if (isCoverImage) {
                            coverimgpicker.launch("image/*");
                        } else {
                            imagePicker.launch("image/*");
                        }
                    } else {
                        Toast.makeText(getContext(),
                                "Gallery permission denied",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );

        return view;

    }



    private void loadCurrentUser() {

        String uid = auth.getCurrentUser().getUid();

        usersRef.child(uid).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    //tu romelime carielia araferi ardaweros
                    String name = (user.name != null) ? user.name : " ";
                    String surname = (user.surname != null) ? user.surname : " ";
                    usr.setText(name + " " + surname);
                    emailTextView.setText(user.email);
                    bioview.setText(user.bio);
                    folcnt1.setText(String.valueOf(user.followerscnt));
                    folcnt2.setText(String.valueOf(user.followingcnt));

                    //profilis foto
                    String imageUrl = (user.imageUrl != null && !user.imageUrl.isEmpty()) ? user.imageUrl : defaultimg;

                    Log.d("ProfileFragment", "Profile Image URL: " + imageUrl);

                    Glide.with(requireContext())
                            .load(imageUrl)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(profileimg);

                    String coverImageUrl = (user.coverurl != null && !user.coverurl.isEmpty())
                            ? user.coverurl
                            : defaultimg2;

                    Glide.with(requireContext())
                            .load(coverImageUrl)
                            .into(coverimg);

                    profileimg.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), FullScreenImageActivity.class);
                        intent.putExtra("imageUrl", imageUrl);
                        requireContext().startActivity(intent);
                    });
                    coverimg.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), FullScreenImageActivity.class);
                        intent.putExtra("imageUrl", coverImageUrl);
                        requireContext().startActivity(intent);
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //profilis fotos atvritva serverze da dayeneba (suratis linki)
    private void uploadImageToImgbb(Uri uri) {
        if (uri == null) return;

        try {

            profileimg.setImageURI(uri);

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

    //uflebit galeriis gaxsna

    private void openGalleryWithPermission() {
        String permission;
        //ufleba androidis versiis mixedvit
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
         //tu gvaq ufleba gavxsnat galerea
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            if (isCoverImage) { //gavxsnat galerea profile an coveris fotostvis
                coverimgpicker.launch("image/*");
            } else {
                imagePicker.launch("image/*");
            }

        } else { //tu ar gvaq vikitxot nebartva
            permissionLauncher.launch(permission);
        }
    }



    //serverze fotos atvirtva
    private void uploadCoverImageToImgbb(Uri uri) {
        if (uri == null) return;

        try {

            coverimg.setImageURI(uri);

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
                    .url("https://api.imgbb.com/1/upload")
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

                        Log.d("ProfileFragment", "Uploaded Cover Image URL: " + imageUrl);

                        String uid = auth.getCurrentUser().getUid();
                        usersRef.child(uid).child("coverurl").setValue(imageUrl);

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

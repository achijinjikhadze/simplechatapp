package com.example.simplechatapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.simplechatapp.R;
import com.example.simplechatapp.activities.FullScreenImageActivity;
import com.example.simplechatapp.models.Message;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends BaseAdapter {

    private ArrayList<Message> messages;
    private Context context;
    private String currentUserId;
    private String recid;
    public MessageAdapter(Context context, ArrayList<Message> messages, String currentUserId, String receiverId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId; //amjamindeli useri/ vigebt meisji gagazvnilia tu migebuli
        this.recid=receiverId;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = messages.get(position);
        boolean isRight = msg.senderId.equals(currentUserId);  //tu am userit gavagzavnet



        if (convertView == null) {
            //romeli mesijis layout gaixsnas, lefti tu righti
                convertView = LayoutInflater.from(context).inflate(isRight ? R.layout.item_message_right : R.layout.item_message_left, parent, false);

        }

        TextView tvMessage = convertView.findViewById(R.id.tvMessage);
        ImageView ivMessageImage = convertView.findViewById(R.id.ivMessageImage);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvSeen = convertView.findViewById(R.id.tvSeen);

        ImageButton msdel = convertView.findViewById(R.id.msdel);

        // tu text vagzavnit da ara fotos
        if (msg.message != null && !msg.message.isEmpty()) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg.message);
            ivMessageImage.setVisibility(View.GONE);  // foto ar igzavneba
        } else {
            tvMessage.setVisibility(View.GONE);  // texti ar igzavneba

        }


        // tu surati igzavneba
        if (msg.imageurl != null && !msg.imageurl.isEmpty()) {
            ivMessageImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(msg.imageurl)
                    .placeholder(R.drawable.loggo)
                    .into(ivMessageImage);

            //sruli fotos gaxsna roca surats davachert
            ivMessageImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, FullScreenImageActivity.class);
               intent.putExtra("imageUrl", msg.imageurl);
                context.startActivity(intent);
            });
        } else {
            ivMessageImage.setVisibility(View.GONE);
        }

        // gagzavnis dro
        tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.timestamp)));

        //tu naxa
        if (isRight && tvSeen != null) {
            tvSeen.setText(msg.seen ? "✓✓" : "✓");
        }

        //mesijis washla
        msdel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = messages.get(position);

                new AlertDialog.Builder(context)
                        .setTitle("შეტყობინების წაშლა")
                        .setMessage("გინდათ რომ წაიშალოს?")
                        .setPositiveButton("წაშლა", (dialog, which) -> {

                            String chatId = currentUserId.compareTo(recid) < 0
                                    ? currentUserId + "_" + recid
                                    : recid + "_" + currentUserId;

                            FirebaseDatabase.getInstance()
                                    .getReference("chats")
                                    .child(chatId)
                                    .child(message.key)
                                    .removeValue();


                        })
                        .setNegativeButton("უკან", null)
                        .show();
            }


        });
        return convertView;
    }


    @Override
    public int getItemViewType(int position) {
        //1:mesiji gavagzavnet, 0:mesiji mivighet
        return messages.get(position).senderId.equals(currentUserId) ? 1 : 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
        //ori layout tipi, 1-gagzavnili mesijebi, 2-migebuli mesijebi
    }

}

package com.example.simplechatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatapp.R;
import com.example.simplechatapp.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MSG_LEFT = 0;
    private static final int MSG_RIGHT = 1;

    private ArrayList<Message> messages;
    private String currentUserId;

    public MessageAdapter(ArrayList<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).senderId.equals(currentUserId)) {
            return MSG_RIGHT;
        } else {
            return MSG_LEFT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_RIGHT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_right, parent, false);
            return new RightMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_left, parent, false);
            return new LeftMessageViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.timestamp));

        if (holder instanceof RightMessageViewHolder) {
            RightMessageViewHolder h = (RightMessageViewHolder) holder;
            h.tvMessage.setText(msg.message);
            h.tvTime.setText(time);
            h.tvSeen.setText(msg.seen ? "✓✓" : "✓");
        } else {
            LeftMessageViewHolder h = (LeftMessageViewHolder) holder;
            h.tvMessage.setText(msg.message);
            h.tvTime.setText(time);
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class LeftMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public LeftMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    static class RightMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSeen;

        public RightMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSeen = itemView.findViewById(R.id.tvSeen);
        }
    }

}

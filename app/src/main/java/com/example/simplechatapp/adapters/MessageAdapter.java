package com.example.simplechatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.simplechatapp.R;
import com.example.simplechatapp.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends BaseAdapter {

    private ArrayList<Message> messages;
    private Context context;
    private String currentUserId;

    public MessageAdapter(Context context, ArrayList<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId; //amjamindeli useri/ vigebt meisji gagazvnilia tu migebuli
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
        boolean isRight = msg.senderId.equals(currentUserId); //tu am userit gavagavnet

        int type = getItemViewType(position);

        if (convertView == null) {
            //romeli mesijis layout gaixsnas, lefti tu righti
            convertView = LayoutInflater.from(context).inflate(isRight ? R.layout.item_message_right : R.layout.item_message_left, parent, false);
        }

        TextView tvMessage = convertView.findViewById(R.id.tvMessage);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvSeen = convertView.findViewById(R.id.tvSeen);

        tvMessage.setText(msg.message);
        tvTime.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.timestamp)));

        if (isRight && tvSeen != null) {
            tvSeen.setText(msg.seen ? "✓✓" : "✓");
        }

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

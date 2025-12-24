package com.example.simplechatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.simplechatapp.R;
import com.example.simplechatapp.models.User;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {

    private ArrayList<User> users;
    private Context context;


    //activitys gadavcemt useris objects
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private OnUserClickListener listener;

    //useris contructori
    public UserAdapter(Context context, ArrayList<User> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      //axali view layoutistvis
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        }

        TextView tvName = convertView.findViewById(R.id.tvUserName);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);

        User user = users.get(position);

        tvName.setText(user.name);
        tvStatus.setText(user.status);

        //clicki
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });

        return convertView;
    }


    /*
    private static class ViewHolder {
        TextView tvName, tvStatus;
    }*/
}

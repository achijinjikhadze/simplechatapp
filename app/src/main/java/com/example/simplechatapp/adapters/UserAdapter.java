package com.example.simplechatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.simplechatapp.R;
import com.example.simplechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UserAdapter extends BaseAdapter {

    private ArrayList<User> users;
    private Context context;

    private String myUid;


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

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        TextView bullet = convertView.findViewById(R.id.bult);
        ImageView ivProfile = convertView.findViewById(R.id.profileImage);

        User user = users.get(position);

        //tu gvari araaq carieli simbolo
        String name = (user.name != null) ? user.name : " ";
        String surname = (user.surname != null) ? user.surname : " ";
        tvName.setText(name + " " + surname);
        //tvName.setText(user.name);
        tvStatus.setText(user.status);


        String chatId = myUid.compareTo(user.uid) < 0 ? myUid + "_" + user.uid : user.uid + "_" + myUid;

        String imageUrl = (user.imageUrl != null && !user.imageUrl.isEmpty()) ? user.imageUrl : "@drawable/user";

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .circleCrop()
                .into(ivProfile);

        //clicki
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });


        //tu nanaxi ar gvaq bulleti iqneba usertan
        FirebaseDatabase.getInstance().getReference("chats")
                .child(chatId)
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        boolean unread = false;
                        for (com.google.firebase.database.DataSnapshot snap : snapshot.getChildren()) {
                            com.example.simplechatapp.models.Message lastMessage = snap.getValue(com.example.simplechatapp.models.Message.class);
                            if (lastMessage != null) {

                                if (!lastMessage.senderId.equals(myUid) && !lastMessage.seen) {
                                    unread = true;
                                }
                            }
                        }
                        bullet.setVisibility(unread ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
                });

        return convertView;
    }


    /*
    private static class ViewHolder {
        TextView tvName, tvStatus;
    }*/
}

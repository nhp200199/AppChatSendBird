package com.example.sendbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<FriendItem> friendItems;
    private ConversationAdapter.Listener listener;

    public FriendAdapter(Context mContext, ArrayList<FriendItem> friendItems) {
        this.mContext = mContext;
        this.friendItems = friendItems;
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.friend__item_vertical, parent, false);
        return new FriendAdapter.ViewHolder(linearLayout);
    }



    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, final int position) {
        LinearLayout linearLayout = holder.linearLayout;

        TextView tv_friendName = (TextView) linearLayout.findViewById(R.id.tv_friend_name);
        CircleImageView civ_friendImg = (CircleImageView) linearLayout.findViewById(R.id.civ_avatar);

        tv_friendName.setText(friendItems.get(position).getName());
        Glide.with(mContext)
                .load(friendItems.get(position).getAvatar())
                .into(civ_friendImg);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;

        ViewHolder(LinearLayout linearLayout) {
            super(linearLayout);

            this.linearLayout = linearLayout;
        }
    }
    public void setListener(ConversationAdapter.Listener listener){
        this.listener = listener;
    }
}

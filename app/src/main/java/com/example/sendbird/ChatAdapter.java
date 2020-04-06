package com.example.sendbird;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPEONE=1;
    public static final int TYPETWO=2;
    private Context mContext;
    private String userId;
    private ArrayList<ChatItem> chatItemArrayList;

    private Listener listener;

    interface Listener {
        void onClick(int position);
    }

    public ChatAdapter(Context context, ArrayList<ChatItem> messageArrayList, String userId) {
        this.mContext=context;
        this.chatItemArrayList=messageArrayList;
        this.userId =userId;

    }

    @Override
    public int getItemViewType(int position) {
        ChatItem item = chatItemArrayList.get(position);
        if (item.getFrom().equals(userId)){
            Log.d("message", String.valueOf(TYPEONE));
            return TYPEONE;
        }
        else{
            Log.d("message", String.valueOf(TYPETWO));
            return TYPETWO;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==TYPEONE) {
            RelativeLayout view= (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.my_message,parent,false);
            return new SentHolder(view);
        }
        else if(viewType==TYPETWO) {
            RelativeLayout view = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.their_message,parent,false);
            return new ReceivedHolder(view);
        }
        else {
            throw new RuntimeException("The type has to be SEND or RECEIVE");
        }

    }


    private void initLayoutSend(final SentHolder holder, ChatItem message) {
        RelativeLayout relativeLayout = holder.relativeLayout;
        TextView tv_message = relativeLayout.findViewById(R.id.tv_message);
        CardView cv_img_container = relativeLayout.findViewById(R.id.cv_img_container);
        ImageView img_message = relativeLayout.findViewById(R.id.img_message);
        cv_img_container.setVisibility(View.GONE);
        tv_message.setVisibility(View.GONE);

        String type = message.getType();
        if(type.equals("text")){
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText(message.getMessage());
        }
        else if(type.equals("image")){
            cv_img_container.setVisibility(View.VISIBLE);

            Glide.with(mContext)
                    .load(message.getMessage())
                    .thumbnail(0.5f)
                    .into(img_message);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onClick(holder.getAdapterPosition());
                }
            }
        });

    }

    private void initLayoutReceive(final ReceivedHolder holder, ChatItem message) {
        RelativeLayout relativeLayout = holder.relativeLayout;
        TextView tv_content = relativeLayout.findViewById(R.id.tv_message);
        TextView tv_user_name_small = relativeLayout.findViewById(R.id.tv_username_small);
        CircleImageView civ_avatar_small = relativeLayout.findViewById(R.id.civ_avatar_small);
        CardView cv_img_container = relativeLayout.findViewById(R.id.cv_img_container);
        ImageView img_message = relativeLayout.findViewById(R.id.img_message);

        cv_img_container.setVisibility(View.GONE);
        tv_content.setVisibility(View.GONE);

        String type = message.getType();
        if(type.equals("text")){
            tv_content.setVisibility(View.VISIBLE);
            tv_content.setText(message.getMessage());
        }
        else if(type.equals("image")){
            cv_img_container.setVisibility(View.VISIBLE);

            Glide.with(mContext)
                    .load(message.getMessage())
                    .thumbnail(0.5f)
                    .into(img_message);
        }
        tv_user_name_small.setText(message.getName());
        Glide.with(mContext)
                .load(message.getAvatar())
                .thumbnail(0.5f)
                .into(civ_avatar_small);

        tv_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onClick(holder.getAdapterPosition());
                }
            }
        });
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem message = chatItemArrayList.get(position);

        switch (holder.getItemViewType()) {
            case TYPEONE:
            {
                initLayoutSend((SentHolder) holder,message);
                break;
            }
            case TYPETWO:
            {
                initLayoutReceive((ReceivedHolder) holder,message);
                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return chatItemArrayList.size();
    }

    public static class SentHolder extends RecyclerView.ViewHolder {
        private RelativeLayout relativeLayout;

        public SentHolder(RelativeLayout itemView) {
            super(itemView);
            this.relativeLayout = itemView;
        }

    }

    public static class ReceivedHolder extends RecyclerView.ViewHolder {
        private RelativeLayout relativeLayout;
        ReceivedHolder(RelativeLayout itemView) {
            super(itemView);
            this.relativeLayout = itemView;
        }

    }
    public void setListener(Listener listener){
        this.listener = listener;
    }
}

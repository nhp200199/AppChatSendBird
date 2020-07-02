package com.example.sendbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<ConversationItem> conversationItems;
    private Listener listener;
    interface Listener {
        void onClick(int position);
    }

    public ConversationAdapter(Context mContext, ArrayList<ConversationItem> conversationItems) {
        this.mContext = mContext;
        this.conversationItems = conversationItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.conversation_item, parent, false);
        return new ViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        LinearLayout linearLayout = holder.linearLayout;
        TextView tv_conversationName = (TextView) linearLayout.findViewById(R.id.tv_conversation_name);
        TextView tv_conversationContent = (TextView) linearLayout.findViewById(R.id.tv_conversation_content);
        TextView tv_conversationTime = (TextView) linearLayout.findViewById(R.id.tv_conversation_time);
        CircleImageView civ_conversationImg = (CircleImageView) linearLayout.findViewById(R.id.civ_conversation_avatar);

        SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat out = new SimpleDateFormat("HH:mm");

        try {
            Date date= in.parse(conversationItems.get(position).getDate());
            tv_conversationTime.setText(out.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
        }

        tv_conversationContent.setText(conversationItems.get(position).getLastMessage());
        tv_conversationName.setText(conversationItems.get(position).getName());
        Glide.with(mContext)
                .load(conversationItems.get(position).getAvatar())
                .placeholder(R.drawable.couple)
                .thumbnail(0.5f)
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(civ_conversationImg);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return conversationItems.size();
    }

    public  static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;
        ViewHolder(LinearLayout view) {
            super(view);
            linearLayout = view;
        }

    }

    public void setListener(Listener listener){
        this.listener = listener;
    }
}

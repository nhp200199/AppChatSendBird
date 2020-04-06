package com.example.sendbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPEONE=1;
    public static final int TYPETWO=2;
    private ArrayList<RequestItem> requestItems;
    private Context mContext;
    public RequestAdapter(ArrayList<RequestItem> list, Context context)
    {
        this.requestItems = list;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        RequestItem item = requestItems.get(position);
        if(item.getType().equals("sent"))
            return TYPEONE;
        else return TYPETWO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPEONE){
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.request_received, parent, false);
            return new ReceivedHolder(linearLayout);
        }
        else if(viewType == TYPETWO)
        {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.resquest_sent, parent, false);
            return new SentHolder(linearLayout);
        }
        else {
            throw new RuntimeException("The type has to be SEND or RECEIVE");
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPEONE:
                initLayoutReceive((ReceivedHolder) holder, position);
                break;
            case TYPETWO:
                initLayoutSend((SentHolder) holder,position);
                break;
        }
    }

    private void initLayoutReceive(ReceivedHolder holder, int position) {
        LinearLayout linearLayout = holder.linearLayout;
        CircleImageView civ_avatar = linearLayout.findViewById(R.id.civ_avatar);
        TextView tv_userName = linearLayout.findViewById(R.id.tv_username);
        ImageView img_accept = linearLayout.findViewById(R.id.img_icon_accept);
        ImageView img_decline = linearLayout.findViewById(R.id.img_icon_decline);

        tv_userName.setText(requestItems.get(position).getName());
        Glide.with(mContext)
                .load(requestItems.get(position).getAvatar())
                .into(civ_avatar);

        img_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest();
            }
        });
        img_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineRequest();
            }
        });
    }

    private void declineRequest() {
    }

    private void acceptRequest() {
    }

    private void initLayoutSend(SentHolder holder, int position) {
        LinearLayout linearLayout = holder.linearLayout;
        CircleImageView civ_avatar = linearLayout.findViewById(R.id.civ_avatar);
        TextView tv_userName = linearLayout.findViewById(R.id.tv_username);
        Button btn_cancelRequest = linearLayout.findViewById(R.id.btn_cancel_request);

        tv_userName.setText(requestItems.get(position).getName());
        Glide.with(mContext)
                .load(requestItems.get(position).getAvatar())
                .into(civ_avatar);

        btn_cancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequest();
            }
        });
    }

    private void cancelRequest() {
    }

    @Override
    public int getItemCount() {
        return requestItems.size();
    }
    public static class SentHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;

        public SentHolder(LinearLayout itemView) {
            super(itemView);
            this.linearLayout = itemView;
        }

    }

    public static class ReceivedHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;
        ReceivedHolder(LinearLayout itemView) {
            super(itemView);
            this.linearLayout = itemView;
        }

    }
}

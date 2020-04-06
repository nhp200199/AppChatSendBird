package com.example.sendbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactItemAdapter extends ArrayAdapter<ContactItem> {

    private Context mContext;
    private List<ContactItem> items;

    public ContactItemAdapter(@NonNull Context context, int resource, @NonNull List<ContactItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.items = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_option, null,
                    false);
        }
         ContactItem item = getItem(position);
         TextView friendName = (TextView) convertView.findViewById(R.id.tv_option_name);
         CircleImageView friendAvatar = (CircleImageView) convertView.findViewById(R.id.civ_option_icon);
         friendName.setText(items.get(position).getName());
        Glide.with(mContext)
                .load(items.get(position).getAvatar())
                .placeholder(R.drawable.couple)
                .into(friendAvatar);

        return  convertView;
    }
}

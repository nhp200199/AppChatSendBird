package com.example.sendbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class OptionAdapter extends ArrayAdapter<OptionItem> {

    private Context mContext;


    public OptionAdapter(@NonNull Context context, int resource, @NonNull List<OptionItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_option, null,
                    false);
        }

        OptionItem option = getItem(position);
        TextView optionName = (TextView) convertView.findViewById(R.id.tv_option_name);
        CircleImageView optoinImg = (CircleImageView) convertView.findViewById(R.id.civ_option_icon);

        optionName.setText(option.getTitle_option());
        Glide.with(mContext)
                .load(option.getImg_option())
                .placeholder(R.drawable.couple)
                .into(optoinImg);
        return  convertView;
    }
}

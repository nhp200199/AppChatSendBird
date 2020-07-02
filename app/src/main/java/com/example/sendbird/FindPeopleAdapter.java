package com.example.sendbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindPeopleAdapter extends ArrayAdapter<OptionItem> {
    private List<OptionItem> friendListFull;
    private Context mContext;

    public FindPeopleAdapter(@NonNull Context context, @NonNull List<OptionItem> friendList) {
        super(context, 0, friendList);

        friendListFull = new ArrayList<>(friendList);
        mContext = context;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return friendFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_option, parent, false
            );
        }

        TextView textViewName = convertView.findViewById(R.id.tv_option_name);
        CircleImageView imageViewFlag = convertView.findViewById(R.id.civ_option_icon);

        OptionItem friendItem = getItem(position);

        if (friendItem != null) {
            textViewName.setText(friendItem.getTitle_option());

            Glide.with(mContext)
                    .load(friendItem.getImg_option())
                    .thumbnail(0.5f)
                    .placeholder(R.drawable.couple)
                    .into(imageViewFlag);

        }

        return convertView;
    }

    private Filter friendFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<OptionItem> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(friendListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (OptionItem item : friendListFull) {
                    if (item.getTitle_option().toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
                if (suggestions.size() ==0){
                    suggestions.add(new OptionItem(R.drawable.ic_logout, "Không tìm thấy ..."));
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

    };
}

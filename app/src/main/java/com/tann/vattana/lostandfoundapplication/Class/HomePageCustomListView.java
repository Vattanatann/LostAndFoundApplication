package com.tann.vattana.lostandfoundapplication.Class;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tann.vattana.lostandfoundapplication.R;

import java.util.ArrayList;

public class HomePageCustomListView extends BaseAdapter {

    private Context activity;
    private ArrayList<LostItemInfo> itemArrayList;

    public HomePageCustomListView(Context activity, ArrayList<LostItemInfo> itemArrayList) {
        this.activity = activity;
        this.itemArrayList = itemArrayList;
    }

    @Override
    public int getCount() {
        return itemArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewContainer {
        public ImageView itemImage;
        public TextView itemTitleTextView;
        public TextView itemPostDateTextView;
        public TextView itemFounderTextView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewContainer viewContainer;
        View row = convertView;

        //---print the index of the row to examine---
        Log.d("HomePageCustomListView",String.valueOf(position));
        if (row == null) {
            Log.d("HomePageCustomListView", "New");
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.custom_row_home_page, null);

            //---create a view container object---
            viewContainer = new ViewContainer();

            //---get the references to all the views in the row---
            viewContainer.itemTitleTextView = (TextView)
                    row.findViewById(R.id.itemTitle);
            viewContainer.itemPostDateTextView = (TextView)
                    row.findViewById(R.id.itemPostDate);
            viewContainer.itemFounderTextView = (TextView)
                    row.findViewById(R.id.itemFounder);
            viewContainer.itemImage = (ImageView)
                    row.findViewById(R.id.itemImage);

            //---assign the view container to the rowView---
            row.setTag(viewContainer);
        } else {

            //---view was previously created; can recycle---
            Log.d("CustomArrayAdapter", "Recycling");

            //---retrieve the previously assigned tag to get
            // a reference to all the views; bypass the findViewByID() process,
            // which is computationally expensive---
            viewContainer = (ViewContainer) row.getTag();
        }

        //---customize the content of each row based on position---
        final LostItemInfo lostItem = (LostItemInfo) getItem(position);

        viewContainer.itemTitleTextView.setText(lostItem.getItemTitle());
        viewContainer.itemPostDateTextView.setText(lostItem.getPostDate());
        viewContainer.itemFounderTextView.setText(lostItem.getFounder());
        Picasso.with(activity)
                .load(lostItem.getItemImageUrl())
                .into(viewContainer.itemImage);
        return row;
    }

}

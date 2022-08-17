package com.simba.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private JSONArray mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    RecyclerAdapter(Context context, JSONArray data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_view_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject jsonObject = mData.optJSONObject(position);
            String newsFrom = jsonObject.getString("source");
            String newsTimestamp = jsonObject.getString("published_at");
            String newsHeader = jsonObject.getString("title");
            String newsDescription = jsonObject.getString("description"); 
            String newsImg = jsonObject.getString("image");

            holder.newsFrom.setText(newsFrom);
            holder.newsTimestamp.setText(newsTimestamp.substring(0,10));
            holder.newsHeader.setText(newsHeader);
            holder.newsDescription.setText(newsDescription);
            Picasso.get().load(newsImg).into(holder.newsImg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
 
    @Override
    public int getItemCount() {
        return mData.length();
    }

 
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView newsFrom,newsTimestamp,newsHeader,newsDescription,newsAuthor;
        ImageView newsImg;

        ViewHolder(View itemView) {
            super(itemView);
            newsFrom = itemView.findViewById(R.id.newsFrom);
            newsTimestamp = itemView.findViewById(R.id.newsTimestamp);
            newsHeader = itemView.findViewById(R.id.newsHeader);
            newsDescription = itemView.findViewById(R.id.newsDescription);
            newsImg = itemView.findViewById(R.id.newsImg);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    JSONObject getItem(int id) {
        JSONObject jsonObject = mData.optJSONObject(id);
        return jsonObject;
    }

    void setObject(JSONArray array) {
        mData = array;
        notifyDataSetChanged();
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}

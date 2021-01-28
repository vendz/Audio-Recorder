package com.example.audiorecorder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {
    private File[] allFiles;
    private TimeAgo timeAgo;
    private onItemListClick onItemListClick;

    public AudioListAdapter(File[] allFiles, onItemListClick onItemListClick) {
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView list_image;
        private TextView list_filename;
        private TextView list_date;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            list_date = itemView.findViewById(R.id.list_date);
            list_filename = itemView.findViewById(R.id.list_filename);
            list_image = itemView.findViewById(R.id.list_image);

            itemView.setOnClickListener(this);
        }

        // onClickListener for all methods
        @Override
        public void onClick(View v) {
            // this way we will get where we clicked
            onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition());
        }
    }
    @NonNull
    @Override
    public AudioListAdapter.AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_item, parent, false);
        // this 'timeAgo' will set value of "how many seconds/minutes/hours/days ago was this file created"
        timeAgo = new TimeAgo();
        return new AudioViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioListAdapter.AudioViewHolder holder, int position) {

        holder.list_filename.setText(allFiles[position].getName());
        holder.list_date.setText(timeAgo.getTimeAgo(allFiles[position].lastModified()));
    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public interface onItemListClick{
        void onClickListener(File file, int position);
    }
}

package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.sg.nushigh.h1930006.anti_ncov.R;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private final List<News> news;

    public NewsAdapter(List<News> news) {
        this.news = news;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView body;
        public TextView date;

        public ViewHolder(@NonNull View itemView, TextView title, TextView body, TextView date) {
            super(itemView);
            this.title = title;
            this.body = body;
            this.date = date;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_news_element, parent, false);
        return new ViewHolder(view, view.findViewById(R.id.text_title), view.findViewById(R.id.text_body),
                view.findViewById(R.id.text_date));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News n = news.get(position);
        holder.title.setText(n.getTitle());
        holder.body.setText(n.getBody());
        holder.date.setText(n.getDate());
    }

    @Override
    public int getItemCount() {
        return news.size();
    }
}

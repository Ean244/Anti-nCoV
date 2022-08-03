package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.sg.nushigh.h1930006.anti_ncov.R;

public class NewsFragment extends Fragment {
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_news, container, false);

        RecyclerView recycler = root.findViewById(R.id.recycler_news);
        TextView loadingText = root.findViewById(R.id.text_loading);
        ProgressBar loadingProgress = root.findViewById(R.id.progress_loading);
        TextView errorText = root.findViewById(R.id.text_loading_error);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("news")
                .get()
                .addOnSuccessListener(task -> {
                    List<News> news = new ArrayList<>();
                    task.getDocuments().forEach(docs -> {
                        Map<String, Object> data = docs.getData();
                        News n = new News(data.get("title").toString(), data.get("body").toString(), data.get("date").toString());
                        news.add(n);
                    });

                    recycler.setAdapter(new NewsAdapter(news));
                    recycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    recycler.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    loadingProgress.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    errorText.setVisibility(View.VISIBLE);
                });

        return root;
    }
}
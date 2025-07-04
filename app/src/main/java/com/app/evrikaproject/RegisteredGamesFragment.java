package com.app.evrikaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class RegisteredGamesFragment extends Fragment {
    private RecyclerView recyclerView;
    private CompetitionAdapter adapter;
    private List<Competition> registeredCompetitions = new ArrayList<>();
    private List<String> registeredGames = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_competitions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        loadRegisteredGames();
        return view;
    }

    private void loadRegisteredGames() {
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                registeredGames = (List<String>) doc.get("registeredGames");
                if (registeredGames == null || registeredGames.isEmpty()) {
                    showEmpty();
                } else {
                    loadCompetitions();
                }
            });
    }

    private void loadCompetitions() {
        FirebaseFirestore.getInstance().collection("competitions")
            .whereIn(FieldPath.of("id"), registeredGames)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                registeredCompetitions.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Competition comp = doc.toObject(Competition.class);
                    registeredCompetitions.add(comp);
                }
                adapter = new CompetitionAdapter(getContext(), registeredCompetitions, new CompetitionAdapter.OnCompetitionClickListener() {
                    @Override
                    public void onJoin(Competition competition) {}
                    @Override
                    public void onRemove(Competition competition) {
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("registeredGames", FieldValue.arrayRemove(competition.id))
                            .addOnSuccessListener(aVoid -> loadRegisteredGames());
                    }
                    @Override
                    public void onViewDetails(Competition competition) {}
                }, userId, true);
                recyclerView.setAdapter(adapter);
            });
    }

    private void showEmpty() {
        recyclerView.setAdapter(null);
        if (getView() != null) {
            ((ViewGroup) recyclerView.getParent()).removeAllViews();
            TextView tv = new TextView(getContext());
            tv.setText("No registered competitions.");
            tv.setTextSize(18);
            ((ViewGroup) getView()).addView(tv);
        }
    }
} 
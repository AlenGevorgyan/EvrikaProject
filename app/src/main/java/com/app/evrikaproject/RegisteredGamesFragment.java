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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldPath;
import java.util.ArrayList;
import java.util.List;

public class RegisteredGamesFragment extends Fragment {
    private RecyclerView recyclerView;
    private CompetitionAdapter adapter;
    private List<Competition> registeredCompetitions = new ArrayList<>();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registered_games, container, false);
        recyclerView = view.findViewById(R.id.recycler_competitions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        // Initialize adapter first
        adapter = new CompetitionAdapter(getContext(), registeredCompetitions, new CompetitionAdapter.OnCompetitionClickListener() {
            @Override
            public void onJoin(Competition competition) {
                // Not applicable for registered games
            }
            @Override
            public void onRemove(Competition competition) {
                if (currentUserId != null && competition.posterId != null) {
                    FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                        .update("registeredGames", com.google.firebase.firestore.FieldValue.arrayRemove(competition.posterId))
                        .addOnSuccessListener(aVoid -> {
                            registeredCompetitions.remove(competition);
                            adapter.notifyDataSetChanged();
                            if (registeredCompetitions.isEmpty()) {
                                showEmpty();
                            }
                        });
                }
            }
            @Override
            public void onViewDetails(Competition competition) {
                // Handle view details if needed
            }
        }, currentUserId, true);
        recyclerView.setAdapter(adapter);
        
        loadRegisteredGames();
        return view;
    }

    private void loadRegisteredGames() {
        if (currentUserId == null) {
            showEmpty();
            return;
        }
        
        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    List<String> registeredGameIds = (List<String>) doc.get("registeredGames");
                    if (registeredGameIds == null || registeredGameIds.isEmpty()) {
                        showEmpty();
                    } else {
                        loadCompetitions(registeredGameIds);
                    }
                } else {
                    showEmpty();
                }
            })
            .addOnFailureListener(e -> {
                showEmpty();
            });
    }

    private void loadCompetitions(List<String> registeredGameIds) {
        if (registeredGameIds.isEmpty()) {
            showEmpty();
            return;
        }
        
        try {
            FirebaseFirestore.getInstance().collection("games")
                .whereIn(FieldPath.documentId(), registeredGameIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    registeredCompetitions.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Competition comp = doc.toObject(Competition.class);
                        if (comp != null) {
                            comp.compId = doc.getId(); // Set the document ID as compId
                            registeredCompetitions.add(comp);
                        }
                    }
                    
                    if (registeredCompetitions.isEmpty()) {
                        showEmpty();
                    } else {
                        adapter.setCompetitions(registeredCompetitions);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("RegisteredGamesFragment", "Error loading competitions: " + e.getMessage());
                    showEmpty();
                });
        } catch (Exception e) {
            android.util.Log.e("RegisteredGamesFragment", "Exception loading competitions: " + e.getMessage());
            showEmpty();
        }
    }

    private void showEmpty() {
        if (getView() != null) {
            ViewGroup parent = (ViewGroup) recyclerView.getParent();
            parent.removeAllViews();
            
            TextView tv = new TextView(getContext());
            tv.setText("No registered competitions found.");
            tv.setTextSize(18);
            tv.setPadding(32, 32, 32, 32);
            tv.setGravity(android.view.Gravity.CENTER);
            
            parent.addView(tv);
        }
    }
} 
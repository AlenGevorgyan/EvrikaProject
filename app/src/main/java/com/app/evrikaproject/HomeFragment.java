package com.app.evrikaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements CompetitionAdapter.OnCompetitionClickListener {
    private RecyclerView recyclerView;
    private CompetitionAdapter adapter;
    private List<Competition> competitions = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_competitions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CompetitionAdapter(getContext(), competitions, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadCompetitions();

        return view;
    }

    private void loadCompetitions() {
        db.collection("competitions").get()
            .addOnSuccessListener(this::onCompetitionsLoaded)
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load competitions", Toast.LENGTH_SHORT).show());
    }

    private void onCompetitionsLoaded(QuerySnapshot queryDocumentSnapshots) {
        competitions.clear();
        for (DocumentSnapshot doc : queryDocumentSnapshots) {
            Competition comp = doc.toObject(Competition.class);
            competitions.add(comp);
        }
        adapter.setCompetitions(competitions);
    }

    @Override
    public void onViewDetails(Competition competition) {
        Toast.makeText(getContext(), "Clicked: " + competition.name, Toast.LENGTH_SHORT).show();
        // TODO: Navigate to details fragment
    }
} 
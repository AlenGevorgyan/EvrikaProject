package com.app.evrikaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements CompetitionAdapter.OnCompetitionClickListener {
    private RecyclerView recyclerView;
    private CompetitionAdapter adapter;
    private List<Competition> competitions = new ArrayList<>();
    private List<Competition> allCompetitions = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_competitions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        adapter = new CompetitionAdapter(getContext(), competitions, new CompetitionAdapter.OnCompetitionClickListener() {
            @Override
            public void onJoin(Competition competition) {
                // TODO: Implement join logic
                Toast.makeText(getContext(), "Joined competition: " + competition.name, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onRemove(Competition competition) {
                // TODO: Implement remove logic
                Toast.makeText(getContext(), "Removed from competition: " + competition.name, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onViewDetails(Competition competition) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, CompetitionDetailsFragment.newInstance(competition.name))
                    .addToBackStack(null)
                    .commit();
            }
        }, userId, false);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadCompetitions();

        FloatingActionButton fab = view.findViewById(R.id.fab_create_competition);
        fab.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreateCompetitionFragment())
                .addToBackStack(null)
                .commit();
        });

        return view;
    }

    private void loadCompetitions() {
        db.collection("competitions").get()
            .addOnSuccessListener(this::onCompetitionsLoaded)
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load competitions", Toast.LENGTH_SHORT).show());
    }

    private void onCompetitionsLoaded(QuerySnapshot queryDocumentSnapshots) {
        competitions.clear();
        allCompetitions.clear();
        for (DocumentSnapshot doc : queryDocumentSnapshots) {
            Competition comp = doc.toObject(Competition.class);
            competitions.add(comp);
            allCompetitions.add(comp);
        }
        adapter.setCompetitions(competitions);
    }

    @Override
    public void onJoin(Competition competition) {

    }

    @Override
    public void onRemove(Competition competition) {

    }

    @Override
    public void onViewDetails(Competition competition) {
        // Navigate to a simple details fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, CompetitionDetailsFragment.newInstance(competition.name))
            .addToBackStack(null)
            .commit();
    }

    public void searchCompetitions(String query) {
        competitions.clear();
        for (Competition c : allCompetitions) {
            if (c.name != null && c.name.toLowerCase().contains(query.toLowerCase())) {
                competitions.add(c);
            }
        }
        adapter.setCompetitions(competitions);
    }

    // Simple details fragment for demonstration
    public static class CompetitionDetailsFragment extends Fragment {
        private static final String ARG_NAME = "name";
        public static CompetitionDetailsFragment newInstance(String name) {
            CompetitionDetailsFragment f = new CompetitionDetailsFragment();
            Bundle b = new Bundle();
            b.putString(ARG_NAME, name);
            f.setArguments(b);
            return f;
        }
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32, 32, 32, 32);
            String name = getArguments() != null ? getArguments().getString(ARG_NAME) : "";
            TextView tv = new TextView(getContext());
            tv.setText("Competition: " + name);
            tv.setTextSize(24);
            layout.addView(tv);
            return layout;
        }
    }
} 
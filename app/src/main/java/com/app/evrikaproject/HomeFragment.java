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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import androidx.core.content.ContextCompat;

public class HomeFragment extends Fragment implements CompetitionAdapter.OnCompetitionClickListener {
    private RecyclerView recyclerView;
    private CompetitionAdapter adapter;
    private MaterialButton btnFilterAll, btnFilterFootball, btnFilterBasketball, btnFilterVolleyball;
    private List<Competition> competitions = new ArrayList<>();
    private List<Competition> allCompetitions = new ArrayList<>();
    private FirebaseFirestore db;
    private List<String> registeredGames = new ArrayList<>();
    private String selectedSport = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_competitions);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        adapter = new CompetitionAdapter(getContext(), competitions, new CompetitionAdapter.OnCompetitionClickListener() {
            @Override
            public void onJoin(Competition competition) {
                if (currentUserId == null) return;
                FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                    .update("registeredGames", FieldValue.arrayUnion(competition.posterId))
                    .addOnSuccessListener(aVoid -> {
                        registeredGames.add(competition.posterId);
                        adapter.setRegisteredGameIds(registeredGames);
                        Toast.makeText(getContext(), "Joined competition: " + competition.game_name, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onRemove(Competition competition) {
                if (currentUserId == null) return;
                FirebaseFirestore.getInstance().collection("users").document(currentUserId)
                    .update("registeredGames", FieldValue.arrayRemove(competition.posterId))
                    .addOnSuccessListener(aVoid -> {
                        registeredGames.remove(competition.posterId);
                        adapter.setRegisteredGameIds(registeredGames);
                        Toast.makeText(getContext(), "Left competition: " + competition.game_name, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to leave: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onViewDetails(Competition competition) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, CompetitionDetailsFragment.newInstance(competition.game_name))
                    .addToBackStack(null)
                    .commit();
            }
        }, currentUserId, false);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadUserRegisteredGames(currentUserId);

        FloatingActionButton fab = view.findViewById(R.id.fab_create_competition);
        fab.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreateCompetitionFragment())
                .addToBackStack(null)
                .commit();
        });

        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterFootball = view.findViewById(R.id.btn_filter_football);
        btnFilterBasketball = view.findViewById(R.id.btn_filter_basketball);
        btnFilterVolleyball = view.findViewById(R.id.btn_filter_volleyball);

        btnFilterAll.setOnClickListener(v -> setSportFilter("All"));
        btnFilterFootball.setOnClickListener(v -> setSportFilter("football"));
        btnFilterBasketball.setOnClickListener(v -> setSportFilter("basketball"));
        btnFilterVolleyball.setOnClickListener(v -> setSportFilter("volleyball"));
        setSportFilter("All");

        return view;
    }

    private void loadUserRegisteredGames(String userId) {
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                registeredGames = (List<String>) doc.get("registeredGames");
                if (registeredGames == null) registeredGames = new ArrayList<>();
                android.util.Log.d("HomeFragment", "Loaded registered games: " + registeredGames.size());
                adapter.setRegisteredGameIds(registeredGames);
                loadCompetitions();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("HomeFragment", "Error loading registered games: " + e.getMessage());
                registeredGames = new ArrayList<>();
                adapter.setRegisteredGameIds(registeredGames);
                loadCompetitions();
            });
    }

    private void loadCompetitions() {
        db.collection("games").get()
            .addOnSuccessListener(this::onCompetitionsLoaded)
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load competitions", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh competitions when returning to the fragment
        loadCompetitions();
    }

    private void onCompetitionsLoaded(QuerySnapshot queryDocumentSnapshots) {
        competitions.clear();
        allCompetitions.clear();
        for (DocumentSnapshot doc : queryDocumentSnapshots) {
            Competition comp = doc.toObject(Competition.class);
            if (comp != null) {
                comp.compId = doc.getId(); // Set the document ID as compId
                competitions.add(comp);
                allCompetitions.add(comp);
            }
        }
        adapter.setCompetitions(competitions);
        adapter.setRegisteredGameIds(registeredGames);
    }

    @Override
    public void onJoin(Competition competition) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("registeredGames", FieldValue.arrayUnion(competition.posterId))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Joined competition: " + competition.game_name, Toast.LENGTH_SHORT).show();
                // Refresh the list
                loadUserRegisteredGames(userId);
            })
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRemove(Competition competition) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("registeredGames", FieldValue.arrayRemove(competition.posterId))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Left competition: " + competition.game_name, Toast.LENGTH_SHORT).show();
                // Refresh the list
                loadUserRegisteredGames(userId);
            })
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to leave: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onViewDetails(Competition competition) {
        // Navigate to a simple details fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, CompetitionDetailsFragment.newInstance(competition.game_name))
            .addToBackStack(null)
            .commit();
    }

    public void searchCompetitions(String query) {
        competitions.clear();
        for (Competition c : allCompetitions) {
            if (c.game_name != null && c.game_name.toLowerCase().contains(query.toLowerCase())) {
                competitions.add(c);
            }
        }
        adapter.setCompetitions(competitions);
    }

    private void setSportFilter(String sport) {
        selectedSport = sport;
        btnFilterAll.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), sport.equals("All") ? R.color.colorPrimary : R.color.colorSecondary));
        btnFilterAll.setTextColor(ContextCompat.getColor(requireContext(), sport.equals("All") ? android.R.color.white : R.color.colorPrimary));
        btnFilterFootball.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), sport.equals("football") ? R.color.colorPrimary : R.color.colorSecondary));
        btnFilterFootball.setTextColor(ContextCompat.getColor(requireContext(), sport.equals("football") ? android.R.color.white : R.color.colorPrimary));
        btnFilterBasketball.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), sport.equals("basketball") ? R.color.colorPrimary : R.color.colorSecondary));
        btnFilterBasketball.setTextColor(ContextCompat.getColor(requireContext(), sport.equals("basketball") ? android.R.color.white : R.color.colorPrimary));
        btnFilterVolleyball.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), sport.equals("volleyball") ? R.color.colorPrimary : R.color.colorSecondary));
        btnFilterVolleyball.setTextColor(ContextCompat.getColor(requireContext(), sport.equals("volleyball") ? android.R.color.white : R.color.colorPrimary));
        filterCompetitionsBySport();
    }

    private void filterCompetitionsBySport() {
        competitions.clear();
        if (selectedSport.equals("All")) {
            competitions.addAll(allCompetitions);
        } else {
            for (Competition c : allCompetitions) {
                if (c.sport != null && c.sport.equalsIgnoreCase(selectedSport)) {
                    competitions.add(c);
                }
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
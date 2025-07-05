package com.app.evrikaproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;
import android.widget.LinearLayout;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FieldValue;
import android.widget.ImageButton;
import android.content.Intent;

public class ProfileFragment extends Fragment {
    private ImageView profileImage;
    private TextView tvUsername, tvRealName, tvGender, tvAge;
    private Button btnPosted, btnRegistered;
    private FrameLayout competitionsContainer;
    private String userId;
    private List<String> registeredGames = new ArrayList<>();
    private List<Competition> registeredCompetitions = new ArrayList<>();
    private CompetitionAdapter registeredAdapter;
    private List<Competition> postedCompetitions = new ArrayList<>();
    private CompetitionAdapter postedAdapter;
    private ImageButton btnSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profile_image);
        tvUsername = view.findViewById(R.id.tv_username);
        tvRealName = view.findViewById(R.id.tv_real_name);
        tvGender = view.findViewById(R.id.tv_gender);
        tvAge = view.findViewById(R.id.tv_age);
        btnPosted = view.findViewById(R.id.btn_posted_competitions);
        btnRegistered = view.findViewById(R.id.btn_registered_competitions);
        competitionsContainer = view.findViewById(R.id.profile_competitions_container);
        btnSettings = view.findViewById(R.id.btn_settings);

        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(this::onUserLoaded);
        }

        btnPosted.setOnClickListener(v -> {
            setButtonSelected(true);
            showPostedCompetitions();
        });
        btnRegistered.setOnClickListener(v -> {
            setButtonSelected(false);
            loadUserRegisteredGames();
            // Add a small delay to ensure registered games are loaded
            new android.os.Handler().postDelayed(() -> {
                showRegisteredCompetitions();
            }, 500);
        });
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainSettingsActivity.class);
            startActivity(intent);
        });
        // Show posted competitions by default
        setButtonSelected(true);
        showPostedCompetitions();
        return view;
    }

    private void onUserLoaded(DocumentSnapshot doc) {
        if (doc.exists()) {
            String username = doc.getString("username");
            String realName = doc.getString("name");
            String realSurname = doc.getString("surname");
            String gender = doc.getString("gender");
            Long ageLong = doc.getLong("age");
            String age = ageLong != null ? "Age: " + String.valueOf(ageLong) : "";
            tvUsername.setText(username != null ? username : "N/A");
            String fullName = ((realName != null ? realName : "N/A") + " " + (realSurname != null ? realSurname : "")).trim();
            tvRealName.setText(!fullName.trim().isEmpty() ? fullName : "N/A");
            tvGender.setText(gender != null ? "Gender: " + gender : "N/A");
            tvAge.setText(age);
            registeredGames = (List<String>) doc.get("registeredGames");
            if (registeredGames == null) registeredGames = new ArrayList<>();
        }
    }

    private void loadUserRegisteredGames() {
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    registeredGames = (List<String>) doc.get("registeredGames");
                    if (registeredGames == null) registeredGames = new ArrayList<>();
                    android.util.Log.d("ProfileFragment", "Loaded registered games: " + registeredGames.size());
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("ProfileFragment", "Error loading registered games: " + e.getMessage());
                registeredGames = new ArrayList<>();
            });
    }

    private void showPostedCompetitions() {
        FirebaseFirestore.getInstance().collection("games")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                postedCompetitions.clear();
                // Create a list of posted game IDs to pass to adapter
                List<String> postedGameIds = new ArrayList<>();
                
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Competition comp = doc.toObject(Competition.class);
                    if (comp != null) {
                        comp.posterId = doc.getId(); // Set the document ID as posterId
                        postedCompetitions.add(comp);
                        postedGameIds.add(doc.getId()); // Add to posted game IDs list
                    }
                }
                
                RecyclerView recyclerView = new RecyclerView(getContext());
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                postedAdapter = new CompetitionAdapter(getContext(), postedCompetitions, new CompetitionAdapter.OnCompetitionClickListener() {
                    @Override
                    public void onJoin(Competition competition) {}
                    @Override
                    public void onRemove(Competition competition) {}
                    @Override
                    public void onViewDetails(Competition competition) {}
                }, userId, true); // Set to true so posted games show as registered
                
                // Set the posted game IDs so adapter knows these are games the user is part of
                postedAdapter.setRegisteredGameIds(postedGameIds);
                
                competitionsContainer.removeAllViews();
                recyclerView.setAdapter(postedAdapter);
                competitionsContainer.addView(recyclerView);
            });
    }

    private void showRegisteredCompetitions() {
        if (registeredGames == null || registeredGames.isEmpty()) {
            competitionsContainer.removeAllViews();
            TextView tv = new TextView(getContext());
            tv.setText("No registered competitions.");
            tv.setTextSize(18);
            competitionsContainer.addView(tv);
            return;
        }
        
        // Add error handling and debugging
        try {
            FirebaseFirestore.getInstance().collection("games")
                .whereIn(FieldPath.documentId(), registeredGames)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    registeredCompetitions.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Competition comp = doc.toObject(Competition.class);
                        if (comp != null) {
                            comp.posterId = doc.getId(); // Set the document ID as posterId
                            registeredCompetitions.add(comp);
                        }
                    }
                    RecyclerView recyclerView = new RecyclerView(getContext());
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    registeredAdapter = new CompetitionAdapter(getContext(), registeredCompetitions, new CompetitionAdapter.OnCompetitionClickListener() {
                        @Override
                        public void onJoin(Competition competition) {}
                        @Override
                        public void onRemove(Competition competition) {
                            if (userId == null) return;
                            FirebaseFirestore.getInstance().collection("users").document(userId)
                                .update("registeredGames", FieldValue.arrayRemove(competition.posterId))
                                .addOnSuccessListener(aVoid -> {
                                    // Refresh the registered games list
                                    loadUserRegisteredGames();
                                    showRegisteredCompetitions();
                                });
                        }
                        @Override
                        public void onViewDetails(Competition competition) {}
                    }, userId, true);
                    recyclerView.setAdapter(registeredAdapter);
                    competitionsContainer.removeAllViews();
                    competitionsContainer.addView(recyclerView);
                })
                .addOnFailureListener(e -> {
                    // Show error message
                    competitionsContainer.removeAllViews();
                    TextView tv = new TextView(getContext());
                    tv.setText("Error loading competitions: " + e.getMessage());
                    tv.setTextSize(18);
                    competitionsContainer.addView(tv);
                });
        } catch (Exception e) {
            // Show error message
            competitionsContainer.removeAllViews();
            TextView tv = new TextView(getContext());
            tv.setText("Error: " + e.getMessage());
            tv.setTextSize(18);
            competitionsContainer.addView(tv);
        }
    }

    private void setButtonSelected(boolean postedSelected) {
        if (postedSelected) {
            btnPosted.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            btnPosted.setTextColor(getResources().getColor(android.R.color.white));
            btnRegistered.setBackgroundTintList(getResources().getColorStateList(R.color.colorSecondary));
            btnRegistered.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            btnRegistered.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            btnRegistered.setTextColor(getResources().getColor(android.R.color.white));
            btnPosted.setBackgroundTintList(getResources().getColorStateList(R.color.colorSecondary));
            btnPosted.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }
} 
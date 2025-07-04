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
    private TextView tvUsername, tvRealName, tvRealSurname, tvGender, tvAge;
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
        tvRealSurname = view.findViewById(R.id.tv_real_surname);
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
            showRegisteredCompetitions();
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
            String realName = doc.getString("realName");
            String realSurname = doc.getString("realSurname");
            String gender = doc.getString("gender");
            Long ageLong = doc.getLong("age");
            String age = ageLong != null ? "Age: " + String.valueOf(ageLong) : "";
            tvUsername.setText(username != null ? username : "");
            tvRealName.setText(realName != null ? realName : "");
            tvRealSurname.setText(realSurname != null ? realSurname : "");
            tvGender.setText(gender != null ? "Gender: " + gender : "");
            tvAge.setText(age);
            registeredGames = (List<String>) doc.get("registeredGames");
            if (registeredGames == null) registeredGames = new ArrayList<>();
        }
    }

    private void showPostedCompetitions() {
        FirebaseFirestore.getInstance().collection("competitions")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                postedCompetitions.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Competition comp = doc.toObject(Competition.class);
                    postedCompetitions.add(comp);
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
                }, userId, false);
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
        FirebaseFirestore.getInstance().collection("competitions")
            .whereIn(FieldPath.of("id"), registeredGames)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                registeredCompetitions.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Competition comp = doc.toObject(Competition.class);
                    registeredCompetitions.add(comp);
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
                            .update("registeredGames", FieldValue.arrayRemove(competition.id))
                            .addOnSuccessListener(aVoid -> showRegisteredCompetitions());
                    }
                    @Override
                    public void onViewDetails(Competition competition) {}
                }, userId, true);
                recyclerView.setAdapter(registeredAdapter);
                competitionsContainer.removeAllViews();
                competitionsContainer.addView(recyclerView);
            });
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
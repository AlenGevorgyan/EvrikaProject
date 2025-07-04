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

public class ProfileFragment extends Fragment {
    private ImageView profileImage;
    private TextView tvUsername, tvRealName, tvRealSurname;
    private Button btnPosted, btnRegistered;
    private FrameLayout competitionsContainer;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profile_image);
        tvUsername = view.findViewById(R.id.tv_username);
        tvRealName = view.findViewById(R.id.tv_real_name);
        tvRealSurname = view.findViewById(R.id.tv_real_surname);
        btnPosted = view.findViewById(R.id.btn_posted_competitions);
        btnRegistered = view.findViewById(R.id.btn_registered_competitions);
        competitionsContainer = view.findViewById(R.id.profile_competitions_container);

        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(this::onUserLoaded);
        }

        btnPosted.setOnClickListener(v -> showPostedCompetitions());
        btnRegistered.setOnClickListener(v -> showRegisteredCompetitions());
        // Show posted competitions by default
        showPostedCompetitions();
        return view;
    }

    private void onUserLoaded(DocumentSnapshot doc) {
        if (doc.exists()) {
            String username = doc.getString("username");
            String realName = doc.getString("realName");
            String realSurname = doc.getString("realSurname");
            tvUsername.setText(username != null ? username : "");
            tvRealName.setText(realName != null ? realName : "");
            tvRealSurname.setText(realSurname != null ? realSurname : "");
            // TODO: Load user image if available
        }
    }

    private void showPostedCompetitions() {
        FirebaseFirestore.getInstance().collection("competitions")
            .whereEqualTo("createdBy", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                competitionsContainer.removeAllViews();
                LinearLayout list = new LinearLayout(getContext());
                list.setOrientation(LinearLayout.VERTICAL);
                if (queryDocumentSnapshots.isEmpty()) {
                    TextView tv = new TextView(getContext());
                    tv.setText("No posted competitions.");
                    tv.setTextSize(18);
                    list.addView(tv);
                } else {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        TextView tv = new TextView(getContext());
                        tv.setText(name != null ? name : "Unnamed Competition");
                        tv.setTextSize(18);
                        list.addView(tv);
                    }
                }
                competitionsContainer.addView(list);
            });
    }

    private void showRegisteredCompetitions() {
        FirebaseFirestore.getInstance().collection("competitions")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                competitionsContainer.removeAllViews();
                LinearLayout list = new LinearLayout(getContext());
                list.setOrientation(LinearLayout.VERTICAL);
                boolean found = false;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    List<String> teams = (List<String>) doc.get("teams");
                    if (teams != null && teams.contains(userId)) {
                        String name = doc.getString("name");
                        TextView tv = new TextView(getContext());
                        tv.setText(name != null ? name : "Unnamed Competition");
                        tv.setTextSize(18);
                        list.addView(tv);
                        found = true;
                    }
                }
                if (!found) {
                    TextView tv = new TextView(getContext());
                    tv.setText("No registered competitions.");
                    tv.setTextSize(18);
                    list.addView(tv);
                }
                competitionsContainer.addView(list);
            });
    }
} 
package com.app.evrikaproject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.widget.ImageButton;
import androidx.cardview.widget.CardView;

public class EditCompetitionActivity extends AppCompatActivity {
    private EditText etName, etDate, etTime, etTeamPlayerCount;
    private AutoCompleteTextView spinnerSport;
    private MaterialButton btnSave, btnDelete;
    private String competitionId;
    private Competition competition;
    private FirebaseFirestore db;
    private String currentUserId;
    private Calendar calendar;
    private LinearLayout requestsContainer;
    private MaterialButton btnPublic, btnPrivate;
    private String selectedType = "public";
    private TextView tvLocation;
    private MaterialButton btnPickLocation;
    private double latitude = 0, longitude = 0;
    private CardView requestsCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_competition);

        competitionId = getIntent().getStringExtra("competition_id");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                       FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (competitionId == null || currentUserId == null) {
            Toast.makeText(this, "Error: Missing competition data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();
        initializeViews();
        loadCompetition();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                android.util.Log.d("EditCompetitionActivity", "onBackPressed called");
                finish();
            }
        });
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Competition");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            // Add click listener to toolbar navigation icon as backup
            toolbar.setNavigationOnClickListener(v -> {
                android.util.Log.d("EditCompetitionActivity", "Toolbar navigation clicked");
                finish();
            });
        }
        
        etName = findViewById(R.id.et_name);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etTeamPlayerCount = findViewById(R.id.et_team_player_count);
        spinnerSport = findViewById(R.id.spinner_sport);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        requestsContainer = findViewById(R.id.requests_container);
        btnPublic = findViewById(R.id.btn_public);
        btnPrivate = findViewById(R.id.btn_private);
        tvLocation = findViewById(R.id.tv_location);
        btnPickLocation = findViewById(R.id.btn_pick_location);
        requestsCard = findViewById(R.id.request_card);

        // Setup spinners
        ArrayAdapter<String> sportAdapter = new ArrayAdapter<>(this, R.layout.item_dropdown_black, new String[]{"football", "basketball", "volleyball"});
        sportAdapter.setDropDownViewResource(R.layout.item_dropdown_black);
        spinnerSport.setAdapter(sportAdapter);
        spinnerSport.setTextColor(getResources().getColor(android.R.color.black));

        // Setup date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Setup time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Setup buttons
        btnSave.setOnClickListener(v -> saveCompetition());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            startActivityForResult(intent, 1023);
        });

        btnPublic.setOnClickListener(v -> {
            selectedType = "public";
            btnPublic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            btnPublic.setTextColor(getResources().getColor(android.R.color.white));
            btnPrivate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.gray)));
            btnPrivate.setTextColor(getResources().getColor(android.R.color.black));
            if (requestsCard != null) requestsCard.setVisibility(View.GONE);
        });
        btnPrivate.setOnClickListener(v -> {
            selectedType = "private";
            btnPrivate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            btnPrivate.setTextColor(getResources().getColor(android.R.color.white));
            btnPublic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.gray)));
            btnPublic.setTextColor(getResources().getColor(android.R.color.black));
            if (requestsCard != null && competition != null && currentUserId.equals(competition.getCreatedBy())) requestsCard.setVisibility(View.VISIBLE);
        });
    }

    private void loadCompetition() {
        db.collection("games").document(competitionId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    competition = documentSnapshot.toObject(Competition.class);
                    if (competition != null) {
                        // Check if current user is the host
                        if (!currentUserId.equals(competition.getCreatedBy())) {
                            Toast.makeText(this, "Only the host can edit this competition", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // Populate fields
                        etName.setText(competition.getGame_name());
                        etDate.setText(competition.getDate());
                        etTime.setText(competition.getTime());
                        etTeamPlayerCount.setText(String.valueOf(competition.getTeamPlayerCount()));
                        latitude = competition.getLatitude();
                        longitude = competition.getLongitude();
                        tvLocation.setText("Location: Lat: " + latitude + ", Lng: " + longitude);

                        // Set spinner selections
                        String sport = competition.getSport();
                        if (sport != null) {
                            spinnerSport.setText(sport, false);
                        }

                        // Set button selection for type
                        String type = competition.getType();
                        if (type != null) {
                            if (type.equals("public")) {
                                selectedType = "public";
                                btnPublic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                                btnPublic.setTextColor(getResources().getColor(android.R.color.white));
                                btnPrivate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                                btnPrivate.setTextColor(getResources().getColor(android.R.color.black));
                                if (requestsCard != null) requestsCard.setVisibility(View.GONE);
                            } else if (type.equals("private")) {
                                selectedType = "private";
                                btnPrivate.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                                btnPrivate.setTextColor(getResources().getColor(android.R.color.white));
                                btnPublic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.gray)));
                                btnPublic.setTextColor(getResources().getColor(android.R.color.black));
                                if (requestsCard != null && currentUserId.equals(competition.getCreatedBy())) requestsCard.setVisibility(View.VISIBLE);
                            }
                        }

                        // Show/hide requests card based on type
                        if (requestsCard != null) {
                            if ("private".equals(competition.getType()) && currentUserId.equals(competition.getCreatedBy())) {
                                requestsCard.setVisibility(View.VISIBLE);
                                showRequests();
                            } else {
                                requestsCard.setVisibility(View.GONE);
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Competition not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading competition: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                updateTimeLabel();
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        etTime.setText(sdf.format(calendar.getTime()));
    }

    private void saveCompetition() {
        String name = etName.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String teamPlayerCountStr = etTeamPlayerCount.getText().toString().trim();
        String sport = spinnerSport.getText().toString();
        String type = selectedType;

        // Validation
        if (name.isEmpty() || date.isEmpty() || time.isEmpty() || teamPlayerCountStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int teamPlayerCount;
        try {
            teamPlayerCount = Integer.parseInt(teamPlayerCountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid team player count", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update competition
        competition.setGame_name(name);
        competition.setDate(date);
        competition.setTime(time);
        competition.setTeamPlayerCount(teamPlayerCount);
        competition.setSport(sport);
        competition.setType(type);
        competition.setLatitude(latitude);
        competition.setLongitude(longitude);

        db.collection("games").document(competitionId).set(competition)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Competition updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update competition: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showDeleteConfirmation() {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Delete Competition")
            .setMessage("Are you sure you want to delete this competition? This action cannot be undone.")
            .setPositiveButton("Delete", (d, which) -> deleteCompetition())
            .setNegativeButton("Cancel", null)
            .create();
        dialog.show();
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) messageView.setTextColor(getResources().getColor(android.R.color.black));
        int titleId = getResources().getIdentifier("alertTitle", "id", "android");
        TextView titleView = dialog.findViewById(titleId);
        if (titleView != null) titleView.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void deleteCompetition() {
        // Delete the competition
        db.collection("games").document(competitionId).delete()
            .addOnSuccessListener(aVoid -> {
                // Also delete the chat room
                db.collection("chat_rooms").document(competitionId).delete()
                    .addOnSuccessListener(aVoid2 -> {
                        Toast.makeText(this, "Competition deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Competition deleted but failed to delete chat room", Toast.LENGTH_SHORT).show();
                        finish();
                    });
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to delete competition: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @SuppressLint("ResourceAsColor")
    private void showRequests() {
        requestsContainer.removeAllViews();
        if (competition.getRequests() == null || competition.getRequests().isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No join requests");
            requestsContainer.addView(tv);
            return;
        }
        for (String requesterId : competition.getRequests()) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv = new TextView(this);
            // Fetch username from Firestore
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(requesterId).get()
                .addOnSuccessListener(userDoc -> {
                    String realName = userDoc.getString("name");
                    String realSurname = userDoc.getString("surname");
                    String displayName = (realName != null ? realName : "") + (realSurname != null ? " " + realSurname : "");
                    tv.setText(!displayName.trim().isEmpty() ? displayName.trim() : requesterId);
                })
                .addOnFailureListener(e -> tv.setText(requesterId));
            // Add margin to the right of the username
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.setMargins(0, 0, 16, 0); // 16px right margin
            tv.setLayoutParams(tvParams);
            // Accept button as ImageButton
            ImageButton btnAccept = new ImageButton(this);
            btnAccept.setImageResource(R.drawable.ic_check);
            btnAccept.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnAccept.setColorFilter(getResources().getColor(R.color.colorPrimary));
            btnAccept.setPadding(3, 3, 3, 3);
            btnAccept.setContentDescription("Accept");
            // Reject button as ImageButton
            ImageButton btnReject = new ImageButton(this);
            btnReject.setImageResource(R.drawable.ic_cancel);
            btnReject.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnReject.setColorFilter(getResources().getColor(R.color.colorPrimary));
            btnReject.setPadding(3, 3, 3, 3);
            btnReject.setContentDescription("Reject");
            // Set equal weight for buttons
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            btnAccept.setLayoutParams(params);
            btnReject.setLayoutParams(params);
            row.addView(tv);
            row.addView(btnAccept);
            row.addView(btnReject);
            requestsContainer.addView(row);
            btnAccept.setOnClickListener(v -> handleAcceptRequest(requesterId));
            btnReject.setOnClickListener(v -> handleRejectRequest(requesterId));
        }
    }

    private void handleAcceptRequest(String requesterId) {
        db.collection("games").document(competitionId)
            .update("requests", com.google.firebase.firestore.FieldValue.arrayRemove(requesterId),
                    "teams", com.google.firebase.firestore.FieldValue.arrayUnion(requesterId))
            .addOnSuccessListener(aVoid -> {
                // Update in-memory arrays
                if (competition.getRequests() != null) competition.getRequests().remove(requesterId);
                if (competition.getTeams() == null) competition.setTeams(new java.util.ArrayList<>());
                if (!competition.getTeams().contains(requesterId)) competition.getTeams().add(requesterId);
                showRequests();
                Toast.makeText(this, "Accepted request", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to accept: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleRejectRequest(String requesterId) {
        db.collection("games").document(competitionId)
            .update("requests", com.google.firebase.firestore.FieldValue.arrayRemove(requesterId))
            .addOnSuccessListener(aVoid -> {
                // Update in-memory array
                if (competition.getRequests() != null) competition.getRequests().remove(requesterId);
                showRequests();
                Toast.makeText(this, "Rejected request", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            android.util.Log.d("EditCompetitionActivity", "Back button pressed");
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1023 && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("lat", 0);
            longitude = data.getDoubleExtra("lng", 0);
            tvLocation.setText("Location: Lat: " + latitude + ", Lng: " + longitude);
        }
    }
} 
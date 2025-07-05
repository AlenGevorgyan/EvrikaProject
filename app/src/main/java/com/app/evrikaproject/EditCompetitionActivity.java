package com.app.evrikaproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
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

public class EditCompetitionActivity extends AppCompatActivity {
    private EditText etName, etDate, etTime, etTeamPlayerCount;
    private Spinner spinnerSport, spinnerType;
    private MaterialButton btnSave, btnDelete;
    private String competitionId;
    private Competition competition;
    private FirebaseFirestore db;
    private String currentUserId;
    private Calendar calendar;

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
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
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

        etName = findViewById(R.id.et_name);
        etDate = findViewById(R.id.et_date);
        etTime = findViewById(R.id.et_time);
        etTeamPlayerCount = findViewById(R.id.et_team_player_count);
        spinnerSport = findViewById(R.id.spinner_sport);
        spinnerType = findViewById(R.id.spinner_type);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);

        // Setup spinners
        String[] sports = {"football", "basketball", "volleyball"};
        ArrayAdapter<String> sportAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sports);
        sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSport.setAdapter(sportAdapter);

        String[] types = {"public", "private"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        // Setup date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Setup time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Setup buttons
        btnSave.setOnClickListener(v -> saveCompetition());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
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
                        etName.setText(competition.getName());
                        etDate.setText(competition.getDate());
                        etTime.setText(competition.getTime());
                        etTeamPlayerCount.setText(String.valueOf(competition.getTeamPlayerCount()));

                        // Set spinner selections
                        String sport = competition.getSport();
                        if (sport != null) {
                            for (int i = 0; i < spinnerSport.getCount(); i++) {
                                if (spinnerSport.getItemAtPosition(i).equals(sport)) {
                                    spinnerSport.setSelection(i);
                                    break;
                                }
                            }
                        }

                        String type = competition.getType();
                        if (type != null) {
                            for (int i = 0; i < spinnerType.getCount(); i++) {
                                if (spinnerType.getItemAtPosition(i).equals(type)) {
                                    spinnerType.setSelection(i);
                                    break;
                                }
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
        String sport = spinnerSport.getSelectedItem().toString();
        String type = spinnerType.getSelectedItem().toString();

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
        competition.setName(name);
        competition.setDate(date);
        competition.setTime(time);
        competition.setTeamPlayerCount(teamPlayerCount);
        competition.setSport(sport);
        competition.setType(type);

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
        new AlertDialog.Builder(this)
            .setTitle("Delete Competition")
            .setMessage("Are you sure you want to delete this competition? This action cannot be undone.")
            .setPositiveButton("Delete", (dialog, which) -> deleteCompetition())
            .setNegativeButton("Cancel", null)
            .show();
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
    public void onBackPressed() {
        android.util.Log.d("EditCompetitionActivity", "onBackPressed called");
        super.onBackPressed();
    }
} 
package com.app.evrikaproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CreateCompetitionFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_LOCATION_REQUEST = 102;
    private ImageView ivMainImage;
    private MaterialButton btnPickImage, btnPickDate, btnPickTime, btnCreate, btnPickLocation;
    private EditText etName, etTeamPlayerCount;
    private Spinner spinnerSport;
    private RadioGroup rgType;
    private TextView tvLocation, tvDate, tvTime;
    private Uri imageUri;
    private String selectedDate = "";
    private String selectedTime = "";
    private double selectedLat = 0, selectedLng = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_create_competition, container, false);
        etName = view.findViewById(R.id.et_competition_name);
        spinnerSport = view.findViewById(R.id.spinner_sport);
        rgType = view.findViewById(R.id.rg_type);
        etTeamPlayerCount = view.findViewById(R.id.et_team_player_count);
        tvLocation = view.findViewById(R.id.tv_location);
        btnPickDate = view.findViewById(R.id.btn_pick_date);
        tvDate = view.findViewById(R.id.tv_date);
        btnPickTime = view.findViewById(R.id.btn_pick_time);
        tvTime = view.findViewById(R.id.tv_time);
        btnCreate = view.findViewById(R.id.btn_create_competition);
        btnPickLocation = view.findViewById(R.id.btn_pick_location);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"football", "basketball", "volleyball"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSport.setAdapter(adapter);

        btnPickDate.setOnClickListener(v -> pickDate());
        btnPickTime.setOnClickListener(v -> pickTime());
        btnCreate.setOnClickListener(v -> createCompetition());
        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapPickerActivity.class);
            startActivityForResult(intent, PICK_LOCATION_REQUEST);
        });

        return view;
    }

    private void pickDate() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                R.style.MyDatePickerDialogTheme,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    tvDate.setText(selectedDate);
                    tvDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void pickTime() {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                R.style.MyTimePickerDialogTheme, // Use your custom style, or android.R.style.Theme_Holo_Light_Dialog_NoActionBar
                (view, hourOfDay, minute) -> {
                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    tvTime.setText(selectedTime);
                    tvTime.setError(null);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // is24HourView
        );
        timePickerDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == requireActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Set your image to ivMainImage here if you use it
        } else if (requestCode == PICK_LOCATION_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0);
            selectedLng = data.getDoubleExtra("lng", 0);
            tvLocation.setText("Lat: " + selectedLat + ", Lng: " + selectedLng);
        }
    }

    private void createCompetition() {
        String name = etName.getText().toString().trim();
        String sport = spinnerSport.getSelectedItem().toString();
        int checkedId = rgType.getCheckedRadioButtonId();
        String type = checkedId == R.id.rb_public ? "public" : checkedId == R.id.rb_private ? "private" : "";
        String teamPlayerCountStr = etTeamPlayerCount.getText().toString().trim();
        int teamPlayerCount = teamPlayerCountStr.isEmpty() ? 0 : Integer.parseInt(teamPlayerCountStr);

        boolean hasError = false;
        if (name.isEmpty()) {
            etName.setError("Enter a name");
            hasError = true;
        }
        if (teamPlayerCount <= 0) {
            etTeamPlayerCount.setError("Enter a valid team player count");
            hasError = true;
        }
        if (selectedDate.isEmpty()) {
            tvDate.setError("Pick a date");
            hasError = true;
        }
        if (selectedTime.isEmpty()) {
            tvTime.setError("Pick a time");
            hasError = true;
        }
        if (type.isEmpty()) {
            Toast.makeText(getContext(), "Select competition type", Toast.LENGTH_SHORT).show();
            hasError = true;
        }
        if (selectedLat == 0 && selectedLng == 0) {
            selectedLat = 40.1776;
            selectedLng = 44.5126;
            tvLocation.setText("Lat: 40.1776, Lng: 44.5126");
        }
        if (hasError) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "anonymous";
        String imageUrl = imageUri != null ? imageUri.toString() : "";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String compId = db.collection("games").document().getId();
        List<String> initialPlayers = new ArrayList<>();
        initialPlayers.add(userId);

        Map<String, Object> competitionData = new HashMap<>();
        competitionData.put("compId", compId);
        competitionData.put("name", name);
        competitionData.put("sport", sport);
        competitionData.put("type", type);
        competitionData.put("posterId", userId);
        competitionData.put("createdBy", userId);
        competitionData.put("teams", initialPlayers);
        competitionData.put("invitedTeams", new ArrayList<>());
        competitionData.put("imageUrl", imageUrl);
        competitionData.put("teamPlayerCount", teamPlayerCount);
        competitionData.put("latitude", selectedLat);
        competitionData.put("longitude", selectedLng);
        competitionData.put("date", selectedDate);
        competitionData.put("time", selectedTime);

        CollectionReference reference = FirebaseFirestore.getInstance().collection("games");

        reference.document(compId).set(competitionData)
                .addOnCompleteListener(toFirebase -> {
                    if (toFirebase.isSuccessful()) {
                        Log.d("Firestore", "Document uploaded successfully");
                        // Add the host to their own registered games
                        addHostToOwnGame(compId, userId);
                        // Create chat room for the competition
                        createChatRoom(compId, name, userId);
                    } else {
                        Log.e("Firestore", "Error uploading document: ");
                    }
                });
    }

    private void addHostToOwnGame(String compId, String userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("registeredGames", FieldValue.arrayUnion(compId))
            .addOnSuccessListener(aVoid -> {
                Log.d("Firestore", "Host added to own game successfully");
            })
            .addOnFailureListener(e -> {
                Log.e("Firestore", "Failed to add host to own game: " + e.getMessage());
            });
    }

    private void createChatRoom(String compId, String competitionName, String userId) {
        List<String> participantIds = new ArrayList<>();
        participantIds.add(userId);
        
        Map<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put("roomId", compId);
        chatRoomData.put("competitionId", compId);
        chatRoomData.put("competitionName", competitionName);
        chatRoomData.put("participantIds", participantIds);
        chatRoomData.put("lastMessage", "");
        chatRoomData.put("lastMessageTime", null);
        
        FirebaseFirestore.getInstance().collection("chat_rooms").document(compId)
            .set(chatRoomData)
            .addOnSuccessListener(aVoid -> {
                Log.d("Firestore", "Chat room created successfully");
            })
            .addOnFailureListener(e -> {
                Log.e("Firestore", "Failed to create chat room: " + e.getMessage());
            });
    }
}
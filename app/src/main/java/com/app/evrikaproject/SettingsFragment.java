package com.app.evrikaproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 101;
    private ImageView ivProfileImage;
    private EditText etUsername, etRealName, etRealSurname, etGender, etAge;
    private Button btnSave, btnPickImage;
    private Uri imageUri;
    private String userId;
    private ImageButton btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        etUsername = view.findViewById(R.id.et_username);
        etRealName = view.findViewById(R.id.et_real_name);
        etRealSurname = view.findViewById(R.id.et_real_surname);
        etGender = view.findViewById(R.id.et_gender);
        etAge = view.findViewById(R.id.et_age);
        btnSave = view.findViewById(R.id.btn_save);
        btnPickImage = view.findViewById(R.id.btn_pick_image);
        btnBack = view.findViewById(R.id.btn_back);
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        loadUserInfo();
        btnPickImage.setOnClickListener(v -> pickImage());
        btnSave.setOnClickListener(v -> saveUserInfo());
        btnBack.setOnClickListener(v -> requireActivity().finish());
        return view;
    }

    private void loadUserInfo() {
        if (userId == null) return;
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener(doc -> {
                etUsername.setText(doc.getString("username"));
                etRealName.setText(doc.getString("realName"));
                etRealSurname.setText(doc.getString("realSurname"));
                etGender.setText(doc.getString("gender"));
                Long ageLong = doc.getLong("age");
                etAge.setText(ageLong != null ? String.valueOf(ageLong) : "");
                // TODO: Load image from URL if available
            });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                ivProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveUserInfo() {
        if (userId == null) return;
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", etUsername.getText().toString().trim());
        updates.put("realName", etRealName.getText().toString().trim());
        updates.put("realSurname", etRealSurname.getText().toString().trim());
        updates.put("gender", etGender.getText().toString().trim());
        try {
            updates.put("age", Integer.parseInt(etAge.getText().toString().trim()));
        } catch (NumberFormatException ignored) {}
        // TODO: Upload image to storage and save URL if imageUri != null
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
} 
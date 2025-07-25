package com.app.evrikaproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText etRealName, etRealSurname, etEmail, etPassword, etConfirmPassword, etAge, etGender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRealName = findViewById(R.id.name);
        etRealSurname = findViewById(R.id.surname);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirmPassword);
        etAge = findViewById(R.id.age);
        etGender = findViewById(R.id.gender);

        etGender.setOnClickListener(v -> showGenderDialog());

        findViewById(R.id.registerBtn).setOnClickListener(v -> registerUser());
        
        // Add click listener for "Click to Login" TextView
        findViewById(R.id.loginNow).setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    private void showGenderDialog() {
        final String[] genders = {"Male", "Female", "Other"};
        new AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(genders, (dialog, which) -> etGender.setText(genders[which]))
            .show();
    }

    private void registerUser() {
        String realName = etRealName.getText().toString().trim();
        String realSurname = etRealSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();

        // Basic validation
        if (realName.isEmpty() || realSurname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || ageStr.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }
        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
                        firebaseUser.sendEmailVerification()
                            .addOnCompleteListener(verifyTask -> {
                                if (verifyTask.isSuccessful()) {
                                    String uid = firebaseUser.getUid();
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("name", realName);
                                    map.put("surname", realSurname);
                                    map.put("uid", uid);
                                    map.put("age", age);
                                    map.put("gender", gender);
                                    map.put("email", email);
                                    map.put("profileImage", "");
                                    map.put("registeredGames", new ArrayList<String>());
                                    FirebaseFirestore.getInstance().collection("users").document(uid).set(map)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Registration successful! Please verify your email.", Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(this, LoginOtp.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                } else {
                                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                }
                            });
                    }
                } else {
                    Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
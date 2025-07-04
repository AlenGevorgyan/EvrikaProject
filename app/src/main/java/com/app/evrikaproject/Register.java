package com.app.evrikaproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class Register extends AppCompatActivity {
    private EditText etUsername, etRealName, etRealSurname, etEmail, etPassword, etConfirmPassword, etAge, etGender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.username);
        etRealName = findViewById(R.id.name);
        etRealSurname = findViewById(R.id.surname);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirmPassword);
        etAge = findViewById(R.id.age);
        etGender = findViewById(R.id.gender);

        etGender.setOnClickListener(v -> showGenderDialog());

        findViewById(R.id.registerNow).setOnClickListener(v -> registerUser());
    }

    private void showGenderDialog() {
        final String[] genders = {"Male", "Female", "Other"};
        new AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setItems(genders, (dialog, which) -> etGender.setText(genders[which]))
            .show();
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String realName = etRealName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String realSurname = etRealSurname.getText().toString().trim();

        // Basic validation
        if (username.isEmpty() || realName.isEmpty() || realSurname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || ageStr.isEmpty() || gender.isEmpty()) {
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
                                    User user = new User(uid, username, realName, realSurname, email, age, gender);
                                    FirebaseFirestore.getInstance().collection("users").document(uid).set(user)
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

    public static class User {
        public String uid;
        public String username;
        public String realName;
        public String realSurname;
        public String email;
        public int age;
        public String gender;
        public List<String> registeredGames;
        public User() {}
        public User(String uid, String username, String realName, String realSurname, String email, int age, String gender) {
            this.uid = uid;
            this.username = username;
            this.realSurname = realSurname;
            this.realName = realName;
            this.email = email;
            this.age = age;
            this.gender = gender;
            this.registeredGames = new ArrayList<>();
        }
    }
}
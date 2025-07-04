package com.app.evrikaproject;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.app.evrikaproject.User;

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword, editTextUsername, editTextConfirmPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextUsername = findViewById(R.id.username);
        editTextConfirmPassword = findViewById(R.id.confirmPassword);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                String username = editTextUsername.getText().toString().trim();

                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(Register.this, "Username required", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Email required", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Password required", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(Register.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Check if username exists
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "Username already taken", Toast.LENGTH_SHORT).show();
                        } else {
                            // Username is unique, proceed with registration
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                                String uid = firebaseUser.getUid();
                                                User user = new User(email, "", username);
                                                ref.child(uid).setValue(user);
                                                sendVerificationEmail(firebaseUser);
                                            } else {
                                                Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Register.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Register.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Register.this, LoginOtp.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.e(TAG, "sendEmailVerification", task.getException());
                                Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}

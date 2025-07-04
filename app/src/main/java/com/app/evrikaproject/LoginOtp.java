package com.app.evrikaproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.evrikaproject.MainActivity;
import com.app.evrikaproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginOtp extends AppCompatActivity {
    MaterialButton verifyOtpButton, resendLinkButton;
    FirebaseAuth mAuth;
    TextView otpInstructionText, timerText;
    private static final long RESEND_COOLDOWN = 60000; // 60 seconds
    private CountDownTimer countDownTimer;
    private boolean isResendEnabled = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        verifyOtpButton = findViewById(R.id.btn_verify_otp);
        resendLinkButton = findViewById(R.id.resendLinkButton);
        otpInstructionText = findViewById(R.id.otpInstructionText);
        timerText = findViewById(R.id.timerText);
        mAuth = FirebaseAuth.getInstance();

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    user.reload().addOnCompleteListener(task -> {
                        if (user.isEmailVerified()) {
                            Toast.makeText(LoginOtp.this, "Email verification successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginOtp.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginOtp.this, "Email not verified. Please check your email.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(LoginOtp.this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        resendLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isResendEnabled) {
                    return;
                }

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    resendLinkButton.setEnabled(false);
                    isResendEnabled = false;
                    startResendCooldown();

                    user.sendEmailVerification().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginOtp.this, "Verification Email Resent", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(LoginOtp.this, "Failed to resend verification email: " + errorMessage, Toast.LENGTH_SHORT).show();

                            resendLinkButton.setEnabled(true);
                            isResendEnabled = true;
                            timerText.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Toast.makeText(LoginOtp.this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startResendCooldown() {
        timerText.setVisibility(View.VISIBLE);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(RESEND_COOLDOWN, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerText.setText("Resend available in: " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                resendLinkButton.setEnabled(true);
                isResendEnabled = true;
                timerText.setVisibility(View.GONE);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
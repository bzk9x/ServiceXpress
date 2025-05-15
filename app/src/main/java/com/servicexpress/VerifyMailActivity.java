package com.servicexpress;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerifyMailActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog progressDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_mail);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize progress dialog using MaterialAlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View progressView = getLayoutInflater().inflate(R.layout.progress_dialog, null);
        builder.setView(progressView);
        builder.setCancelable(false);
        progressDialog = builder.create();

        // Initialize views
        TextView messageText = findViewById(R.id.message);
        Button verifyButton = findViewById(R.id.is_verified);
        TextView resendButton = findViewById(R.id.resend_mail);

        // Set user's email in message
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            messageText.setText("We've sent a verification link to " + user.getEmail());
        }

        // Setup verify button click listener
        verifyButton.setOnClickListener(v -> checkEmailVerification());

        // Setup resend verification email button
        resendButton.setOnClickListener(v -> resendVerificationEmail());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void checkEmailVerification() {
        // Show progress dialog
        progressDialog.show();
        
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        // Update user's verification status in Firestore
                        db.collection("users")
                            .document(user.getUid())
                            .update("emailVerified", true)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                // Navigate to MainActivity
                                Intent intent = new Intent(VerifyMailActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(VerifyMailActivity.this,
                                    "Error updating verification status",
                                    Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(VerifyMailActivity.this,
                            "Please verify your email first",
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                }
            });
        }
    }
    
    private void resendVerificationEmail() {
        // Show progress dialog
        progressDialog.show();
        
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifyMailActivity.this,
                            "Verification email sent",
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VerifyMailActivity.this,
                            "Failed to send verification email",
                            Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
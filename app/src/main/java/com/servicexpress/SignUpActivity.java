package com.servicexpress;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import androidx.appcompat.app.AlertDialog;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog progressDialog;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        
        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize progress dialog using MaterialAlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View progressView = getLayoutInflater().inflate(R.layout.progress_dialog, null);
        builder.setView(progressView);
        builder.setCancelable(false);
        progressDialog = builder.create();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.back).setOnClickListener(v -> finish());

        Button signUp = findViewById(R.id.sign_up_button);
        EditText firstName = findViewById(R.id.first_name);
        EditText lastName = findViewById(R.id.last_name);
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.password);

        // Regex for name validation (only letters and spaces allowed)
        String nameRegex = "^[a-zA-Z\\s]*$";
        // Regex for email validation
        String emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        signUp.setOnClickListener(v -> {
            String firstNameInput = firstName.getText().toString().trim();
            String lastNameInput = lastName.getText().toString().trim();
            String emailInput = email.getText().toString().trim();
            String passwordInput = password.getText().toString().trim();

            // Validate first name
            if (firstNameInput.isEmpty() || !firstNameInput.matches(nameRegex)) {
                Toast.makeText(this, "First name should only contain letters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate last name
            if (lastNameInput.isEmpty() || !lastNameInput.matches(nameRegex)) {
                Toast.makeText(this, "Last name should only contain letters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate email
            if (!emailInput.matches(emailRegex)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password length
            if (passwordInput.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            // All validations passed
            // TODO: Implement sign up logic

            // Show progress dialog
            progressDialog.show();

            // Create user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Get the user ID and send verification email
                            FirebaseUser user = Objects.requireNonNull(mAuth.getCurrentUser());
                            user.sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        // Get the user ID
                                        String userId = user.getUid();
                            
                                        // Create user data map
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("firstName", firstNameInput);
                                        userData.put("lastName", lastNameInput);
                                        userData.put("email", emailInput);
                                        
                                        // Add timestamps
                                        Date now = new Date();
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                        timeFormat.setTimeZone(TimeZone.getDefault());
                                        
                                        userData.put("dateCreated", dateFormat.format(now));
                                        userData.put("timeCreated", timeFormat.format(now));
                                        
                                        // Device information
                                        userData.put("deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                                        userData.put("deviceModel", Build.MODEL);
                                        userData.put("deviceManufacturer", Build.MANUFACTURER);
                                        userData.put("androidVersion", Build.VERSION.RELEASE);
                                        userData.put("sdkVersion", Build.VERSION.SDK_INT);

                                        // Store user data in Firestore
                                        db.collection("users")
                                                .document(userId)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    progressDialog.dismiss();
                                                    // Start VerifyMailActivity
                                                    Intent intent = new Intent(SignUpActivity.this, VerifyMailActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(SignUpActivity.this,
                                                            "Error saving user data: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(SignUpActivity.this,
                                                "Failed to send verification email: " + Objects.requireNonNull(emailTask.getException()).getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            // If sign up fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, 
                                    "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
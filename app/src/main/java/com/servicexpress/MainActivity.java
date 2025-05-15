package com.servicexpress;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get current Firebase user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            // User is signed in
            if (currentUser.isEmailVerified()) {
                // Email is verified, go to Home
                Intent homeActivity = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(homeActivity);
            } else {
                // Email not verified, go to verification
                Intent verifyMailActivity = new Intent(MainActivity.this, VerifyMailActivity.class);
                startActivity(verifyMailActivity);
            }
        } else {
            // No user signed in, go to Onboarding
            Intent onboardingActivity = new Intent(MainActivity.this, OnboardingActivity.class);
            startActivity(onboardingActivity);
        }
        
        finish();
    }
}
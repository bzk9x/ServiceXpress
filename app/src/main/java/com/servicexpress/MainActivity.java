package com.servicexpress;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent onboardingActivity = new Intent(MainActivity.this, OnboardingActivity.class);
        startActivity(onboardingActivity);
        finish();
    }
}
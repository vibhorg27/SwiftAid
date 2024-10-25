package com.example.sos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mLogin, mRegistration;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        // Initialize Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();

        // Auth state listener to redirect user if already logged in
        firebaseAuthStateListener = firebaseAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                startActivity(intent);
                finish();
            }
        };

        // Initialize views
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.login);
        mRegistration = findViewById(R.id.registration);
        mProgressBar = findViewById(R.id.progressBar);  // New ProgressBar

        // Set onClick listeners for buttons
        mRegistration.setOnClickListener(v -> registerUser());
        mLogin.setOnClickListener(v -> loginUser());

        // Disable buttons if fields are empty
        setupTextListeners();
    }

    // Register new user
    private void registerUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            showProgress(true);  // Show loading indicator
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        showProgress(false);  // Hide loading indicator
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            DatabaseReference currentUserDb = FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child("Customers")
                                    .child(userId);
                            currentUserDb.setValue(true);
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Login existing user
    private void loginUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (validateInputs(email, password)) {
            showProgress(true);  // Show loading indicator
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        showProgress(false);  // Hide loading indicator
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Validate input fields
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is required");
            return false;
        }
        return true;
    }

    // Show or hide progress bar
    private void showProgress(boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mLogin.setEnabled(!show);
        mRegistration.setEnabled(!show);
    }

    // Disable buttons until both fields are filled
    private void setupTextListeners() {
        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Enable or disable buttons based on input
    private void updateButtonState() {
        boolean enable = !TextUtils.isEmpty(mEmail.getText().toString().trim()) &&
                !TextUtils.isEmpty(mPassword.getText().toString().trim());
        mLogin.setEnabled(enable);
        mRegistration.setEnabled(enable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthStateListener != null) {
            mAuth.removeAuthStateListener(firebaseAuthStateListener);
        }
    }
}
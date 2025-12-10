package com.example.ramkrsmama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvToRegister;

    private FirebaseAuth auth;
    private ProgressDialog loading;

    @Override
    protected void onStart() {
        super.onStart();

        // Jika user sudah login, langsung masuk dashboard
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initFirebase();
        initActions();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvToRegister = findViewById(R.id.tvToRegister);

        loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage("Memproses...");
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
    }

    private void initActions() {

        tvToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (!validateInput(email, password)) return;

            loading.show();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        loading.dismiss();
                        Toast.makeText(this, "Login sukses!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        loading.dismiss();
                        Toast.makeText(this, "Login gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });
    }

    private boolean validateInput(String email, String password) {

        if (email.isEmpty()) {
            edtEmail.setError("Email wajib diisi");
            edtEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Format email tidak valid");
            edtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Password wajib diisi");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Minimal 6 karakter");
            edtPassword.requestFocus();
            return false;
        }

        return true;
    }
}

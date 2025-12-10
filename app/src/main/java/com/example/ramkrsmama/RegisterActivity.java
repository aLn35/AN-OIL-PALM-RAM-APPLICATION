package com.example.ramkrsmama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword, edtConfirmPassword, edtPhone;
    private Button btnRegister;
    private TextView tvToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initFirebase();
        initActions();
    }

    private void initViews() {
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPhone = findViewById(R.id.edtPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvToLogin = findViewById(R.id.tvToLogin);

        loading = new ProgressDialog(this);
        loading.setMessage("Memproses...");
        loading.setCancelable(false);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initActions() {

        tvToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if (!validateInput(name, email, password, confirmPassword, phone)) return;

            registerUser(name, email, password, phone);
        });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword, String phone) {

        if (name.isEmpty()) {
            edtName.setError("Nama wajib diisi");
            edtName.requestFocus();
            return false;
        }

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

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Password tidak sama");
            edtConfirmPassword.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            edtPhone.setError("Nomor telepon wajib diisi");
            edtPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String name, String email, String password, String phone) {

        loading.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    saveUserData(uid, name, email, phone);

                })
                .addOnFailureListener(e -> {
                    loading.dismiss();
                    Toast.makeText(this, "Gagal daftar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveUserData(String uid, String name, String email, String phone) {

        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(unused -> {
                    loading.dismiss();
                    Toast.makeText(this, "Registrasi sukses!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    loading.dismiss();
                    Toast.makeText(this, "Gagal simpan user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

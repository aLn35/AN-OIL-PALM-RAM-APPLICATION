package com.example.ramkrsmama;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddEditCustomerActivity extends AppCompatActivity {

    private EditText edtNama, edtTelp, edtAlamat, edtKebun, edtCatatan;
    private Button btnSimpan;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private String uid;
    private Customer editingCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_customer);

        initViews();
        initFirebase();

        // jika mode EDIT
        if (getIntent().hasExtra("customer")) {
            editingCustomer = (Customer) getIntent().getSerializableExtra("customer");
            fillData();
        }

        btnSimpan.setOnClickListener(v -> saveCustomer());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        edtNama = findViewById(R.id.edtNama);
        edtTelp = findViewById(R.id.edtTelp);
        edtAlamat = findViewById(R.id.edtAlamat);
        edtKebun = findViewById(R.id.edtKebun);
        edtCatatan = findViewById(R.id.edtCatatan);

        btnSimpan = findViewById(R.id.btnSimpan);
        btnBack = findViewById(R.id.btnBack);   // <-- FIX WAJIB
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
    }

    private void fillData() {
        edtNama.setText(editingCustomer.nama);
        edtTelp.setText(editingCustomer.telepon);
        edtAlamat.setText(editingCustomer.alamat);
        edtKebun.setText(editingCustomer.kebun);
        edtCatatan.setText(editingCustomer.catatan);
    }

    private void saveCustomer() {
        String nama = edtNama.getText().toString().trim();
        String telp = edtTelp.getText().toString().trim();
        String alamat = edtAlamat.getText().toString().trim();
        String kebun = edtKebun.getText().toString().trim();
        String catatan = edtCatatan.getText().toString().trim();

        if (nama.isEmpty()) {
            edtNama.setError("Nama wajib diisi");
            return;
        }

        Customer c = editingCustomer != null ? editingCustomer : new Customer();

        c.nama = nama;
        c.telepon = telp;
        c.alamat = alamat;
        c.kebun = kebun;
        c.catatan = catatan;

        if (c.createdAt == 0) c.createdAt = System.currentTimeMillis();

        // MODE TAMBAH
        if (editingCustomer == null) {

            db.collection("users").document(uid)
                    .collection("customers")
                    .add(c.toMap())
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Pelanggan ditambahkan", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());

        }
        // MODE EDIT
        else {
            db.collection("users").document(uid)
                    .collection("customers")
                    .document(editingCustomer.id)
                    .set(c.toMap())
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Pelanggan diperbarui", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}

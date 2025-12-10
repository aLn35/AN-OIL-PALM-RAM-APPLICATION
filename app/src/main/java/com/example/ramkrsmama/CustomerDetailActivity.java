package com.example.ramkrsmama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomerDetailActivity extends AppCompatActivity {

    private TextView tvNama, tvPhone, tvLokasi, tvCatatan, tvTotalUtang, tvToggleHistory;
    private Button btnKelola, btnEdit, btnHapus;
    private RecyclerView rvTransaksi;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String uid;

    private Customer customer;
    private final List<DebtTransaction> txList = new ArrayList<>();
    private DebtTransactionAdapter adapter;

    private boolean historyVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        customer = (Customer) getIntent().getSerializableExtra("customer");
        if (customer == null) {
            Toast.makeText(this, "Data pelanggan tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        fillProfile();
        setupRecycler();
        loadTransactions();

        btnKelola.setOnClickListener(v -> {
            Intent i = new Intent(this, DebtPaymentActivity.class);
            i.putExtra("customer", customer);
            startActivity(i);
        });

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditCustomerActivity.class);
            i.putExtra("customer", customer);
            startActivity(i);
        });

        btnHapus.setOnClickListener(v -> confirmDelete());

        tvToggleHistory.setOnClickListener(v -> toggleHistory());
    }

    private void initViews() {
        tvNama = findViewById(R.id.tvNama);
        tvPhone = findViewById(R.id.tvPhone);
        tvLokasi = findViewById(R.id.tvLokasi);
        tvCatatan = findViewById(R.id.tvCatatan);
        tvTotalUtang = findViewById(R.id.tvTotalUtang);
        tvToggleHistory = findViewById(R.id.tvToggleHistory);

        btnKelola = findViewById(R.id.btnKelolaUtang);
        btnEdit = findViewById(R.id.btnEdit);
        btnHapus = findViewById(R.id.btnHapus);

        rvTransaksi = findViewById(R.id.rvTransaksi);
        progressBar = findViewById(R.id.progressBar);
    }

    private void fillProfile() {
        tvNama.setText(customer.nama);
        tvPhone.setText(customer.telepon);

        String lokasi = "";
        if (customer.alamat != null && !customer.alamat.trim().isEmpty())
            lokasi += customer.alamat;

        if (customer.kebun != null && !customer.kebun.trim().isEmpty()) {
            if (!lokasi.isEmpty()) lokasi += " • ";
            lokasi += customer.kebun;
        }
        if (lokasi.isEmpty()) lokasi = "-";

        tvLokasi.setText(lokasi);
        tvCatatan.setText(customer.catatan == null || customer.catatan.isEmpty() ? "-" : customer.catatan);
        updateTotalUtangLabel();
    }

    private void updateTotalUtangLabel() {
        tvTotalUtang.setText(
                String.format(Locale.getDefault(), "Rp %,.0f", customer.totalUtang)
        );
    }

    private void setupRecycler() {
        rvTransaksi.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DebtTransactionAdapter(txList);
        rvTransaksi.setAdapter(adapter);
    }

    private void loadTransactions() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(uid)
                .collection("customers").document(customer.id)
                .collection("debtTransactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, error) -> {

                    progressBar.setVisibility(View.GONE);

                    if (error != null) return;

                    txList.clear();
                    double total = 0;

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {

                            DebtTransaction t = d.toObject(DebtTransaction.class);
                            if (t == null) continue;

                            t.id = d.getId();
                            txList.add(t);

                            if ("ADD".equals(t.type)) total += t.amount;
                            if ("PAY".equals(t.type) || "NOTE_PAY".equals(t.type))
                                total -= t.amount;
                        }
                    }

                    if (total < 0) total = 0;

                    customer.totalUtang = total;

                    db.collection("users").document(uid)
                            .collection("customers")
                            .document(customer.id)
                            .update("totalUtang", total);

                    updateTotalUtangLabel();
                    adapter.notifyDataSetChanged();
                });
    }

    private void toggleHistory() {
        historyVisible = !historyVisible;
        rvTransaksi.setVisibility(historyVisible ? View.VISIBLE : View.GONE);
        tvToggleHistory.setText(historyVisible ? "Sembunyikan Riwayat" : "Tampilkan Riwayat");
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Pelanggan")
                .setMessage("Yakin hapus pelanggan beserta semua riwayat utangnya?")
                .setPositiveButton("Hapus", (d, w) -> deleteCustomer())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteCustomer() {

        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(uid)
                .collection("customers").document(customer.id)
                .collection("debtTransactions")
                .get()
                .addOnSuccessListener(snap -> {

                    for (DocumentSnapshot d : snap)
                        d.getReference().delete();

                    db.collection("users").document(uid)
                            .collection("customers")
                            .document(customer.id)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Pelanggan dihapus!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
    }
}

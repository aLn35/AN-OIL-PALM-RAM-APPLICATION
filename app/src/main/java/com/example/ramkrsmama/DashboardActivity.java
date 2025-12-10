package com.example.ramkrsmama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvIncome, tvExpense, tvProfit, tvDebt;
    private TextView tvGreeting;
    private TextView tabTransaksi, tabUtang;
    private LinearLayout containerList;

    private ImageView btnLogout;

    private FirebaseFirestore db;
    private String uid;

    private double totalPendapatan = 0;
    private double totalPengeluaran = 0;
    private double totalUtang = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        initFirebase();
        initBottomNav();

        loadUserName();
        loadPendapatanDanPengeluaran();
        loadUtangPelanggan();

        tabTransaksi.setOnClickListener(v -> showTransaksiTerbaru());
        tabUtang.setOnClickListener(v -> showDaftarUtang());

        btnLogout.setOnClickListener(v -> logout());
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);

        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvProfit = findViewById(R.id.tvProfit);
        tvDebt = findViewById(R.id.tvDebt);

        tabTransaksi = findViewById(R.id.tabTransaksi);
        tabUtang = findViewById(R.id.tabUtang);
        containerList = findViewById(R.id.containerList);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Toast.makeText(this, "Anda belum login!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    // ======================================================
    // 🟢 GREETING / SAPAAN USER
    // ======================================================
    private void loadUserName() {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(d -> {
                    String nama = d.getString("nama");
                    if (nama == null || nama.trim().isEmpty())
                        nama = "Pengguna";

                    tvGreeting.setText("Hai, " + nama + " 👋");
                });
    }

    // ======================================================
    // 🔵 LOGOUT SYSTEM
    // ======================================================
    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Tidak", null)
                .show();
    }

    // ======================================================
    // 🔻 BOTTOM NAV
    // ======================================================
    private void initBottomNav() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navNota = findViewById(R.id.navNota);
        LinearLayout navCustomer = findViewById(R.id.navCustomer);

        if (navHome == null) return;

        navHome.setSelected(true);

        navHome.setOnClickListener(v -> {});
        navNota.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateNotaActivity.class));
            overridePendingTransition(0, 0);
        });
        navCustomer.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerListActivity.class));
            overridePendingTransition(0, 0);
        });
    }

    // ======================================================
    // 🔵 HITUNG PENDAPATAN + PENGELUARAN
    // ======================================================
    private void loadPendapatanDanPengeluaran() {

        db.collection("users").document(uid)
                .collection("notas")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {

                    if (e != null) return;

                    totalPendapatan = 0;
                    totalPengeluaran = 0;

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {

                            double subtotal = d.getDouble("subtotal") != null ? d.getDouble("subtotal") : 0;
                            double muat = d.getDouble("muat") != null ? d.getDouble("muat") : 0;
                            double ampang = d.getDouble("ampang") != null ? d.getDouble("ampang") : 0;
                            double utang = d.getDouble("utang") != null ? d.getDouble("utang") : 0;

                            // Pendapatan = subtotal - potong utang
                            totalPendapatan += Math.max(subtotal - utang, 0);

                            // Pengeluaran = muat + ampang
                            totalPengeluaran += (muat + ampang);
                        }
                    }

                    updateDashboardNumbers();
                    showTransaksiTerbaru();
                });
    }

    private void loadUtangPelanggan() {
        db.collection("users").document(uid)
                .collection("customers")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) return;

                    totalUtang = 0;

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Double ut = d.getDouble("totalUtang");
                            if (ut != null) totalUtang += ut;
                        }
                    }

                    tvDebt.setText(formatRupiah(totalUtang));
                });
    }

    private void updateDashboardNumbers() {
        tvIncome.setText(formatRupiah(totalPendapatan));
        tvExpense.setText(formatRupiah(totalPengeluaran));

        double profit = Math.max(totalPendapatan - totalPengeluaran, 0);
        tvProfit.setText(formatRupiah(profit));
    }

    // ======================================================
    // 🔵 TAB: TRANSAKSI TERBARU
    // ======================================================
    private void showTransaksiTerbaru() {
        tabTransaksi.setBackgroundResource(R.drawable.tab_selected);
        tabTransaksi.setTextColor(0xFF1B5E20);
        tabUtang.setBackgroundResource(R.drawable.tab_unselected);
        tabUtang.setTextColor(0xFF555555);

        containerList.removeAllViews();

        db.collection("users").document(uid)
                .collection("notas")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(25)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        addNotaToList(d);
                    }
                });
    }

    private void addNotaToList(DocumentSnapshot d) {
        View item = getLayoutInflater().inflate(R.layout.item_nota_dashboard, containerList, false);

        TextView tvNama = item.findViewById(R.id.tvNama);
        TextView tvTanggal = item.findViewById(R.id.tvTanggal);
        TextView tvTotal = item.findViewById(R.id.tvTotal);

        String nama = d.getString("nama");
        long timestamp = d.getLong("timestamp") != null ? d.getLong("timestamp") : 0;
        double totalAkhir = d.getDouble("totalAkhir") != null ? d.getDouble("totalAkhir") : 0;

        tvNama.setText(nama != null ? nama : "-");
        tvTotal.setText(formatRupiah(totalAkhir));
        tvTanggal.setText(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(timestamp));

        item.setOnClickListener(v -> {
            Intent i = new Intent(this, PreviewActivity.class);
            i.putExtra("notaId", d.getId());
            startActivity(i);
        });

        containerList.addView(item);
    }

    // ======================================================
    // 🔵 TAB: DAFTAR UTANG
    // ======================================================
    private void showDaftarUtang() {

        tabUtang.setBackgroundResource(R.drawable.tab_selected);
        tabUtang.setTextColor(0xFF1B5E20);

        tabTransaksi.setBackgroundResource(R.drawable.tab_unselected);
        tabTransaksi.setTextColor(0xFF555555);

        containerList.removeAllViews();

        db.collection("users").document(uid)
                .collection("customers")
                .orderBy("totalUtang", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        addCustomerDebtToList(d);
                    }
                });
    }

    private void addCustomerDebtToList(DocumentSnapshot d) {
        View item = getLayoutInflater().inflate(R.layout.item_debt_dashboard, containerList, false);

        TextView tvNama = item.findViewById(R.id.tvNama);
        TextView tvUtang = item.findViewById(R.id.tvUtang);

        tvNama.setText(d.getString("nama"));
        double utang = d.getDouble("totalUtang") != null ? d.getDouble("totalUtang") : 0;
        tvUtang.setText(formatRupiah(utang));

        item.setOnClickListener(v -> {
            Customer c = d.toObject(Customer.class);
            if (c != null) {
                c.id = d.getId();
                Intent i = new Intent(this, CustomerDetailActivity.class);
                i.putExtra("customer", c);
                startActivity(i);
            }
        });

        containerList.addView(item);
    }

    // ======================================================
    private String formatRupiah(double n) {
        return "Rp " + String.format(Locale.getDefault(), "%,.0f", n).replace(",", ".");
    }
}

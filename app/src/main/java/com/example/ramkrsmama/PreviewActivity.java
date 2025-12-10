package com.example.ramkrsmama;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PreviewActivity extends AppCompatActivity {

    private TextView tvNama, tvTanggal, tvJam,
            tvKendaraan, tvMasuk, tvKeluar, tvNetto, tvHarga,
            tvSubtotal, tvMuat, tvAmpang, tvPotongUtang, tvTotalAkhir;

    private FirebaseFirestore db;
    private String uid, notaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        initViews();

        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());


        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        notaId = getIntent().getStringExtra("notaId");

        if (notaId != null) {
            loadNotaDetail();     // dari dashboard
        } else {
            NotaData n = (NotaData) getIntent().getSerializableExtra("nota");
            if (n == null) {
                Toast.makeText(this, "Data nota tidak ditemukan!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            fillUIFromNotaData(n); // dari CreateNotaActivity
        }
    }

    private void initViews() {
        tvNama = findViewById(R.id.tvNama);
        tvTanggal = findViewById(R.id.tvTanggal);
        tvJam = findViewById(R.id.tvJam);
        tvKendaraan = findViewById(R.id.tvKendaraan);

        tvMasuk = findViewById(R.id.tvMasuk);
        tvKeluar = findViewById(R.id.tvKeluar);

        // 💥 FIX ID NETTO (XML kamu memakai tvNettoVal)
        tvNetto = findViewById(R.id.tvNettoVal);

        tvHarga = findViewById(R.id.tvHarga);
        tvSubtotal = findViewById(R.id.tvSubtotalVal);

        tvMuat = findViewById(R.id.tvPotMuat);
        tvAmpang = findViewById(R.id.tvPotAmpang);
        tvPotongUtang = findViewById(R.id.tvPotUtang);

        tvTotalAkhir = findViewById(R.id.tvTotalAkhirVal);
    }



    // ========================= FROM FIRESTORE =============================
    private void loadNotaDetail() {
        db.collection("users").document(uid)
                .collection("notas")
                .document(notaId)
                .get()
                .addOnSuccessListener(this::fillUIFromFirestore)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal memuat nota!", Toast.LENGTH_LONG).show()
                );
    }

    private void fillUIFromFirestore(DocumentSnapshot d) {

        if (!d.exists()) {
            Toast.makeText(this, "Nota tidak ditemukan!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvNama.setText(d.getString("nama"));
        tvKendaraan.setText(d.getString("kendaraan"));

        tvMasuk.setText(String.valueOf(getD(d, "masuk")));
        tvKeluar.setText(String.valueOf(getD(d, "keluar")));
        tvNetto.setText(String.valueOf(getD(d, "netto")));

        tvHarga.setText(formatRupiah(getD(d, "harga")));
        tvSubtotal.setText(formatRupiah(getD(d, "subtotal")));

        tvMuat.setText(formatRupiah(getD(d, "muat")));
        tvAmpang.setText(formatRupiah(getD(d, "ampang")));
        tvPotongUtang.setText(formatRupiah(getD(d, "utang")));

        tvTotalAkhir.setText(formatRupiah(getD(d, "totalAkhir")));

        long t = d.getLong("timestamp");
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        tvTanggal.setText(df.format(t));
        tvJam.setText(tf.format(t));
    }

    private double getD(DocumentSnapshot d, String key) {
        Double v = d.getDouble(key);
        return v == null ? 0 : v;
    }

    // ========================= FROM CREATE NOTA =============================
    private void fillUIFromNotaData(NotaData n) {

        tvNama.setText(n.nama);
        tvKendaraan.setText(n.kendaraan);

        tvMasuk.setText(String.valueOf(n.masuk));
        tvKeluar.setText(String.valueOf(n.keluar));
        tvNetto.setText(String.valueOf(n.netto));

        tvHarga.setText(formatRupiah(n.harga));
        tvSubtotal.setText(formatRupiah(n.subtotal));

        tvMuat.setText(formatRupiah(n.muat));
        tvAmpang.setText(formatRupiah(n.ampang));
        tvPotongUtang.setText(formatRupiah(n.utang));

        tvTotalAkhir.setText(formatRupiah(n.totalAkhir));

        tvTanggal.setText(n.date);
        tvJam.setText(n.time);
    }

    private String formatRupiah(double n) {
        return "Rp " + String.format(Locale.getDefault(), "%,.0f", n).replace(",", ".");
    }
}

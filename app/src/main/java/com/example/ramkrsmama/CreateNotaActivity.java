package com.example.ramkrsmama;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;   // ← INI YANG HILANG!!
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class CreateNotaActivity extends AppCompatActivity {

    // OUTPUT
    private TextView tvDate, tvTime, tvNetto, tvSubtotal, tvTotalPotongan, tvTotalAkhir;

    // INPUT
    private EditText edtKendaraan, edtPotPercent, edtMasuk, edtKeluar, edtPrice;
    private EditText edtMuat, edtAmpang, edtUtang;
    private AutoCompleteTextView edtNama;

    // CHECKBOX
    private CheckBox cbMuat, cbAmpang, cbUtang;

    // LAYOUT POTONGAN
    private LinearLayout layoutPotongan;

    // BUTTON PANEL
    private Button btnPreview, btnSaveNota, btnPdfOption, btnReset;

    // DATE
    private Calendar calendar;

    // FIREBASE
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // PERMISSION
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // list nama customer untuk dropdown
    private final List<String> customerNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_nota);

        initViews();
        initFirebase();
        initDateTime();
        initAutoCalc();
        initListeners();
        initPermissionLauncher();
        initBottomNav();
        loadCustomerNamesForDropdown();
    }

    // ========================= BOTTOM NAVIGATION =============================
    private void initBottomNav() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navNota = findViewById(R.id.navNota);
        LinearLayout navCustomer = findViewById(R.id.navCustomer);

        if (navHome == null || navNota == null || navCustomer == null) return;

        navNota.setSelected(true);

        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class))
        );

        navNota.setOnClickListener(v -> {
            // sudah di sini
        });

        navCustomer.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerListActivity.class))
        );
    }
    // ========================================================================

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvNetto = findViewById(R.id.tvNetto);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTotalPotongan = findViewById(R.id.tvTotalPotongan);
        tvTotalAkhir = findViewById(R.id.tvTotalAkhir);

        edtKendaraan = findViewById(R.id.edtKendaraan);
        edtNama = findViewById(R.id.edtNama);
        edtPotPercent = findViewById(R.id.edtPotPercent);
        edtMasuk = findViewById(R.id.edtMasuk);
        edtKeluar = findViewById(R.id.edtKeluar);
        edtPrice = findViewById(R.id.edtPrice);

        edtMuat = findViewById(R.id.edtMuat);
        edtAmpang = findViewById(R.id.edtAmpang);
        edtUtang = findViewById(R.id.edtUtang);

        cbMuat = findViewById(R.id.cbMuat);
        cbAmpang = findViewById(R.id.cbAmpang);
        cbUtang = findViewById(R.id.cbUtang);

        layoutPotongan = findViewById(R.id.layoutPotongan);

        btnPreview = findViewById(R.id.btnPreview);
        btnSaveNota = findViewById(R.id.btnSaveNota);
        btnPdfOption = findViewById(R.id.btnPdfOption);
        btnReset = findViewById(R.id.btnReset);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // ============================================================
    // TANGGAL & JAM (BISA DIUBAH)
    // ============================================================
    private void initDateTime() {
        calendar = Calendar.getInstance();
        updateDateTimeViews();

        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
    }

    private void updateDateTimeViews() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        tvDate.setText(df.format(calendar.getTime()));
        tvTime.setText(tf.format(calendar.getTime()));
    }

    private void showDatePicker() {
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeViews();
        }, y, m, d).show();
    }

    private void showTimePicker() {
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateDateTimeViews();
        }, h, min, true).show();
    }

    // ============================================================
    // DROPDOWN NAMA PELANGGAN (AUTO-COMPLETE)
    // ============================================================
    private void loadCustomerNamesForDropdown() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("customers")
                .get()
                .addOnSuccessListener(snap -> {
                    customerNames.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String nama = d.getString("nama");
                        if (nama != null && !nama.trim().isEmpty()) {
                            customerNames.add(nama.trim());
                        }
                    }

                    android.widget.ArrayAdapter<String> adapter =
                            new android.widget.ArrayAdapter<>(this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    customerNames);

                    edtNama.setAdapter(adapter);
                    edtNama.setThreshold(1);
                });
    }

    // ============================================================
    // AUTO HITUNG NOTA
    // ============================================================
    private void initPermissionLauncher() {
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        granted -> {
                            if (granted) savePdf();
                            else Toast.makeText(this, "Permission ditolak", Toast.LENGTH_SHORT).show();
                        });
    }

    private void initAutoCalc() {
        addWatcher(edtPotPercent);
        addWatcher(edtMasuk);
        addWatcher(edtKeluar);
        addWatcher(edtPrice);
        addWatcher(edtMuat);
        addWatcher(edtAmpang);
        addWatcher(edtUtang);

        cbMuat.setOnCheckedChangeListener((v, b) -> calculate());
        cbAmpang.setOnCheckedChangeListener((v, b) -> calculate());
        cbUtang.setOnCheckedChangeListener((v, b) -> calculate());
    }

    private void addWatcher(EditText e) {
        e.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { calculate(); }
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void calculate() {
        double masuk = getD(edtMasuk);
        double keluar = getD(edtKeluar);
        double persen = getD(edtPotPercent);
        double harga = getD(edtPrice);

        // BRUTO
        double bruto = Math.max(masuk - keluar, 0);

        // POTONGAN PERSEN -> (masuk - keluar) * persen%
        double potPersen = bruto * (persen / 100.0);

        // NETTO = bruto - potPersen
        double netto = Math.max(bruto - potPersen, 0);

        // SUBTOTAL
        double subtotal = netto * harga;

        // POTONGAN LAIN
        double potMuat = cbMuat.isChecked() ? getD(edtMuat) : 0;
        double potAmp = cbAmpang.isChecked() ? getD(edtAmpang) : 0;
        double potUtg = cbUtang.isChecked() ? getD(edtUtang) : 0;

        double totalPot = potMuat + potAmp + potUtg;
        double totalAkhir = Math.max(subtotal - totalPot, 0);

        layoutPotongan.setVisibility(
                (cbMuat.isChecked() || cbAmpang.isChecked() || cbUtang.isChecked())
                        ? View.VISIBLE : View.GONE
        );

        tvNetto.setText(f(netto));
        tvSubtotal.setText(f(subtotal));
        tvTotalPotongan.setText(f(totalPot));
        tvTotalAkhir.setText(f(totalAkhir));
    }

    private double getD(EditText e) {
        String s = e.getText().toString().trim();
        if (s.isEmpty()) return 0;
        try { return Double.parseDouble(s); } catch (Exception ex) { return 0; }
    }

    // *** FIX PENTING DI SINI ***
    // sekarang kita jaga supaya "4456,45" jadi 4456.45, bukan 445645 lagi
    private double getD(TextView t) {
        String s = t.getText().toString()
                .replace("Rp", "")
                .replace(" ", "")
                .trim();

        if (s.isEmpty()) return 0;

        // kalau ada titik & koma → anggap format Indonesia: 1.234,56
        if (s.contains(",") && s.contains(".")) {
            s = s.replace(".", "").replace(",", ".");
        } else if (s.contains(",")) {
            // cuma koma → anggap desimal
            s = s.replace(",", ".");
        }

        try {
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return 0;
        }
    }

    private String f(double d) {
        // pakai format tanpa pemisah ribuan, 2 decimal
        return String.format(Locale.getDefault(), "%.2f", d);
    }

    // ============================================================
    // LISTENER BUTTON
    // ============================================================
    private void initListeners() {

        btnPreview.setOnClickListener(v -> {
            calculate();
            NotaData data = buildNotaData();
            Intent i = new Intent(this, PreviewActivity.class);
            i.putExtra("nota", data);
            startActivity(i);
        });

        btnSaveNota.setOnClickListener(v -> saveNotaFirestore());
        btnPdfOption.setOnClickListener(v -> showPdfDialog());
        btnReset.setOnClickListener(v -> resetForm());
    }

    private NotaData buildNotaData() {
        NotaData n = new NotaData();
        n.date = tvDate.getText().toString();
        n.time = tvTime.getText().toString();
        n.nama = edtNama.getText().toString().trim();
        n.kendaraan = edtKendaraan.getText().toString().trim();

        n.masuk = getD(edtMasuk);
        n.keluar = getD(edtKeluar);
        n.persen = getD(edtPotPercent);
        n.netto = getD(tvNetto);
        n.harga = getD(edtPrice);

        n.muat = cbMuat.isChecked() ? getD(edtMuat) : 0;
        n.ampang = cbAmpang.isChecked() ? getD(edtAmpang) : 0;
        n.utang = cbUtang.isChecked() ? getD(edtUtang) : 0;

        n.subtotal = getD(tvSubtotal);
        n.totalPot = getD(tvTotalPotongan);
        n.totalAkhir = getD(tvTotalAkhir);

        n.createdAt = calendar.getTimeInMillis();

        return n;
    }

    private void saveNotaFirestore() {
        NotaData n = buildNotaData();

        if (n.nama == null || n.nama.trim().isEmpty()) {
            edtNama.setError("Nama pemasok wajib diisi / dipilih");
            return;
        }

        FirestoreHelper.saveNota(n, success -> {
            if (success) {
                Toast.makeText(this, "Nota berhasil disimpan!", Toast.LENGTH_SHORT).show();
                resetForm();
            } else {
                Toast.makeText(this, "Gagal menyimpan nota!", Toast.LENGTH_LONG).show();
            }
        });
    }

    // ============================================================
    // PDF
    // ============================================================
    private void showPdfDialog() {
        String[] ops = {"Print", "Simpan PDF"};

        new AlertDialog.Builder(this)
                .setTitle("PDF Options")
                .setItems(ops, (diag, i) -> {
                    if (i == 0) {
                        Toast.makeText(this, "Hubungkan printer!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) savePdf();
                        else requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                })
                .show();
    }

    private void savePdf() {
        try {
            NotaData data = buildNotaData();
            byte[] pdf = PdfGenerator.generatePdfBytes(this, data);

            String filename = "nota_" + System.currentTimeMillis() + ".pdf";

            Uri uri = saveToDownloads(filename, pdf);
            Toast.makeText(this, "PDF disimpan!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Gagal PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Uri saveToDownloads(String filename, byte[] bytes) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.IS_PENDING, 1);

                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri uri = getContentResolver().insert(collection, values);

                OutputStream os = getContentResolver().openOutputStream(uri);
                os.write(bytes);
                os.close();

                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                getContentResolver().update(uri, values, null, null);

                return uri;

            } else {

                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloads.exists()) downloads.mkdirs();

                File file = new File(downloads, filename);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();

                return Uri.fromFile(file);
            }

        } catch (Exception e) {
            return null;
        }
    }

    private void resetForm() {
        edtKendaraan.setText("");
        edtNama.setText("");
        edtPotPercent.setText("");
        edtMasuk.setText("");
        edtKeluar.setText("");
        edtPrice.setText("");
        edtMuat.setText("");
        edtAmpang.setText("");
        edtUtang.setText("");

        cbMuat.setChecked(false);
        cbAmpang.setChecked(false);
        cbUtang.setChecked(false);

        tvNetto.setText("0.00");
        tvSubtotal.setText("0.00");
        tvTotalPotongan.setText("0.00");
        tvTotalAkhir.setText("0.00");

        calendar = Calendar.getInstance();
        updateDateTimeViews();
    }
}

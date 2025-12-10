package com.example.ramkrsmama;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DebtPaymentActivity extends AppCompatActivity {

    private Spinner spCustomer, spType;
    private EditText edtAmount, edtNote;
    private TextView tvDate;
    private Button btnChooseFile, btnSave;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String uid;

    private List<Customer> customerList = new ArrayList<>();
    private List<String> customerNames = new ArrayList<>();
    private Uri selectedImageUri = null;

    private final int PICK_IMAGE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_payment);

        spCustomer = findViewById(R.id.spCustomer);
        spType = findViewById(R.id.spType);
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        tvDate = findViewById(R.id.tvDate);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        // set tanggal sekarang
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        // dropdown tipe transaksi
        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                        Arrays.asList("Bayar Utang", "Tambah Utang"));

        spType.setAdapter(typeAdapter);

        loadCustomers();

        tvDate.setOnClickListener(v -> showDatePicker());
        btnChooseFile.setOnClickListener(v -> chooseImage());
        btnSave.setOnClickListener(v -> saveTransaction());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadCustomers() {
        db.collection("users").document(uid)
                .collection("customers")
                .get()
                .addOnSuccessListener(snap -> {
                    customerList.clear();
                    customerNames.clear();

                    customerNames.add("Pilih pelanggan");

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Customer c = d.toObject(Customer.class);
                        if (c != null) {
                            c.id = d.getId();
                            customerList.add(c);

                            String listText = c.nama + " • Utang: Rp " +
                                    String.format("%,.0f", c.totalUtang).replace(",", ".");

                            customerNames.add(listText);
                        }
                    }

                    ArrayAdapter<String> customerAdapter =
                            new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, customerNames);

                    spCustomer.setAdapter(customerAdapter);
                });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> tvDate.setText(day + "/" + (month + 1) + "/" + year),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void chooseImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            btnChooseFile.setText("File dipilih ✔");
        }
    }

    private void saveTransaction() {

        if (spCustomer.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Pilih pelanggan dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = edtAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            edtAmount.setError("Jumlah wajib diisi");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String note = edtNote.getText().toString();
        String type = spType.getSelectedItem().toString().equals("Bayar Utang") ? "PAY" : "ADD";
        String date = tvDate.getText().toString();

        Customer customer = customerList.get(spCustomer.getSelectedItemPosition() - 1);

        DebtTransaction tx = new DebtTransaction();
        tx.amount = amount;
        tx.type = type;
        tx.date = date;
        tx.note = note;
        tx.createdAt = System.currentTimeMillis();

        if (selectedImageUri == null) {
            saveToFirestore(customer, tx, null);
        } else {
            uploadImageThenSave(customer, tx);
        }
    }

    private void uploadImageThenSave(Customer customer, DebtTransaction tx) {

        StorageReference ref = storage.getReference()
                .child("debtProof/" + uid + "/" + System.currentTimeMillis() + ".jpg");

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(result ->
                        ref.getDownloadUrl().addOnSuccessListener(uri -> saveToFirestore(customer, tx, uri.toString()))
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void saveToFirestore(Customer c, DebtTransaction tx, String proofUrl) {

        if (proofUrl != null) tx.proofUrl = proofUrl;

        // PASTIKAN createdAt terisi
        if (tx.createdAt == 0) {
            tx.createdAt = System.currentTimeMillis();
        }

        db.collection("users").document(uid)
                .collection("customers").document(c.id)
                .collection("debtTransactions")
                .add(tx.toMap())
                .addOnSuccessListener(doc -> {

                    // hitung ulang total utang lokal
                    double newTotal = c.totalUtang;

                    if (tx.type.equals("ADD")) newTotal += tx.amount;
                    if (tx.type.equals("PAY")) newTotal -= tx.amount;

                    if (newTotal < 0) newTotal = 0;

                    // update total utang customer
                    db.collection("users").document(uid)
                            .collection("customers").document(c.id)
                            .update("totalUtang", newTotal)
                            .addOnSuccessListener(a -> {

                                Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

}

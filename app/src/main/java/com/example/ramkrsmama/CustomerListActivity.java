package com.example.ramkrsmama;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class CustomerListActivity extends AppCompatActivity {

    private RecyclerView rvCustomers;
    private ProgressBar progressBar;
    private CustomerListAdapter adapter;

    private FirebaseFirestore db;
    private String uid;

    private final List<Customer> customerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        initViews();
        initBottomNav();
        initFirebase();
        setupRecycler();
        loadCustomers();
    }

    private void initViews() {
        rvCustomers = findViewById(R.id.rvCustomers);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditCustomerActivity.class))
        );
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Anda belum login!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecycler() {
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CustomerListAdapter(
                customerList,
                c -> {
                    Intent i = new Intent(this, CustomerDetailActivity.class);
                    i.putExtra("customer", c);
                    startActivity(i);
                },
                c -> {
                    Intent i = new Intent(this, DebtPaymentActivity.class);
                    i.putExtra("customer", c);
                    startActivity(i);
                }
        );

        rvCustomers.setAdapter(adapter);
    }

    private void initBottomNav() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navNota = findViewById(R.id.navNota);
        LinearLayout navCustomer = findViewById(R.id.navCustomer);

        navCustomer.setSelected(true);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            overridePendingTransition(0,0);
        });

        navNota.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateNotaActivity.class));
            overridePendingTransition(0,0);
        });
    }

    private void loadCustomers() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(uid)
                .collection("customers")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, error) -> {

                    progressBar.setVisibility(View.GONE);

                    if (error != null) return;

                    customerList.clear();

                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {

                            Customer c = d.toObject(Customer.class);
                            if (c == null) continue;

                            c.id = d.getId();
                            if (c.nama == null) c.nama = "-";
                            if (c.telepon == null) c.telepon = "-";
                            if (c.alamat == null) c.alamat = "-";
                            if (c.kebun == null) c.kebun = "-";
                            if (c.catatan == null) c.catatan = "-";

                            if (d.getDouble("totalUtang") != null)
                                c.totalUtang = d.getDouble("totalUtang");

                            customerList.add(c);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}

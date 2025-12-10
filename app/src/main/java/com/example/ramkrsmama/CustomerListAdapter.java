package com.example.ramkrsmama;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CustomerListAdapter extends RecyclerView.Adapter<CustomerListAdapter.ViewHolder> {

    public interface OnCustomerClick {
        void onClick(Customer c);
    }

    public interface OnCustomerPay {
        void onPay(Customer c);
    }

    private List<Customer> list;
    private OnCustomerClick clickListener;
    private OnCustomerPay payListener;

    public CustomerListAdapter(List<Customer> list, OnCustomerClick clickListener, OnCustomerPay payListener) {
        this.list = list;
        this.clickListener = clickListener;
        this.payListener = payListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Customer c = list.get(position);

        h.tvNama.setText(c.nama != null ? c.nama : "-");
        h.tvTelepon.setText(c.telepon != null ? c.telepon : "-");

        String lokasi = "";
        if (c.alamat != null && !c.alamat.trim().isEmpty()) lokasi += c.alamat;
        if (c.kebun != null && !c.kebun.trim().isEmpty()) {
            if (!lokasi.isEmpty()) lokasi += " • ";
            lokasi += c.kebun;
        }
        h.tvAlamat.setText(lokasi.isEmpty() ? "-" : lokasi);

        h.tvCatatan.setText(
                c.catatan == null || c.catatan.trim().isEmpty()
                        ? "-"
                        : c.catatan
        );

        h.tvTotalUtang.setText(
                String.format(Locale.getDefault(), "Rp %,.0f", c.totalUtang)
        );

        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(c);
        });

        h.btnPay.setOnClickListener(v -> {
            if (payListener != null) payListener.onPay(c);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvNama, tvTelepon, tvAlamat, tvCatatan, tvTotalUtang;
        Button btnPay;

        public ViewHolder(@NonNull View v) {
            super(v);

            tvNama = v.findViewById(R.id.tvNama);
            tvTelepon = v.findViewById(R.id.tvTelepon);
            tvAlamat = v.findViewById(R.id.tvAlamat);
            tvCatatan = v.findViewById(R.id.tvCatatan);
            tvTotalUtang = v.findViewById(R.id.tvTotalUtang);
            btnPay = v.findViewById(R.id.btnPay);
        }
    }
}

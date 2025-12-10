package com.example.ramkrsmama;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DebtTransactionAdapter extends RecyclerView.Adapter<DebtTransactionAdapter.VH> {

    private List<DebtTransaction> list;
    private SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public DebtTransactionAdapter(List<DebtTransaction> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_debt_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        DebtTransaction t = list.get(pos);

        if (t.type.equals("PAY")) {
            h.tvIcon.setText("✔");
            h.tvIcon.setTextColor(0xFF2E7D32);

            h.tvTitle.setText("Pembayaran");
            h.tvTitle.setTextColor(0xFF2E7D32);

            h.tvAmount.setText("- Rp " + String.format("%,.0f", t.amount));
            h.tvAmount.setTextColor(0xFF2E7D32);

        } else {
            h.tvIcon.setText("+");
            h.tvIcon.setTextColor(0xFFD32F2F);

            h.tvTitle.setText("Penambahan Utang");
            h.tvTitle.setTextColor(0xFFD32F2F);

            h.tvAmount.setText("+ Rp " + String.format("%,.0f", t.amount));
            h.tvAmount.setTextColor(0xFFD32F2F);
        }

        h.tvDesc.setText(t.note == null || t.note.isEmpty() ? "-" : t.note);
        h.tvDate.setText(df.format(t.createdAt));

        if (t.proofUrl != null && !t.proofUrl.isEmpty()) {
            h.tvLihatBukti.setVisibility(View.VISIBLE);
            h.tvLihatBukti.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(android.net.Uri.parse(t.proofUrl));
                v.getContext().startActivity(i);
            });
        } else {
            h.tvLihatBukti.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvIcon, tvTitle, tvAmount, tvDate, tvDesc, tvLihatBukti;

        public VH(@NonNull View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tvIcon);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvDate = v.findViewById(R.id.tvDate);
            tvDesc = v.findViewById(R.id.tvDesc);
            tvLihatBukti = v.findViewById(R.id.tvLihatBukti);
        }
    }
}

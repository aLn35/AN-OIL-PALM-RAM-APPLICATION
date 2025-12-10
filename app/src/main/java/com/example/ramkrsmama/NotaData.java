package com.example.ramkrsmama;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NotaData implements Serializable {

    public String date;        // contoh: 10/12/2025
    public String time;        // contoh: 14:30
    public String nama;        // nama pelanggan
    public String kendaraan;

    public double masuk;
    public double keluar;
    public double persen;
    public double netto;
    public double harga;

    public double muat;        // biaya muat / supir  -> PENGELUARAN
    public double ampang;      // ampang-ampang       -> PENGELUARAN
    public double utang;       // potong utang        -> TIDAK pengeluaran, cuma kurangi utang

    public double subtotal;    // netto * harga
    public double totalPot;    // muat + ampang + utang
    public double totalAkhir;  // subtotal - totalPot (uang CASH diterima)

    public long createdAt;     // untuk sorting

    public NotaData() {
        // butuh untuk Firestore
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("date", date);
        m.put("time", time);
        m.put("nama", nama);
        m.put("kendaraan", kendaraan);

        m.put("masuk", masuk);
        m.put("keluar", keluar);
        m.put("persen", persen);
        m.put("netto", netto);
        m.put("harga", harga);

        m.put("muat", muat);
        m.put("ampang", ampang);
        m.put("utang", utang);

        m.put("subtotal", subtotal);
        m.put("totalPot", totalPot);
        m.put("totalAkhir", totalAkhir);

        m.put("createdAt", createdAt == 0 ? System.currentTimeMillis() : createdAt);

        return m;
    }
}

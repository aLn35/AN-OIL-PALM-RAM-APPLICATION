package com.example.ramkrsmama;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Customer implements Serializable {

    public String id;
    public String nama;
    public String telepon;
    public String alamat;
    public String kebun;
    public String catatan;

    public double totalUtang;
    public long createdAt;

    public Customer() {
        // Needed for Firestore
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("nama", nama);
        m.put("telepon", telepon);
        m.put("alamat", alamat);
        m.put("kebun", kebun);
        m.put("catatan", catatan);
        m.put("totalUtang", totalUtang);
        m.put("createdAt", createdAt == 0 ? System.currentTimeMillis() : createdAt);
        return m;
    }
}

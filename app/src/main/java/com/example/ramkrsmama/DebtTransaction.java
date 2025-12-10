package com.example.ramkrsmama;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DebtTransaction implements Serializable {

    public String id;
    public String customerId;
    public String type;        // ADD, PAY, NOTE_PAY
    public double amount;
    public long createdAt;
    public String note;
    public String proofUrl;
    public boolean fromNota;
    public String date;

    public DebtTransaction() {
        // Firestore requires empty constructor
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("type", type);
        m.put("amount", amount);
        m.put("note", note == null ? "" : note);
        m.put("proofUrl", proofUrl);
        m.put("fromNota", fromNota);
        m.put("date", date);
        m.put("createdAt", createdAt == 0 ? System.currentTimeMillis() : createdAt);
        return m;
    }
}

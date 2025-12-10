package com.example.ramkrsmama;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "FirestoreHelper";

    public interface SaveCallback {
        void onComplete(boolean success);
    }

    private static String getUid() {
        return FirebaseAuth.getInstance().getUid();
    }

    // ============================================================
    // 1) SAVE NOTA KE /users/{uid}/notas
    //    + kalau ada POTONG UTANG -> kurangi utang customer
    // ============================================================
    public static void saveNota(NotaData nota, SaveCallback cb) {

        String uid = getUid();
        if (uid == null) {
            Log.e(TAG, "UID null saat saveNota()");
            if (cb != null) cb.onComplete(false);
            return;
        }

        if (nota.createdAt == 0) {
            nota.createdAt = System.currentTimeMillis();
        }

        Map<String, Object> map = nota.toMap();
        map.put("timestamp", System.currentTimeMillis()); // untuk orderBy di dashboard

        db.collection("users").document(uid)
                .collection("notas")
                .add(map)
                .addOnSuccessListener(doc -> {

                    Log.d(TAG, "Nota tersimpan: " + doc.getId());

                    // Kalau ada potong utang -> kurangi utang customer
                    if (nota.utang > 0 && nota.nama != null && !nota.nama.trim().isEmpty()) {
                        reduceCustomerDebt(nota.nama.trim(), nota.utang);
                    }

                    if (cb != null) cb.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Gagal simpan nota: ", e);
                    if (cb != null) cb.onComplete(false);
                });
    }

    // ============================================================
    // 2) KURANGI UTANG CUSTOMER (dipanggil dari Nota: type = NOTE_PAY)
    // ============================================================
    public static void reduceCustomerDebt(String namaCustomer, double amount) {

        String uid = getUid();
        if (uid == null) {
            Log.e(TAG, "UID null saat reduceCustomerDebt()");
            return;
        }

        db.collection("users").document(uid)
                .collection("customers")
                .whereEqualTo("nama", namaCustomer)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {

                    if (snap == null || snap.isEmpty()) {
                        Log.w(TAG, "Customer tidak ditemukan untuk nama: " + namaCustomer);
                        return;
                    }

                    DocumentSnapshot doc = snap.getDocuments().get(0);
                    String id = doc.getId();

                    Double oldDebtObj = doc.getDouble("totalUtang");
                    double oldDebt = oldDebtObj != null ? oldDebtObj : 0;

                    double newDebt = Math.max(oldDebt - amount, 0);

                    // update total utang
                    db.collection("users").document(uid)
                            .collection("customers")
                            .document(id)
                            .update("totalUtang", newDebt)
                            .addOnSuccessListener(unused ->
                                    Log.d(TAG, "totalUtang diupdate jadi: " + newDebt)
                            )
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Gagal update totalUtang: ", e)
                            );

                    // catat transaksi NOTE_PAY di riwayat
                    Map<String, Object> tx = new HashMap<>();
                    tx.put("amount", amount);
                    tx.put("type", "NOTE_PAY");
                    tx.put("createdAt", System.currentTimeMillis());
                    tx.put("note", "Potong utang dari Nota");

                    db.collection("users").document(uid)
                            .collection("customers")
                            .document(id)
                            .collection("debtTransactions")
                            .add(tx)
                            .addOnSuccessListener(ref ->
                                    Log.d(TAG, "Riwayat utang NOTE_PAY disimpan: " + ref.getId())
                            )
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Gagal simpan riwayat utang: ", e)
                            );
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Gagal query customer untuk reduceDebt: ", e)
                );
    }
}

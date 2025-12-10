package com.example.ramkrsmama;
import java.text.NumberFormat;
import java.util.Locale;

public class RupiahFormatter {
    public static String format(long amount){
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id","ID"));
        String s = nf.format(amount);
        // nf returns "Rp 1.000,00" — adjust to "Rp.1.000,-"
        s = s.replace("Rp","Rp.").replace(",00","") + ",-";
        return s;
    }
}

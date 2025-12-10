package com.example.ramkrsmama;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.ByteArrayOutputStream;

public class PdfGenerator {

    public static byte[] generatePdfBytes(Context ctx, NotaData nota) {

        PdfDocument pdf = new PdfDocument();

        // Create A4 page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint title = new Paint();
        Paint sub = new Paint();
        Paint label = new Paint();
        Paint value = new Paint();
        Paint line = new Paint();
        Paint bold = new Paint();

        // ================= FONT SETTINGS =================
        title.setTextSize(20f);
        title.setColor(Color.BLACK);
        title.setFakeBoldText(true);

        sub.setTextSize(12f);
        sub.setColor(Color.DKGRAY);

        label.setTextSize(13f);
        label.setColor(Color.BLACK);

        value.setTextSize(13f);
        value.setColor(Color.BLACK);
        value.setTextAlign(Paint.Align.RIGHT);

        bold.setTextSize(15f);
        bold.setFakeBoldText(true);
        bold.setColor(Color.BLACK);

        line.setColor(Color.GRAY);
        line.setStrokeWidth(2);

        int xLeft = 40;
        int xRight = 550;
        int y = 60;

        // ================= HEADER =================
        canvas.drawText("KEMPAS RAM SEHATI (KRS)", xLeft, y, title);
        y += 20;
        canvas.drawText("JL BLOK KOSONG • SUBAN – RAWANG KEMPAS", xLeft, y, sub);

        y += 25;
        drawDashedLine(canvas, xLeft, xRight, y);
        y += 30;

        // ================= INFO PEMASOK =================
        y = drawRow(canvas, "Tanggal", nota.date, xLeft, y, label, value);
        y = drawRow(canvas, "Jam", nota.time, xLeft, y, label, value);
        y = drawRow(canvas, "Nama Pemasok", nota.nama, xLeft, y, label, value);
        y = drawRow(canvas, "Kendaraan", nota.kendaraan, xLeft, y, label, value);

        y += 18;
        drawDashedLine(canvas, xLeft, xRight, y);
        y += 25;

        // ================= DATA TIMBANG =================
        y = drawRow(canvas, "Masuk (KG)", f(nota.masuk), xLeft, y, label, value);
        y = drawRow(canvas, "Keluar (KG)", f(nota.keluar), xLeft, y, label, value);
        y = drawRow(canvas, "Potongan (%)", f(nota.persen), xLeft, y, label, value);
        y = drawRow(canvas, "Netto (KG)", f(nota.netto), xLeft, y, label, value);
        y = drawRow(canvas, "Harga / Kg", "Rp " + f(nota.harga), xLeft, y, label, value);

        y += 18;
        drawDashedLine(canvas, xLeft, xRight, y);
        y += 25;

        // ================= TOTAL =================
        y = drawRow(canvas, "Subtotal", "Rp " + f(nota.subtotal), xLeft, y, label, value);
        y = drawRow(canvas, "Total Potongan", "Rp " + f(nota.totalPot), xLeft, y, label, value);

        y += 10;
        canvas.drawText("TOTAL AKHIR", xLeft, y + 5, bold);
        bold.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Rp " + f(nota.totalAkhir), xRight, y + 5, bold);
        bold.setTextAlign(Paint.Align.LEFT);

        y += 20;
        drawDashedLine(canvas, xLeft, xRight, y);

        pdf.finishPage(page);

        // Convert to byte[]
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            pdf.writeTo(stream);
        } catch (Exception e) { e.printStackTrace(); }

        pdf.close();
        return stream.toByteArray();
    }

    // ================= SUPPORT FUNCTIONS =================

    private static void drawDashedLine(Canvas c, int x1, int x2, int y) {
        Paint p = new Paint();
        p.setColor(Color.GRAY);
        p.setStrokeWidth(2);

        int dash = 10;
        for (int x = x1; x < x2; x += dash * 2) {
            c.drawLine(x, y, x + dash, y, p);
        }
    }

    private static int drawRow(Canvas c, String left, String right, int xLeft, int y,
                               Paint pLeft, Paint pRight) {
        c.drawText(left, xLeft, y, pLeft);
        c.drawText(right, 550, y, pRight);
        return y + 25;
    }

    private static String f(double d) {
        return String.format("%,.2f", d);
    }
}

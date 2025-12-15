package com.example.pawmart;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InvoicePdfUtil {

    public static void generateAndShare(Context ctx, List<CartItem> items, String address) {
        if (items == null || items.isEmpty()) {
            Toast.makeText(ctx, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "PawMart_Invoice_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        Paint bold = new Paint();
        bold.setFakeBoldText(true);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4-ish
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int x = 40, y = 60;

        bold.setTextSize(18);
        canvas.drawText("PawMart Invoice", x, y, bold);

        paint.setTextSize(12);
        y += 25;
        canvas.drawText("Date: " + new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date()), x, y, paint);

        y += 20;
        canvas.drawText("Delivery Address:", x, y, bold);
        y += 16;
        canvas.drawText(address == null ? "-" : address, x, y, paint);

        y += 30;
        canvas.drawText("Items:", x, y, bold);

        y += 18;
        canvas.drawText("Name", x, y, bold);
        canvas.drawText("Qty", 360, y, bold);
        canvas.drawText("Total", 440, y, bold);

        y += 10;
        canvas.drawLine(x, y, 555, y, paint);
        y += 18;

        double grandTotal = 0;

        for (CartItem ci : items) {
            Product p = ci.getProduct();
            if (p == null) continue;

            double line = p.getPrice() * ci.getQuantity();
            grandTotal += line;

            canvas.drawText(safe(p.getName()), x, y, paint);
            canvas.drawText(String.valueOf(ci.getQuantity()), 370, y, paint);
            canvas.drawText(String.format(Locale.getDefault(), "$%.2f", line), 440, y, paint);

            y += 18;
            if (y > 760) break; // simple one-page safety
        }

        y += 10;
        canvas.drawLine(x, y, 555, y, paint);
        y += 22;

        bold.setTextSize(14);
        canvas.drawText("Grand Total: " + String.format(Locale.getDefault(), "$%.2f", grandTotal), x, y, bold);

        document.finishPage(page);

        try {
            Uri pdfUri = saveToDownloads(ctx, document, fileName);
            document.close();

            if (pdfUri != null) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("application/pdf");
                share.putExtra(Intent.EXTRA_STREAM, pdfUri);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ctx.startActivity(Intent.createChooser(share, "Share Invoice PDF"));
            }
        } catch (Exception e) {
            document.close();
            Toast.makeText(ctx, "PDF failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static Uri saveToDownloads(Context ctx, PdfDocument doc, String fileName) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Downloads.IS_PENDING, 1);
            uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        } else {
            uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        }

        if (uri == null) return null;

        OutputStream os = ctx.getContentResolver().openOutputStream(uri);
        if (os == null) return null;

        doc.writeTo(os);
        os.flush();
        os.close();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            ctx.getContentResolver().update(uri, values, null, null);
        }

        Toast.makeText(ctx, "Invoice saved to Downloads", Toast.LENGTH_SHORT).show();
        return uri;
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
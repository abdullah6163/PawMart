package com.example.pawmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvSummary;
    private MaterialButton btnPayBkash, btnGenerateInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvSummary = findViewById(R.id.tvSummary);
        btnPayBkash = findViewById(R.id.btnPayBkash);
        btnGenerateInvoice = findViewById(R.id.btnGenerateInvoice);

        String address = getIntent().getStringExtra("address");
        tvSummary.setText("Deliver to:\n" + address);

        btnPayBkash.setOnClickListener(v -> {

            Toast.makeText(this, "bKash integration goes here (needs backend)", Toast.LENGTH_LONG).show();
            CartManager.getInstance().clearCart(getApplicationContext());
            finish();
        });

        btnGenerateInvoice.setOnClickListener(v -> {
            // generate PDF now
            InvoicePdfUtil.generateAndShare(this, CartManager.getInstance().getCartItems(), address);
        });
    }
}
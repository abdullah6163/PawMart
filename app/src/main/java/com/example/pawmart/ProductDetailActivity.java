package com.example.pawmart;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView img;
    private TextView tvName, tvCat, tvPrice;
    private MaterialButton btnAddCart, btnWishlist;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String productId;
    private Product loadedProduct;
    private boolean isInWishlist = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        img = findViewById(R.id.imgProduct);
        tvName = findViewById(R.id.tvName);
        tvCat = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        btnAddCart = findViewById(R.id.btnAddCart);
        btnWishlist = findViewById(R.id.btnWishlist);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProduct();
        checkWishlistStatus();

        btnAddCart.setOnClickListener(v -> addToCart());
        btnWishlist.setOnClickListener(v -> toggleWishlist());
    }

    private void loadProduct() {
        db.collection("products").document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    loadedProduct = doc.toObject(Product.class);
                    if (loadedProduct == null) return;

                    // IMPORTANT: store id
                    loadedProduct.setId(doc.getId());

                    tvName.setText(loadedProduct.getName());
                    tvCat.setText(loadedProduct.getCategory());
                    tvPrice.setText("$" + String.format("%.2f", loadedProduct.getPrice()));

                    Glide.with(this)
                            .load(loadedProduct.getImageUrl())
                            .placeholder(R.drawable.ic_pet_welcome)
                            .into(img);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void checkWishlistStatus() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .collection("wishlist").document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    isInWishlist = doc.exists();
                    updateWishlistButton();
                });
    }

    private void updateWishlistButton() {
        btnWishlist.setText(isInWishlist ? "REMOVE FROM WISHLIST" : "ADD TO WISHLIST");
    }

    private void addToCart() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (loadedProduct == null) {
            Toast.makeText(this, "Loading product...", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("productId", loadedProduct.getId());
        cartData.put("name", loadedProduct.getName());
        cartData.put("category", loadedProduct.getCategory());
        cartData.put("price", loadedProduct.getPrice());
        cartData.put("imageUrl", loadedProduct.getImageUrl());
        cartData.put("updatedAt", FieldValue.serverTimestamp());
        cartData.put("quantity", FieldValue.increment(1));

        db.collection("users").document(uid)
                .collection("cart").document(productId)
                .set(cartData, SetOptions.merge())
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void toggleWishlist() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Login required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (loadedProduct == null) {
            Toast.makeText(this, "Loading product...", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        if (isInWishlist) {
            db.collection("users").document(uid)
                    .collection("wishlist").document(productId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        isInWishlist = false;
                        updateWishlistButton();
                        Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Map<String, Object> wish = new HashMap<>();
            wish.put("productId", loadedProduct.getId());
            wish.put("name", loadedProduct.getName());
            wish.put("category", loadedProduct.getCategory());
            wish.put("price", loadedProduct.getPrice());
            wish.put("imageUrl", loadedProduct.getImageUrl());
            wish.put("createdAt", FieldValue.serverTimestamp());

            db.collection("users").document(uid)
                    .collection("wishlist").document(productId)
                    .set(wish)
                    .addOnSuccessListener(unused -> {
                        isInWishlist = true;
                        updateWishlistButton();
                        Toast.makeText(this, "Added to wishlist", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
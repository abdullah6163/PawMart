package com.example.pawmart;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final String PREFS = "pawmart_cart_prefs";
    private static final String KEY_CART = "cart_json";

    private static CartManager instance;
    private final List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // ✅ call once when app starts (ex: MainActivity.onCreate)
    public void load(Context context) {
        cartItems.clear();
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_CART, "[]");

        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);

                Product p = new Product();
                p.setId(o.optString("id", ""));
                p.setName(o.optString("name", ""));
                p.setCategory(o.optString("category", ""));
                p.setPrice(o.optDouble("price", 0));
                p.setImageUrl(o.optString("imageUrl", ""));

                int qty = o.optInt("qty", 1);

                CartItem item = new CartItem(p, qty);
                cartItems.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void save(Context context) {
        JSONArray arr = new JSONArray();
        for (CartItem item : cartItems) {
            Product p = item.getProduct();

            JSONObject o = new JSONObject();
            try {
                o.put("id", p.getId());
                o.put("name", p.getName());
                o.put("category", p.getCategory());
                o.put("price", p.getPrice());
                o.put("imageUrl", p.getImageUrl());
                o.put("qty", item.getQuantity());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            arr.put(o);
        }

        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_CART, arr.toString()).apply();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addToCart(Product product, Context context) {
        if (product == null || product.getId() == null) return;

        for (CartItem item : cartItems) {
            if (product.getId().equals(item.getProduct().getId())) {
                item.setQuantity(item.getQuantity() + 1);
                save(context);
                return;
            }
        }

        cartItems.add(new CartItem(product, 1));
        save(context);
    }

    public void increaseQty(String productId, Context context) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + 1);
                save(context);
                return;
            }
        }
    }

    public void decreaseQty(String productId, Context context) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getProduct().getId().equals(productId)) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    cartItems.remove(i);
                }
                save(context);
                return;
            }
        }
    }

    public void remove(String productId, Context context) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProduct().getId().equals(productId)) {
                cartItems.remove(i);
                save(context);
                return;
            }
        }
    }

    public void clearCart(Context context) {
        cartItems.clear();
        save(context);
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }
}
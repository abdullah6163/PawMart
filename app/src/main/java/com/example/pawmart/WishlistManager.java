package com.example.pawmart;

import java.util.ArrayList;
import java.util.List;

public class WishlistManager {

    private static WishlistManager instance;
    private List<Product> wishlist;

    private WishlistManager() {
        wishlist = new ArrayList<>();
    }

    public static WishlistManager getInstance() {
        if (instance == null) {
            instance = new WishlistManager();
        }
        return instance;
    }

    public void addToWishlist(Product product) {
        if (!wishlist.contains(product)) {
            wishlist.add(product);
        }
    }

    public void removeFromWishlist(Product product) {
        wishlist.remove(product);
    }

    public List<Product> getWishlist() {
        return wishlist;
    }

    public boolean isInWishlist(Product product) {
        return wishlist.contains(product);
    }
}
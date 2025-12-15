package com.example.pawmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private RecyclerView recyclerWishlist;
    private ProductAdapter adapter;
    private final List<Product> list = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerWishlist = view.findViewById(R.id.recyclerWishlist);
        recyclerWishlist.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ProductAdapter(
                list,
                product -> addToCart(product),
                product -> openProductDetails(product)
        );

        recyclerWishlist.setAdapter(adapter);

        loadWishlist();
    }

    private void loadWishlist() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Login required", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .collection("wishlist")
                .get()
                .addOnSuccessListener(qs -> {
                    list.clear();

                    for (QueryDocumentSnapshot doc : qs) {
                        // Here doc is the wishlist item doc
                        // You are storing product data inside wishlist doc (name, price, imageUrl, etc)

                        String id = doc.getString("productId");
                        String name = doc.getString("name");
                        String cat = doc.getString("category");
                        Double priceD = doc.getDouble("price");
                        String imageUrl = doc.getString("imageUrl");

                        if (id == null) id = doc.getId();
                        if (name == null) name = "";
                        if (cat == null) cat = "";
                        if (priceD == null) priceD = 0.0;
                        if (imageUrl == null) imageUrl = "";

                        Product p = new Product();
                        p.setId(id);
                        p.setName(name);
                        p.setCategory(cat);
                        p.setPrice(priceD);
                        p.setImageUrl(imageUrl);

                        list.add(p);
                    }

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        Toast.makeText(getContext(), "Wishlist is empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void addToCart(Product product) {
        CartManager.getInstance().addToCart(product , requireContext());
        Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
    }

    private void openProductDetails(Product product) {
        if (getActivity() == null) return;

        if (product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(getContext(), "Product ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("productId", product.getId());
        startActivity(intent);
    }
}
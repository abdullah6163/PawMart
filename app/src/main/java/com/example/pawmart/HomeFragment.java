package com.example.pawmart;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;

    // Adapter list (what user sees)
    private final List<Product> productList = new ArrayList<>();

    // Master list (all loaded products)
    private final List<Product> allProducts = new ArrayList<>();

    private TextView catFood, catAccessories, catToys, catGrooming;
    private EditText editSearch;

    private FirebaseFirestore db;
    private CartManager cartManager;

    // Track current category
    private String currentCategory = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerProducts = view.findViewById(R.id.recyclerProducts);
        editSearch = view.findViewById(R.id.editSearch);

        catFood = view.findViewById(R.id.catFood);
        catAccessories = view.findViewById(R.id.catAccessories);
        catToys = view.findViewById(R.id.catToys);
        catGrooming = view.findViewById(R.id.catGrooming);

        recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        db = FirebaseFirestore.getInstance();
        cartManager = CartManager.getInstance();

        productAdapter = new ProductAdapter(
                productList,
                product -> {
                    cartManager.addToCart(product, requireContext());
                    Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                },
                this::openProductDetails
        );
        recyclerProducts.setAdapter(productAdapter);

        setupCategoryClicks();
        setupSearchBar();

        loadProductsFromFirestore(); // load all once
    }

    private void setupSearchBar() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryClicks() {
        catFood.setOnClickListener(v -> { currentCategory = "Food"; applyFilters(); });
        catAccessories.setOnClickListener(v -> { currentCategory = "Accessories"; applyFilters(); });
        catToys.setOnClickListener(v -> { currentCategory = "Toys"; applyFilters(); });
        catGrooming.setOnClickListener(v -> { currentCategory = "Grooming"; applyFilters(); });

        // If you want: click again to show all
        // Example: long press resets category
        catFood.setOnLongClickListener(v -> { currentCategory = null; applyFilters(); return true; });
        catAccessories.setOnLongClickListener(v -> { currentCategory = null; applyFilters(); return true; });
        catToys.setOnLongClickListener(v -> { currentCategory = null; applyFilters(); return true; });
        catGrooming.setOnLongClickListener(v -> { currentCategory = null; applyFilters(); return true; });
    }

    private void loadProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allProducts.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            if (p.getId() == null || p.getId().isEmpty()) p.setId(doc.getId());
                            allProducts.add(p);
                        }
                    }

                    applyFilters(); // show after loading
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void applyFilters() {
        String query = editSearch.getText() != null
                ? editSearch.getText().toString().trim().toLowerCase()
                : "";

        productList.clear();

        for (Product p : allProducts) {
            boolean matchCategory = (currentCategory == null)
                    || (p.getCategory() != null && p.getCategory().equalsIgnoreCase(currentCategory));

            boolean matchQuery = query.isEmpty()
                    || (p.getName() != null && p.getName().toLowerCase().contains(query))
                    || (p.getCategory() != null && p.getCategory().toLowerCase().contains(query));

            if (matchCategory && matchQuery) {
                productList.add(p);
            }
        }

        productAdapter.notifyDataSetChanged();
    }

    private void openProductDetails(Product product) {
        if (getActivity() == null) return;

        if (product.getId() == null || product.getId().isEmpty()) {
            Toast.makeText(getContext(), "Product ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.Intent i = new android.content.Intent(getActivity(), ProductDetailActivity.class);
        i.putExtra("productId", product.getId());
        startActivity(i);
    }
}
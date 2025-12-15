package com.example.pawmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerCart;
    private TextView textTotal;
    private Button btnCheckout;

    private CartAdapter cartAdapter;
    private CartManager cartManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerCart = view.findViewById(R.id.recyclerCart);
        textTotal = view.findViewById(R.id.textTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        recyclerCart.setLayoutManager(new LinearLayoutManager(getContext()));

        // ✅ Cart manager
        cartManager = CartManager.getInstance();

        // ✅ IMPORTANT: load persisted cart
        cartManager.load(requireContext());

        List<CartItem> cartItems = cartManager.getCartItems();

        // ✅ FIXED constructor (3 params)
        cartAdapter = new CartAdapter(
                cartItems,
                cartManager,
                this::updateTotal
        );

        recyclerCart.setAdapter(cartAdapter);

        updateTotal();

        btnCheckout.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CheckoutActivity.class));
        });
    }

    private void updateTotal() {
        double total = cartManager.getTotalPrice();
        textTotal.setText("Total: $" + String.format("%.2f", total));
    }
}
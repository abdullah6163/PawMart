package com.example.pawmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private final List<CartItem> cartItems;
    private final CartManager cartManager;
    private final OnCartChangedListener listener;

    public CartAdapter(List<CartItem> cartItems, CartManager cartManager, OnCartChangedListener listener) {
        this.cartItems = cartItems;
        this.cartManager = cartManager;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        holder.textName.setText(product.getName());
        holder.textCategory.setText(product.getCategory());
        holder.textQuantity.setText(String.valueOf(cartItem.getQuantity()));

        double lineTotal = product.getPrice() * cartItem.getQuantity();
        holder.textPrice.setText("$" + String.format("%.2f", lineTotal));

        Glide.with(holder.imageProduct.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_pet_welcome)
                .into(holder.imageProduct);

        holder.btnPlus.setOnClickListener(v -> {
            cartManager.increaseQty(product.getId(), v.getContext());
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) listener.onCartChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            cartManager.decreaseQty(product.getId(), v.getContext());
            notifyDataSetChanged(); // because item may be removed
            if (listener != null) listener.onCartChanged();
        });

        holder.btnRemove.setOnClickListener(v -> {
            cartManager.remove(product.getId(), v.getContext());
            notifyDataSetChanged();
            if (listener != null) listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView imageProduct;
        TextView textName, textCategory, textPrice, textQuantity;

        Button btnPlus, btnMinus;
        TextView btnRemove; // IMPORTANT: remove is TextView in your XML

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageCartProduct);
            textName = itemView.findViewById(R.id.textCartProductName);
            textCategory = itemView.findViewById(R.id.textCartProductCategory);
            textPrice = itemView.findViewById(R.id.textCartPrice);
            textQuantity = itemView.findViewById(R.id.textCartQuantity);

            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
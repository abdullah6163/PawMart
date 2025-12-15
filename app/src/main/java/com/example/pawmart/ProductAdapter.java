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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnAddToCartListener {
        void onAdd(Product product);
    }

    public interface OnProductClickListener {
        void onClick(Product product);
    }

    private final List<Product> productList;
    private final OnAddToCartListener addListener;
    private final OnProductClickListener clickListener;

    public ProductAdapter(List<Product> productList,
                          OnAddToCartListener addListener,
                          OnProductClickListener clickListener) {
        this.productList = productList;
        this.addListener = addListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.textProductName.setText(product.getName());
        holder.textProductCategory.setText(product.getCategory());
        holder.textProductPrice.setText("$" + String.format("%.2f", product.getPrice()));

        Glide.with(holder.imageProduct.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_pet_welcome)
                .into(holder.imageProduct);

        holder.btnAddToCart.setOnClickListener(v -> {
            if (addListener != null) addListener.onAdd(product);
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView textProductName, textProductCategory, textProductPrice;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductCategory = itemView.findViewById(R.id.textProductCategory);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
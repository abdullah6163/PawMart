package com.example.pawmart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvNameValue, tvEmailValue, tvAddressValue, tvChangeInfo, tvChangePic;
    private LinearLayout rowPromo, rowLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvNameValue = view.findViewById(R.id.tvNameValue);
        tvEmailValue = view.findViewById(R.id.tvEmailValue);
        tvAddressValue = view.findViewById(R.id.tvAddressValue);
        tvChangeInfo = view.findViewById(R.id.tvChangeInfo);
        tvChangePic = view.findViewById(R.id.tvChangePic);

        rowPromo = view.findViewById(R.id.rowPromo);
        rowLogout = view.findViewById(R.id.rowLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUser();

        tvChangeInfo.setOnClickListener(v -> showEditInfoDialog());
        rowPromo.setOnClickListener(v -> showPromoDialog());
        rowLogout.setOnClickListener(v -> logout());

        tvChangePic.setOnClickListener(v ->
                Toast.makeText(getContext(), "Profile picture update: later (Firebase Storage)", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        tvEmailValue.setText(user.getEmail() != null ? user.getEmail() : "—");

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("name");
                    String address = doc.getString("address");

                    if (name == null || name.isEmpty()) {
                        name = user.getDisplayName() != null ? user.getDisplayName() : "User";
                    }

                    tvNameValue.setText(name);
                    tvAddressValue.setText(address != null && !address.isEmpty() ? address : "—");
                })
                .addOnFailureListener(e -> {
                    String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
                    tvNameValue.setText(name);
                    tvAddressValue.setText("—");
                });
    }

    private void showEditInfoDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        EditText etName = new EditText(getContext());
        etName.setHint("Name");
        etName.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText etAddress = new EditText(getContext());
        etAddress.setHint("Address");
        etAddress.setInputType(InputType.TYPE_CLASS_TEXT);

        // Prefill current UI values
        etName.setText(tvNameValue.getText().toString().equals("—") ? "" : tvNameValue.getText().toString());
        etAddress.setText(tvAddressValue.getText().toString().equals("—") ? "" : tvAddressValue.getText().toString());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        layout.addView(etName);
        layout.addView(etAddress);

        new AlertDialog.Builder(getContext())
                .setTitle("Change Info")
                .setView(layout)
                .setPositiveButton("Save", (d, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newAddress = etAddress.getText().toString().trim();

                    Map<String, Object> map = new HashMap<>();
                    map.put("name", newName);
                    map.put("address", newAddress);

                    db.collection("users")
                            .document(user.getUid())
                            .set(map, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                tvNameValue.setText(newName.isEmpty() ? "—" : newName);
                                tvAddressValue.setText(newAddress.isEmpty() ? "—" : newAddress);
                                Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPromoDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Promo Code")
                .setMessage("Feature coming soon.\n(We’ll apply discount in checkout later.)")
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        // Firebase sign out
        auth.signOut();

        // Also sign out Google session so it doesn't auto-pick account next time
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient client = GoogleSignIn.getClient(requireContext(), gso);
        client.signOut().addOnCompleteListener(task -> {
            Intent i = new Intent(requireActivity(), WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}
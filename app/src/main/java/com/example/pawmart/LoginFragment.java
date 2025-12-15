package com.example.pawmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogle;
    private TextView tvForgotPassword, tvGoToSignup;

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);

        etEmail = v.findViewById(R.id.etEmail);
        etPassword = v.findViewById(R.id.etPassword);
        btnLogin = v.findViewById(R.id.btnLogin);
        btnGoogle = v.findViewById(R.id.btnGoogle);
        tvForgotPassword = v.findViewById(R.id.tvForgotPassword);
        tvGoToSignup = v.findViewById(R.id.tvGoToSignup);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> doLogin());

        tvGoToSignup.setOnClickListener(view -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).loadFragment(new SignupFragment());
            }
        });

        tvForgotPassword.setOnClickListener(view -> doResetPassword());

        btnGoogle.setOnClickListener(view -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).startGoogleSignIn(); // ✅ correct method
            }
        });

        return v;
    }

    private void doLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass  = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) { etEmail.setError("Enter email"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Invalid email"); return; }

        if (TextUtils.isEmpty(pass)) { etPassword.setError("Enter password"); return; }
        if (pass.length() < 6) { etPassword.setError("Minimum 6 characters"); return; }

        btnLogin.setEnabled(false);
        btnGoogle.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnGoogle.setEnabled(true);

                    if (!task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                task.getException() != null ? task.getException().getMessage() : "Login failed",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    }
                });
    }

    private void doResetPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email first");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Reset link sent to email", Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
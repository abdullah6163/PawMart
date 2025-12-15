package com.example.pawmart;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupFragment extends Fragment {

    private EditText signupName, signupEmail, signupPassword, signupConfirmPassword;
    private Button btnSignup;
    private TextView signupToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    // At least 8 chars, 1 number, 1 special
    private static final Pattern STRONG_PASSWORD =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         // at least 1 digit
                    "(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/\\\\|`~])" + // special
                    ".{8,}" +               // length >= 8
                    "$");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_signup, container, false);

        // ✅ IDs MUST match fragment_signup.xml
        signupName = v.findViewById(R.id.signupName);
        signupEmail = v.findViewById(R.id.signupEmail);
        signupPassword = v.findViewById(R.id.signupPassword);
        signupConfirmPassword = v.findViewById(R.id.signupConfirmPassword);
        btnSignup = v.findViewById(R.id.btnSignup);
        signupToLogin = v.findViewById(R.id.signupToLogin);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnSignup.setOnClickListener(view -> doSignup());

        signupToLogin.setOnClickListener(view -> goToLogin());

        return v;
    }

    private void doSignup() {
        String name = safeText(signupName);
        String email = safeText(signupEmail);
        String pass = safeText(signupPassword);
        String confirm = safeText(signupConfirmPassword);

        // validation
        if (TextUtils.isEmpty(name)) { signupName.setError("Enter name"); return; }

        if (TextUtils.isEmpty(email)) { signupEmail.setError("Enter email"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Invalid email");
            return;
        }

        if (TextUtils.isEmpty(pass)) { signupPassword.setError("Enter password"); return; }
        if (!STRONG_PASSWORD.matcher(pass).matches()) {
            signupPassword.setError("Min 8 chars + 1 number + 1 special char");
            return;
        }

        if (TextUtils.isEmpty(confirm)) { signupConfirmPassword.setError("Confirm password"); return; }
        if (!pass.equals(confirm)) {
            signupConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnSignup.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        btnSignup.setEnabled(true);
                        Toast.makeText(getContext(),
                                task.getException() != null ? task.getException().getMessage() : "Signup failed",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) {
                        btnSignup.setEnabled(true);
                        Toast.makeText(getContext(), "User creation error", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Save user profile to Firestore (optional but recommended)
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", user.getUid());
                    userMap.put("name", name);
                    userMap.put("email", email);
                    userMap.put("provider", "email");
                    userMap.put("createdAt", Timestamp.now());

                    firestore.collection("users")
                            .document(user.getUid())
                            .set(userMap)
                            .addOnSuccessListener(unused -> {
                                btnSignup.setEnabled(true);
                                Toast.makeText(getContext(), "Account created successfully", Toast.LENGTH_SHORT).show();
                                goToLogin();
                            })
                            .addOnFailureListener(e -> {
                                // Even if Firestore fails, account is created in Firebase Auth
                                btnSignup.setEnabled(true);
                                Toast.makeText(getContext(),
                                        "Account created, but profile save failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                goToLogin();
                            });
                });
    }

    private void goToLogin() {
        if (getActivity() instanceof AuthActivity) {
            ((AuthActivity) getActivity()).loadFragment(new LoginFragment());
        }
    }

    private String safeText(EditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
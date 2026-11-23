
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.model.AdminLoginRequest;
import com.autismcentre.model.AdminLoginResponse;
import com.autismcentre.model.ParentLoginRequest;
import com.autismcentre.model.ParentLoginResponse;
import com.autismcentre.repository.AuthRepository;
import com.autismcentre.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.TherapistLoginRequest;
import com.autismcentre.model.TherapistLoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "UserLogin";

    private MaterialButton btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private AuthRepository authRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        authRepository = new AuthRepository(this);
        sessionManager = new SessionManager(this);

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(
                        LoginActivity.this,
                        RegisterActivity.class
                ))
        );

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(
                        LoginActivity.this,
                        ForgotPasswordActivity.class
                ))
        );

        btnLogin.setOnClickListener(v -> {
            if (validateLogin()) {
                performLogin();
            }
        });
    }

    private boolean validateLogin() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (email.isEmpty()) {
            etEmail.setError("Enter Email");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter Valid Email");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Minimum 6 Characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performLogin() {
        String email = getText(etEmail).toLowerCase();
        String password = getText(etPassword);

        btnLogin.setEnabled(false);
        btnLogin.setText("Please wait...");

        ParentLoginRequest request =
                new ParentLoginRequest(email, password);

        authRepository.loginParent(
                request,
                new Callback<ParentLoginResponse>() {
                    @Override
                    public void onResponse(
                            Call<ParentLoginResponse> call,
                            Response<ParentLoginResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {


                            ParentLoginResponse result =
                                    response.body();

                            if ("PARENT".equalsIgnoreCase(
                                    result.getRole())) {

                                saveParentSession(result);
                                return;
                            }

                            // Unexpected role: try Admin endpoint.
                            performAdminLogin(email, password);
                            return;
                        }

                        // Parent credentials failed; try Admin login.
                        performAdminLogin(email, password);
                    }

                    @Override
                    public void onFailure(
                            Call<ParentLoginResponse> call,
                            Throwable t) {

                        Log.e(TAG, "Parent login request failed", t);

                        if (!isFinishing() && !isDestroyed()) {
                            finishLoginWithError(
                                    "Server se connect nahi ho pa raha. Internet check karein."
                            );
                        }
                    }
                }
        );
    }

    private void performTherapistLogin(String email, String password) {

        TherapistLoginRequest request =
                new TherapistLoginRequest(email, password);

        ApiClient.getApiService()
                .loginTherapist(request)
                .enqueue(new retrofit2.Callback<TherapistLoginResponse>() {

                    @Override
                    public void onResponse(
                            retrofit2.Call<TherapistLoginResponse> call,
                            retrofit2.Response<TherapistLoginResponse> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            finishLoginWithError("Email ya password galat hai.");
                            return;
                        }

                        TherapistLoginResponse result = response.body();

                        if (result.getToken() == null
                                || result.getToken().trim().isEmpty()
                                || result.getUserId() == null
                                || result.getTherapistId() == null
                                || result.getTherapistId() <= 0
                                || result.getBranchId() == null
                                || result.getBranchId() <= 0
                                || result.getRole() == null
                                || !"THERAPIST".equalsIgnoreCase(result.getRole())) {

                            finishLoginWithError(
                                    "Therapist login response invalid hai."
                            );
                            return;
                        }

                        sessionManager.saveTherapistSession(
                                result.getToken(),
                                result.getUserId(),
                                result.getTherapistId(),
                                result.getBranchId(),
                                result.getBranchName(),
                                result.getTherapyType(),
                                result.getName(),
                                result.getEmail(),
                                result.getRole()
                        );

                        if (!sessionManager.hasValidTherapistSession()) {
                            sessionManager.clearSession();
                            finishLoginWithError(
                                    "Therapist session save nahi hua."
                            );
                            return;
                        }

                        Toast.makeText(
                                LoginActivity.this,
                                "Therapist login successful",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent intent = new Intent(
                                LoginActivity.this,
                                TherapistHomeDashboardActivity.class
                        );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<TherapistLoginResponse> call,
                            Throwable t) {

                        Log.e(
                                "TherapistLogin",
                                "Therapist login API failed",
                                t
                        );

                        finishLoginWithError(
                                "Server connection error. Please try again."
                        );
                    }
                });
    }

    private void performAdminLogin(
            String email,
            String password) {

        AdminLoginRequest request =
                new AdminLoginRequest(email, password);

        authRepository.loginAdmin(
                request,
                new Callback<AdminLoginResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminLoginResponse> call,
                            Response<AdminLoginResponse> response) {


                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            performTherapistLogin(email, password);
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            AdminLoginResponse result =
                                    response.body();

                            if (result.getToken() == null
                                    || result.getToken().trim().isEmpty()
                                    || !"ADMIN".equalsIgnoreCase(
                                    result.getRole())) {

                                finishLoginWithError(
                                        "Admin login response invalid hai."
                                );
                                return;
                            }

                            sessionManager.saveSession(
                                    result.getToken(),
                                    result.getUserId(),
                                    null,
                                    result.getName(),
                                    result.getEmail(),
                                    result.getRole()
                            );

                            if (!sessionManager
                                    .hasValidSessionForRole("ADMIN")) {

                                sessionManager.clearSession();

                                finishLoginWithError(
                                        "Admin session save nahi hua."
                                );
                                return;
                            }

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Admin login successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    AdminHomeDashboardActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();

                        } else {
                            Log.w(TAG,
                                    "Admin login failed. HTTP "
                                            + response.code());

                            finishLoginWithError(
                                    "Email ya password galat hai."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminLoginResponse> call,
                            Throwable t) {

                        Log.e(TAG, "Admin login request failed", t);

                        if (!isFinishing() && !isDestroyed()) {
                            finishLoginWithError(
                                    "Server se connect nahi ho pa raha. Internet check karein."
                            );
                        }
                    }
                }
        );
    }

    private void saveParentSession(
            ParentLoginResponse result) {

        if (result.getToken() == null
                || result.getToken().trim().isEmpty()
                || !"PARENT".equalsIgnoreCase(result.getRole())) {

            finishLoginWithError(
                    "Parent login response invalid hai."
            );
            return;
        }

        sessionManager.saveSession(
                result.getToken(),
                result.getUserId(),
                result.getParentId(),
                result.getName(),
                result.getEmail(),
                result.getRole()
        );

        if (!sessionManager.hasValidSessionData()) {
            sessionManager.clearSession();

            finishLoginWithError(
                    "Parent session save nahi hua."
            );
            return;
        }

        Toast.makeText(
                this,
                "Login successful",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                LoginActivity.this,
                NewParentHomeActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void finishLoginWithError(String message) {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");

        Toast.makeText(
                LoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }
}

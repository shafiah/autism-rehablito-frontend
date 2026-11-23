
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentResetPasswordRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnResetPassword;

    private ApiService apiService;
    private String resetToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        resetToken = getIntent().getStringExtra("reset_token");

        if (resetToken == null || resetToken.trim().isEmpty()) {
            Toast.makeText(this,
                    "Reset session expired. Please try again.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ApiClient.initialize(getApplicationContext());
        apiService = ApiClient.getApiService();

        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {

        if (!validatePassword()) {
            return;
        }

        String newPassword = etNewPassword.getText().toString().trim();

        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");

        ParentResetPasswordRequest request =
                new ParentResetPasswordRequest(resetToken, newPassword);

        apiService.resetParentPassword(request)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call,
                                           Response<Void> response) {

                        btnResetPassword.setEnabled(true);
                        btnResetPassword.setText("Reset Password");

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    ResetPasswordActivity.this,
                                    "Password Reset Successfully",
                                    Toast.LENGTH_LONG
                            ).show();

                            Intent intent = new Intent(
                                    ResetPasswordActivity.this,
                                    LoginActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(
                                    ResetPasswordActivity.this,
                                    "Password reset failed. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                        btnResetPassword.setEnabled(true);
                        btnResetPassword.setText("Reset Password");

                        Toast.makeText(
                                ResetPasswordActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private boolean validatePassword() {

        String password = etNewPassword.getText().toString().trim();
        String confirmPassword =
                etConfirmPassword.getText().toString().trim();

        if (password.isEmpty()) {
            etNewPassword.setError("Enter Password");
            return false;
        }

        if (password.length() < 6) {
            etNewPassword.setError("Minimum 6 Characters");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Confirm Password");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Password Doesn't Match");
            return false;
        }

        return true;
    }
}

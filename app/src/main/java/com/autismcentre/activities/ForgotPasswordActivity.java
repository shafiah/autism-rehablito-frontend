
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentForgotPasswordRequest;
import com.autismcentre.model.ParentForgotPasswordResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private MaterialButton btnSendOtp;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        // Initialize API
        ApiClient.initialize(getApplicationContext());
        apiService = ApiClient.getApiService();

        btnSendOtp.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {

        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Enter Email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {

            etEmail.setError("Enter Valid Email");
            return;
        }

        btnSendOtp.setEnabled(false);
        btnSendOtp.setText("Sending...");

        ParentForgotPasswordRequest request =
                new ParentForgotPasswordRequest(email);

        apiService.forgotParentPassword(request)
                .enqueue(new Callback<ParentForgotPasswordResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentForgotPasswordResponse> call,
                            Response<ParentForgotPasswordResponse> response) {

                        btnSendOtp.setEnabled(true);
                        btnSendOtp.setText("Send OTP");

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    ForgotPasswordActivity.this,
                                    OTPVerificationActivity.class
                            );

                            intent.putExtra("parent_email", email);

                            startActivity(intent);

                        } else {

                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Request failed. Please check your email.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentForgotPasswordResponse> call,
                            Throwable t) {

                        btnSendOtp.setEnabled(true);
                        btnSendOtp.setText("Send OTP");

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}

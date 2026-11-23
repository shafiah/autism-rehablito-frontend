
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentVerifyOtpRequest;
import com.autismcentre.model.ParentVerifyOtpResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText etOtp;
    private MaterialButton btnVerifyOtp;
    private TextView tvResendOtp;

    private ApiService apiService;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        // Get email from Forgot Password screen
        email = getIntent().getStringExtra("parent_email");

        if (email == null || email.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Email not found. Please try again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        // Initialize API
        ApiClient.initialize(getApplicationContext());
        apiService = ApiClient.getApiService();

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());

        // Resend OTP will be integrated in next step
        tvResendOtp.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Resend OTP will be available soon.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void verifyOtp() {

        String otp = etOtp.getText().toString().trim();

        if (otp.isEmpty()) {
            etOtp.setError("Enter OTP");
            return;
        }

        if (!otp.matches("[0-9]{6}")) {
            etOtp.setError("Enter valid 6 digit OTP");
            return;
        }

        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Verifying...");

        ParentVerifyOtpRequest request =
                new ParentVerifyOtpRequest(email, otp);

        apiService.verifyParentOtp(request)
                .enqueue(new Callback<ParentVerifyOtpResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentVerifyOtpResponse> call,
                            Response<ParentVerifyOtpResponse> response) {

                        btnVerifyOtp.setEnabled(true);
                        btnVerifyOtp.setText("Verify OTP");

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getResetToken() != null) {

                            String resetToken =
                                    response.body().getResetToken();

                            Toast.makeText(
                                    OTPVerificationActivity.this,
                                    "OTP verified successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent = new Intent(
                                    OTPVerificationActivity.this,
                                    ResetPasswordActivity.class
                            );

                            intent.putExtra("reset_token", resetToken);

                            startActivity(intent);
                            finish();

                        } else {

                            Toast.makeText(
                                    OTPVerificationActivity.this,
                                    "Invalid OTP or verification failed",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentVerifyOtpResponse> call,
                            Throwable t) {

                        btnVerifyOtp.setEnabled(true);
                        btnVerifyOtp.setText("Verify OTP");

                        Toast.makeText(
                                OTPVerificationActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}


package com.autismcentre.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentFeedbackRequest;
import com.autismcentre.model.ParentFeedbackResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackFormActivity extends BaseParentActivity {

    private static final String TAG = "FeedbackFormActivity";

    private TextView tvTherapistName;
    private TextView tvSpecialization;
    private TextView tvFeedbackChildName;

    private RatingBar ratingBar;
    private TextInputEditText etFeedback;

    private MaterialButton btnSubmitFeedback;

    private ApiService apiService;

    private Long childId;
    private Long therapistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_feedback_form);

        // =====================================================
        // API INITIALIZATION
        // =====================================================

        ApiClient.initialize(getApplicationContext());
        apiService = ApiClient.getApiService();

        // =====================================================
        // INITIALIZE VIEWS
        // =====================================================

        tvTherapistName = findViewById(R.id.tvTherapistName);
        tvSpecialization = findViewById(R.id.tvSpecialization);
        tvFeedbackChildName = findViewById(R.id.tvFeedbackChildName);

        ratingBar = findViewById(R.id.ratingBar);
        etFeedback = findViewById(R.id.etFeedback);

        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        // =====================================================
        // RECEIVE INTENT DATA
        // =====================================================

        childId = getIntent().getLongExtra("childId", -1L);
        therapistId = getIntent().getLongExtra("therapistId", -1L);

        String therapistName =
                getIntent().getStringExtra("therapistName");

        String specialization =
                getIntent().getStringExtra("specialization");

        String childName =
                getIntent().getStringExtra("childName");

        // =====================================================
        // DISPLAY DETAILS
        // =====================================================

        if (therapistName != null && !therapistName.trim().isEmpty()) {
            tvTherapistName.setText(therapistName);
        }

        if (specialization != null && !specialization.trim().isEmpty()) {
            tvSpecialization.setText(specialization);
        }

        if (childName != null && !childName.trim().isEmpty()) {
            tvFeedbackChildName.setText("Feedback for " + childName);
        } else {
            tvFeedbackChildName.setText("Feedback for Child");
        }

        // =====================================================
        // SUBMIT FEEDBACK
        // =====================================================

        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    // =====================================================
    // SUBMIT FEEDBACK API
    // =====================================================

    private void submitFeedback() {

        if (childId == null || childId <= 0
                || therapistId == null || therapistId <= 0) {

            Toast.makeText(
                    this,
                    "Child or therapist details unavailable. Please try again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        int rating = Math.round(ratingBar.getRating());

        String feedback = etFeedback.getText() != null
                ? etFeedback.getText().toString().trim()
                : "";

        // Rating validation
        if (rating < 1 || rating > 5) {

            Toast.makeText(
                    this,
                    "Please select a rating",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Feedback validation
        if (feedback.isEmpty()) {

            etFeedback.setError("Enter your feedback");
            etFeedback.requestFocus();

            return;
        }

        if (feedback.length() > 500) {

            etFeedback.setError("Feedback cannot exceed 500 characters");
            etFeedback.requestFocus();

            return;
        }

        // =====================================================
        // CREATE REQUEST
        // =====================================================

        ParentFeedbackRequest request =
                new ParentFeedbackRequest(
                        childId,
                        therapistId,
                        rating,
                        feedback
                );

        // Prevent duplicate submission
        btnSubmitFeedback.setEnabled(false);
        btnSubmitFeedback.setText("Submitting...");

        // =====================================================
        // API CALL
        // =====================================================

        apiService.submitParentFeedback(request)
                .enqueue(new Callback<ParentFeedbackResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentFeedbackResponse> call,
                            Response<ParentFeedbackResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnSubmitFeedback.setEnabled(true);
                        btnSubmitFeedback.setText("Submit Feedback");

                        if (response.isSuccessful()
                                && response.body() != null) {

                            Log.d(
                                    TAG,
                                    "Feedback submitted successfully. Feedback ID: "
                                            + response.body().getFeedbackId()
                            );

                            Toast.makeText(
                                    FeedbackFormActivity.this,
                                    "Feedback submitted successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();

                        } else {

                            Log.e(
                                    TAG,
                                    "Feedback submission failed. HTTP: "
                                            + response.code()
                            );

                            Toast.makeText(
                                    FeedbackFormActivity.this,
                                    "Unable to submit feedback. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentFeedbackResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnSubmitFeedback.setEnabled(true);
                        btnSubmitFeedback.setText("Submit Feedback");

                        Log.e(
                                TAG,
                                "Feedback API request failed",
                                t
                        );

                        Toast.makeText(
                                FeedbackFormActivity.this,
                                "Network error. Please try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // =====================================================
    // BOTTOM NAVIGATION
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_feedback;
    }
}

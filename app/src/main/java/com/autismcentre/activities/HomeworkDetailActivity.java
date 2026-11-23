
package com.autismcentre.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentHomeworkRemarkRequest;
import com.autismcentre.model.ParentHomeworkResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeworkDetailActivity extends BaseParentActivity {

    private static final String TAG = "HomeworkDetailActivity";

    private TextView tvHomeworkTitle;
    private TextView tvHomeworkStatus;
    private TextView tvTherapistName;
    private TextView tvAssignedDate;
    private TextView tvDueDate;
    private TextView tvInstructions;
    private TextView tvTherapistRemarks;

    private EditText etParentRemarks;
    private MaterialButton btnSubmitParentRemarks;

    private Long childId;
    private Long homeworkId;

    private String savedParentRemarks = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_homework_detail);
        setPageTitle("Autism Centre");

        ApiClient.initialize(getApplicationContext());

        childId = getIntent().getLongExtra("childId", -1);
        homeworkId = getIntent().getLongExtra("homeworkId", -1);

        initializeViews();

        if (childId == -1 || homeworkId == -1) {
            Toast.makeText(this,
                    "Invalid homework details",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSubmitParentRemarks.setOnClickListener(v -> submitParentRemarks());

        loadHomeworkDetails();
    }

    private void initializeViews() {

        tvHomeworkTitle = findViewById(R.id.tvHomeworkTitle);
        tvHomeworkStatus = findViewById(R.id.tvHomeworkStatus);
        tvTherapistName = findViewById(R.id.tvTherapistName);
        tvAssignedDate = findViewById(R.id.tvAssignedDate);
        tvDueDate = findViewById(R.id.tvDueDate);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvTherapistRemarks = findViewById(R.id.tvTherapistRemarks);

        etParentRemarks = findViewById(R.id.etParentRemarks);
        btnSubmitParentRemarks = findViewById(R.id.btnSubmitParentRemarks);
    }

    private void loadHomeworkDetails() {

        ApiClient.getApiService()
                .getMyChildHomeworkById(childId, homeworkId)
                .enqueue(new Callback<ParentHomeworkResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentHomeworkResponse> call,
                            Response<ParentHomeworkResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            displayHomework(response.body());
                        } else {
                            showError("Unable to load homework details");
                            Log.e(TAG, "Load failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentHomeworkResponse> call,
                            Throwable t) {

                        Log.e(TAG, "Homework API error", t);
                        showError("Network error. Please try again.");
                    }
                });
    }

    private void displayHomework(ParentHomeworkResponse homework) {

        tvHomeworkTitle.setText(
                displayValue(homework.getTitle(), "Homework")
        );

        tvHomeworkStatus.setText(
                displayValue(homework.getStatus(), "Not Available")
        );

        tvTherapistName.setText(
                displayValue(homework.getTherapistName(), "Not Available")
        );

        tvAssignedDate.setText(
                displayValue(homework.getAssignedDate(), "Not Available")
        );

        tvDueDate.setText(
                displayValue(homework.getDueDate(), "Not Available")
        );

        tvInstructions.setText(
                displayValue(homework.getInstructions(),
                        "No instructions provided.")
        );

        tvTherapistRemarks.setText(
                displayValue(homework.getTherapistRemarks(),
                        "No remarks from therapist yet.")
        );

        savedParentRemarks = homework.getParentRemarks() == null
                ? ""
                : homework.getParentRemarks();

// Keep the input box empty after loading saved homework.
        etParentRemarks.setText("");

        btnSubmitParentRemarks.setText(
                TextUtils.isEmpty(savedParentRemarks)
                        ? "Submit Remarks"
                        : "Update Remarks"
        );
    }

    private void submitParentRemarks() {

        String remarks = etParentRemarks.getText()
                .toString()
                .trim();

        if (TextUtils.isEmpty(remarks)) {
            etParentRemarks.setError("Please enter your remarks");
            etParentRemarks.requestFocus();
            return;
        }

        if (remarks.length() > 2000) {
            etParentRemarks.setError(
                    "Remarks cannot exceed 2000 characters"
            );
            etParentRemarks.requestFocus();
            return;
        }

        btnSubmitParentRemarks.setEnabled(false);
        btnSubmitParentRemarks.setText("Saving...");

        ParentHomeworkRemarkRequest request =
                new ParentHomeworkRemarkRequest(remarks);

        ApiClient.getApiService()
                .updateMyChildHomeworkRemark(
                        childId,
                        homeworkId,
                        request
                )
                .enqueue(new Callback<ParentHomeworkResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentHomeworkResponse> call,
                            Response<ParentHomeworkResponse> response) {

                        btnSubmitParentRemarks.setEnabled(true);

                        if (response.isSuccessful()
                                && response.body() != null) {

                            displayHomework(response.body());

                            Toast.makeText(
                                    HomeworkDetailActivity.this,
                                    "Remarks saved successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            btnSubmitParentRemarks.setText(
                                    TextUtils.isEmpty(savedParentRemarks)
                                            ? "Submit Remarks"
                                            : "Update Remarks"
                            );

                            Log.e(TAG,
                                    "Remark save failed: " + response.code());

                            Toast.makeText(
                                    HomeworkDetailActivity.this,
                                    "Unable to save remarks. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentHomeworkResponse> call,
                            Throwable t) {

                        btnSubmitParentRemarks.setEnabled(true);

                        btnSubmitParentRemarks.setText(
                                TextUtils.isEmpty(savedParentRemarks)
                                        ? "Submit Remarks"
                                        : "Update Remarks"
                        );

                        Log.e(TAG, "Remark API error", t);

                        Toast.makeText(
                                HomeworkDetailActivity.this,
                                "Network error. Please try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private String displayValue(String value, String fallback) {

        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value;
    }

    private void showError(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_homework;
    }
}

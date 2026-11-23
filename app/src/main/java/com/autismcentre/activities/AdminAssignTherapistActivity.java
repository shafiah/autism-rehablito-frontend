package com.autismcentre.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminTherapistResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.autismcentre.model.AdminChildTherapistResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAssignTherapistActivity extends BaseAdminActivity {

    private TextView tvChildName;
    private TextView tvCurrentTherapist;
    private MaterialAutoCompleteTextView actSelectTherapist;
    private TextView tvTherapistSpecialization;
    private TextView tvTherapistAvailability;
    private MaterialButton btnAssignTherapist;

    private ApiService apiService;

    private Long childId;
    private Long branchId;

    private String selectedTherapist = "";
    private String selectedTherapyType = "";
    private Long selectedTherapistId;

    private final List<AdminTherapistResponse> activeTherapists =
            new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_assign_therapist
        );

        apiService = ApiClient.getApiService();

        initializeAssignTherapistViews();

        if (!loadChildInformation()) {
            return;
        }

        setupTherapistDropdown();
        setupAssignTherapistButton();
    }

    private void initializeAssignTherapistViews() {
        tvChildName = findViewById(R.id.tvAssignChildName);
        tvCurrentTherapist = findViewById(R.id.tvCurrentTherapist);
        actSelectTherapist = findViewById(R.id.actSelectTherapist);
        tvTherapistSpecialization =
                findViewById(R.id.tvTherapistSpecialization);
        tvTherapistAvailability =
                findViewById(R.id.tvTherapistAvailability);
        btnAssignTherapist = findViewById(R.id.btnAssignTherapist);
    }

    private boolean loadChildInformation() {
        Intent intent = getIntent();

        childId = intent.getLongExtra("child_id", -1L);
        branchId = intent.hasExtra("branch_id")
                ? intent.getLongExtra("branch_id", -1L)
                : null;

        if (childId == null || childId <= 0
                || branchId == null || branchId <= 0) {

            Toast.makeText(
                    this,
                    "Child ID or branch ID not available. Please reopen Child Details.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return false;
        }

        String childName = intent.getStringExtra("child_name");
        String currentTherapist =
                intent.getStringExtra("current_therapist");

        tvChildName.setText(
                childName != null && !childName.trim().isEmpty()
                        ? childName
                        : "Not available"
        );

        tvCurrentTherapist.setText(
                currentTherapist != null
                        && !currentTherapist.trim().isEmpty()
                        ? currentTherapist
                        : "Not assigned"
        );

        return true;
    }

    private void setupTherapistDropdown() {
        actSelectTherapist.setEnabled(false);
        actSelectTherapist.setText("Loading therapists...", false);

        apiService.getAdminTherapists(branchId).enqueue(
                new Callback<List<AdminTherapistResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<AdminTherapistResponse>> call,
                            Response<List<AdminTherapistResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            actSelectTherapist.setText("", false);

                            Toast.makeText(
                                    AdminAssignTherapistActivity.this,
                                    "Failed to load therapists. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        activeTherapists.clear();

                        for (AdminTherapistResponse therapist
                                : response.body()) {

                            if (therapist == null
                                    || therapist.getTherapistId() == null
                                    || !Boolean.TRUE.equals(
                                    therapist.getActive())
                                    || therapist.getBranchId() == null
                                    || !branchId.equals(
                                    therapist.getBranchId())) {
                                continue;
                            }

                            activeTherapists.add(therapist);
                        }

                        List<String> therapistNames =
                                new ArrayList<>();

                        for (AdminTherapistResponse therapist
                                : activeTherapists) {

                            String name = therapist.getName();

                            therapistNames.add(
                                    name == null || name.trim().isEmpty()
                                            ? "Name not available"
                                            : name
                            );
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        AdminAssignTherapistActivity.this,
                                        android.R.layout
                                                .simple_dropdown_item_1line,
                                        therapistNames
                                );

                        actSelectTherapist.setAdapter(adapter);
                        actSelectTherapist.setText("", false);
                        actSelectTherapist.setEnabled(
                                !activeTherapists.isEmpty()
                        );

                        actSelectTherapist.setOnItemClickListener(
                                (parent, view, position, id) -> {

                                    if (position < 0
                                            || position >=
                                            activeTherapists.size()) {
                                        return;
                                    }

                                    AdminTherapistResponse therapist =
                                            activeTherapists.get(position);

                                    selectedTherapistId = therapist.getTherapistId();

                                    selectedTherapist =
                                            therapist.getName() == null
                                                    ? ""
                                                    : therapist.getName();

                                    selectedTherapyType =
                                            therapist.getTherapyType() == null
                                                    ? ""
                                                    : therapist.getTherapyType();
                                    tvTherapistSpecialization.setText(
                                            "Specialization: "
                                                    + displayValue(
                                                    therapist.getTherapyType())
                                    );

                                    tvTherapistAvailability.setText(
                                            "Availability: "
                                                    + displayValue(
                                                    therapist.getAvailability())
                                    );
                                }
                        );

                        if (activeTherapists.isEmpty()) {
                            Toast.makeText(
                                    AdminAssignTherapistActivity.this,
                                    "No active therapists found for this branch",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminTherapistResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        actSelectTherapist.setText("", false);

                        Toast.makeText(
                                AdminAssignTherapistActivity.this,
                                "Therapist network error: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private String displayValue(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not available"
                : value;
    }

    private void setupAssignTherapistButton() {
        btnAssignTherapist.setOnClickListener(v -> {

            if (selectedTherapistId == null
                    || selectedTherapist.isEmpty()) {

                Toast.makeText(
                        AdminAssignTherapistActivity.this,
                        "Please select a therapist",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            showAssignConfirmation();
        });
    }

    private void showAssignConfirmation() {
        String message = "Assign "
                + selectedTherapist
                + " to "
                + tvChildName.getText().toString()
                + "?";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Therapist Assignment")
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Assign",
                        (dialog, which) ->
                                completeTherapistAssignment()
                )
                .show();
    }

    /*
     * Temporary existing result behavior.
     * Real backend assignment will be connected in the next step.
     */

    private void completeTherapistAssignment() {

        if (childId == null || selectedTherapistId == null) {
            Toast.makeText(
                    this,
                    "Child or therapist ID is missing",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        btnAssignTherapist.setEnabled(false);
        btnAssignTherapist.setText("Assigning...");

        apiService.assignTherapistToChild(
                childId,
                selectedTherapistId
        ).enqueue(new Callback<AdminChildTherapistResponse>() {

            @Override
            public void onResponse(
                    Call<AdminChildTherapistResponse> call,
                    Response<AdminChildTherapistResponse> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnAssignTherapist.setEnabled(true);
                btnAssignTherapist.setText("Assign Therapist");

                if (response.isSuccessful()
                        && response.body() != null) {

                    AdminChildTherapistResponse assignment =
                            response.body();

                    if (!Boolean.TRUE.equals(assignment.getActive())) {
                        Toast.makeText(
                                AdminAssignTherapistActivity.this,
                                "Assignment was not confirmed as active",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    Intent resultIntent = new Intent();

                    String therapistName =
                            assignment.getTherapistName() != null
                                    ? assignment.getTherapistName()
                                    : selectedTherapist;

                    String therapyType =
                            assignment.getTherapyType() != null
                                    ? assignment.getTherapyType()
                                    : selectedTherapyType;

                    resultIntent.putExtra(
                            "assigned_therapist",
                            therapistName
                    );

                    resultIntent.putExtra(
                            "therapy_type",
                            therapyType
                    );

                    setResult(Activity.RESULT_OK, resultIntent);

                    Toast.makeText(
                            AdminAssignTherapistActivity.this,
                            "Therapist assigned successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();

                } else {

                    String errorMessage = "No detailed error returned";

                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (java.io.IOException e) {
                        errorMessage = e.getMessage();
                    }

                    android.util.Log.e(
                            "TherapistAssignment",
                            "HTTP " + response.code() + ": " + errorMessage
                    );

                    Toast.makeText(
                            AdminAssignTherapistActivity.this,
                            "Assignment failed: " + errorMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<AdminChildTherapistResponse> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnAssignTherapist.setEnabled(true);
                btnAssignTherapist.setText("Assign Therapist");

                Toast.makeText(
                        AdminAssignTherapistActivity.this,
                        "Assignment network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

}
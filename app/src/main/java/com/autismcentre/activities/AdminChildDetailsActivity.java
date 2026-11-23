package com.autismcentre.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminChildTherapistResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.widget.Toast;

import com.autismcentre.model.AdminChildResponse;
import com.autismcentre.repository.AdminChildRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChildDetailsActivity extends BaseAdminActivity {

    private TextView tvChildName;
    private TextView tvChildStatus;
    private TextView tvChildAge;
    private TextView tvParentName;
    private TextView tvParentContact;
    private TextView tvTherapistName;
    private TextView tvTherapyType;
    private TextView tvTherapySchedule;
    private TextView tvPaymentStatus;

    private MaterialButton btnEditChild;
    private MaterialButton btnChangeChildStatus;
    private MaterialButton btnChangeTherapist;
    private TextView tvChildBranch;

    private AdminChildRepository adminChildRepository;
    private MaterialButton btnUnassignTherapist;
    private Long childId;
    private Long childBranchId;

    /*
     * Temporary frontend status.
     * Backend integration ke baad ye value API se aayegi.
     */
    private boolean isChildActive = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_child_details
        );

        adminChildRepository = new AdminChildRepository();

        childId = getIntent().getLongExtra("child_id", -1L);

        if (childId == -1L) {
            Toast.makeText(
                    this,
                    "Child ID not found",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        initializeChildDetailsViews();
        loadChildDetailsFromApi();
        loadAssignedTherapist();
        setupEditChildButton();
        setupChildStatusButton();
        setupChangeTherapistButton();
        setupUnassignTherapistButton();

    }

    private void loadChildDetailsFromApi() {

        adminChildRepository.getAdminChildById(
                childId,
                new Callback<AdminChildResponse>() {

                    @Override
                    public void onResponse(
                            Call<AdminChildResponse> call,
                            Response<AdminChildResponse> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null) {

                            AdminChildResponse child = response.body();
                            childBranchId = child.getBranchId();

                            isChildActive = Boolean.TRUE.equals(child.getActive());

                            updateChildStatusUI();

                            // Child name
                            tvChildName.setText(
                                    child.getChildName() != null
                                            ? child.getChildName()
                                            : "Not available"
                            );

                            // Active / inactive status
                            boolean active = Boolean.TRUE.equals(
                                    child.getActive()
                            );

                            tvChildStatus.setText(
                                    active ? "Active" : "Inactive"
                            );

                            // Date of birth
                            tvChildAge.setText(
                                    "Date of Birth: "
                                            + (child.getDateOfBirth() != null
                                            ? child.getDateOfBirth()
                                            : "Not available")
                            );

                            // Branch
                            tvChildBranch.setText(
                                    child.getBranchName() != null
                                            ? child.getBranchName()
                                            : "Not assigned"
                            );

                            // Parent name
                            tvParentName.setText(
                                    child.getParentName() != null
                                            ? child.getParentName()
                                            : "Not available"
                            );

                            // Parent contact
                            String contact =
                                    child.getParentMobileNumber() != null
                                            ? child.getParentMobileNumber()
                                            : "Not available";

                            tvParentContact.setText(contact);

                            // Therapy types
                           /* if (child.getTherapyTypes() != null
                                    && !child.getTherapyTypes().isEmpty()) {

                                tvTherapyType.setText(
                                        android.text.TextUtils.join(
                                                ", ",
                                                child.getTherapyTypes()
                                        )
                                );

                            } else {
                                tvTherapyType.setText("Not assigned");
                            } */

                            // These fields are not yet supplied by this API.
                           // tvTherapistName.setText("Not assigned");
                            tvTherapySchedule.setText("Not available");
                            tvPaymentStatus.setText("Not available");

                            // Medical notes: show them if a suitable view exists
                            // in your XML. Do not use an unrelated existing ID.

                        } else {
                            Toast.makeText(
                                    AdminChildDetailsActivity.this,
                                    "Failed to load child. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminChildResponse> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                AdminChildDetailsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void loadAssignedTherapist() {

        com.autismcentre.api.ApiService apiService =
                com.autismcentre.api.ApiClient.getApiService();

        apiService.getTherapistsByChild(childId).enqueue(
                new Callback<java.util.List<
                        com.autismcentre.model.AdminChildTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<java.util.List<
                                    com.autismcentre.model.AdminChildTherapistResponse>>
                                    call,
                            Response<java.util.List<
                                    com.autismcentre.model.AdminChildTherapistResponse>>
                                    response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            android.util.Log.e(
                                    "ChildTherapist",
                                    "Failed to load assignments. HTTP "
                                            + response.code()
                            );
                            return;
                        }

                        StringBuilder therapistNames = new StringBuilder();
                        StringBuilder therapyTypes = new StringBuilder();

                        for (com.autismcentre.model.AdminChildTherapistResponse assignment
                                : response.body()) {

                            if (assignment == null
                                    || !Boolean.TRUE.equals(
                                    assignment.getActive())) {
                                continue;
                            }

                            String name = assignment.getTherapistName();
                            String type = assignment.getTherapyType();

                            if (name != null && !name.trim().isEmpty()) {
                                if (therapistNames.length() > 0) {
                                    therapistNames.append("\n");
                                }
                                therapistNames.append(name.trim());
                            }

                            if (type != null && !type.trim().isEmpty()) {
                                String cleanType = type.trim();

                                boolean alreadyAdded = false;
                                for (String existing :
                                        therapyTypes.toString().split("\n")) {
                                    if (existing.equalsIgnoreCase(cleanType)) {
                                        alreadyAdded = true;
                                        break;
                                    }
                                }

                                if (!alreadyAdded) {
                                    if (therapyTypes.length() > 0) {
                                        therapyTypes.append("\n");
                                    }
                                    therapyTypes.append(cleanType);
                                }
                            }
                        }

                        tvTherapistName.setText(
                                therapistNames.length() > 0
                                        ? therapistNames.toString()
                                        : "Not assigned"
                        );

                        tvTherapyType.setText(
                                therapyTypes.length() > 0
                                        ? therapyTypes.toString()
                                        : "Not assigned"
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<java.util.List<
                                    com.autismcentre.model.AdminChildTherapistResponse>>
                                    call,
                            Throwable t) {

                        android.util.Log.e(
                                "ChildTherapist",
                                "Failed to load assigned therapists",
                                t
                        );
                    }
                }
        );
    }

    private void initializeChildDetailsViews() {

        tvChildName = findViewById(R.id.tvChildName);
        tvChildStatus = findViewById(R.id.tvChildStatus);
        tvChildAge = findViewById(R.id.tvChildAge);
        tvChildBranch = findViewById(R.id.tvChildBranch);

        tvParentName = findViewById(R.id.tvParentName);
        tvParentContact = findViewById(R.id.tvParentContact);

        tvTherapistName = findViewById(R.id.tvTherapistName);
        tvTherapyType = findViewById(R.id.tvTherapyType);
        tvTherapySchedule = findViewById(R.id.tvTherapySchedule);

        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);

        btnEditChild = findViewById(
                R.id.btnEditChild
        );

        btnChangeChildStatus = findViewById(
                R.id.btnChangeChildStatus
        );

        btnChangeTherapist = findViewById(
                R.id.btnChangeTherapist
        );

        btnUnassignTherapist = findViewById(
                R.id.btnUnassignTherapist
        );
    }

   /* private void loadChildDetails() {

        // Temporary demo data.
        // Backend integration baad mein hogi.

        tvChildName.setText("Aarav Kumar");

        tvChildAge.setText("5 Years");

        tvChildBranch.setText("Branch: Siwan");

        tvParentName.setText("Rahul Kumar");

        tvParentContact.setText(
                "+91 98765 43210"
        );

        tvTherapistName.setText(
                "Dr. Neha Sharma"
        );

        tvTherapyType.setText(
                "Occupational Therapy"
        );

        tvTherapySchedule.setText(
                "Monday, Wednesday & Friday - 10:00 AM"
        );

        tvPaymentStatus.setText(
                "Paid"
        );

        updateChildStatusUI();
    } */


    private void setupEditChildButton() {
        btnEditChild.setOnClickListener(v -> {
            if (childId == null) {
                Toast.makeText(
                        AdminChildDetailsActivity.this,
                        "Child ID not found",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    AdminChildDetailsActivity.this,
                    AdminEditChildActivity.class
            );

            intent.putExtra("child_id", childId);
            startActivity(intent);
        });
    }

    private void setupChangeTherapistButton() {

        btnChangeTherapist.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminChildDetailsActivity.this,
                    AdminAssignTherapistActivity.class
            );

            intent.putExtra(
                    "child_name",
                    tvChildName.getText().toString()
            );


            intent.putExtra(
                    "current_therapist",
                    tvTherapistName.getText().toString()
            );

            intent.putExtra("child_id", childId);

            if (childBranchId != null) {
                intent.putExtra("branch_id", childBranchId);
            }

            startActivityForResult(
                    intent,
                    1001
            );
        });
    }

    private void setupUnassignTherapistButton() {
        btnUnassignTherapist.setOnClickListener(v ->
                loadTherapistsForUnassign()
        );
    }

    private void loadTherapistsForUnassign() {

        ApiService apiService = ApiClient.getApiService();

        apiService.getTherapistsByChild(childId).enqueue(
                new Callback<List<AdminChildTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminChildTherapistResponse>> call,
                            Response<List<AdminChildTherapistResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(
                                    AdminChildDetailsActivity.this,
                                    "Failed to load assigned therapists. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        List<AdminChildTherapistResponse> activeAssignments =
                                new ArrayList<>();

                        List<String> therapistNames = new ArrayList<>();

                        for (AdminChildTherapistResponse assignment
                                : response.body()) {

                            if (assignment == null
                                    || !Boolean.TRUE.equals(assignment.getActive())) {
                                continue;
                            }

                            activeAssignments.add(assignment);

                            String name = assignment.getTherapistName();
                            String type = assignment.getTherapyType();

                            therapistNames.add(
                                    (name != null ? name : "Unknown therapist")
                                            + " — "
                                            + (type != null ? type : "Therapy not specified")
                            );
                        }

                        if (activeAssignments.isEmpty()) {
                            Toast.makeText(
                                    AdminChildDetailsActivity.this,
                                    "No active therapist to unassign",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        String[] options = therapistNames.toArray(new String[0]);

                        new MaterialAlertDialogBuilder(
                                AdminChildDetailsActivity.this
                        )
                                .setTitle("Select Therapist to Unassign")
                                .setItems(options, (dialog, which) -> {
                                    AdminChildTherapistResponse selected =
                                            activeAssignments.get(which);

                                    confirmUnassignTherapist(selected);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminChildTherapistResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                AdminChildDetailsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void confirmUnassignTherapist(
            AdminChildTherapistResponse assignment) {

        String therapistName = assignment.getTherapistName() != null
                ? assignment.getTherapistName()
                : "this therapist";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Unassignment")
                .setMessage(
                        "Unassign " + therapistName
                                + " from " + tvChildName.getText().toString() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Unassign", (dialog, which) ->
                        unassignTherapist(assignment))
                .show();
    }

    private void unassignTherapist(
            AdminChildTherapistResponse assignment) {

        if (assignment.getAssignmentId() == null) {
            Toast.makeText(
                    this,
                    "Assignment ID is missing",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        btnUnassignTherapist.setEnabled(false);

        ApiClient.getApiService()
                .unassignTherapistFromChild(assignment.getAssignmentId())
                .enqueue(new Callback<AdminChildTherapistResponse>() {

                    @Override
                    public void onResponse(
                            Call<AdminChildTherapistResponse> call,
                            Response<AdminChildTherapistResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnUnassignTherapist.setEnabled(true);

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    AdminChildDetailsActivity.this,
                                    "Therapist unassigned successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadAssignedTherapist();
                        } else {
                            Toast.makeText(
                                    AdminChildDetailsActivity.this,
                                    "Unassign failed. HTTP " + response.code(),
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

                        btnUnassignTherapist.setEnabled(true);

                        Toast.makeText(
                                AdminChildDetailsActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 1001
                && resultCode == Activity.RESULT_OK
                && data != null) {

            String assignedTherapist =
                    data.getStringExtra(
                            "assigned_therapist"
                    );

            String therapyType =
                    data.getStringExtra(
                            "therapy_type"
                    );

            if (assignedTherapist != null) {

                tvTherapistName.setText(
                        assignedTherapist
                );
            }

            if (therapyType != null) {

                tvTherapyType.setText(
                        therapyType
                );
            }
            loadAssignedTherapist();
        }
    }

    private void setupChildStatusButton() {

        btnChangeChildStatus.setOnClickListener(v -> {

            if (isChildActive) {
                showDeactivateConfirmation();
            } else {
                showActivateConfirmation();
            }
        });
    }


    private void showDeactivateConfirmation() {
        String childName = tvChildName.getText().toString();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Deactivate Child")
                .setMessage(
                        "Are you sure you want to deactivate "
                                + childName + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Deactivate", (dialog, which) ->
                        updateChildStatus(false))
                .show();
    }

    private void showActivateConfirmation() {
        String childName = tvChildName.getText().toString();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Activate Child")
                .setMessage(
                        "Are you sure you want to activate "
                                + childName + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Activate", (dialog, which) ->
                        updateChildStatus(true))
                .show();
    }

    private void updateChildStatus(boolean activate) {
        if (childId == null || childId <= 0) {
            Toast.makeText(
                    this,
                    "Child ID not found",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        btnChangeChildStatus.setEnabled(false);

        ApiService apiService = ApiClient.getApiService();

        Call<AdminChildResponse> call = activate
                ? apiService.activateAdminChild(childId)
                : apiService.deactivateAdminChild(childId);

        call.enqueue(new Callback<AdminChildResponse>() {
            @Override
            public void onResponse(
                    Call<AdminChildResponse> call,
                    Response<AdminChildResponse> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnChangeChildStatus.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    AdminChildResponse child = response.body();

                    isChildActive = Boolean.TRUE.equals(child.getActive());

                    updateChildStatusUI();

                    Toast.makeText(
                            AdminChildDetailsActivity.this,
                            isChildActive
                                    ? "Child has been activated"
                                    : "Child has been deactivated",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            AdminChildDetailsActivity.this,
                            "Status update failed. HTTP " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<AdminChildResponse> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnChangeChildStatus.setEnabled(true);

                Toast.makeText(
                        AdminChildDetailsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void updateChildStatusUI() {

        if (isChildActive) {

            tvChildStatus.setText("Active");

            btnChangeChildStatus.setText(
                    "Deactivate Child"
            );

        } else {

            tvChildStatus.setText("Inactive");

            btnChangeChildStatus.setText(
                    "Activate Child"
            );
        }
    }
}
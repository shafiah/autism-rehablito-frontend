package com.autismcentre.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;

public class AdminTherapistDetailsActivity extends BaseAdminActivity {

    private static final int REQUEST_EDIT_THERAPIST = 1001;

    private android.widget.TextView tvAdminTherapistName;
    private android.widget.TextView tvAdminTherapistSpecialization;
    private android.widget.TextView tvAdminTherapistPhone;
    private android.widget.TextView tvAdminTherapistEmail;
    private android.widget.TextView tvAdminTherapistBranch;
    private android.widget.TextView tvAdminTherapistStatus;

    private MaterialButton btnAdminEditTherapist;
    private MaterialButton btnAdminChangeTherapistStatus;
    private MaterialButton btnAdminDeleteTherapist;

    private String therapistName;
    private String specialization;
    private String phone;
    private String email;
    private String branch;
    private String status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_therapist_details
        );

        initializeAdminTherapistDetailsViews();

        loadAdminTherapistDetails();

        setupAdminEditTherapistButton();
        setupAdminChangeStatusButton();
        setupAdminDeleteTherapistButton();
    }

    private void initializeAdminTherapistDetailsViews() {

        tvAdminTherapistName =
                findViewById(R.id.tvAdminTherapistName);

        tvAdminTherapistSpecialization =
                findViewById(R.id.tvAdminTherapistSpecialization);

        tvAdminTherapistPhone =
                findViewById(R.id.tvAdminTherapistPhone);

        tvAdminTherapistEmail =
                findViewById(R.id.tvAdminTherapistEmail);

        tvAdminTherapistBranch =
                findViewById(R.id.tvAdminTherapistBranch);

        tvAdminTherapistStatus =
                findViewById(R.id.tvAdminTherapistStatus);

        btnAdminEditTherapist =
                findViewById(R.id.btnAdminEditTherapist);

        btnAdminChangeTherapistStatus =
                findViewById(
                        R.id.btnAdminChangeTherapistStatus
                );

        btnAdminDeleteTherapist =
                findViewById(
                        R.id.btnAdminDeleteTherapist
                );
    }

    private void loadAdminTherapistDetails() {

        therapistName =
                getIntent().getStringExtra(
                        "therapist_name"
                );

        specialization =
                getIntent().getStringExtra(
                        "therapist_specialization"
                );

        phone =
                getIntent().getStringExtra(
                        "therapist_phone"
                );

        email =
                getIntent().getStringExtra(
                        "therapist_email"
                );

        branch =
                getIntent().getStringExtra(
                        "therapist_branch"
                );

        status =
                getIntent().getStringExtra(
                        "therapist_status"
                );

        if (therapistName == null) {
            therapistName = "Dr. Neha Sharma";
        }

        if (specialization == null) {
            specialization = "Occupational Therapy";
        }

        if (phone == null) {
            phone = "9876543210";
        }

        if (email == null) {
            email = "neha.sharma@gmail.com";
        }

        if (branch == null) {
            branch = "Patna";
        }

        if (status == null) {
            status = "Active";
        }

        updateAdminTherapistDetailsUI();
    }

    private void updateAdminTherapistDetailsUI() {

        tvAdminTherapistName.setText(
                therapistName
        );

        tvAdminTherapistSpecialization.setText(
                specialization
        );

        tvAdminTherapistPhone.setText(
                phone
        );

        tvAdminTherapistEmail.setText(
                email
        );

        tvAdminTherapistBranch.setText(
                branch
        );

        tvAdminTherapistStatus.setText(
                status
        );

        if ("Active".equalsIgnoreCase(status)) {

            tvAdminTherapistStatus.setTextColor(
                    Color.parseColor("#388E3C")
            );

            btnAdminChangeTherapistStatus.setText(
                    "Deactivate Therapist"
            );

            btnAdminDeleteTherapist.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvAdminTherapistStatus.setTextColor(
                    Color.parseColor("#D32F2F")
            );

            btnAdminChangeTherapistStatus.setText(
                    "Activate Therapist"
            );

            btnAdminDeleteTherapist.setVisibility(
                    View.GONE
            );
        }
    }

    private void setupAdminEditTherapistButton() {

        btnAdminEditTherapist.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminTherapistDetailsActivity.this,
                    AdminEditTherapistActivity.class
            );

            intent.putExtra(
                    "therapist_name",
                    therapistName
            );

            intent.putExtra(
                    "therapist_specialization",
                    specialization
            );

            intent.putExtra(
                    "therapist_phone",
                    phone
            );

            intent.putExtra(
                    "therapist_email",
                    email
            );

            intent.putExtra(
                    "therapist_branch",
                    branch
            );

            intent.putExtra(
                    "therapist_status",
                    status
            );

            startActivityForResult(
                    intent,
                    REQUEST_EDIT_THERAPIST
            );
        });
    }

    private void setupAdminChangeStatusButton() {

        btnAdminChangeTherapistStatus.setOnClickListener(v -> {

            if ("Active".equalsIgnoreCase(status)) {

                showDeactivateTherapistDialog();

            } else {

                showActivateTherapistDialog();
            }
        });
    }

    private void showDeactivateTherapistDialog() {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Deactivate Therapist?")
                .setMessage(
                        "Are you sure you want to deactivate "
                                + therapistName
                                + "?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Deactivate",
                        (dialog, which) -> {

                            status = "Inactive";

                            updateAdminTherapistDetailsUI();

                            Snackbar.make(
                                    findViewById(
                                            R.id.btnAdminChangeTherapistStatus
                                    ),
                                    "Therapist deactivated successfully",
                                    Snackbar.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    private void showActivateTherapistDialog() {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Activate Therapist?")
                .setMessage(
                        "Are you sure you want to activate "
                                + therapistName
                                + "?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Activate",
                        (dialog, which) -> {

                            status = "Active";

                            updateAdminTherapistDetailsUI();

                            Snackbar.make(
                                    findViewById(
                                            R.id.btnAdminChangeTherapistStatus
                                    ),
                                    "Therapist activated successfully",
                                    Snackbar.LENGTH_SHORT
                            ).show();
                        }
                )
                .show();
    }

    private void setupAdminDeleteTherapistButton() {

        btnAdminDeleteTherapist.setOnClickListener(v -> {

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Deactivate Therapist Record?")
                    .setMessage(
                            "This will remove "
                                    + therapistName
                                    + " from active therapist management. "
                                    + "Historical sessions and reports should "
                                    + "be preserved."
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Deactivate",
                            (dialog, which) -> {

                                Intent resultIntent =
                                        new Intent();

                                resultIntent.putExtra(
                                        "therapist_deactivated",
                                        true
                                );

                                setResult(
                                        RESULT_OK,
                                        resultIntent
                                );

                                Snackbar.make(
                                        findViewById(
                                                R.id.btnAdminDeleteTherapist
                                        ),
                                        "Therapist record deactivated",
                                        Snackbar.LENGTH_SHORT
                                ).show();

                                finish();
                            }
                    )
                    .show();
        });
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == REQUEST_EDIT_THERAPIST
                && resultCode == RESULT_OK
                && data != null) {

            String updatedName =
                    data.getStringExtra(
                            "therapist_name"
                    );

            String updatedSpecialization =
                    data.getStringExtra(
                            "therapist_specialization"
                    );

            String updatedPhone =
                    data.getStringExtra(
                            "therapist_phone"
                    );

            String updatedEmail =
                    data.getStringExtra(
                            "therapist_email"
                    );

            String updatedBranch =
                    data.getStringExtra(
                            "therapist_branch"
                    );

            String updatedStatus =
                    data.getStringExtra(
                            "therapist_status"
                    );

            if (updatedName != null) {
                therapistName = updatedName;
            }

            if (updatedSpecialization != null) {
                specialization =
                        updatedSpecialization;
            }

            if (updatedPhone != null) {
                phone = updatedPhone;
            }

            if (updatedEmail != null) {
                email = updatedEmail;
            }

            if (updatedBranch != null) {
                branch = updatedBranch;
            }

            if (updatedStatus != null) {
                status = updatedStatus;
            }

            updateAdminTherapistDetailsUI();
        }
    }
}
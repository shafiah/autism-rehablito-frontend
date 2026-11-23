package com.autismcentre.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.widget.TextView;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;


public class AdminHomeworkDetailsActivity extends BaseAdminActivity {

    private TextView tvAdminHomeworkDetailChild;
    private TextView tvAdminHomeworkDetailTherapist;
    private TextView tvAdminHomeworkDetailBranch;
    private TextView tvAdminHomeworkDetailTherapy;
    private TextView tvAdminHomeworkDetailAssignedDate;
    private TextView tvAdminHomeworkDetailDueDate;
    private TextView tvAdminHomeworkDetailStatus;

    private TextView tvAdminHomeworkDetailTitle;
    private TextView tvAdminHomeworkDetailInstructions;
    private TextView tvAdminHomeworkDetailRemarks;
    private TextView tvAdminHomeworkDetailParentInstructions;

    private MaterialButton btnAdminHomeworkBack;

    private String homeworkChild;
    private String homeworkTherapist;
    private String homeworkBranch;
    private String homeworkTherapy;
    private String homeworkAssignedDate;
    private String homeworkDueDate;
    private String homeworkStatus;

    private String homeworkTitle;
    private String homeworkInstructions;
    private String homeworkRemarks;
    private String homeworkParentInstructions;
   // private MaterialButton btnAdminEditHomework;

   // private MaterialButton btnAdminHomeworkArchive;

    private String homeworkRecordStatus;
    private static final int REQUEST_EDIT_HOMEWORK = 5001;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_homework_details
        );

        initializeAdminHomeworkDetailsViews();

        loadAdminHomeworkDetails();

        updateAdminHomeworkDetailsUI();

        setupAdminHomeworkBackButton();

       // setupAdminHomeworkEditButton();

       // setupAdminHomeworkArchiveButton();
    }

    private void initializeAdminHomeworkDetailsViews() {

        tvAdminHomeworkDetailChild =
                findViewById(
                        R.id.tvAdminHomeworkDetailChild
                );

        tvAdminHomeworkDetailTherapist =
                findViewById(
                        R.id.tvAdminHomeworkDetailTherapist
                );

        tvAdminHomeworkDetailBranch =
                findViewById(
                        R.id.tvAdminHomeworkDetailBranch
                );

        tvAdminHomeworkDetailTherapy =
                findViewById(
                        R.id.tvAdminHomeworkDetailTherapy
                );

        tvAdminHomeworkDetailAssignedDate =
                findViewById(
                        R.id.tvAdminHomeworkDetailAssignedDate
                );

        tvAdminHomeworkDetailDueDate =
                findViewById(
                        R.id.tvAdminHomeworkDetailDueDate
                );

        tvAdminHomeworkDetailStatus =
                findViewById(
                        R.id.tvAdminHomeworkDetailStatus
                );

        tvAdminHomeworkDetailTitle =
                findViewById(
                        R.id.tvAdminHomeworkDetailTitle
                );

        tvAdminHomeworkDetailInstructions =
                findViewById(
                        R.id.tvAdminHomeworkDetailInstructions
                );

        tvAdminHomeworkDetailRemarks =
                findViewById(
                        R.id.tvAdminHomeworkDetailRemarks
                );

        tvAdminHomeworkDetailParentInstructions =
                findViewById(
                        R.id.tvAdminHomeworkDetailParentInstructions
                );

        btnAdminHomeworkBack =
                findViewById(
                        R.id.btnAdminHomeworkBack
                );

       /* btnAdminEditHomework =
                findViewById(
                        R.id.btnAdminEditHomework
                );

        btnAdminHomeworkArchive =
                findViewById(
                        R.id.btnAdminHomeworkArchive
                ); */
    }

   /* private void setupAdminHomeworkEditButton() {

        btnAdminEditHomework.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminHomeworkDetailsActivity.this,
                    AdminEditHomeworkActivity.class
            );

            intent.putExtra(
                    "homework_child",
                    homeworkChild
            );

            intent.putExtra(
                    "homework_therapist",
                    homeworkTherapist
            );

            intent.putExtra(
                    "homework_branch",
                    homeworkBranch
            );

            intent.putExtra(
                    "homework_therapy",
                    homeworkTherapy
            );

            intent.putExtra(
                    "homework_title",
                    homeworkTitle
            );

            intent.putExtra(
                    "homework_instructions",
                    homeworkInstructions
            );

            intent.putExtra(
                    "homework_remarks",
                    homeworkRemarks
            );

            intent.putExtra(
                    "homework_parent_instructions",
                    homeworkParentInstructions
            );

            intent.putExtra(
                    "homework_assigned_date",
                    homeworkAssignedDate
            );

            intent.putExtra(
                    "homework_due_date",
                    homeworkDueDate
            );

            intent.putExtra(
                    "homework_status",
                    homeworkStatus
            );
            intent.putExtra(
                    "homework_record_status",
                    homeworkRecordStatus
            );

            startActivityForResult(
                    intent,
                    REQUEST_EDIT_HOMEWORK
            );
        });
    }   */

   /* private void setupAdminHomeworkArchiveButton() {

       // updateAdminHomeworkArchiveButton();

        btnAdminHomeworkArchive.setOnClickListener(v -> {

            if ("Active".equalsIgnoreCase(
                    homeworkRecordStatus
            )) {

                new com.google.android.material.dialog.MaterialAlertDialogBuilder(
                        this
                )
                        .setTitle("Archive Homework")
                        .setMessage(
                                "Are you sure you want to archive this "
                                        + "homework record?\n\n"
                                        + "The record will be preserved and "
                                        + "can be restored later."
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Archive",
                                (dialog, which) -> {

                                    homeworkRecordStatus =
                                            "Archived";

                                    updateAdminHomeworkArchiveButton();

                                    android.widget.Toast.makeText(
                                            this,
                                            "Homework archived successfully",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();
                                }
                        )
                        .show();

            } else {

                new com.google.android.material.dialog.MaterialAlertDialogBuilder(
                        this
                )
                        .setTitle("Restore Homework")
                        .setMessage(
                                "Do you want to restore this homework "
                                        + "record?"
                        )
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Restore",
                                (dialog, which) -> {

                                    homeworkRecordStatus =
                                            "Active";

                                    updateAdminHomeworkArchiveButton();

                                    android.widget.Toast.makeText(
                                            this,
                                            "Homework restored successfully",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show();
                                }
                        )
                        .show();
            }
        });
    } */

    private void loadAdminHomeworkDetails() {

        Intent homeworkIntent = getIntent();

        homeworkChild =
                homeworkIntent.getStringExtra(
                        "homework_child"
                );

        homeworkTherapist =
                homeworkIntent.getStringExtra(
                        "homework_therapist"
                );

        homeworkBranch =
                homeworkIntent.getStringExtra(
                        "homework_branch"
                );

        homeworkTherapy =
                homeworkIntent.getStringExtra(
                        "homework_therapy"
                );

        homeworkAssignedDate =
                homeworkIntent.getStringExtra(
                        "homework_assigned_date"
                );

        homeworkDueDate =
                homeworkIntent.getStringExtra(
                        "homework_due_date"
                );

        homeworkStatus =
                homeworkIntent.getStringExtra(
                        "homework_status"
                );

        homeworkRecordStatus =
                homeworkIntent.getStringExtra(
                        "homework_record_status"
                );

        if (homeworkRecordStatus == null) {
            homeworkRecordStatus = "Active";
        }

        homeworkTitle =
                homeworkIntent.getStringExtra(
                        "homework_title"
                );

        homeworkInstructions =
                homeworkIntent.getStringExtra(
                        "homework_instructions"
                );

        homeworkRemarks =
                homeworkIntent.getStringExtra(
                        "homework_remarks"
                );

        homeworkParentInstructions =
                homeworkIntent.getStringExtra(
                        "homework_parent_instructions"
                );

        /*
         * Demo fallback data.
         * Backend integration will replace this later.
         */

        if (homeworkChild == null) {
            homeworkChild = "Ayaan Singh";
        }

        if (homeworkTherapist == null) {
            homeworkTherapist = "Dr. Neha Sharma";
        }

        if (homeworkBranch == null) {
            homeworkBranch = "Patna";
        }

        if (homeworkTherapy == null) {
            homeworkTherapy = "Occupational Therapy";
        }

        if (homeworkAssignedDate == null) {
            homeworkAssignedDate = "21 Sep 2026";
        }

        if (homeworkDueDate == null) {
            homeworkDueDate = "28 Sep 2026";
        }

        if (homeworkStatus == null) {
            homeworkStatus = "Assigned";
        }

        if (homeworkTitle == null) {
            homeworkTitle =
                    "Fine Motor Skill Practice";
        }

        if (homeworkInstructions == null) {
            homeworkInstructions =
                    "Practice simple fine motor activities "
                            + "for 15 minutes each day. Use safe "
                            + "age-appropriate objects and encourage "
                            + "the child to complete the activity "
                            + "with minimal assistance.";
        }

        if (homeworkRemarks == null
                || homeworkRemarks.trim().isEmpty()) {
            homeworkRemarks = "No therapist remarks provided yet.";
        }

        if (homeworkParentInstructions == null) {
            homeworkParentInstructions =
                    "Parents should provide a calm environment "
                            + "during the activity and encourage "
                            + "the child without forcing completion.";
        }
    }

    private void updateAdminHomeworkDetailsUI() {

        tvAdminHomeworkDetailChild.setText(
                homeworkChild
        );

        tvAdminHomeworkDetailTherapist.setText(
                homeworkTherapist
        );

        tvAdminHomeworkDetailBranch.setText(
                homeworkBranch
        );

        tvAdminHomeworkDetailTherapy.setText(
                homeworkTherapy
        );

        tvAdminHomeworkDetailAssignedDate.setText(
                homeworkAssignedDate
        );

        tvAdminHomeworkDetailDueDate.setText(
                homeworkDueDate
        );

        tvAdminHomeworkDetailStatus.setText(
                homeworkStatus
        );

        tvAdminHomeworkDetailTitle.setText(
                homeworkTitle
        );

        tvAdminHomeworkDetailInstructions.setText(
                homeworkInstructions
        );

        tvAdminHomeworkDetailRemarks.setText(
                "Therapist Remarks:\n" + homeworkRemarks
        );

        tvAdminHomeworkDetailParentInstructions.setText(
                homeworkParentInstructions
        );

        updateAdminHomeworkStatusAppearance();
    }

   /* private void updateAdminHomeworkArchiveButton() {

        if ("Archived".equalsIgnoreCase(
                homeworkRecordStatus
        )) {

            btnAdminHomeworkArchive.setText(
                    "Restore Homework"
            );

        } else {

            btnAdminHomeworkArchive.setText(
                    "Archive Homework"
            );
        }
    }  */

    private void updateAdminHomeworkStatusAppearance() {

        if ("Completed".equalsIgnoreCase(
                homeworkStatus
        )) {

            tvAdminHomeworkDetailStatus.setTextColor(
                    Color.parseColor("#2E7D32")
            );

        } else if ("Overdue".equalsIgnoreCase(
                homeworkStatus
        )) {

            tvAdminHomeworkDetailStatus.setTextColor(
                    Color.parseColor("#C62828")
            );

        } else if ("In Progress".equalsIgnoreCase(
                homeworkStatus
        )) {

            tvAdminHomeworkDetailStatus.setTextColor(
                    Color.parseColor("#1565C0")
            );

        } else {

            tvAdminHomeworkDetailStatus.setTextColor(
                    Color.parseColor("#F57C00")
            );
        }
    }

    private void setupAdminHomeworkBackButton() {

        btnAdminHomeworkBack.setOnClickListener(v -> {

            Intent resultIntent = new Intent();

            resultIntent.putExtra(
                    "homework_child",
                    homeworkChild
            );

            resultIntent.putExtra(
                    "homework_therapist",
                    homeworkTherapist
            );

            resultIntent.putExtra(
                    "homework_branch",
                    homeworkBranch
            );

            resultIntent.putExtra(
                    "homework_therapy",
                    homeworkTherapy
            );

            resultIntent.putExtra(
                    "homework_title",
                    homeworkTitle
            );

            resultIntent.putExtra(
                    "homework_instructions",
                    homeworkInstructions
            );

            resultIntent.putExtra(
                    "homework_remarks",
                    homeworkRemarks
            );

            resultIntent.putExtra(
                    "homework_parent_instructions",
                    homeworkParentInstructions
            );

            resultIntent.putExtra(
                    "homework_assigned_date",
                    homeworkAssignedDate
            );

            resultIntent.putExtra(
                    "homework_due_date",
                    homeworkDueDate
            );

            resultIntent.putExtra(
                    "homework_status",
                    homeworkStatus
            );

            // Step 8: Active / Archived status
            resultIntent.putExtra(
                    "homework_record_status",
                    homeworkRecordStatus
            );

            setResult(
                    RESULT_OK,
                    resultIntent
            );

            finish();
        });
    }

    @Override
    public void onBackPressed() {

        Intent resultIntent = new Intent();

        resultIntent.putExtra(
                "homework_child",
                homeworkChild
        );

        resultIntent.putExtra(
                "homework_therapist",
                homeworkTherapist
        );

        resultIntent.putExtra(
                "homework_branch",
                homeworkBranch
        );

        resultIntent.putExtra(
                "homework_therapy",
                homeworkTherapy
        );

        resultIntent.putExtra(
                "homework_title",
                homeworkTitle
        );

        resultIntent.putExtra(
                "homework_instructions",
                homeworkInstructions
        );

        resultIntent.putExtra(
                "homework_remarks",
                homeworkRemarks
        );

        resultIntent.putExtra(
                "homework_parent_instructions",
                homeworkParentInstructions
        );

        resultIntent.putExtra(
                "homework_assigned_date",
                homeworkAssignedDate
        );

        resultIntent.putExtra(
                "homework_due_date",
                homeworkDueDate
        );

        resultIntent.putExtra(
                "homework_status",
                homeworkStatus
        );

        resultIntent.putExtra(
                "homework_record_status",
                homeworkRecordStatus
        );

        setResult(
                RESULT_OK,
                resultIntent
        );

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == REQUEST_EDIT_HOMEWORK
                && resultCode == RESULT_OK
                && data != null) {

            homeworkTitle =
                    data.getStringExtra(
                            "homework_title"
                    );

            homeworkInstructions =
                    data.getStringExtra(
                            "homework_instructions"
                    );

            homeworkRemarks =
                    data.getStringExtra(
                            "homework_remarks"
                    );

            homeworkParentInstructions =
                    data.getStringExtra(
                            "homework_parent_instructions"
                    );

            homeworkAssignedDate =
                    data.getStringExtra(
                            "homework_assigned_date"
                    );

            homeworkDueDate =
                    data.getStringExtra(
                            "homework_due_date"
                    );

            homeworkStatus =
                    data.getStringExtra(
                            "homework_status"
                    );
            homeworkRecordStatus =
                    data.getStringExtra(
                            "homework_record_status"
                    );

            if (homeworkRecordStatus == null) {
                homeworkRecordStatus = "Active";
            }

            //updateAdminHomeworkArchiveButton();

            updateAdminHomeworkDetailsUI();
        }
    }
}
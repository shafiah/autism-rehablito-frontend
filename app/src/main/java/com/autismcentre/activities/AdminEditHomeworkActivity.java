package com.autismcentre.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AdminEditHomeworkActivity extends BaseAdminActivity {

    private TextInputEditText etAdminEditHomeworkTitle;
    private TextInputEditText etAdminEditHomeworkInstructions;
    private TextInputEditText etAdminEditHomeworkRemarks;
    private TextInputEditText etAdminEditHomeworkParentInstructions;

    private MaterialAutoCompleteTextView actAdminEditHomeworkStatus;

    private TextInputEditText etAdminEditHomeworkAssignedDate;
    private TextInputEditText etAdminEditHomeworkDueDate;

    private MaterialButton btnAdminSaveHomeworkChanges;

    private String homeworkChild;
    private String homeworkTherapist;
    private String homeworkBranch;
    private String homeworkTherapy;

    private String homeworkTitle;
    private String homeworkInstructions;
    private String homeworkRemarks;
    private String homeworkParentInstructions;

    private String homeworkAssignedDate;
    private String homeworkDueDate;
    private String homeworkStatus;

    private String homeworkRecordStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_edit_homework
        );

        initializeAdminEditHomeworkViews();

        loadExistingHomeworkData();

        setupAdminHomeworkStatusDropdown();

        setupAdminHomeworkDatePickers();

        setupAdminSaveHomeworkButton();
    }

    private void initializeAdminEditHomeworkViews() {

        etAdminEditHomeworkTitle =
                findViewById(R.id.etAdminEditHomeworkTitle);

        etAdminEditHomeworkInstructions =
                findViewById(R.id.etAdminEditHomeworkInstructions);

        etAdminEditHomeworkRemarks =
                findViewById(R.id.etAdminEditHomeworkRemarks);

        etAdminEditHomeworkParentInstructions =
                findViewById(R.id.etAdminEditHomeworkParentInstructions);

        actAdminEditHomeworkStatus =
                findViewById(R.id.actAdminEditHomeworkStatus);

        etAdminEditHomeworkAssignedDate =
                findViewById(R.id.etAdminEditHomeworkAssignedDate);

        etAdminEditHomeworkDueDate =
                findViewById(R.id.etAdminEditHomeworkDueDate);

        btnAdminSaveHomeworkChanges =
                findViewById(R.id.btnAdminSaveHomeworkChanges);
    }

    private void loadExistingHomeworkData() {

        homeworkChild =
                getIntent().getStringExtra("homework_child");

        homeworkTherapist =
                getIntent().getStringExtra("homework_therapist");

        homeworkBranch =
                getIntent().getStringExtra("homework_branch");

        homeworkTherapy =
                getIntent().getStringExtra("homework_therapy");

        homeworkTitle =
                getIntent().getStringExtra("homework_title");

        homeworkInstructions =
                getIntent().getStringExtra("homework_instructions");

        homeworkRemarks =
                getIntent().getStringExtra("homework_remarks");

        homeworkParentInstructions =
                getIntent().getStringExtra(
                        "homework_parent_instructions"
                );

        homeworkAssignedDate =
                getIntent().getStringExtra(
                        "homework_assigned_date"
                );

        homeworkDueDate =
                getIntent().getStringExtra(
                        "homework_due_date"
                );

        homeworkStatus =
                getIntent().getStringExtra("homework_status");

        String homeworkRecordStatus =
                getIntent().getStringExtra(
                        "homework_record_status"
                );

        if (homeworkRecordStatus == null) {
            homeworkRecordStatus = "Active";
        }

        if (homeworkTitle == null) {
            homeworkTitle = "Fine Motor Skill Practice";
        }

        if (homeworkInstructions == null) {
            homeworkInstructions =
                    "Practice simple fine motor activities "
                            + "for 15 minutes each day.";
        }

        if (homeworkRemarks == null) {
            homeworkRemarks =
                    "Encourage the child to complete the "
                            + "activity independently.";
        }

        if (homeworkParentInstructions == null) {
            homeworkParentInstructions =
                    "Provide a calm environment and encourage "
                            + "the child during the activity.";
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

        etAdminEditHomeworkTitle.setText(homeworkTitle);

        etAdminEditHomeworkInstructions.setText(
                homeworkInstructions
        );

        etAdminEditHomeworkRemarks.setText(
                homeworkRemarks
        );

        etAdminEditHomeworkParentInstructions.setText(
                homeworkParentInstructions
        );

        etAdminEditHomeworkAssignedDate.setText(
                homeworkAssignedDate
        );

        etAdminEditHomeworkDueDate.setText(
                homeworkDueDate
        );
    }

    private void setupAdminHomeworkStatusDropdown() {

        String[] statuses = {
                "Assigned",
                "In Progress",
                "Completed",
                "Overdue"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        statuses
                );

        actAdminEditHomeworkStatus.setAdapter(
                statusAdapter
        );

        actAdminEditHomeworkStatus.setText(
                homeworkStatus,
                false
        );

        actAdminEditHomeworkStatus.setOnItemClickListener(
                (parent, view, position, id) -> {

                    homeworkStatus =
                            statuses[position];
                }
        );
    }

    private void setupAdminHomeworkDatePickers() {

        etAdminEditHomeworkAssignedDate.setOnClickListener(
                v -> showAdminDatePicker(
                        etAdminEditHomeworkAssignedDate
                )
        );

        etAdminEditHomeworkDueDate.setOnClickListener(
                v -> showAdminDatePicker(
                        etAdminEditHomeworkDueDate
                )
        );
    }

    private void showAdminDatePicker(
            TextInputEditText targetField
    ) {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String selectedDate =
                                    dayOfMonth
                                            + " "
                                            + getMonthName(month)
                                            + " "
                                            + year;

                            targetField.setText(
                                    selectedDate
                            );
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        datePickerDialog.show();
    }

    private String getMonthName(int month) {

        String[] months = {
                "Jan", "Feb", "Mar", "Apr",
                "May", "Jun", "Jul", "Aug",
                "Sep", "Oct", "Nov", "Dec"
        };

        return months[month];
    }

    private void setupAdminSaveHomeworkButton() {

        btnAdminSaveHomeworkChanges.setOnClickListener(
                v -> saveAdminHomeworkChanges()
        );
    }

    private void saveAdminHomeworkChanges() {

        String updatedTitle =
                etAdminEditHomeworkTitle
                        .getText()
                        .toString()
                        .trim();

        String updatedInstructions =
                etAdminEditHomeworkInstructions
                        .getText()
                        .toString()
                        .trim();

        String updatedRemarks =
                etAdminEditHomeworkRemarks
                        .getText()
                        .toString()
                        .trim();

        String updatedParentInstructions =
                etAdminEditHomeworkParentInstructions
                        .getText()
                        .toString()
                        .trim();

        String updatedAssignedDate =
                etAdminEditHomeworkAssignedDate
                        .getText()
                        .toString()
                        .trim();

        String updatedDueDate =
                etAdminEditHomeworkDueDate
                        .getText()
                        .toString()
                        .trim();

        if (updatedTitle.isEmpty()) {

            etAdminEditHomeworkTitle.setError(
                    "Homework title is required"
            );

            etAdminEditHomeworkTitle.requestFocus();

            return;
        }

        if (updatedInstructions.isEmpty()) {

            etAdminEditHomeworkInstructions.setError(
                    "Homework instructions are required"
            );

            etAdminEditHomeworkInstructions.requestFocus();

            return;
        }

        if (updatedAssignedDate.isEmpty()) {

            etAdminEditHomeworkAssignedDate.setError(
                    "Assigned date is required"
            );

            return;
        }

        if (updatedDueDate.isEmpty()) {

            etAdminEditHomeworkDueDate.setError(
                    "Due date is required"
            );

            return;
        }

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
                updatedTitle
        );

        resultIntent.putExtra(
                "homework_instructions",
                updatedInstructions
        );

        resultIntent.putExtra(
                "homework_remarks",
                updatedRemarks
        );

        resultIntent.putExtra(
                "homework_parent_instructions",
                updatedParentInstructions
        );

        resultIntent.putExtra(
                "homework_assigned_date",
                updatedAssignedDate
        );

        resultIntent.putExtra(
                "homework_due_date",
                updatedDueDate
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

        Toast.makeText(
                this,
                "Homework updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}
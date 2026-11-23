package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

public class AdminEditTherapistActivity extends BaseAdminActivity {

    private TextInputEditText etAdminEditTherapistName;
    private TextInputEditText etAdminEditTherapistPhone;
    private TextInputEditText etAdminEditTherapistEmail;

    private MaterialAutoCompleteTextView actAdminEditTherapistSpecialization;
    private MaterialAutoCompleteTextView actAdminEditTherapistBranch;
    private MaterialAutoCompleteTextView actAdminEditTherapistStatus;

    private MaterialButton btnAdminSaveTherapistChanges;

    private String selectedSpecialization = "";
    private String selectedBranch = "";
    private String selectedStatus = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_edit_therapist
        );

        initializeAdminEditTherapistViews();

        setupAdminSpecializationDropdown();
        setupAdminBranchDropdown();
        setupAdminStatusDropdown();

        loadAdminTherapistData();

        setupAdminSaveButton();
    }

    private void initializeAdminEditTherapistViews() {

        etAdminEditTherapistName =
                findViewById(R.id.etAdminEditTherapistName);

        etAdminEditTherapistPhone =
                findViewById(R.id.etAdminEditTherapistPhone);

        etAdminEditTherapistEmail =
                findViewById(R.id.etAdminEditTherapistEmail);

        actAdminEditTherapistSpecialization =
                findViewById(
                        R.id.actAdminEditTherapistSpecialization
                );

        actAdminEditTherapistBranch =
                findViewById(
                        R.id.actAdminEditTherapistBranch
                );

        actAdminEditTherapistStatus =
                findViewById(
                        R.id.actAdminEditTherapistStatus
                );

        btnAdminSaveTherapistChanges =
                findViewById(
                        R.id.btnAdminSaveTherapistChanges
                );
    }

    private void setupAdminSpecializationDropdown() {

        String[] specializations = {
                "Occupational Therapy",
                "Speech Therapy",
                "Physiotherapy",
                "Behavioral Therapy"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        specializations
                );

        actAdminEditTherapistSpecialization
                .setAdapter(adapter);

        actAdminEditTherapistSpecialization
                .setOnItemClickListener(
                        (parent, view, position, id) -> {

                            selectedSpecialization =
                                    specializations[position];
                        }
                );
    }

    private void setupAdminBranchDropdown() {

        String[] branches = {
                "Patna",
                "Siwan",
                "Madhubani",
                "Muzaffarpur",
                "Hajipur",
                "Ramnagri"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        branches
                );

        actAdminEditTherapistBranch
                .setAdapter(adapter);

        actAdminEditTherapistBranch
                .setOnItemClickListener(
                        (parent, view, position, id) -> {

                            selectedBranch =
                                    branches[position];
                        }
                );
    }

    private void setupAdminStatusDropdown() {

        String[] statuses = {
                "Active",
                "Inactive"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        statuses
                );

        actAdminEditTherapistStatus
                .setAdapter(adapter);

        actAdminEditTherapistStatus
                .setOnItemClickListener(
                        (parent, view, position, id) -> {

                            selectedStatus =
                                    statuses[position];
                        }
                );
    }

    private void loadAdminTherapistData() {

        String therapistName =
                getIntent().getStringExtra(
                        "therapist_name"
                );

        String specialization =
                getIntent().getStringExtra(
                        "therapist_specialization"
                );

        String phone =
                getIntent().getStringExtra(
                        "therapist_phone"
                );

        String email =
                getIntent().getStringExtra(
                        "therapist_email"
                );

        String branch =
                getIntent().getStringExtra(
                        "therapist_branch"
                );

        String status =
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

        etAdminEditTherapistName.setText(therapistName);
        etAdminEditTherapistPhone.setText(phone);
        etAdminEditTherapistEmail.setText(email);

        actAdminEditTherapistSpecialization
                .setText(specialization, false);

        actAdminEditTherapistBranch
                .setText(branch, false);

        actAdminEditTherapistStatus
                .setText(status, false);

        selectedSpecialization = specialization;
        selectedBranch = branch;
        selectedStatus = status;
    }

    private void setupAdminSaveButton() {

        btnAdminSaveTherapistChanges.setOnClickListener(
                v -> validateAndSaveTherapist()
        );
    }

    private void validateAndSaveTherapist() {

        String therapistName =
                getText(etAdminEditTherapistName);

        String phone =
                getText(etAdminEditTherapistPhone);

        String email =
                getText(etAdminEditTherapistEmail);

        if (TextUtils.isEmpty(therapistName)) {

            etAdminEditTherapistName.setError(
                    "Therapist name is required"
            );

            etAdminEditTherapistName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(selectedSpecialization)) {

            Toast.makeText(
                    this,
                    "Please select specialization",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(phone)) {

            etAdminEditTherapistPhone.setError(
                    "Phone number is required"
            );

            etAdminEditTherapistPhone.requestFocus();
            return;
        }

        if (phone.length() != 10
                || !phone.matches("\\d{10}")) {

            etAdminEditTherapistPhone.setError(
                    "Enter a valid 10-digit phone number"
            );

            etAdminEditTherapistPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {

            etAdminEditTherapistEmail.setError(
                    "Email is required"
            );

            etAdminEditTherapistEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            etAdminEditTherapistEmail.setError(
                    "Enter a valid email address"
            );

            etAdminEditTherapistEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(selectedBranch)) {

            Toast.makeText(
                    this,
                    "Please select therapist branch",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(selectedStatus)) {

            Toast.makeText(
                    this,
                    "Please select account status",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent resultIntent = new Intent();

        resultIntent.putExtra(
                "therapist_name",
                therapistName
        );

        resultIntent.putExtra(
                "therapist_specialization",
                selectedSpecialization
        );

        resultIntent.putExtra(
                "therapist_phone",
                phone
        );

        resultIntent.putExtra(
                "therapist_email",
                email
        );

        resultIntent.putExtra(
                "therapist_branch",
                selectedBranch
        );

        resultIntent.putExtra(
                "therapist_status",
                selectedStatus
        );

        setResult(
                RESULT_OK,
                resultIntent
        );

        Toast.makeText(
                this,
                "Therapist updated successfully",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private String getText(TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }
}
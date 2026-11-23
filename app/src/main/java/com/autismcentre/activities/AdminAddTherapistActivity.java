
package com.autismcentre.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminTherapistCreateRequest;
import com.autismcentre.model.AdminTherapistCreateResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddTherapistActivity extends BaseAdminActivity {

    private static final String TAG = "AdminAddTherapist";

    private TextInputEditText etAdminTherapistName;
    private TextInputEditText etAdminTherapistPhone;
    private TextInputEditText etAdminTherapistEmail;
    private TextInputEditText etAdminTherapistPassword;

    private MaterialAutoCompleteTextView
            actAdminTherapistSpecialization;

    private MaterialAutoCompleteTextView
            actAdminTherapistBranch;

    private MaterialButton btnAdminAddTherapist;

    private ApiService apiService;

    private final List<AdminBranchResponse> branches =
            new ArrayList<>();

    private Long selectedBranchId = null;
    private String selectedSpecialization = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_add_therapist
        );

        apiService = ApiClient.getApiService();

        initializeViews();
        setupSpecializationDropdown();
        setupBranchDropdown();
        setupAddButton();

        loadBranches();
    }

    private void initializeViews() {

        etAdminTherapistName =
                findViewById(R.id.etAdminTherapistName);

        etAdminTherapistPhone =
                findViewById(R.id.etAdminTherapistPhone);

        etAdminTherapistEmail =
                findViewById(R.id.etAdminTherapistEmail);

        etAdminTherapistPassword =
                findViewById(R.id.etAdminTherapistPassword);

        actAdminTherapistSpecialization =
                findViewById(
                        R.id.actAdminTherapistSpecialization
                );

        actAdminTherapistBranch =
                findViewById(R.id.actAdminTherapistBranch);

        btnAdminAddTherapist =
                findViewById(R.id.btnAdminAddTherapist);
    }

    private void setupSpecializationDropdown() {

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

        actAdminTherapistSpecialization.setAdapter(adapter);

        actAdminTherapistSpecialization.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectedSpecialization =
                            specializations[position];
                }
        );
    }

    private void setupBranchDropdown() {

        actAdminTherapistBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position >= 0 && position < branches.size()) {
                        AdminBranchResponse branch =
                                branches.get(position);

                        selectedBranchId = branch.getId();

                        actAdminTherapistBranch.setText(
                                branch.getBranchName(),
                                false
                        );
                    }
                }
        );
    }

    private void loadBranches() {

        actAdminTherapistBranch.setEnabled(false);

        apiService.getAdminBranches().enqueue(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            actAdminTherapistBranch.setEnabled(false);

                            Toast.makeText(
                                    AdminAddTherapistActivity.this,
                                    "Branches load nahi hui. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        branches.clear();

                        for (AdminBranchResponse branch
                                : response.body()) {

                            if (branch != null
                                    && branch.getId() != null
                                    && Boolean.TRUE.equals(
                                    branch.getActive())
                                    && branch.getBranchName() != null) {

                                branches.add(branch);
                            }
                        }

                        List<String> branchNames =
                                new ArrayList<>();

                        for (AdminBranchResponse branch : branches) {
                            branchNames.add(branch.getBranchName());
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        AdminAddTherapistActivity.this,
                                        android.R.layout
                                                .simple_dropdown_item_1line,
                                        branchNames
                                );

                        actAdminTherapistBranch.setAdapter(adapter);
                        actAdminTherapistBranch.setEnabled(
                                !branches.isEmpty()
                        );

                        if (branches.isEmpty()) {
                            Toast.makeText(
                                    AdminAddTherapistActivity.this,
                                    "Koi active branch available nahi hai.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Branch loading failed", t);

                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(
                                    AdminAddTherapistActivity.this,
                                    "Branch load error: "
                                            + t.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );
    }

    private void setupAddButton() {

        btnAdminAddTherapist.setOnClickListener(
                v -> validateAndCreateTherapist()
        );
    }

    private void validateAndCreateTherapist() {

        String name = getText(etAdminTherapistName);
        String phone = getText(etAdminTherapistPhone);
        String email = getText(etAdminTherapistEmail);
        String password = getText(etAdminTherapistPassword);

        if (TextUtils.isEmpty(name)) {
            etAdminTherapistName.setError("Name is required");
            etAdminTherapistName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(selectedSpecialization)) {
            actAdminTherapistSpecialization.setError(
                    "Select specialization"
            );
            actAdminTherapistSpecialization.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)
                || !phone.matches("[0-9]{10}")) {
            etAdminTherapistPhone.setError(
                    "Enter a valid 10-digit mobile number"
            );
            etAdminTherapistPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)
                || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etAdminTherapistEmail.setError(
                    "Enter a valid email"
            );
            etAdminTherapistEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etAdminTherapistPassword.setError(
                    "Password must be at least 6 characters"
            );
            etAdminTherapistPassword.requestFocus();
            return;
        }

        if (selectedBranchId == null) {
            actAdminTherapistBranch.setError(
                    "Select an active branch"
            );
            actAdminTherapistBranch.requestFocus();
            return;
        }

        createTherapist(name, email, phone, password);
    }

    private void createTherapist(
            String name,
            String email,
            String phone,
            String password) {

        btnAdminAddTherapist.setEnabled(false);
        btnAdminAddTherapist.setText("Creating...");

        AdminTherapistCreateRequest request =
                new AdminTherapistCreateRequest(
                        name,
                        email.toLowerCase(),
                        phone,
                        password,
                        selectedBranchId,
                        selectedSpecialization,
                        null
                );

        apiService.createAdminTherapist(request).enqueue(
                new Callback<AdminTherapistCreateResponse>() {

                    @Override
                    public void onResponse(
                            Call<AdminTherapistCreateResponse> call,
                            Response<AdminTherapistCreateResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnAdminAddTherapist.setEnabled(true);
                        btnAdminAddTherapist.setText("Add Therapist");

                        if (response.isSuccessful()
                                && response.body() != null) {

                            AdminTherapistCreateResponse result =
                                    response.body();

                            Toast.makeText(
                                    AdminAddTherapistActivity.this,
                                    result.getMessage() != null
                                            ? result.getMessage()
                                            : "Therapist created successfully",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();

                        } else {

                            String message =
                                    "Therapist create failed. HTTP "
                                            + response.code();

                            try {
                                if (response.errorBody() != null) {
                                    String error =
                                            response.errorBody().string();

                                    Log.e(TAG, "Create error: " + error);

                                    if (!error.trim().isEmpty()) {
                                        message = message + "\n" + error;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Could not read error body", e);
                            }

                            Toast.makeText(
                                    AdminAddTherapistActivity.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminTherapistCreateResponse> call,
                            Throwable t) {

                        Log.e(TAG, "Therapist create request failed", t);

                        if (!isFinishing() && !isDestroyed()) {
                            btnAdminAddTherapist.setEnabled(true);
                            btnAdminAddTherapist.setText("Add Therapist");

                            Toast.makeText(
                                    AdminAddTherapistActivity.this,
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );
    }

    private String getText(TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }
}

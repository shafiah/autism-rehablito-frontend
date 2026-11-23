
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminTherapistResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTherapistManagementActivity extends BaseAdminActivity {

    private static final String TAG = "AdminTherapistMgmt";

    private TextInputEditText etAdminSearchTherapist;
    private MaterialAutoCompleteTextView actAdminTherapistBranch;
    private LinearLayout adminTherapistListContainer;
    private TextView tvAdminNoTherapists;
    private MaterialButton btnAdminAddTherapist;

    private ApiService apiService;

    private final List<AdminBranchResponse> branches =
            new ArrayList<>();

    private final List<AdminTherapistResponse> therapists =
            new ArrayList<>();

    private String selectedAdminTherapistBranch = "All Branches";
    private Long selectedBranchId = null;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_therapist_management
        );

        apiService = ApiClient.getApiService();

        initializeViews();
        setupSearch();
        setupBranchDropdown();
        setupAddTherapistButton();

        loadBranches();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (apiService != null) {
            loadTherapists();
        }
    }

    private void initializeViews() {

        etAdminSearchTherapist =
                findViewById(R.id.etAdminSearchTherapist);

        actAdminTherapistBranch =
                findViewById(R.id.actAdminTherapistBranch);

        adminTherapistListContainer =
                findViewById(R.id.adminTherapistListContainer);

        tvAdminNoTherapists =
                findViewById(R.id.tvAdminNoTherapists);

        btnAdminAddTherapist =
                findViewById(R.id.btnAdminAddTherapist);
    }

    private void setupSearch() {

        etAdminSearchTherapist.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        currentSearchQuery = s.toString().trim();
                        displayTherapists();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    private void setupBranchDropdown() {

        actAdminTherapistBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position == 0) {
                        selectedAdminTherapistBranch = "All Branches";
                        selectedBranchId = null;
                    } else {
                        int branchIndex = position - 1;

                        if (branchIndex >= 0
                                && branchIndex < branches.size()) {

                            AdminBranchResponse branch =
                                    branches.get(branchIndex);

                            selectedAdminTherapistBranch =
                                    branch.getBranchName();

                            selectedBranchId = branch.getId();
                        }
                    }

                    loadTherapists();
                }
        );
    }

    private void loadBranches() {

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

                            Toast.makeText(
                                    AdminTherapistManagementActivity.this,
                                    "Branch loading failed. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        branches.clear();

                        for (AdminBranchResponse branch : response.body()) {

                            if (branch != null
                                    && branch.getId() != null
                                    && branch.getBranchName() != null
                                    && Boolean.TRUE.equals(
                                    branch.getActive())) {

                                branches.add(branch);
                            }
                        }

                        List<String> branchNames =
                                new ArrayList<>();

                        branchNames.add("All Branches");

                        for (AdminBranchResponse branch : branches) {
                            branchNames.add(branch.getBranchName());
                        }

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        AdminTherapistManagementActivity.this,
                                        android.R.layout
                                                .simple_dropdown_item_1line,
                                        branchNames
                                );

                        actAdminTherapistBranch.setAdapter(adapter);

                        actAdminTherapistBranch.setText(
                                "All Branches",
                                false
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Branch request failed", t);

                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(
                                    AdminTherapistManagementActivity.this,
                                    "Branch network error: "
                                            + t.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );
    }

    private void loadTherapists() {

        adminTherapistListContainer.removeAllViews();

        tvAdminNoTherapists.setVisibility(View.VISIBLE);
        tvAdminNoTherapists.setText("Loading therapists...");

        apiService.getAdminTherapists(selectedBranchId).enqueue(
                new Callback<List<AdminTherapistResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminTherapistResponse>> call,
                            Response<List<AdminTherapistResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            therapists.clear();
                            therapists.addAll(response.body());

// Latest created therapist first (descending order)
                            therapists.sort(
                                    Comparator.comparing(
                                            AdminTherapistResponse::getCreatedAt,
                                            Comparator.nullsLast(Comparator.reverseOrder())
                                    )
                            );

                            displayTherapists();

                        } else {

                            tvAdminNoTherapists.setVisibility(View.VISIBLE);
                            tvAdminNoTherapists.setText(
                                    "Unable to load therapists. HTTP "
                                            + response.code()
                            );

                            Toast.makeText(
                                    AdminTherapistManagementActivity.this,
                                    "Therapist loading failed. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminTherapistResponse>> call,
                            Throwable t) {

                        Log.e(TAG, "Therapist request failed", t);

                        if (!isFinishing() && !isDestroyed()) {
                            tvAdminNoTherapists.setVisibility(View.VISIBLE);
                            tvAdminNoTherapists.setText(
                                    "Network error while loading therapists"
                            );

                            Toast.makeText(
                                    AdminTherapistManagementActivity.this,
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                }
        );
    }

    private void displayTherapists() {

        adminTherapistListContainer.removeAllViews();

        String query = currentSearchQuery.toLowerCase();
        int visibleCount = 0;

        for (AdminTherapistResponse therapist : therapists) {

            if (therapist == null) {
                continue;
            }

            String name = safe(therapist.getName());
            String branchName = safe(therapist.getBranchName());

            boolean matchesSearch =
                    name.toLowerCase().contains(query)
                            || safe(therapist.getEmail())
                            .toLowerCase().contains(query)
                            || safe(therapist.getMobileNumber())
                            .contains(query);

            boolean matchesBranch =
                    selectedBranchId == null
                            || selectedBranchId.equals(
                            therapist.getBranchId());

            if (!matchesSearch || !matchesBranch) {
                continue;
            }

            addTherapistCard(therapist);
            visibleCount++;
        }

        if (visibleCount == 0) {
            tvAdminNoTherapists.setVisibility(View.VISIBLE);
            tvAdminNoTherapists.setText(
                    therapists.isEmpty()
                            ? "No therapists found"
                            : "No matching therapists found"
            );
        } else {
            tvAdminNoTherapists.setVisibility(View.GONE);
        }
    }

    private void addTherapistCard(
            AdminTherapistResponse therapist) {

        View therapistView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_admin_therapist,
                        adminTherapistListContainer,
                        false
                );

        TextView tvTherapistName =
                therapistView.findViewById(
                        R.id.tvAdminTherapistName
                );

        TextView tvTherapistSpecialization =
                therapistView.findViewById(
                        R.id.tvAdminTherapistSpecialization
                );

        TextView tvTherapistPhone =
                therapistView.findViewById(
                        R.id.tvAdminTherapistPhone
                );

        TextView tvTherapistEmail =
                therapistView.findViewById(
                        R.id.tvAdminTherapistEmail
                );

        TextView tvTherapistBranch =
                therapistView.findViewById(
                        R.id.tvAdminTherapistBranch
                );

        TextView tvTherapistStatus =
                therapistView.findViewById(
                        R.id.tvAdminTherapistStatus
                );

        tvTherapistName.setText(
                safe(therapist.getName())
        );

        tvTherapistSpecialization.setText(
                safe(therapist.getTherapyType())
        );

        tvTherapistPhone.setText(
                "Phone: " + safe(therapist.getMobileNumber())
        );

        tvTherapistEmail.setText(
                "Email: " + safe(therapist.getEmail())
        );

        tvTherapistBranch.setText(
                "Branch: " + safe(therapist.getBranchName())
        );

        boolean active = Boolean.TRUE.equals(therapist.getActive());

        tvTherapistStatus.setText(
                active ? "Active" : "Inactive"
        );

        therapistView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminTherapistManagementActivity.this,
                    AdminTherapistDetailsActivity.class
            );

            intent.putExtra(
                    "therapist_id",
                    therapist.getTherapistId() != null
                            ? therapist.getTherapistId()
                            : -1L
            );

            intent.putExtra(
                    "therapist_name",
                    safe(therapist.getName())
            );

            intent.putExtra(
                    "therapist_specialization",
                    safe(therapist.getTherapyType())
            );

            intent.putExtra(
                    "therapist_phone",
                    safe(therapist.getMobileNumber())
            );

            intent.putExtra(
                    "therapist_email",
                    safe(therapist.getEmail())
            );

            intent.putExtra(
                    "therapist_branch",
                    safe(therapist.getBranchName())
            );

            intent.putExtra(
                    "therapist_status",
                    active ? "Active" : "Inactive"
            );

            startActivity(intent);
        });

        adminTherapistListContainer.addView(therapistView);
    }

    private void setupAddTherapistButton() {

        btnAdminAddTherapist.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminTherapistManagementActivity.this,
                    AdminAddTherapistActivity.class
            );

            startActivity(intent);
        });
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not available"
                : value;
    }
}

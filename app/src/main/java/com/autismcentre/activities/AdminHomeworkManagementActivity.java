
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.autismcentre.model.AdminHomeworkResponse;
import com.autismcentre.repository.AdminBranchRepository;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeworkManagementActivity extends BaseAdminActivity {

    private TextInputEditText etAdminSearchHomework;

    private MaterialAutoCompleteTextView actAdminHomeworkBranch;
    private MaterialAutoCompleteTextView actAdminHomeworkTherapy;
    private MaterialAutoCompleteTextView actAdminHomeworkStatus;

    private LinearLayout adminHomeworkListContainer;
    private TextView tvAdminNoHomework;

    private final List<AdminBranchResponse> branchList = new ArrayList<>();
    private final List<AdminHomeworkResponse> homeworkList = new ArrayList<>();

    private AdminBranchRepository adminBranchRepository;
    private ApiService apiService;

    private Long selectedBranchId = null;

    private String selectedAdminHomeworkBranch = "All Branches";
    private String selectedAdminHomeworkTherapy = "All Therapy Types";
    private String selectedAdminHomeworkStatus = "All Status";

    private static final int REQUEST_HOMEWORK_DETAILS = 6001;

    private boolean isLoadingHomework = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_homework_management
        );

        adminBranchRepository = new AdminBranchRepository();
        apiService = ApiClient.getApiService();

        initializeAdminHomeworkViews();
        setupAdminHomeworkTherapyDropdown();
        setupAdminHomeworkStatusDropdown();
        setupAdminHomeworkSearch();

        setupAdminHomeworkBranchDropdown();
       // hideRecordStatusDropdown();
    }

    private void initializeAdminHomeworkViews() {

        etAdminSearchHomework =
                findViewById(R.id.etAdminSearchHomework);

        actAdminHomeworkBranch =
                findViewById(R.id.actAdminHomeworkBranch);

        actAdminHomeworkTherapy =
                findViewById(R.id.actAdminHomeworkTherapy);

        actAdminHomeworkStatus =
                findViewById(R.id.actAdminHomeworkStatus);

        adminHomeworkListContainer =
                findViewById(R.id.adminHomeworkListContainer);

        tvAdminNoHomework =
                findViewById(R.id.tvAdminNoHomework);
    }

    /*
     * Record-status filtering is no longer supported.
     * Hide the old view in case it still exists in the XML.
     */
  /*  private void hideRecordStatusDropdown() {

        View recordStatusView =
                findViewById(R.id.actAdminHomeworkRecordStatus);

        if (recordStatusView != null) {
            recordStatusView.setVisibility(View.GONE);
        }
    } */

    // ---------------------------------------------------------
    // BRANCH DROPDOWN: LOAD FROM BACKEND
    // ---------------------------------------------------------

    private void setupAdminHomeworkBranchDropdown() {

        List<String> branchNames = new ArrayList<>();
        branchNames.add("All Branches");

        ArrayAdapter<String> branchAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        branchNames
                );

        actAdminHomeworkBranch.setAdapter(branchAdapter);
        actAdminHomeworkBranch.setText("All Branches", false);
        actAdminHomeworkBranch.setEnabled(false);

        adminBranchRepository.getAdminBranches(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            branchList.clear();
                            branchNames.clear();
                            branchNames.add("All Branches");

                            for (AdminBranchResponse branch : response.body()) {

                                if (branch.getId() != null
                                        && branch.getBranchName() != null
                                        && Boolean.TRUE.equals(branch.getActive())) {

                                    branchList.add(branch);
                                    branchNames.add(branch.getBranchName());
                                }
                            }

                            branchAdapter.notifyDataSetChanged();
                            actAdminHomeworkBranch.setEnabled(true);
                            actAdminHomeworkBranch.setText(
                                    "All Branches",
                                    false
                            );

                            actAdminHomeworkBranch.setOnItemClickListener(
                                    (parent, view, position, id) -> {

                                        if (position == 0) {
                                            selectedBranchId = null;
                                            selectedAdminHomeworkBranch =
                                                    "All Branches";
                                        } else {
                                            AdminBranchResponse selected =
                                                    branchList.get(position - 1);

                                            selectedBranchId = selected.getId();
                                            selectedAdminHomeworkBranch =
                                                    selected.getBranchName();
                                        }

                                        loadAdminHomework();
                                    }
                            );

                            loadAdminHomework();

                        } else {
                            actAdminHomeworkBranch.setEnabled(true);

                            Toast.makeText(
                                    AdminHomeworkManagementActivity.this,
                                    "Unable to load branches.",
                                    Toast.LENGTH_LONG
                            ).show();

                            loadAdminHomework();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        actAdminHomeworkBranch.setEnabled(true);

                        Toast.makeText(
                                AdminHomeworkManagementActivity.this,
                                "Branch API error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        loadAdminHomework();
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // THERAPY FILTER
    // ---------------------------------------------------------

    private void setupAdminHomeworkTherapyDropdown() {

        String[] therapies = {
                "All Therapy Types",
                "Occupational Therapy",
                "Speech Therapy",
                "Physiotherapy",
                "Behavioral Therapy"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        therapies
                );

        actAdminHomeworkTherapy.setAdapter(adapter);
        actAdminHomeworkTherapy.setText(
                "All Therapy Types",
                false
        );

        actAdminHomeworkTherapy.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedAdminHomeworkTherapy =
                            therapies[position];

                    loadAdminHomework();
                }
        );
    }

    // ---------------------------------------------------------
    // STATUS FILTER: NO OVERDUE OPTION
    // ---------------------------------------------------------

    private void setupAdminHomeworkStatusDropdown() {

        String[] statuses = {
                "All Status",
                "Assigned",
                "In Progress",
                "Completed"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        statuses
                );

        actAdminHomeworkStatus.setAdapter(adapter);
        actAdminHomeworkStatus.setText("All Status", false);

        actAdminHomeworkStatus.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedAdminHomeworkStatus =
                            statuses[position];

                    loadAdminHomework();
                }
        );
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    private void setupAdminHomeworkSearch() {

        etAdminSearchHomework.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        filterAdminHomework(s.toString().trim());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // LOAD REAL HOMEWORK FROM BACKEND
    // ---------------------------------------------------------

    private void loadAdminHomework() {

        if (adminHomeworkListContainer == null) {
            return;
        }

        isLoadingHomework = true;
        adminHomeworkListContainer.removeAllViews();
        homeworkList.clear();

        tvAdminNoHomework.setVisibility(View.VISIBLE);
        tvAdminNoHomework.setText("Loading homework...");

        Long branchId = selectedBranchId;

        String therapyType =
                "All Therapy Types".equals(selectedAdminHomeworkTherapy)
                        ? null
                        : selectedAdminHomeworkTherapy;

        String status =
                "All Status".equals(selectedAdminHomeworkStatus)
                        ? null
                        : selectedAdminHomeworkStatus
                        .trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "_");

        apiService.getAdminHomework(
                branchId,
                null,
                status,
                therapyType
        ).enqueue(new Callback<List<AdminHomeworkResponse>>() {

            @Override
            public void onResponse(
                    Call<List<AdminHomeworkResponse>> call,
                    Response<List<AdminHomeworkResponse>> response
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                isLoadingHomework = false;

                if (response.isSuccessful()
                        && response.body() != null) {

                    homeworkList.clear();
                    homeworkList.addAll(response.body());

                    renderAdminHomework();

                } else {
                    adminHomeworkListContainer.removeAllViews();

                    tvAdminNoHomework.setText(
                            "Unable to load homework. HTTP "
                                    + response.code()
                    );

                    tvAdminNoHomework.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(
                    Call<List<AdminHomeworkResponse>> call,
                    Throwable t
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                isLoadingHomework = false;
                adminHomeworkListContainer.removeAllViews();

                tvAdminNoHomework.setText(
                        "Failed to load homework. Check your connection."
                );

                tvAdminNoHomework.setVisibility(View.VISIBLE);
            }
        });
    }

    // ---------------------------------------------------------
    // RENDER BACKEND RESULTS
    // ---------------------------------------------------------

    private void renderAdminHomework() {

        adminHomeworkListContainer.removeAllViews();

        for (AdminHomeworkResponse homework : homeworkList) {
            addAdminHomework(homework);
        }

        filterAdminHomework(
                etAdminSearchHomework.getText() == null
                        ? ""
                        : etAdminSearchHomework.getText().toString().trim()
        );
    }

    private void addAdminHomework(AdminHomeworkResponse homework) {

        View homeworkView = LayoutInflater
                .from(this)
                .inflate(
                        R.layout.item_admin_homework,
                        adminHomeworkListContainer,
                        false
                );

        TextView tvHomeworkChild =
                homeworkView.findViewById(R.id.tvAdminHomeworkChild);

        TextView tvHomeworkTherapist =
                homeworkView.findViewById(R.id.tvAdminHomeworkTherapist);

        TextView tvHomeworkTherapy =
                homeworkView.findViewById(R.id.tvAdminHomeworkTherapy);

        TextView tvHomeworkBranch =
                homeworkView.findViewById(R.id.tvAdminHomeworkBranch);

        TextView tvHomeworkDueDate =
                homeworkView.findViewById(R.id.tvAdminHomeworkDueDate);

        TextView tvHomeworkStatus =
                homeworkView.findViewById(R.id.tvAdminHomeworkStatus);

        View recordStatusView =
                homeworkView.findViewById(R.id.tvAdminHomeworkRecordStatus);

        if (recordStatusView != null) {
            recordStatusView.setVisibility(View.GONE);
        }

        String childName = safeText(homework.getChildName(), "Unknown Child");
        String therapistName = safeText(
                homework.getTherapistName(),
                "Not assigned"
        );
        android.util.Log.d(
                "ADMIN_HOMEWORK",
                "Homework ID: " + homework.getHomeworkId()
                        + ", Therapy Type from API: "
                        + homework.getTherapyType()
        );
        String therapyType = safeText(
                homework.getTherapyType(),
                "Not specified"
        );
        String branchName = safeText(
                homework.getBranchName(),
                "Not specified"
        );
        String dueDate = formatDate(homework.getDueDate());
        String status = formatStatus(homework.getStatus());

        tvHomeworkChild.setText(childName);
        tvHomeworkTherapist.setText("Therapist: " + therapistName);
        tvHomeworkTherapy.setText(therapyType);
        tvHomeworkBranch.setText("Branch: " + branchName);
        tvHomeworkDueDate.setText("Due: " + dueDate);
        tvHomeworkStatus.setText(status);

        homeworkView.setOnClickListener(v -> {

            if (homework.getHomeworkId() == null) {
                Toast.makeText(
                        AdminHomeworkManagementActivity.this,
                        "Homework ID is missing.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Intent intent = new Intent(
                    AdminHomeworkManagementActivity.this,
                    AdminHomeworkDetailsActivity.class
            );

            intent.putExtra(
                    "homework_id",
                    homework.getHomeworkId()
            );

            // Keep these extras for compatibility with the current details screen.
            intent.putExtra("homework_child", childName);
            intent.putExtra("homework_therapist", therapistName);
            intent.putExtra("homework_therapy", therapyType);
            intent.putExtra("homework_branch", branchName);
            intent.putExtra("homework_assigned_date",
                    formatDate(homework.getAssignedDate()));
            intent.putExtra("homework_due_date", dueDate);
            intent.putExtra("homework_status", status);
            intent.putExtra("homework_title",
                    safeText(homework.getTitle(), "Homework Details"));
            intent.putExtra("homework_instructions",
                    safeText(homework.getInstructions(), ""));
            intent.putExtra("homework_remarks",
                    safeText(homework.getTherapistRemarks(), "")
            );
            intent.putExtra("homework_parent_instructions",
                    safeText(homework.getParentInstructions(), ""));

            startActivityForResult(
                    intent,
                    REQUEST_HOMEWORK_DETAILS
            );
        });

        adminHomeworkListContainer.addView(homeworkView);
    }

    // ---------------------------------------------------------
    // LOCAL SEARCH ON LOADED BACKEND RESULTS
    // ---------------------------------------------------------

    private void filterAdminHomework(String query) {

        if (isLoadingHomework) {
            return;
        }

        int count = adminHomeworkListContainer.getChildCount();
        boolean found = false;

        String searchQuery = query.toLowerCase(Locale.ROOT);

        for (int i = 0; i < count; i++) {

            View homeworkView =
                    adminHomeworkListContainer.getChildAt(i);

            TextView tvChild =
                    homeworkView.findViewById(R.id.tvAdminHomeworkChild);

            TextView tvTherapist =
                    homeworkView.findViewById(R.id.tvAdminHomeworkTherapist);

            String childName = tvChild.getText()
                    .toString()
                    .toLowerCase(Locale.ROOT);

            String therapistName = tvTherapist.getText()
                    .toString()
                    .toLowerCase(Locale.ROOT);

            boolean matches =
                    searchQuery.isEmpty()
                            || childName.contains(searchQuery)
                            || therapistName.contains(searchQuery);

            homeworkView.setVisibility(
                    matches ? View.VISIBLE : View.GONE
            );

            if (matches) {
                found = true;
            }
        }

        if (found) {
            tvAdminNoHomework.setVisibility(View.GONE);
        } else {
            tvAdminNoHomework.setVisibility(View.VISIBLE);

            if (count == 0) {
                tvAdminNoHomework.setText("No homework records found.");
            } else {
                tvAdminNoHomework.setText("No matching homework found.");
            }
        }
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value.trim();
    }

    private String formatStatus(String status) {

        if (status == null || status.trim().isEmpty()) {
            return "Unknown";
        }

        String normalized = status.trim()
                .replace("_", " ")
                .toLowerCase(Locale.ROOT);

        String[] words = normalized.split("\\s+");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (formatted.length() > 0) {
                formatted.append(" ");
            }

            formatted.append(
                    Character.toUpperCase(word.charAt(0))
            );

            if (word.length() > 1) {
                formatted.append(word.substring(1));
            }
        }

        return formatted.toString();
    }

    private String formatDate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not specified";
        }

        try {
            java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat(
                            "yyyy-MM-dd",
                            java.util.Locale.ENGLISH
                    );

            inputFormat.setLenient(false);

            java.util.Date date = inputFormat.parse(value);

            if (date == null) {
                return value;
            }

            java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat(
                            "dd MMM yyyy",
                            java.util.Locale.ENGLISH
                    );

            return outputFormat.format(date);

        } catch (Exception e) {
            return value;
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_HOMEWORK_DETAILS
                && resultCode == RESULT_OK) {
            loadAdminHomework();
        }
    }
}


package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.model.AdminChildResponse;
import com.autismcentre.repository.AdminChildRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChildManagementActivity extends BaseAdminActivity {

    private TextInputEditText etSearchChild;
    private LinearLayout childListContainer;
    private TextView tvNoChildren;
    private MaterialButton btnAddChild;

    private AdminChildRepository adminChildRepository;

    private final List<AdminChildResponse> childList =
            new ArrayList<>();

    private String currentSearchQuery = "";

    private TextView tvTotalChildrenCount;
    private TextView tvActiveChildrenCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_child_management
        );

        initializeChildManagementViews();

        adminChildRepository = new AdminChildRepository();

        setupChildSearch();
        setupAddChildButton();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload children when returning from Add Child or Details.
        if (adminChildRepository != null) {
            loadChildrenFromApi();
        }
    }

    // =====================================================
    // INITIALIZE VIEWS
    // =====================================================

    private void initializeChildManagementViews() {

        etSearchChild = findViewById(R.id.etSearchChild);
        childListContainer = findViewById(R.id.childListContainer);
        tvNoChildren = findViewById(R.id.tvNoChildren);
        btnAddChild = findViewById(R.id.btnAddChild);
        tvTotalChildrenCount = findViewById(R.id.tvTotalChildrenCount);
        tvActiveChildrenCount = findViewById(R.id.tvActiveChildrenCount);
    }

    private void updateChildCounts() {
        int totalChildren = childList.size();
        int activeChildren = 0;

        for (AdminChildResponse child : childList) {
            if (Boolean.TRUE.equals(child.getActive())) {
                activeChildren++;
            }
        }

        tvTotalChildrenCount.setText(String.valueOf(totalChildren));
        tvActiveChildrenCount.setText(String.valueOf(activeChildren));
    }



    // =====================================================
    // ADD CHILD BUTTON
    // =====================================================

    private void setupAddChildButton() {

        btnAddChild.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminChildManagementActivity.this,
                    AdminAddChildActivity.class
            );

            startActivity(intent);
        });
    }

    // =====================================================
    // SEARCH
    // =====================================================

    private void setupChildSearch() {

        etSearchChild.addTextChangedListener(
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
                        displayChildren();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    // =====================================================
    // LOAD CHILDREN FROM BACKEND
    // =====================================================

    private void loadChildrenFromApi() {

        tvNoChildren.setVisibility(View.VISIBLE);
        tvNoChildren.setText("Loading children...");

        adminChildRepository.getAdminChildren(
                new Callback<List<AdminChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminChildResponse>> call,
                            Response<List<AdminChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {

                            childList.clear();
                            childList.addAll(response.body());

                            updateChildCounts();
                            displayChildren();

                        } else {

                            childList.clear();
                            childListContainer.removeAllViews();

                            updateChildCounts();

                            tvNoChildren.setVisibility(View.VISIBLE);
                            tvNoChildren.setText(
                                    "Children load nahi hue. HTTP " + response.code()
                            );

                            Toast.makeText(
                                    AdminChildManagementActivity.this,
                                    "Child list API failed. HTTP " + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminChildResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        childList.clear();
                        updateChildCounts();
                        childListContainer.removeAllViews();

                        tvNoChildren.setVisibility(View.VISIBLE);
                        tvNoChildren.setText(
                                "Unable to connect to server"
                        );

                        android.util.Log.e(
                                "AdminChildAPI",
                                "Failed to load children",
                                t
                        );

                        Toast.makeText(
                                AdminChildManagementActivity.this,
                                "Server se child list nahi mili.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // DISPLAY AND FILTER CHILDREN
    // =====================================================

    private void displayChildren() {

        childListContainer.removeAllViews();

        String query = currentSearchQuery.toLowerCase();

        int displayedCount = 0;

        for (AdminChildResponse child : childList) {

            String childName = child.getChildName() == null
                    ? ""
                    : child.getChildName();

            if (!childName.toLowerCase().contains(query)) {
                continue;
            }

            String dateOfBirth = child.getDateOfBirth() == null
                    ? "Not provided"
                    : child.getDateOfBirth();

            String parentName = child.getParentName() == null
                    ? "Not assigned"
                    : child.getParentName();

            String branchName = child.getBranchName() == null
                    ? "Not assigned"
                    : child.getBranchName();

            List<String> therapies = child.getTherapyTypes();

            String therapyText;

            if (therapies == null || therapies.isEmpty()) {
                therapyText = "Therapy: Not assigned";
            } else {
                therapyText = "Therapy: "
                        + android.text.TextUtils.join(", ", therapies);
            }

            String status = Boolean.TRUE.equals(child.getActive())
                    ? "Active"
                    : "Inactive";

            addChildCard(
                    child,
                    childName,
                    "DOB: " + dateOfBirth,
                    "Parent: " + parentName,
                    branchName,
                    therapyText,
                    status
            );

            displayedCount++;
        }

        if (displayedCount == 0) {

            tvNoChildren.setVisibility(View.VISIBLE);

            if (childList.isEmpty()) {
                tvNoChildren.setText("No children registered");
            } else if (!query.isEmpty()) {
                tvNoChildren.setText("No child found");
            } else {
                tvNoChildren.setText("No children registered");
            }

        } else {
            tvNoChildren.setVisibility(View.GONE);
        }
    }

    // =====================================================
    // ADD CHILD CARD
    // =====================================================

    private void addChildCard(
            AdminChildResponse child,
            String childName,
            String dateOfBirth,
            String parentName,
            String branch,
            String therapy,
            String status) {

        View childView = LayoutInflater.from(this)
                .inflate(
                        R.layout.item_admin_child,
                        childListContainer,
                        false
                );

        TextView tvChildName =
                childView.findViewById(R.id.tvChildName);

        TextView tvChildAge =
                childView.findViewById(R.id.tvChildAge);

        TextView tvChildParent =
                childView.findViewById(R.id.tvChildParent);

        TextView tvChildBranch =
                childView.findViewById(R.id.tvChildBranch);

        TextView tvChildTherapy =
                childView.findViewById(R.id.tvChildTherapy);

        TextView tvChildStatus =
                childView.findViewById(R.id.tvChildStatus);

        tvChildName.setText(childName);
        tvChildAge.setText(dateOfBirth);
        tvChildParent.setText(parentName);
        tvChildBranch.setText("Branch: " + branch);
        tvChildTherapy.setText(therapy);
        tvChildStatus.setText(status);

        childView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminChildManagementActivity.this,
                    AdminChildDetailsActivity.class
            );

            if (child.getChildId() != null) {
                intent.putExtra(
                        "child_id",
                        child.getChildId()
                );
            }

            startActivity(intent);
        });

        childListContainer.addView(childView);
    }
}

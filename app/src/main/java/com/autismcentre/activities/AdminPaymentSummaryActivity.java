
package com.autismcentre.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminPaymentSummaryResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPaymentSummaryActivity extends BaseAdminActivity {

    private MaterialAutoCompleteTextView actAdminSummaryBranch;

    private TextView tvAdminSummaryTotalCollection;
    private TextView tvAdminSummaryPendingAmount;
    private TextView tvAdminSummaryTotalTransactions;

    private TextView tvAdminSummaryPaidCount;
    private TextView tvAdminSummaryPendingCount;
    private TextView tvAdminSummaryFailedCount;

    private MaterialButton btnAdminSummaryBack;

    private ApiService apiService;

    private final List<AdminBranchResponse> branchList =
            new ArrayList<>();

    private final List<String> branchNames =
            new ArrayList<>();

    private Long selectedBranchId = null;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_payment_summary
        );

        apiService = ApiClient.getApiService();

        initializeAdminPaymentSummaryViews();

        setupAdminPaymentSummaryBackButton();

        loadAdminBranches();
    }

    private void initializeAdminPaymentSummaryViews() {

        actAdminSummaryBranch =
                findViewById(R.id.actAdminSummaryBranch);

        tvAdminSummaryTotalCollection =
                findViewById(R.id.tvAdminSummaryTotalCollection);

        tvAdminSummaryPendingAmount =
                findViewById(R.id.tvAdminSummaryPendingAmount);

        tvAdminSummaryTotalTransactions =
                findViewById(R.id.tvAdminSummaryTotalTransactions);

        tvAdminSummaryPaidCount =
                findViewById(R.id.tvAdminSummaryPaidCount);

        tvAdminSummaryPendingCount =
                findViewById(R.id.tvAdminSummaryPendingCount);

        tvAdminSummaryFailedCount =
                findViewById(R.id.tvAdminSummaryFailedCount);

        btnAdminSummaryBack =
                findViewById(R.id.btnAdminSummaryBack);
    }

    // Load active branches dynamically from backend
    private void loadAdminBranches() {

        apiService.getAdminBranches().enqueue(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            branchList.clear();
                            branchNames.clear();

                            branchNames.add("All Branches");

                            for (AdminBranchResponse branch
                                    : response.body()) {

                                if (branch.getActive() != null
                                        && branch.getActive()) {

                                    branchList.add(branch);

                                    branchNames.add(
                                            branch.getBranchName()
                                    );
                                }
                            }

                            setupAdminPaymentSummaryBranchDropdown();

                            // Load overall summary initially
                            loadAdminPaymentSummary(null);

                        } else {

                            Toast.makeText(
                                    AdminPaymentSummaryActivity.this,
                                    "Unable to load branches",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                AdminPaymentSummaryActivity.this,
                                "Branch loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void setupAdminPaymentSummaryBranchDropdown() {

        ArrayAdapter<String> branchAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        branchNames
                );

        actAdminSummaryBranch.setAdapter(branchAdapter);

        actAdminSummaryBranch.setText(
                "All Branches",
                false
        );

        actAdminSummaryBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position == 0) {

                        selectedBranchId = null;

                    } else {

                        selectedBranchId =
                                branchList.get(position - 1).getId();
                    }

                    loadAdminPaymentSummary(selectedBranchId);
                }
        );
    }

    // Load summary for all branches or selected branch
    private void loadAdminPaymentSummary(Long branchId) {

        Call<AdminPaymentSummaryResponse> call;

        if (branchId == null) {

            call = apiService.getAdminPaymentSummary();

        } else {

            call = apiService.getAdminPaymentSummaryByBranch(
                    branchId
            );
        }

        call.enqueue(
                new Callback<AdminPaymentSummaryResponse>() {

                    @Override
                    public void onResponse(
                            Call<AdminPaymentSummaryResponse> call,
                            Response<AdminPaymentSummaryResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            updateAdminPaymentSummaryUI(
                                    response.body()
                            );

                        } else {

                            Toast.makeText(
                                    AdminPaymentSummaryActivity.this,
                                    "Unable to load payment summary",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminPaymentSummaryResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                AdminPaymentSummaryActivity.this,
                                "Payment summary loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void updateAdminPaymentSummaryUI(
            AdminPaymentSummaryResponse summary
    ) {

        NumberFormat currencyFormat =
                NumberFormat.getCurrencyInstance(
                        new Locale("en", "IN")
                );

        BigDecimal totalCollection =
                summary.getTotalCollection() != null
                        ? summary.getTotalCollection()
                        : BigDecimal.ZERO;

        BigDecimal pendingAmount =
                summary.getPendingTransactionAmount() != null
                        ? summary.getPendingTransactionAmount()
                        : BigDecimal.ZERO;

        Long totalTransactions =
                summary.getTotalTransactions() != null
                        ? summary.getTotalTransactions()
                        : 0L;

        Long successfulTransactions =
                summary.getSuccessfulTransactions() != null
                        ? summary.getSuccessfulTransactions()
                        : 0L;

        Long pendingTransactions =
                summary.getPendingTransactions() != null
                        ? summary.getPendingTransactions()
                        : 0L;

        Long failedTransactions =
                summary.getFailedTransactions() != null
                        ? summary.getFailedTransactions()
                        : 0L;

        tvAdminSummaryTotalCollection.setText(
                "Total Collection: "
                        + currencyFormat.format(totalCollection)
        );

        tvAdminSummaryPendingAmount.setText(
                "Pending Transaction Amount: "
                        + currencyFormat.format(pendingAmount)
        );

        tvAdminSummaryTotalTransactions.setText(
                "Total Transactions: "
                        + totalTransactions
        );

        tvAdminSummaryPaidCount.setText(
                "Successful Transactions: "
                        + successfulTransactions
        );

        tvAdminSummaryPendingCount.setText(
                "Pending Transactions: "
                        + pendingTransactions
        );

        tvAdminSummaryFailedCount.setText(
                "Failed Transactions: "
                        + failedTransactions
        );
    }

    private void setupAdminPaymentSummaryBackButton() {

        btnAdminSummaryBack.setOnClickListener(
                v -> finish()
        );
    }
}

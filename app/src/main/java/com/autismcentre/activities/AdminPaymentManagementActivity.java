
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
import com.autismcentre.model.AdminPaymentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPaymentManagementActivity extends BaseAdminActivity {

    private TextInputEditText etAdminSearchPayment;

    private MaterialAutoCompleteTextView actAdminPaymentBranch;
    private MaterialAutoCompleteTextView actAdminPaymentStatus;

    private LinearLayout adminPaymentListContainer;
    private TextView tvAdminNoPayment;

    private MaterialButton btnAdminAddPayment;
    private MaterialButton btnAdminPaymentSummary;

    private ApiService apiService;

    private final List<AdminBranchResponse> branchList =
            new ArrayList<>();

    private final List<String> branchNames =
            new ArrayList<>();

    private final List<AdminPaymentResponse> allPayments =
            new ArrayList<>();

    private Long selectedBranchId = null;

    private String selectedAdminPaymentStatus = "All";

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    new Locale("en", "IN")
            );

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_payment_management
        );

        apiService = ApiClient.getApiService();

        initializeAdminPaymentViews();

        setupAdminPaymentStatusDropdown();

        setupAdminPaymentSearch();

        setupAdminPaymentSummaryButton();

        // Add Payment functionality is preserved for future use.
        // setupAdminAddPaymentButton();

        // Hide Add Payment button for now.
        btnAdminAddPayment.setVisibility(View.GONE);

        loadAdminBranches();
    }

    private void initializeAdminPaymentViews() {

        etAdminSearchPayment =
                findViewById(R.id.etAdminSearchPayment);

        actAdminPaymentBranch =
                findViewById(R.id.actAdminPaymentBranch);

        actAdminPaymentStatus =
                findViewById(R.id.actAdminPaymentStatus);

        adminPaymentListContainer =
                findViewById(R.id.adminPaymentListContainer);

        tvAdminNoPayment =
                findViewById(R.id.tvAdminNoPayment);

        btnAdminAddPayment =
                findViewById(R.id.btnAdminAddPayment);

        btnAdminPaymentSummary =
                findViewById(R.id.btnAdminPaymentSummary);
    }

    // Preserved for future use.
    private void setupAdminAddPaymentButton() {

        btnAdminAddPayment.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminPaymentManagementActivity.this,
                    AdminAddPaymentActivity.class
            );

            startActivityForResult(intent, 7001);
        });
    }

    private void setupAdminPaymentSummaryButton() {

        btnAdminPaymentSummary.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminPaymentManagementActivity.this,
                    AdminPaymentSummaryActivity.class
            );

            startActivity(intent);
        });
    }

    // Load active branches from backend.
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

                            setupAdminPaymentBranchDropdown();

                            loadAdminPayments(null);

                        } else {

                            Toast.makeText(
                                    AdminPaymentManagementActivity.this,
                                    "Unable to load branches",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Still try loading all payment records.
                            loadAdminPayments(null);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                AdminPaymentManagementActivity.this,
                                "Branch loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                        loadAdminPayments(null);
                    }
                }
        );
    }

    private void setupAdminPaymentBranchDropdown() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        branchNames
                );

        actAdminPaymentBranch.setAdapter(adapter);

        actAdminPaymentBranch.setText(
                "All Branches",
                false
        );

        actAdminPaymentBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (position == 0) {

                        selectedBranchId = null;

                    } else {

                        selectedBranchId =
                                branchList.get(position - 1).getId();
                    }

                    loadAdminPayments(selectedBranchId);
                }
        );
    }

    private void setupAdminPaymentStatusDropdown() {

        String[] statuses = {
                "All",
                "SUCCESS",
                "PENDING",
                "FAILED",
                "ERROR"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        statuses
                );

        actAdminPaymentStatus.setAdapter(adapter);

        actAdminPaymentStatus.setText("All", false);

        actAdminPaymentStatus.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedAdminPaymentStatus =
                            statuses[position];

                    renderAdminPayments();
                }
        );
    }

    private void setupAdminPaymentSearch() {

        etAdminSearchPayment.addTextChangedListener(
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
                        renderAdminPayments();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    // Load successful and non-successful payment records.
    private void loadAdminPayments(Long branchId) {

        final List<AdminPaymentResponse> combinedPayments =
                new ArrayList<>();

        final int[] completedRequests = {0};
        final boolean[] requestFailed = {false};

        adminPaymentListContainer.removeAllViews();

        Call<List<AdminPaymentResponse>> paidCall;

        Call<List<AdminPaymentResponse>> unpaidCall;

        if (branchId == null) {

            paidCall = apiService.getAdminPaidPayments();

            unpaidCall = apiService.getAdminUnpaidPayments();

        } else {

            paidCall =
                    apiService.getAdminPaidPaymentsByBranch(branchId);

            unpaidCall =
                    apiService.getAdminUnpaidPaymentsByBranch(branchId);
        }

        Callback<List<AdminPaymentResponse>> callback =
                new Callback<List<AdminPaymentResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminPaymentResponse>> call,
                            Response<List<AdminPaymentResponse>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            combinedPayments.addAll(response.body());

                        } else {

                            requestFailed[0] = true;
                        }

                        finishPaymentLoading();
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminPaymentResponse>> call,
                            Throwable t
                    ) {

                        requestFailed[0] = true;

                        finishPaymentLoading();
                    }

                    private void finishPaymentLoading() {

                        completedRequests[0]++;

                        if (completedRequests[0] == 2) {

                            if (requestFailed[0]) {

                                Toast.makeText(
                                        AdminPaymentManagementActivity.this,
                                        "Unable to load all payment records",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            allPayments.clear();
                            allPayments.addAll(combinedPayments);

                            renderAdminPayments();
                        }
                    }
                };

        paidCall.enqueue(callback);
        unpaidCall.enqueue(callback);
    }

    // Apply search and status filters locally.
    private void renderAdminPayments() {

        adminPaymentListContainer.removeAllViews();

        String searchText = "";

        if (etAdminSearchPayment.getText() != null) {

            searchText = etAdminSearchPayment
                    .getText()
                    .toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);
        }

        for (AdminPaymentResponse payment : allPayments) {

            if (matchesAdminPaymentFilter(
                    payment,
                    searchText
            )) {

                addAdminPayment(payment);
            }
        }

        updateAdminPaymentEmptyState();
    }

    private boolean matchesAdminPaymentFilter(
            AdminPaymentResponse payment,
            String searchText
    ) {

        String childName = safeString(payment.getChildName());
        String parentName = safeString(payment.getParentName());
        String branchName = safeString(payment.getBranchName());
        String therapyType = safeString(payment.getTherapyType());
        String status = safeString(payment.getStatus());

        boolean branchMatches =
                selectedBranchId == null
                        || (payment.getBranchId() != null
                        && selectedBranchId.equals(
                        payment.getBranchId()
                ));

        boolean statusMatches =
                "All".equals(selectedAdminPaymentStatus)
                        || selectedAdminPaymentStatus.equalsIgnoreCase(
                        status
                );

        boolean searchMatches =
                searchText.isEmpty()
                        || childName.toLowerCase(Locale.ROOT)
                        .contains(searchText)
                        || parentName.toLowerCase(Locale.ROOT)
                        .contains(searchText)
                        || branchName.toLowerCase(Locale.ROOT)
                        .contains(searchText)
                        || therapyType.toLowerCase(Locale.ROOT)
                        .contains(searchText);

        return branchMatches
                && statusMatches
                && searchMatches;
    }

    private void addAdminPayment(
            AdminPaymentResponse payment
    ) {

        View paymentView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_admin_payment,
                        adminPaymentListContainer,
                        false
                );

        TextView tvChild =
                paymentView.findViewById(
                        R.id.tvAdminPaymentChild
                );

        TextView tvParent =
                paymentView.findViewById(
                        R.id.tvAdminPaymentParent
                );

        TextView tvTherapist =
                paymentView.findViewById(
                        R.id.tvAdminPaymentTherapist
                );

        TextView tvBranch =
                paymentView.findViewById(
                        R.id.tvAdminPaymentBranch
                );

        TextView tvTherapy =
                paymentView.findViewById(
                        R.id.tvAdminPaymentTherapy
                );

        TextView tvActualFee =
                paymentView.findViewById(
                        R.id.tvAdminPaymentActualFee
                );

        TextView tvDiscount =
                paymentView.findViewById(
                        R.id.tvAdminPaymentDiscount
                );

        TextView tvFinalPayable =
                paymentView.findViewById(
                        R.id.tvAdminPaymentFinalPayable
                );

        TextView tvReceived =
                paymentView.findViewById(
                        R.id.tvAdminPaymentReceived
                );

        TextView tvDue =
                paymentView.findViewById(
                        R.id.tvAdminPaymentDue
                );

        TextView tvPaymentDate =
                paymentView.findViewById(
                        R.id.tvAdminPaymentDate
                );

        TextView tvStatus =
                paymentView.findViewById(
                        R.id.tvAdminPaymentStatus
                );

        tvChild.setText(
                getString(
                        R.string.admin_payment_child,
                        safeString(payment.getChildName())
                )
        );

        tvParent.setText(
                getString(
                        R.string.admin_payment_parent,
                        safeString(payment.getParentName())
                )
        );

        // Therapist information is not available in the DTO.
        tvTherapist.setVisibility(View.GONE);

        tvBranch.setText(
                getString(
                        R.string.admin_payment_branch,
                        safeString(payment.getBranchName())
                )
        );

        tvTherapy.setText(
                getString(
                        R.string.admin_payment_therapy,
                        safeString(payment.getTherapyType())
                )
        );

        tvActualFee.setText(
                "Actual Fee: "
                        + formatAmount(payment.getActualFee())
        );

        tvDiscount.setText(
                "Discount: "
                        + formatAmount(payment.getDiscount())
        );

        tvFinalPayable.setText(
                "Final Payable: "
                        + formatAmount(payment.getFinalPayable())
        );

        tvReceived.setText(
                "Received: "
                        + formatAmount(payment.getPaidAmount())
        );

        // Due amount is not provided by the backend DTO.
        tvDue.setVisibility(View.GONE);

        String paymentDate =
                formatPaymentDate(
                        payment.getPaymentDate(),
                        payment.getCreatedAt()
                );

        tvPaymentDate.setText(
                getString(
                        R.string.admin_payment_date,
                        paymentDate
                )
        );

        tvStatus.setText(
                safeString(payment.getStatus())
        );

        paymentView.setOnClickListener(v -> {

            Intent paymentIntent = new Intent(
                    AdminPaymentManagementActivity.this,
                    AdminPaymentDetailsActivity.class
            );

            paymentIntent.putExtra(
                    "payment_child",
                    safeString(payment.getChildName())
            );

            paymentIntent.putExtra(
                    "payment_parent",
                    safeString(payment.getParentName())
            );

            paymentIntent.putExtra(
                    "payment_therapist",
                    "Not available"
            );

            paymentIntent.putExtra(
                    "payment_branch",
                    safeString(payment.getBranchName())
            );

            paymentIntent.putExtra(
                    "payment_therapy",
                    safeString(payment.getTherapyType())
            );

            paymentIntent.putExtra(
                    "payment_actual_fee",
                    amountToInt(payment.getActualFee())
            );

            paymentIntent.putExtra(
                    "payment_discount",
                    amountToInt(payment.getDiscount())
            );

            paymentIntent.putExtra(
                    "payment_final_payable",
                    amountToInt(payment.getFinalPayable())
            );

            paymentIntent.putExtra(
                    "payment_received",
                    amountToInt(payment.getPaidAmount())
            );

            paymentIntent.putExtra(
                    "payment_due",
                    0
            );

            paymentIntent.putExtra(
                    "payment_date",
                    paymentDate
            );

            paymentIntent.putExtra(
                    "payment_method",
                    "Not available"
            );

            paymentIntent.putExtra(
                    "payment_transaction_id",
                    "Not available"
            );

            paymentIntent.putExtra(
                    "payment_status",
                    safeString(payment.getStatus())
            );

            paymentIntent.putExtra(
                    "payment_notes",
                    "Not available"
            );

            paymentIntent.putExtra(
                    "payment_id",
                    payment.getPaymentId()
            );

            startActivity(paymentIntent);
        });

        adminPaymentListContainer.addView(paymentView);
    }

    private String formatAmount(BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return currencyFormat.format(amount);
    }

    private int amountToInt(BigDecimal amount) {

        return amount != null ? amount.intValue() : 0;
    }

    private String formatPaymentDate(
            String paymentDate,
            String createdAt
    ) {

        String dateValue = paymentDate;

        if (dateValue == null || dateValue.trim().isEmpty()) {
            dateValue = createdAt;
        }

        if (dateValue == null || dateValue.trim().isEmpty()) {
            return "Not available";
        }

        String[] inputPatterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String pattern : inputPatterns) {

            try {

                SimpleDateFormat inputFormat =
                        new SimpleDateFormat(pattern, Locale.ENGLISH);

                inputFormat.setLenient(false);

                Date date = inputFormat.parse(dateValue);

                if (date != null) {

                    SimpleDateFormat outputFormat =
                            new SimpleDateFormat(
                                    "dd MMM yyyy",
                                    Locale.ENGLISH
                            );

                    return outputFormat.format(date);
                }

            } catch (ParseException ignored) {
                // Try next date pattern.
            }
        }

        return dateValue;
    }
    private String safeString(String value) {

        return value != null && !value.trim().isEmpty()
                ? value
                : "Not available";
    }

    private void updateAdminPaymentEmptyState() {

        if (adminPaymentListContainer.getChildCount() == 0) {

            tvAdminNoPayment.setVisibility(View.VISIBLE);

        } else {

            tvAdminNoPayment.setVisibility(View.GONE);
        }
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

        // Preserved for future Add Payment functionality.
        if (requestCode == 7001
                && resultCode == RESULT_OK) {

            loadAdminPayments(selectedBranchId);
        }
    }
}

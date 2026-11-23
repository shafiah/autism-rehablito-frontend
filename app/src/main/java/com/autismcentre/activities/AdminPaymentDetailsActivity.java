package com.autismcentre.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;

public class AdminPaymentDetailsActivity extends BaseAdminActivity {

    private TextView tvAdminPaymentDetailChild;
    private TextView tvAdminPaymentDetailParent;
    private TextView tvAdminPaymentDetailTherapist;
    private TextView tvAdminPaymentDetailBranch;
    private TextView tvAdminPaymentDetailTherapy;

    private TextView tvAdminPaymentDetailActualFee;
    private TextView tvAdminPaymentDetailDiscount;
    private TextView tvAdminPaymentDetailFinalPayable;
    private TextView tvAdminPaymentDetailReceived;
    private TextView tvAdminPaymentDetailDue;

    private TextView tvAdminPaymentDetailDate;
    private TextView tvAdminPaymentDetailMethod;
    private TextView tvAdminPaymentDetailTransactionId;
    private TextView tvAdminPaymentDetailStatus;
    private TextView tvAdminPaymentDetailNotes;

    private MaterialButton btnAdminPaymentBack;
    private MaterialButton btnAdminViewPaymentReceipt;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_payment_details
        );

        initializeAdminPaymentDetailsViews();

        loadAdminPaymentDetails();

        setupAdminPaymentBackButton();

        setupAdminViewPaymentReceipt();
    }

    private void initializeAdminPaymentDetailsViews() {

        tvAdminPaymentDetailChild =
                findViewById(
                        R.id.tvAdminPaymentDetailChild
                );

        tvAdminPaymentDetailParent =
                findViewById(
                        R.id.tvAdminPaymentDetailParent
                );

        tvAdminPaymentDetailTherapist =
                findViewById(
                        R.id.tvAdminPaymentDetailTherapist
                );

        tvAdminPaymentDetailBranch =
                findViewById(
                        R.id.tvAdminPaymentDetailBranch
                );

        tvAdminPaymentDetailTherapy =
                findViewById(
                        R.id.tvAdminPaymentDetailTherapy
                );

        tvAdminPaymentDetailActualFee =
                findViewById(
                        R.id.tvAdminPaymentDetailActualFee
                );

        tvAdminPaymentDetailDiscount =
                findViewById(
                        R.id.tvAdminPaymentDetailDiscount
                );

        tvAdminPaymentDetailFinalPayable =
                findViewById(
                        R.id.tvAdminPaymentDetailFinalPayable
                );

        tvAdminPaymentDetailReceived =
                findViewById(
                        R.id.tvAdminPaymentDetailReceived
                );

        tvAdminPaymentDetailDue =
                findViewById(
                        R.id.tvAdminPaymentDetailDue
                );

        tvAdminPaymentDetailDate =
                findViewById(
                        R.id.tvAdminPaymentDetailDate
                );

        tvAdminPaymentDetailMethod =
                findViewById(
                        R.id.tvAdminPaymentDetailMethod
                );

        tvAdminPaymentDetailTransactionId =
                findViewById(
                        R.id.tvAdminPaymentDetailTransactionId
                );

        tvAdminPaymentDetailStatus =
                findViewById(
                        R.id.tvAdminPaymentDetailStatus
                );

        tvAdminPaymentDetailNotes =
                findViewById(
                        R.id.tvAdminPaymentDetailNotes
                );

        btnAdminPaymentBack =
                findViewById(
                        R.id.btnAdminPaymentBack
                );

        btnAdminViewPaymentReceipt =
                findViewById(
                        R.id.btnAdminViewPaymentReceipt
                );
    }

    private void loadAdminPaymentDetails() {

        String childName =
                getIntent().getStringExtra(
                        "payment_child"
                );

        String parentName =
                getIntent().getStringExtra(
                        "payment_parent"
                );

        String therapistName =
                getIntent().getStringExtra(
                        "payment_therapist"
                );

        String branch =
                getIntent().getStringExtra(
                        "payment_branch"
                );

        String therapyType =
                getIntent().getStringExtra(
                        "payment_therapy"
                );

        int actualFee =
                getIntent().getIntExtra(
                        "payment_actual_fee",
                        0
                );

        int discountAmount =
                getIntent().getIntExtra(
                        "payment_discount",
                        0
                );

        int finalPayableAmount =
                getIntent().getIntExtra(
                        "payment_final_payable",
                        0
                );

        int receivedAmount =
                getIntent().getIntExtra(
                        "payment_received",
                        0
                );

        int dueAmount =
                getIntent().getIntExtra(
                        "payment_due",
                        0
                );

        String paymentDate =
                getIntent().getStringExtra(
                        "payment_date"
                );

        String paymentMethod =
                getIntent().getStringExtra(
                        "payment_method"
                );

        String transactionId =
                getIntent().getStringExtra(
                        "payment_transaction_id"
                );

        String paymentStatus =
                getIntent().getStringExtra(
                        "payment_status"
                );

        String paymentNotes =
                getIntent().getStringExtra(
                        "payment_notes"
                );

        if (childName == null) {
            childName = "Aarav Kumar";
        }

        if (parentName == null) {
            parentName = "Rahul Kumar";
        }

        if (therapistName == null) {
            therapistName = "Dr. Amit Kumar";
        }

        if (branch == null) {
            branch = "Siwan";
        }

        if (therapyType == null) {
            therapyType = "Occupational Therapy";
        }

        if (paymentDate == null) {
            paymentDate = "21 Sep 2026";
        }

        if (paymentMethod == null) {
            paymentMethod = "UPI";
        }

        if (transactionId == null) {
            transactionId = "TXN123456";
        }

        if (paymentStatus == null) {
            paymentStatus = "Paid";
        }

        if (paymentNotes == null) {
            paymentNotes =
                    "September therapy payment";
        }

        tvAdminPaymentDetailChild.setText(
                getString(
                        R.string.admin_payment_child,
                        childName
                )
        );

        tvAdminPaymentDetailParent.setText(
                getString(
                        R.string.admin_payment_parent,
                        parentName
                )
        );

        tvAdminPaymentDetailTherapist.setText(
                getString(
                        R.string.admin_payment_therapist,
                        therapistName
                )
        );

        tvAdminPaymentDetailBranch.setText(
                getString(
                        R.string.admin_payment_branch,
                        branch
                )
        );

        tvAdminPaymentDetailTherapy.setText(
                getString(
                        R.string.admin_payment_therapy,
                        therapyType
                )
        );

        tvAdminPaymentDetailActualFee.setText(
                getString(
                        R.string.admin_payment_actual_fee,
                        actualFee
                )
        );

        tvAdminPaymentDetailDiscount.setText(
                getString(
                        R.string.admin_payment_discount,
                        discountAmount
                )
        );

        tvAdminPaymentDetailFinalPayable.setText(
                getString(
                        R.string.admin_payment_final_payable,
                        finalPayableAmount
                )
        );

        tvAdminPaymentDetailReceived.setText(
                getString(
                        R.string.admin_payment_received,
                        receivedAmount
                )
        );

        tvAdminPaymentDetailDue.setText(
                getString(
                        R.string.admin_payment_due,
                        dueAmount
                )
        );

        tvAdminPaymentDetailDate.setText(
                getString(
                        R.string.admin_payment_date,
                        paymentDate
                )
        );

        tvAdminPaymentDetailMethod.setText(
                getString(
                        R.string.admin_payment_method,
                        paymentMethod
                )
        );

        tvAdminPaymentDetailTransactionId.setText(
                getString(
                        R.string.admin_payment_transaction,
                        transactionId
                )
        );

        tvAdminPaymentDetailStatus.setText(
                paymentStatus
        );

        tvAdminPaymentDetailNotes.setText(
                getString(
                        R.string.admin_payment_notes,
                        paymentNotes
                )
        );
    }

    private void setupAdminViewPaymentReceipt() {

        btnAdminViewPaymentReceipt.setOnClickListener(
                v -> {

                    android.content.Intent receiptIntent =
                            new android.content.Intent(
                                    AdminPaymentDetailsActivity.this,
                                    AdminPaymentReceiptActivity.class
                            );

                    receiptIntent.putExtra(
                            "payment_receipt_number",
                            "RCPT-2026-001"
                    );

                    receiptIntent.putExtra(
                            "payment_date",
                            getIntent().getStringExtra(
                                    "payment_date"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_child",
                            getIntent().getStringExtra(
                                    "payment_child"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_parent",
                            getIntent().getStringExtra(
                                    "payment_parent"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_branch",
                            getIntent().getStringExtra(
                                    "payment_branch"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_therapist",
                            getIntent().getStringExtra(
                                    "payment_therapist"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_therapy",
                            getIntent().getStringExtra(
                                    "payment_therapy"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_actual_fee",
                            getIntent().getIntExtra(
                                    "payment_actual_fee",
                                    0
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_discount",
                            getIntent().getIntExtra(
                                    "payment_discount",
                                    0
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_final_payable",
                            getIntent().getIntExtra(
                                    "payment_final_payable",
                                    0
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_received",
                            getIntent().getIntExtra(
                                    "payment_received",
                                    0
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_due",
                            getIntent().getIntExtra(
                                    "payment_due",
                                    0
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_method",
                            getIntent().getStringExtra(
                                    "payment_method"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_transaction_id",
                            getIntent().getStringExtra(
                                    "payment_transaction_id"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_status",
                            getIntent().getStringExtra(
                                    "payment_status"
                            )
                    );

                    receiptIntent.putExtra(
                            "payment_notes",
                            getIntent().getStringExtra(
                                    "payment_notes"
                            )
                    );

                    startActivity(receiptIntent);
                }
        );
    }

    private void setupAdminPaymentBackButton() {

        btnAdminPaymentBack.setOnClickListener(
                v -> finish()
        );
    }
}
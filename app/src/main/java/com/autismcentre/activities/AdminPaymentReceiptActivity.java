package com.autismcentre.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;

public class AdminPaymentReceiptActivity
        extends BaseAdminActivity {

    private TextView tvAdminReceiptNumber;
    private TextView tvAdminReceiptDate;

    private TextView tvAdminReceiptChild;
    private TextView tvAdminReceiptParent;
    private TextView tvAdminReceiptBranch;
    private TextView tvAdminReceiptTherapist;
    private TextView tvAdminReceiptTherapy;

    private TextView tvAdminReceiptActualFee;
    private TextView tvAdminReceiptDiscount;
    private TextView tvAdminReceiptFinalPayable;
    private TextView tvAdminReceiptReceived;
    private TextView tvAdminReceiptDue;

    private TextView tvAdminReceiptPaymentMethod;
    private TextView tvAdminReceiptTransactionId;
    private TextView tvAdminReceiptStatus;
    private TextView tvAdminReceiptNotes;

    private MaterialButton btnAdminReceiptBack;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_payment_receipt
        );

        initializeAdminReceiptViews();

        loadAdminReceiptData();

        setupAdminReceiptBackButton();
    }

    private void initializeAdminReceiptViews() {

        tvAdminReceiptNumber =
                findViewById(
                        R.id.tvAdminReceiptNumber
                );

        tvAdminReceiptDate =
                findViewById(
                        R.id.tvAdminReceiptDate
                );

        tvAdminReceiptChild =
                findViewById(
                        R.id.tvAdminReceiptChild
                );

        tvAdminReceiptParent =
                findViewById(
                        R.id.tvAdminReceiptParent
                );

        tvAdminReceiptBranch =
                findViewById(
                        R.id.tvAdminReceiptBranch
                );

        tvAdminReceiptTherapist =
                findViewById(
                        R.id.tvAdminReceiptTherapist
                );

        tvAdminReceiptTherapy =
                findViewById(
                        R.id.tvAdminReceiptTherapy
                );

        tvAdminReceiptActualFee =
                findViewById(
                        R.id.tvAdminReceiptActualFee
                );

        tvAdminReceiptDiscount =
                findViewById(
                        R.id.tvAdminReceiptDiscount
                );

        tvAdminReceiptFinalPayable =
                findViewById(
                        R.id.tvAdminReceiptFinalPayable
                );

        tvAdminReceiptReceived =
                findViewById(
                        R.id.tvAdminReceiptReceived
                );

        tvAdminReceiptDue =
                findViewById(
                        R.id.tvAdminReceiptDue
                );

        tvAdminReceiptPaymentMethod =
                findViewById(
                        R.id.tvAdminReceiptPaymentMethod
                );

        tvAdminReceiptTransactionId =
                findViewById(
                        R.id.tvAdminReceiptTransactionId
                );

        tvAdminReceiptStatus =
                findViewById(
                        R.id.tvAdminReceiptStatus
                );

        tvAdminReceiptNotes =
                findViewById(
                        R.id.tvAdminReceiptNotes
                );

        btnAdminReceiptBack =
                findViewById(
                        R.id.btnAdminReceiptBack
                );
    }

    private void loadAdminReceiptData() {

        String receiptNumber =
                getIntent().getStringExtra(
                        "payment_receipt_number"
                );

        String paymentDate =
                getIntent().getStringExtra(
                        "payment_date"
                );

        String childName =
                getIntent().getStringExtra(
                        "payment_child"
                );

        String parentName =
                getIntent().getStringExtra(
                        "payment_parent"
                );

        String branch =
                getIntent().getStringExtra(
                        "payment_branch"
                );

        String therapistName =
                getIntent().getStringExtra(
                        "payment_therapist"
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

        if (receiptNumber == null) {
            receiptNumber = "RCPT-2026-001";
        }

        if (paymentDate == null) {
            paymentDate = "21 Sep 2026";
        }

        if (childName == null) {
            childName = "Aarav Kumar";
        }

        if (parentName == null) {
            parentName = "Rahul Kumar";
        }

        if (branch == null) {
            branch = "Siwan";
        }

        if (therapistName == null) {
            therapistName = "Dr. Amit Kumar";
        }

        if (therapyType == null) {
            therapyType = "Occupational Therapy";
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

        tvAdminReceiptNumber.setText(
                getString(
                        R.string.admin_receipt_number,
                        receiptNumber
                )
        );

        tvAdminReceiptDate.setText(
                getString(
                        R.string.admin_receipt_date,
                        paymentDate
                )
        );

        tvAdminReceiptChild.setText(
                getString(
                        R.string.admin_payment_child,
                        childName
                )
        );

        tvAdminReceiptParent.setText(
                getString(
                        R.string.admin_payment_parent,
                        parentName
                )
        );

        tvAdminReceiptBranch.setText(
                getString(
                        R.string.admin_payment_branch,
                        branch
                )
        );

        tvAdminReceiptTherapist.setText(
                getString(
                        R.string.admin_payment_therapist,
                        therapistName
                )
        );

        tvAdminReceiptTherapy.setText(
                getString(
                        R.string.admin_payment_therapy,
                        therapyType
                )
        );

        tvAdminReceiptActualFee.setText(
                getString(
                        R.string.admin_payment_actual_fee,
                        actualFee
                )
        );

        tvAdminReceiptDiscount.setText(
                getString(
                        R.string.admin_payment_discount,
                        discountAmount
                )
        );

        tvAdminReceiptFinalPayable.setText(
                getString(
                        R.string.admin_payment_final_payable,
                        finalPayableAmount
                )
        );

        tvAdminReceiptReceived.setText(
                getString(
                        R.string.admin_payment_received,
                        receivedAmount
                )
        );

        tvAdminReceiptDue.setText(
                getString(
                        R.string.admin_payment_due,
                        dueAmount
                )
        );

        tvAdminReceiptPaymentMethod.setText(
                getString(
                        R.string.admin_payment_method,
                        paymentMethod
                )
        );

        tvAdminReceiptTransactionId.setText(
                getString(
                        R.string.admin_payment_transaction,
                        transactionId
                )
        );

        tvAdminReceiptStatus.setText(
                paymentStatus
        );

        tvAdminReceiptNotes.setText(
                getString(
                        R.string.admin_payment_notes,
                        paymentNotes
                )
        );
    }

    private void setupAdminReceiptBackButton() {

        btnAdminReceiptBack.setOnClickListener(
                v -> finish()
        );
    }
}
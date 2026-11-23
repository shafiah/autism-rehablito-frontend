package com.autismcentre.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminAddPaymentActivity extends BaseAdminActivity {

    private MaterialAutoCompleteTextView actAdminAddPaymentBranch;
    private MaterialAutoCompleteTextView actAdminAddPaymentChild;
    private MaterialAutoCompleteTextView actAdminAddPaymentParent;
    private MaterialAutoCompleteTextView actAdminAddPaymentTherapist;
    private MaterialAutoCompleteTextView actAdminAddPaymentTherapy;
    private MaterialAutoCompleteTextView actAdminAddPaymentMethod;

    private TextInputEditText etAdminAddPaymentActualFee;
    private TextInputEditText etAdminAddPaymentDiscount;
    private TextInputEditText etAdminAddPaymentFinalPayable;
    private TextInputEditText etAdminAddPaymentReceived;
    private TextInputEditText etAdminAddPaymentDue;
    private TextInputEditText etAdminAddPaymentDate;
    private TextInputEditText etAdminAddPaymentDueDate;
    private TextInputEditText etAdminAddPaymentTransactionId;
    private TextInputEditText etAdminAddPaymentNotes;

    private MaterialButton btnAdminSavePayment;
    private MaterialButton btnAdminCancelPayment;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_add_payment
        );

        initializeAdminAddPaymentViews();

        setupAdminAddPaymentDropdowns();

        setupAdminAddPaymentCalculations();

        setupAdminPaymentDatePicker();

        setupAdminPaymentDueDatePicker();

        setupAdminAddPaymentButtons();
    }

    private void initializeAdminAddPaymentViews() {

        actAdminAddPaymentBranch =
                findViewById(
                        R.id.actAdminAddPaymentBranch
                );

        actAdminAddPaymentChild =
                findViewById(
                        R.id.actAdminAddPaymentChild
                );

        actAdminAddPaymentParent =
                findViewById(
                        R.id.actAdminAddPaymentParent
                );

        actAdminAddPaymentTherapist =
                findViewById(
                        R.id.actAdminAddPaymentTherapist
                );

        actAdminAddPaymentTherapy =
                findViewById(
                        R.id.actAdminAddPaymentTherapy
                );

        actAdminAddPaymentMethod =
                findViewById(
                        R.id.actAdminAddPaymentMethod
                );

        etAdminAddPaymentActualFee =
                findViewById(
                        R.id.etAdminAddPaymentActualFee
                );

        etAdminAddPaymentDiscount =
                findViewById(
                        R.id.etAdminAddPaymentDiscount
                );

        etAdminAddPaymentFinalPayable =
                findViewById(
                        R.id.etAdminAddPaymentFinalPayable
                );

        etAdminAddPaymentReceived =
                findViewById(
                        R.id.etAdminAddPaymentReceived
                );

        etAdminAddPaymentDue =
                findViewById(
                        R.id.etAdminAddPaymentDue
                );

        etAdminAddPaymentDate =
                findViewById(
                        R.id.etAdminAddPaymentDate
                );

        etAdminAddPaymentDueDate =
                findViewById(
                        R.id.etAdminAddPaymentDueDate
                );

        etAdminAddPaymentTransactionId =
                findViewById(
                        R.id.etAdminAddPaymentTransactionId
                );

        etAdminAddPaymentNotes =
                findViewById(
                        R.id.etAdminAddPaymentNotes
                );

        btnAdminSavePayment =
                findViewById(
                        R.id.btnAdminSavePayment
                );

        btnAdminCancelPayment =
                findViewById(
                        R.id.btnAdminCancelPayment
                );
    }

    private void setupAdminAddPaymentDropdowns() {

        setupDropdown(
                actAdminAddPaymentBranch,
                new String[]{
                        "Patna",
                        "Siwan",
                        "Madhubani",
                        "Muzaffarpur",
                        "Hajipur",
                        "Ramnagri"
                },
                "Select Branch"
        );

        setupDropdown(
                actAdminAddPaymentChild,
                new String[]{
                        "Aarav Kumar",
                        "Ayaan Singh",
                        "Advik Sharma",
                        "Arhaan Khan"
                },
                "Select Child"
        );

        setupDropdown(
                actAdminAddPaymentParent,
                new String[]{
                        "Rahul Kumar",
                        "Priya Singh",
                        "Sanjay Kumar",
                        "Neha Devi",
                        "Rakesh Kumar",
                        "Pooja Gupta"
                },
                "Select Parent"
        );

        setupDropdown(
                actAdminAddPaymentTherapist,
                new String[]{
                        "Dr. Neha Sharma",
                        "Dr. Priya Singh",
                        "Dr. Amit Kumar",
                        "Dr. Riya Verma",
                        "Dr. Anjali Singh",
                        "Dr. Sneha Gupta"
                },
                "Select Therapist"
        );

        setupDropdown(
                actAdminAddPaymentTherapy,
                new String[]{
                        "Occupational Therapy",
                        "Speech Therapy"
                },
                "Select Therapy"
        );

        setupDropdown(
                actAdminAddPaymentMethod,
                new String[]{
                        "Cash",
                        "UPI",
                        "Bank Transfer",
                        "Card",
                        "Other"
                },
                "Select Payment Method"
        );
    }

    private String calculateAdminPaymentStatus(
            int finalPayable,
            int received,
            String dueDate
    ) {

        if (received <= 0) {

            if (isAdminPaymentOverdue(dueDate)) {
                return "Overdue";
            }

            return "Pending";
        }

        if (received < finalPayable) {

            if (isAdminPaymentOverdue(dueDate)) {
                return "Overdue";
            }

            return "Partial";
        }

        return "Paid";
    }

    private boolean isAdminPaymentOverdue(
            String dueDate
    ) {

        if (dueDate == null || dueDate.trim().isEmpty()) {
            return false;
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                );

        dateFormat.setLenient(false);

        try {

            Date paymentDueDate =
                    dateFormat.parse(dueDate);

            if (paymentDueDate == null) {
                return false;
            }

            Calendar today =
                    Calendar.getInstance();

            today.set(
                    Calendar.HOUR_OF_DAY,
                    0
            );

            today.set(
                    Calendar.MINUTE,
                    0
            );

            today.set(
                    Calendar.SECOND,
                    0
            );

            today.set(
                    Calendar.MILLISECOND,
                    0
            );

            return paymentDueDate.before(
                    today.getTime()
            );

        } catch (ParseException exception) {

            return false;
        }
    }

    private void setupDropdown(
            MaterialAutoCompleteTextView dropdown,
            String[] values,
            String defaultText
    ) {

        android.widget.ArrayAdapter<String> adapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout
                                .simple_dropdown_item_1line,
                        values
                );

        dropdown.setAdapter(adapter);
        dropdown.setText("", false);
        dropdown.setHint(defaultText);
    }

    private void setupAdminAddPaymentCalculations() {

        TextWatcher paymentWatcher =
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
                        calculateAdminPaymentAmounts();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                };

        etAdminAddPaymentActualFee
                .addTextChangedListener(paymentWatcher);

        etAdminAddPaymentDiscount
                .addTextChangedListener(paymentWatcher);

        etAdminAddPaymentReceived
                .addTextChangedListener(paymentWatcher);
    }

    private void calculateAdminPaymentAmounts() {

        int actualFee =
                getAmountFromField(
                        etAdminAddPaymentActualFee
                );

        int discount =
                getAmountFromField(
                        etAdminAddPaymentDiscount
                );

        int received =
                getAmountFromField(
                        etAdminAddPaymentReceived
                );

        if (discount > actualFee) {
            discount = actualFee;
        }

        int finalPayable =
                actualFee - discount;

        if (received > finalPayable) {
            received = finalPayable;
        }

        int due =
                finalPayable - received;

        etAdminAddPaymentFinalPayable.setText(
                String.valueOf(finalPayable)
        );

        etAdminAddPaymentDue.setText(
                String.valueOf(due)
        );
    }

    private int getAmountFromField(
            TextInputEditText field
    ) {

        if (field.getText() == null) {
            return 0;
        }

        String value =
                field.getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private void setupAdminPaymentDatePicker() {

        etAdminAddPaymentDate.setOnClickListener(
                v -> showAdminDatePicker(
                        etAdminAddPaymentDate
                )
        );
    }

    private void setupAdminPaymentDueDatePicker() {

        etAdminAddPaymentDueDate.setOnClickListener(
                v -> showAdminDatePicker(
                        etAdminAddPaymentDueDate
                )
        );
    }

    private void showAdminDatePicker(
            TextInputEditText targetField
    ) {

        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            Calendar selectedDate =
                                    Calendar.getInstance();

                            selectedDate.set(
                                    year,
                                    month,
                                    dayOfMonth
                            );

                            SimpleDateFormat format =
                                    new SimpleDateFormat(
                                            "dd MMM yyyy",
                                            Locale.getDefault()
                                    );

                            targetField.setText(
                                    format.format(
                                            selectedDate.getTime()
                                    )
                            );
                        },
                        calendar.get(
                                Calendar.YEAR
                        ),
                        calendar.get(
                                Calendar.MONTH
                        ),
                        calendar.get(
                                Calendar.DAY_OF_MONTH
                        )
                );

        dialog.show();
    }


    private void setupAdminAddPaymentButtons() {

        btnAdminSavePayment.setOnClickListener(
                v -> saveAdminPayment()
        );

        btnAdminCancelPayment.setOnClickListener(
                v -> finish()
        );
    }

    private void saveAdminPayment() {

        String branch =
                getDropdownValue(
                        actAdminAddPaymentBranch
                );

        String child =
                getDropdownValue(
                        actAdminAddPaymentChild
                );

        String parent =
                getDropdownValue(
                        actAdminAddPaymentParent
                );

        String therapist =
                getDropdownValue(
                        actAdminAddPaymentTherapist
                );

        String therapy =
                getDropdownValue(
                        actAdminAddPaymentTherapy
                );

        String paymentMethod =
                getDropdownValue(
                        actAdminAddPaymentMethod
                );

        String paymentDate =
                getFieldValue(
                        etAdminAddPaymentDate
                );

        String dueDate =
                getFieldValue(
                        etAdminAddPaymentDueDate
                );

        String transactionId =
                getFieldValue(
                        etAdminAddPaymentTransactionId
                );

        int actualFee =
                getAmountFromField(
                        etAdminAddPaymentActualFee
                );

        int discount =
                getAmountFromField(
                        etAdminAddPaymentDiscount
                );

        int received =
                getAmountFromField(
                        etAdminAddPaymentReceived
                );

        if (branch.isEmpty()
                || child.isEmpty()
                || parent.isEmpty()
                || therapist.isEmpty()
                || therapy.isEmpty()
                || paymentMethod.isEmpty()
                || paymentDate.isEmpty()
                || dueDate.isEmpty()
                || actualFee <= 0) {

            Toast.makeText(
                    this,
                    R.string.admin_payment_fill_required,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (discount > actualFee) {

            etAdminAddPaymentDiscount.setError(
                    getString(
                            R.string.admin_payment_discount_invalid
                    )
            );

            return;
        }

        int finalPayable =
                actualFee - discount;

        if (received > finalPayable) {

            etAdminAddPaymentReceived.setError(
                    getString(
                            R.string.admin_payment_received_invalid
                    )
            );

            return;
        }

        int due =
                finalPayable - received;

        String status =
                calculateAdminPaymentStatus(
                        finalPayable,
                        received,
                        dueDate
                );

        String notes =
                getFieldValue(
                        etAdminAddPaymentNotes
                );

        android.content.Intent resultIntent =
                new android.content.Intent();

        resultIntent.putExtra(
                "payment_child",
                child
        );

        resultIntent.putExtra(
                "payment_parent",
                parent
        );

        resultIntent.putExtra(
                "payment_therapist",
                therapist
        );

        resultIntent.putExtra(
                "payment_branch",
                branch
        );

        resultIntent.putExtra(
                "payment_therapy",
                therapy
        );

        resultIntent.putExtra(
                "payment_actual_fee",
                actualFee
        );

        resultIntent.putExtra(
                "payment_discount",
                discount
        );

        resultIntent.putExtra(
                "payment_final_payable",
                finalPayable
        );

        resultIntent.putExtra(
                "payment_received",
                received
        );

        resultIntent.putExtra(
                "payment_due",
                due
        );

        resultIntent.putExtra(
                "payment_date",
                paymentDate
        );

        resultIntent.putExtra(
                "payment_due_date",
                dueDate
        );

        resultIntent.putExtra(
                "payment_method",
                paymentMethod
        );

        resultIntent.putExtra(
                "payment_transaction_id",
                transactionId
        );

        resultIntent.putExtra(
                "payment_status",
                status
        );

        resultIntent.putExtra(
                "payment_notes",
                notes
        );

        setResult(
                RESULT_OK,
                resultIntent
        );

        Toast.makeText(
                this,
                R.string.admin_payment_saved,
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private String getDropdownValue(
            MaterialAutoCompleteTextView dropdown
    ) {

        if (dropdown.getText() == null) {
            return "";
        }

        return dropdown.getText()
                .toString()
                .trim();
    }

    private String getFieldValue(
            TextInputEditText field
    ) {

        if (field.getText() == null) {
            return "";
        }

        return field.getText()
                .toString()
                .trim();
    }
}
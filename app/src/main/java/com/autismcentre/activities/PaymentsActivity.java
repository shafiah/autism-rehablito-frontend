
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentPaymentDetailsResponse;
import com.autismcentre.model.ParentPaymentOptionResponse;
import com.autismcentre.model.ParentVerifyPaymentRequest;
import com.autismcentre.model.ParentVerifyPaymentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.autismcentre.model.ParentPaymentSummaryResponse;

import com.autismcentre.model.ParentCreatePaymentRequest;
import com.autismcentre.model.ParentCreatePaymentResponse;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Build;

import android.widget.LinearLayout;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentsActivity extends BaseParentActivity
        implements PaymentResultWithDataListener {
    private MaterialButton btnViewReceipt;

    private TextView tvPaymentChildName;
    private TextView tvPaymentChildInfo;
    private TextView tvPaymentOptionsMessage;

    private AutoCompleteTextView actPaymentTherapy;

    private View cardPaymentSummary;

    private TextView tvPaymentActualFee;
    private TextView tvPaymentDiscount;
    private TextView tvPaymentFinalPayable;
    private TextInputLayout tilPaymentTherapy;
    private ProgressBar progressPaymentOptions;

    private ApiService apiService;

    private Long selectedChildId;
    private String selectedTherapyType;

    private MaterialButton btnPayNow;

    private ParentCreatePaymentResponse currentPaymentOrder;

    private String pendingPaymentId;
    private String pendingOrderId;
    private String pendingPaymentSignature;

    private LinearLayout layoutPaymentHistory;
    private TextView tvPaymentHistoryMessage;
    private ProgressBar progressPaymentHistory;

    private List<ParentPaymentOptionResponse> paymentOptions =
            new ArrayList<>();

    private final ExecutorService receiptExecutor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_payments);

        initializeViews();

        ApiClient.initialize(getApplicationContext());
        apiService = ApiClient.getApiService();

        //setupReceiptButton();

        btnPayNow.setOnClickListener(v -> createPaymentOrder());

        loadLinkedChild();
        loadPaymentHistory();
    }

    private void initializeViews() {

       // btnViewReceipt = findViewById(R.id.btnViewReceipt);

        tvPaymentChildName =
                findViewById(R.id.tvPaymentChildName);

        tvPaymentChildInfo =
                findViewById(R.id.tvPaymentChildInfo);

        tvPaymentOptionsMessage =
                findViewById(R.id.tvPaymentOptionsMessage);

        actPaymentTherapy =
                findViewById(R.id.actPaymentTherapy);

        tilPaymentTherapy =
                findViewById(R.id.tilPaymentTherapy);

        progressPaymentOptions =
                findViewById(R.id.progressPaymentOptions);

        cardPaymentSummary = findViewById(R.id.cardPaymentSummary);

        tvPaymentActualFee = findViewById(R.id.tvPaymentActualFee);
        tvPaymentDiscount = findViewById(R.id.tvPaymentDiscount);
        tvPaymentFinalPayable = findViewById(R.id.tvPaymentFinalPayable);

        layoutPaymentHistory = findViewById(R.id.layoutPaymentHistory);
        tvPaymentHistoryMessage = findViewById(R.id.tvPaymentHistoryMessage);
        progressPaymentHistory = findViewById(R.id.progressPaymentHistory);

        btnPayNow = findViewById(R.id.btnPayNow);

        btnPayNow.setEnabled(false);
        btnPayNow.setVisibility(View.GONE);

        Checkout.preload(getApplicationContext());

        cardPaymentSummary.setVisibility(View.GONE);

        actPaymentTherapy.setEnabled(false);
    }

    // =========================================
// Create Payment Order
// =========================================

    private void createPaymentOrder() {

        if (selectedChildId == null || selectedTherapyType == null) {
            Toast.makeText(
                    this,
                    "Please select a therapy first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnPayNow.setEnabled(false);
        btnPayNow.setText("Creating Order...");

        ParentCreatePaymentRequest request =
                new ParentCreatePaymentRequest(
                        selectedChildId,
                        selectedTherapyType
                );

        apiService.createParentPaymentOrder(request).enqueue(
                new Callback<ParentCreatePaymentResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentCreatePaymentResponse> call,
                            Response<ParentCreatePaymentResponse> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnPayNow.setEnabled(true);
                        btnPayNow.setText("Pay Now");

                        if (!response.isSuccessful() || response.body() == null) {

                            Toast.makeText(
                                    PaymentsActivity.this,
                                    "Unable to create payment order.",
                                    Toast.LENGTH_LONG
                            ).show();

                            Log.e(
                                    "PAYMENT_API",
                                    "Create order failed: " + response.code()
                            );
                            return;
                        }

                        currentPaymentOrder = response.body();

                        if (currentPaymentOrder.getRazorpayOrderId() == null ||
                                currentPaymentOrder.getRazorpayKeyId() == null ||
                                currentPaymentOrder.getFinalPayable() == null) {

                            Toast.makeText(
                                    PaymentsActivity.this,
                                    "Invalid payment order response.",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        Log.d(
                                "PAYMENT_API",
                                "Order created. Payment ID: " +
                                        currentPaymentOrder.getPaymentId()
                        );

                        openRazorpayCheckout(currentPaymentOrder);
                    }

                    @Override
                    public void onFailure(
                            Call<ParentCreatePaymentResponse> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnPayNow.setEnabled(true);
                        btnPayNow.setText("Pay Now");

                        Toast.makeText(
                                PaymentsActivity.this,
                                "Network error while creating payment order.",
                                Toast.LENGTH_LONG
                        ).show();

                        Log.e(
                                "PAYMENT_API",
                                "Create order network error",
                                t
                        );
                    }
                }
        );
    }

    // =========================================
// Open Razorpay Checkout
// =========================================

    private void openRazorpayCheckout(
            ParentCreatePaymentResponse paymentOrder) {

        try {

            Checkout checkout = new Checkout();

            checkout.setKeyID(paymentOrder.getRazorpayKeyId());

            JSONObject options = new JSONObject();

            options.put("name", "Autism Centre");
            options.put("description", "Therapy Payment");

            // Amount is in paise. Backend response amount is in rupees.
            long amountInPaise = Math.round(
                    paymentOrder.getFinalPayable() * 100
            );

            options.put("amount", String.valueOf(amountInPaise));
            options.put("currency", paymentOrder.getCurrency());

            // Use the order created by backend.
            options.put(
                    "order_id",
                    paymentOrder.getRazorpayOrderId()
            );

            JSONObject prefill = new JSONObject();

            options.put("prefill", prefill);

            btnPayNow.setEnabled(false);
            btnPayNow.setText("Payment Processing...");

            checkout.open(this, options);

        } catch (Exception e) {

            Log.e(
                    "RAZORPAY",
                    "Error opening Razorpay Checkout",
                    e
            );

            Toast.makeText(
                    this,
                    "Unable to open payment gateway.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================
// Razorpay Payment Success
// =========================================
    @Override
    public void onPaymentSuccess(
            String razorpayPaymentId,
            PaymentData paymentData) {

        pendingPaymentId = razorpayPaymentId;
        pendingOrderId = paymentData.getOrderId();
        pendingPaymentSignature = paymentData.getSignature();

        btnPayNow.setEnabled(false);
        btnPayNow.setText("Verifying...");

        verifyPayment();
    }
// =========================================
// Razorpay Payment Error
// =========================================

    @Override
    public void onPaymentError(
            int code,
            String response,
            PaymentData paymentData) {

        btnPayNow.setEnabled(true);
        btnPayNow.setText("Pay Now");

        Log.e(
                "RAZORPAY",
                "Payment error code: " + code +
                        ", response: " + response
        );

        Toast.makeText(
                this,
                "Payment was not completed.",
                Toast.LENGTH_LONG
        ).show();
    }

    private void verifyPayment() {

        if (pendingPaymentId == null ||
                pendingOrderId == null ||
                pendingPaymentSignature == null) {

            btnPayNow.setEnabled(true);
            btnPayNow.setText("Pay Now");

            Toast.makeText(
                    this,
                    "Payment verification details missing",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        ParentVerifyPaymentRequest request =
                new ParentVerifyPaymentRequest(
                        pendingPaymentId,
                        pendingOrderId,
                        pendingPaymentSignature
                );

        ApiClient.getApiService()
                .verifyParentPayment(request)
                .enqueue(new Callback<ParentVerifyPaymentResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentVerifyPaymentResponse> call,
                            Response<ParentVerifyPaymentResponse> response) {

                        btnPayNow.setEnabled(true);
                        btnPayNow.setText("Pay Now");

                        if (response.isSuccessful() && response.body() != null) {

                            ParentVerifyPaymentResponse result = response.body();

                            if ("SUCCESS".equalsIgnoreCase(
                                    String.valueOf(result.getPaymentStatus()))) {

                                Toast.makeText(
                                        PaymentsActivity.this,
                                        "Payment verified successfully!",
                                        Toast.LENGTH_LONG
                                ).show();

                                loadPaymentHistory();

                            } else {
                                Toast.makeText(
                                        PaymentsActivity.this,
                                        "Payment verification is pending",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                        } else {
                            Toast.makeText(
                                    PaymentsActivity.this,
                                    "Payment verification failed. Please retry.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentVerifyPaymentResponse> call,
                            Throwable t) {

                        btnPayNow.setEnabled(true);
                        btnPayNow.setText("Pay Now");

                        Toast.makeText(
                                PaymentsActivity.this,
                                "Network error during payment verification",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
    private void loadPaymentHistory() {

        progressPaymentHistory.setVisibility(View.VISIBLE);
        tvPaymentHistoryMessage.setVisibility(View.GONE);
        layoutPaymentHistory.removeAllViews();

        ApiClient.getApiService()
                .getParentPaymentHistory()
                .enqueue(new Callback<List<ParentPaymentDetailsResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentPaymentDetailsResponse>> call,
                            Response<List<ParentPaymentDetailsResponse>> response) {

                        progressPaymentHistory.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {

                            List<ParentPaymentDetailsResponse> paymentList =
                                    response.body();

                            if (paymentList.isEmpty()) {
                                showPaymentHistoryMessage("No payment history found.");
                                return;
                            }

                            tvPaymentHistoryMessage.setVisibility(View.GONE);

                            for (ParentPaymentDetailsResponse payment : paymentList) {
                                addPaymentHistoryCard(payment);
                            }

                        } else {
                            showPaymentHistoryMessage(
                                    "Unable to load payment history."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentPaymentDetailsResponse>> call,
                            Throwable t) {

                        progressPaymentHistory.setVisibility(View.GONE);

                        showPaymentHistoryMessage(
                                "Network error while loading payment history."
                        );
                    }
                });
    }

    private void addPaymentHistoryCard(
            ParentPaymentDetailsResponse payment) {

        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(18, 16, 18, 16);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(18f);
        background.setStroke(2, Color.LTGRAY);

        cardLayout.setBackground(background);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 14);
        cardLayout.setLayoutParams(cardParams);

        String therapy = payment.getTherapyType();

        if ("SPEECH".equalsIgnoreCase(therapy)) {
            therapy = "Speech Therapy";
        } else if ("OCCUPATIONAL".equalsIgnoreCase(therapy)) {
            therapy = "Occupational Therapy";
        } else if ("SPEECH_OCCUPATIONAL".equalsIgnoreCase(therapy)) {
            therapy = "Speech + Occupational Therapy";
        }

        addHistoryText(
                cardLayout,
                therapy,
                18,
                Color.BLACK,
                true
        );

        addHistoryText(
                cardLayout,
                "Amount: ₹" + payment.getAmount(),
                16,
                Color.DKGRAY,
                true
        );

        String status = payment.getStatus();

        TextView statusView = addHistoryText(
                cardLayout,
                "Status: " + status,
                14,
                "SUCCESS".equalsIgnoreCase(status)
                        ? Color.rgb(46, 125, 50)
                        : Color.rgb(230, 126, 34),
                true
        );

        addHistoryText(
                cardLayout,
                "Payment Date: " +
                        (payment.getPaymentDate() != null
                                ? payment.getPaymentDate()
                                : "Not completed"),
                13,
                Color.DKGRAY,
                false
        );

        addHistoryText(
                cardLayout,
                "Receipt Reference: " +
                        (payment.getReceiptReference() != null
                                ? payment.getReceiptReference()
                                : "Not available"),
                13,
                Color.DKGRAY,
                false
        );

        // Show receipt button only for successful payments
        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {

            com.google.android.material.button.MaterialButton
                    btnReceipt = new com.google.android.material.button.MaterialButton(this);

            btnReceipt.setText("View Receipt");
            btnReceipt.setTextSize(14);
            btnReceipt.setCornerRadius(12);
            btnReceipt.setBackgroundTintList(
                    ColorStateList.valueOf(getColor(R.color.parent_navy))
            );
            btnReceipt.setTextColor(Color.WHITE);

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            buttonParams.setMargins(0, 12, 0, 0);

            btnReceipt.setLayoutParams(buttonParams);

            btnReceipt.setOnClickListener(v -> {
                downloadPaymentReceipt(payment.getPaymentId());
            });

            cardLayout.addView(btnReceipt);
        }

        layoutPaymentHistory.addView(cardLayout);
    }




    private void downloadPaymentReceipt(Long paymentId) {

        if (paymentId == null) {
            Toast.makeText(this, "Payment ID not available",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Downloading receipt...",
                Toast.LENGTH_SHORT).show();

        receiptExecutor.execute(() -> {

            Uri fileUri = null;

            try {
                Response<ResponseBody> response =
                        apiService.getParentPaymentReceipt(paymentId).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    int code = response.code();

                    runOnUiThread(() -> Toast.makeText(
                            PaymentsActivity.this,
                            "Receipt download failed: " + code,
                            Toast.LENGTH_LONG
                    ).show());

                    return;
                }

                try (ResponseBody responseBody = response.body();
                     InputStream inputStream = responseBody.byteStream()) {

                    String fileName =
                            "payment-receipt-" + paymentId + ".pdf";

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_DOWNLOADS);

                        fileUri = getContentResolver().insert(
                                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                values
                        );

                        if (fileUri == null) {
                            throw new IOException("Unable to create PDF file");
                        }

                        try (OutputStream outputStream =
                                     getContentResolver().openOutputStream(fileUri)) {

                            if (outputStream == null) {
                                throw new IOException("Unable to open PDF file");
                            }

                            byte[] buffer = new byte[8192];
                            int bytesRead;

                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }

                            outputStream.flush();
                        }

                    } else {
                        throw new IOException(
                                "Public Downloads implementation for Android below 10 is not configured yet."
                        );
                    }
                }

                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(
                                PaymentsActivity.this,
                                "Receipt saved in Downloads",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {

                if (fileUri != null) {
                    getContentResolver().delete(fileUri, null, null);
                }

                Log.e("PAYMENT_RECEIPT", "Receipt save failed", e);

                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(
                                PaymentsActivity.this,
                                "Unable to save receipt",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });
    }


    private TextView addHistoryText(
            LinearLayout parent,
            String text,
            int textSize,
            int textColor,
            boolean bold) {

        TextView textView = new TextView(this);

        textView.setText(text);
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);

        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 9);
        textView.setLayoutParams(params);

        parent.addView(textView);

        return textView;
    }

    private void showPaymentHistoryMessage(String message) {

        layoutPaymentHistory.removeAllViews();

        tvPaymentHistoryMessage.setText(message);
        tvPaymentHistoryMessage.setVisibility(View.VISIBLE);
    }
   /* private void setupReceiptButton() {

        btnViewReceipt.setOnClickListener(v -> {

            Intent intent = new Intent(
                    PaymentsActivity.this,
                    ReceiptActivity.class
            );

            startActivity(intent);
        });
    } */

    // =========================================
    // Load Linked Child
    // =========================================

    private void loadLinkedChild() {

        tvPaymentChildName.setText("Loading child details...");
        tvPaymentChildInfo.setText("Please wait");

        apiService.getMyChildren().enqueue(
                new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (!response.isSuccessful() ||
                                response.body() == null) {

                            showChildError(
                                    "Unable to load child details."
                            );
                            return;
                        }

                        List<ParentChildResponse> children =
                                response.body();

                        if (children.isEmpty()) {

                            showChildError(
                                    "No child is linked to your account."
                            );
                            return;
                        }

                        // Current application flow supports one child.
                        // Select the active linked child automatically.

                        ParentChildResponse activeChild = null;

                        for (ParentChildResponse child : children) {

                            if (child != null &&
                                    Boolean.TRUE.equals(child.getActive())) {

                                activeChild = child;
                                break;
                            }
                        }

                        if (activeChild == null) {

                            showChildError(
                                    "No active child found. Please contact the centre."
                            );
                            return;
                        }

                        selectedChildId = activeChild.getChildId();

                        if (selectedChildId == null) {

                            showChildError(
                                    "Child ID is missing."
                            );
                            return;
                        }

                        tvPaymentChildName.setText(
                                activeChild.getChildName()
                        );

                        String childInfo =
                                "Branch: " +
                                        (activeChild.getBranchName() != null
                                                ? activeChild.getBranchName()
                                                : "Not available");

                        tvPaymentChildInfo.setText(childInfo);

                        //loadPaymentOptions(selectedChildId);
                        setupTherapyDropdown();
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        showChildError(
                                "Network error while loading child details."
                        );
                    }
                }
        );
    }

    private void showChildError(String message) {

        tvPaymentChildName.setText("Child details unavailable");
        tvPaymentChildInfo.setText(message);

        actPaymentTherapy.setEnabled(false);

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    // =========================================
    // Load Therapy Payment Options
    // =========================================


    // =========================================
    // Setup Dynamic Therapy Dropdown
    // =========================================

    // =========================================
// Hardcoded Therapy Dropdown
// =========================================

    private void setupTherapyDropdown() {

        List<String> therapyLabels = new ArrayList<>();

        therapyLabels.add("Speech Therapy");
        therapyLabels.add("Occupational Therapy");
        therapyLabels.add("Speech + Occupational Therapy");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        therapyLabels
                );

        actPaymentTherapy.setAdapter(adapter);

        actPaymentTherapy.setEnabled(true);

        actPaymentTherapy.setOnItemClickListener(
                (parent, view, position, id) -> {

                    switch (position) {

                        case 0:
                            selectedTherapyType = "SPEECH";
                            break;

                        case 1:
                            selectedTherapyType = "OCCUPATIONAL";
                            break;

                        case 2:
                            selectedTherapyType = "SPEECH_OCCUPATIONAL";
                            break;

                        default:
                            selectedTherapyType = null;
                            break;
                    }

                    if (selectedTherapyType != null) {
                        loadPaymentSummary();
                    }

                    Log.d(
                            "PAYMENT_API",
                            "Selected Therapy: " + selectedTherapyType
                    );
                }
        );

        tvPaymentOptionsMessage.setVisibility(View.GONE);
    }

    // =========================================
// Load Payment Summary
// =========================================

    private void loadPaymentSummary() {

        btnPayNow.setEnabled(false);
        btnPayNow.setVisibility(View.GONE);
        currentPaymentOrder = null;

        if (selectedChildId == null || selectedTherapyType == null) {
            return;
        }

        cardPaymentSummary.setVisibility(View.GONE);
        progressPaymentOptions.setVisibility(View.VISIBLE);

        apiService.getParentPaymentSummary(
                selectedChildId,
                selectedTherapyType
        ).enqueue(new Callback<ParentPaymentSummaryResponse>() {

            @Override
            public void onResponse(
                    Call<ParentPaymentSummaryResponse> call,
                    Response<ParentPaymentSummaryResponse> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                progressPaymentOptions.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {

                    Toast.makeText(
                            PaymentsActivity.this,
                            "Unable to load payment summary.",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                ParentPaymentSummaryResponse summary = response.body();

                tvPaymentActualFee.setText(
                        "₹" + summary.getActualFee()
                );

                tvPaymentDiscount.setText(
                        "₹" + summary.getDiscount()
                );

                tvPaymentFinalPayable.setText(
                        "₹" + summary.getFinalPayable()
                );

                cardPaymentSummary.setVisibility(View.VISIBLE);

                Log.d(
                        "PAYMENT_API",
                        "Payment summary loaded: " + summary.getFinalPayable()
                );

                cardPaymentSummary.setVisibility(View.VISIBLE);

                btnPayNow.setVisibility(View.VISIBLE);
                btnPayNow.setEnabled(true);
            }



            @Override
            public void onFailure(
                    Call<ParentPaymentSummaryResponse> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                progressPaymentOptions.setVisibility(View.GONE);

                Toast.makeText(
                        PaymentsActivity.this,
                        "Network error while loading payment summary.",
                        Toast.LENGTH_LONG
                ).show();

                Log.e(
                        "PAYMENT_API",
                        "Summary API error",
                        t
                );
            }
        });
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_payments;
    }
}


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
import com.autismcentre.model.AdminNotificationCampaignResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.autismcentre.model.NotificationResponse;
import java.util.Collections;
import java.util.Comparator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminNotificationManagementActivity
        extends BaseAdminActivity {

    private TextInputEditText etAdminSearchNotification;

    private MaterialAutoCompleteTextView actAdminNotificationBranch;
    private MaterialAutoCompleteTextView actAdminNotificationType;
    private MaterialAutoCompleteTextView actAdminNotificationStatus;

    private LinearLayout adminNotificationListContainer;

    private TextView tvAdminNoNotification;

    private MaterialButton btnAdminCreateNotification;

    private ApiService apiService;

    private final List<AdminNotificationItem>
            allAdminNotifications = new ArrayList<>();

    private final List<AdminBranchResponse>
            allAdminBranches = new ArrayList<>();

    private final Map<Long, String>
            branchNameMap = new HashMap<>();

    private final List<String>
            branchNames = new ArrayList<>();

    private Long selectedBranchId = null;

    //private String selectedAdminNotificationBranch ="All Branches";

    private String selectedAdminNotificationType =
            "All Types";

    private String selectedAdminNotificationStatus =
            "All Status";

    private Call<List<AdminNotificationCampaignResponse>>
            campaignCall;
    private final List<NotificationResponse>
            allAdminReceivedNotifications = new ArrayList<>();

    private Call<List<NotificationResponse>>
            receivedNotificationsCall;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_notification_management
        );

        ApiClient.initialize(this);

        apiService = ApiClient.getApiService();

        initializeAdminNotificationViews();

        setupAdminNotificationBranchFilter();

        setupAdminNotificationTypeFilter();

        setupAdminNotificationStatusFilter();

        setupAdminNotificationSearch();

        setupAdminCreateNotificationButton();

        loadAdminBranches();
    }

    private void initializeAdminNotificationViews() {

        etAdminSearchNotification =
                findViewById(
                        R.id.etAdminSearchNotification
                );

        actAdminNotificationBranch =
                findViewById(
                        R.id.actAdminNotificationBranch
                );

        actAdminNotificationType =
                findViewById(
                        R.id.actAdminNotificationType
                );

        actAdminNotificationStatus =
                findViewById(
                        R.id.actAdminNotificationStatus
                );

        adminNotificationListContainer =
                findViewById(
                        R.id.adminNotificationListContainer
                );

        tvAdminNoNotification =
                findViewById(
                        R.id.tvAdminNoNotification
                );

        btnAdminCreateNotification =
                findViewById(
                        R.id.btnAdminCreateNotification
                );
    }

    // ==========================================
    // Create Notification Navigation
    // ==========================================

    private void setupAdminCreateNotificationButton() {

        btnAdminCreateNotification.setOnClickListener(
                v -> {

                    Intent intent = new Intent(
                            AdminNotificationManagementActivity.this,
                            AdminCreateNotificationActivity.class
                    );

                    startActivityForResult(
                            intent,
                            8001
                    );
                }
        );
    }

    private void loadAdminReceivedNotifications() {

        if (receivedNotificationsCall != null) {
            receivedNotificationsCall.cancel();
        }

        receivedNotificationsCall =
                apiService.getAdminNotifications();

        receivedNotificationsCall.enqueue(
                new Callback<List<NotificationResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<NotificationResponse>> call,
                            Response<List<NotificationResponse>> response
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    AdminNotificationManagementActivity.this,
                                    "Unable to load received notifications. Code: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            allAdminReceivedNotifications.clear();
                            displayAdminNotifications();
                            return;
                        }

                        allAdminReceivedNotifications.clear();

                        allAdminReceivedNotifications.addAll(
                                response.body()
                        );

                        displayAdminNotifications();
                    }

                    @Override
                    public void onFailure(
                            Call<List<NotificationResponse>> call,
                            Throwable t
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        Toast.makeText(
                                AdminNotificationManagementActivity.this,
                                "Received notifications loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }



    // ==========================================
    // Load Branches From Backend
    // ==========================================

    private void loadAdminBranches() {

        apiService.getAdminBranches().enqueue(
                new Callback<List<AdminBranchResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    AdminNotificationManagementActivity.this,
                                    "Unable to load branches.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadAdminNotificationCampaigns();
                            loadAdminReceivedNotifications();

                            return;
                        }

                        allAdminBranches.clear();
                        branchNameMap.clear();
                        branchNames.clear();

                        branchNames.add("All Branches");

                        allAdminBranches.addAll(
                                response.body()
                        );

                        for (AdminBranchResponse branch
                                : allAdminBranches) {

                            if (branch.getId() == null) {
                                continue;
                            }

                            String branchName =
                                    branch.getBranchName();

                            if (branchName == null
                                    || branchName.trim().isEmpty()) {
                                continue;
                            }

                            branchNameMap.put(
                                    branch.getId(),
                                    branchName
                            );

                            branchNames.add(branchName);
                        }

                        setupAdminBranchAdapter();

                        loadAdminNotificationCampaigns();
                        loadAdminReceivedNotifications();
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                AdminNotificationManagementActivity.this,
                                "Branch loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        loadAdminNotificationCampaigns();
                        loadAdminReceivedNotifications();
                    }
                }
        );
    }

    private void setupAdminBranchAdapter() {

        ArrayAdapter<String> branchAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        branchNames
                );

        actAdminNotificationBranch.setAdapter(
                branchAdapter
        );

        actAdminNotificationBranch.setText(
                "All Branches",
                false
        );
    }

    // ==========================================
    // Notification Campaign History API
    // ==========================================

    private void loadAdminNotificationCampaigns() {

        if (campaignCall != null) {
            campaignCall.cancel();
        }

        String readStatus = "ALL";

        if ("Read".equalsIgnoreCase(
                selectedAdminNotificationStatus
        )) {

            readStatus = "READ";

        } else if ("Unread".equalsIgnoreCase(
                selectedAdminNotificationStatus
        )) {

            readStatus = "UNREAD";
        }

        campaignCall =
                apiService.getAdminNotificationCampaigns(
                        selectedBranchId,
                        readStatus
                );

        campaignCall.enqueue(
                new Callback<List<AdminNotificationCampaignResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<AdminNotificationCampaignResponse>> call,
                            Response<List<AdminNotificationCampaignResponse>> response
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    AdminNotificationManagementActivity.this,
                                    "Unable to load notification history. Code: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            allAdminNotifications.clear();

                            displayAdminNotifications();

                            return;
                        }

                        allAdminNotifications.clear();

                        for (AdminNotificationCampaignResponse campaign
                                : response.body()) {

                            allAdminNotifications.add(
                                    convertCampaignToItem(campaign)
                            );
                        }

                        displayAdminNotifications();
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminNotificationCampaignResponse>> call,
                            Throwable t
                    ) {

                        if (call.isCanceled()) {
                            return;
                        }

                        Toast.makeText(
                                AdminNotificationManagementActivity.this,
                                "Notification loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        allAdminNotifications.clear();

                        displayAdminNotifications();
                    }
                }
        );
    }

    private AdminNotificationItem convertCampaignToItem(
            AdminNotificationCampaignResponse campaign
    ) {

        Long branchId = campaign.getBranchId();

        String branchName;

        if (branchId == null) {

            branchName = "All Branches";

        } else {

            branchName = branchNameMap.get(branchId);

            if (branchName == null) {
                branchName = "Branch #" + branchId;
            }
        }

        String type = convertNotificationType(
                campaign.getType()
        );

        String recipient = convertRecipient(
                campaign.getRecipient()
        );

        String date = formatCampaignDate(
                campaign.getCreatedAt()
        );

        if (date.isEmpty()
                && campaign.getScheduledAt() != null) {

            date = formatCampaignDate(
                    campaign.getScheduledAt()
            );
        }

        return new AdminNotificationItem(
                campaign.getCampaignId(),
                branchId,
                campaign.getTitle(),
                campaign.getMessage(),
                type,
                recipient,
                branchName,
                date,
                convertCampaignStatus(campaign.getStatus()),
                campaign.getTotalRecipients(),
                campaign.getReadCount(),
                campaign.getUnreadCount()
        );
    }

    // ==========================================
    // Backend Enum Conversion
    // ==========================================

    private String convertNotificationType(String type) {

        if (type == null) {
            return "General";
        }

        switch (type.toUpperCase(Locale.ROOT)) {

            case "SESSION":
                return "Session";

            case "PAYMENT":
                return "Payment";

            case "HOMEWORK":
                return "Homework";

            case "REPORT":
                return "Report";

            case "SYSTEM":
                return "General";

            case "COMMUNICATION":
                return "Communication";

            case "FEEDBACK":
                return "Feedback";

            default:
                return type;
        }
    }

    private String convertRecipient(String recipient) {

        if (recipient == null) {
            return "Unknown";
        }

        switch (recipient.toUpperCase(Locale.ROOT)) {

            case "PARENT":
                return "Parent";

            case "THERAPIST":
                return "Therapist";

            case "BOTH":
                return "Both";

            default:
                return recipient;
        }
    }

    private String convertCampaignStatus(String status) {

        if (status == null) {
            return "Unknown";
        }

        switch (status.toUpperCase(Locale.ROOT)) {

            case "SCHEDULED":
                return "Scheduled";

            case "SENDING":
                return "Sending";

            case "SENT":
                return "Sent";

            case "FAILED":
                return "Failed";

            case "CANCELLED":
                return "Cancelled";

            default:
                return status;
        }
    }

    private String formatCampaignDate(String dateValue) {

        if (dateValue == null || dateValue.trim().isEmpty()) {
            return "";
        }

        try {

            String normalizedDate = dateValue.trim();

            if (normalizedDate.contains(".")) {
                int dotIndex = normalizedDate.indexOf(".");
                int timezoneIndex = normalizedDate.indexOf("+", dotIndex);

                if (timezoneIndex == -1) {
                    timezoneIndex = normalizedDate.indexOf("Z", dotIndex);
                }

                if (timezoneIndex == -1) {
                    normalizedDate = normalizedDate.substring(0, dotIndex);
                } else {
                    normalizedDate =
                            normalizedDate.substring(0, dotIndex)
                                    + normalizedDate.substring(timezoneIndex);
                }
            }

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.ENGLISH
                    );

            // Backend timestamp is interpreted as UTC
            inputFormat.setTimeZone(
                    TimeZone.getTimeZone("UTC")
            );

            Date parsedDate = inputFormat.parse(normalizedDate);

            if (parsedDate == null) {
                return dateValue;
            }

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.ENGLISH
                    );

            // Display in Indian Standard Time
            outputFormat.setTimeZone(
                    TimeZone.getTimeZone("Asia/Kolkata")
            );

            return outputFormat.format(parsedDate);

        } catch (Exception e) {

            return dateValue.replace("T", " ");
        }
    }
    // ==========================================
    // Branch Filter
    // ==========================================

    private void setupAdminNotificationBranchFilter() {

        actAdminNotificationBranch.setOnItemClickListener(
                (parent, view, position, id) -> {

                   // selectedAdminNotificationBranch = branchNames.get(position);

                    if (position == 0) {

                        selectedBranchId = null;

                    } else {

                        AdminBranchResponse selectedBranch =
                                allAdminBranches.get(position - 1);

                        selectedBranchId =
                                selectedBranch.getId();
                    }

                    loadAdminNotificationCampaigns();
                }
        );
    }

    // ==========================================
    // Notification Type Filter
    // ==========================================

    private void setupAdminNotificationTypeFilter() {

        String[] types = {
                "All Types",
                "General",
                "Session",
                "Payment",
                "Homework",
                "Report",
                "Announcement"
        };

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        types
                );

        actAdminNotificationType.setAdapter(
                typeAdapter
        );

        actAdminNotificationType.setText(
                "All Types",
                false
        );

        actAdminNotificationType.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedAdminNotificationType =
                            types[position];

                    displayAdminNotifications();
                }
        );
    }

    // ==========================================
    // Campaign Status Filter
    // ==========================================

    private void setupAdminNotificationStatusFilter() {

        String[] statuses = {
                "All Status",
                "Scheduled",
                "Sending",
                "Sent",
                "Failed",
                "Cancelled",
                "Read",
                "Unread"
        };

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        statuses
                );

        actAdminNotificationStatus.setAdapter(
                statusAdapter
        );

        actAdminNotificationStatus.setText(
                "All Status",
                false
        );

        actAdminNotificationStatus.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedAdminNotificationStatus =
                            statuses[position];

                    loadAdminNotificationCampaigns();
                }
        );
    }

    // ==========================================
    // Search
    // ==========================================

    private void setupAdminNotificationSearch() {

        etAdminSearchNotification.addTextChangedListener(
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

                        displayAdminNotifications();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    // ==========================================
    // Display Campaigns
    // ==========================================

    private void displayAdminNotifications() {

        adminNotificationListContainer.removeAllViews();

        String searchText = "";

        if (etAdminSearchNotification.getText() != null) {

            searchText =
                    etAdminSearchNotification
                            .getText()
                            .toString()
                            .trim()
                            .toLowerCase(Locale.ROOT);
        }

        List<AdminNotificationEntry> combinedList =
                new ArrayList<>();

        // ==========================================
        // Add Campaign Notifications
        // ==========================================

        for (AdminNotificationItem campaign
                : allAdminNotifications) {

            Date date = parseNotificationDate(
                    campaign.date
            );

            combinedList.add(
                    new AdminNotificationEntry(
                            campaign,
                            null,
                            date
                    )
            );
        }

        // ==========================================
        // Add Received Notifications
        // ==========================================

        for (NotificationResponse received
                : allAdminReceivedNotifications) {

            Date date = parseNotificationDate(
                    received.getCreatedAt()
            );

            combinedList.add(
                    new AdminNotificationEntry(
                            null,
                            received,
                            date
                    )
            );
        }

        // ==========================================
        // Sort Newest First
        // ==========================================

        Collections.sort(
                combinedList,
                new Comparator<AdminNotificationEntry>() {

                    @Override
                    public int compare(
                            AdminNotificationEntry first,
                            AdminNotificationEntry second
                    ) {

                        if (first.createdAt == null
                                && second.createdAt == null) {
                            return 0;
                        }

                        if (first.createdAt == null) {
                            return 1;
                        }

                        if (second.createdAt == null) {
                            return -1;
                        }

                        return second.createdAt.compareTo(
                                first.createdAt
                        );
                    }
                }
        );

        int visibleNotificationCount = 0;

        // ==========================================
        // Display Combined Sorted Notifications
        // ==========================================

        for (AdminNotificationEntry entry : combinedList) {

            // --------------------------------------
            // Campaign Notification
            // --------------------------------------

            if (entry.campaign != null) {

                AdminNotificationItem notification =
                        entry.campaign;

                boolean matchesSearch =
                        searchText.isEmpty()
                                || containsText(
                                notification.title, searchText)
                                || containsText(
                                notification.message, searchText)
                                || containsText(
                                notification.recipient, searchText)
                                || containsText(
                                notification.branch, searchText);

                boolean matchesBranch =
                        selectedBranchId == null
                                || notification.branchId == null
                                || selectedBranchId.equals(
                                notification.branchId);

                boolean matchesType =
                        "All Types".equals(
                                selectedAdminNotificationType)
                                || notification.type.equalsIgnoreCase(
                                selectedAdminNotificationType)
                                || ("Announcement".equalsIgnoreCase(
                                selectedAdminNotificationType)
                                && "General".equalsIgnoreCase(
                                notification.type));

                boolean matchesStatus =
                        matchesSelectedStatus(notification);

                if (matchesSearch
                        && matchesBranch
                        && matchesType
                        && matchesStatus) {

                    addAdminNotificationCard(notification);

                    visibleNotificationCount++;
                }
            }

            // --------------------------------------
            // Received Notification
            // --------------------------------------

            else if (entry.received != null) {

                NotificationResponse notification =
                        entry.received;

                String type = convertNotificationType(
                        notification.getType()
                );

                String branch = branchNameMap.get(
                        notification.getBranchId()
                );

                if (branch == null) {
                    branch = notification.getBranchId() == null
                            ? "Unknown"
                            : "Branch #" + notification.getBranchId();
                }

                boolean isRead = Boolean.TRUE.equals(
                        notification.getIsRead()
                );

                boolean matchesSearch =
                        searchText.isEmpty()
                                || containsText(
                                notification.getTitle(), searchText)
                                || containsText(
                                notification.getMessage(), searchText)
                                || containsText("Admin", searchText)
                                || containsText(branch, searchText)
                                || containsText(type, searchText);

                boolean matchesBranch =
                        selectedBranchId == null
                                || selectedBranchId.equals(
                                notification.getBranchId());

                boolean matchesType =
                        "All Types".equals(
                                selectedAdminNotificationType)
                                || type.equalsIgnoreCase(
                                selectedAdminNotificationType)
                                || ("Announcement".equalsIgnoreCase(
                                selectedAdminNotificationType)
                                && "General".equalsIgnoreCase(type));

                boolean matchesStatus =
                        "All Status".equalsIgnoreCase(
                                selectedAdminNotificationStatus)
                                || ("Read".equalsIgnoreCase(
                                selectedAdminNotificationStatus)
                                && isRead)
                                || ("Unread".equalsIgnoreCase(
                                selectedAdminNotificationStatus)
                                && !isRead);

                if (matchesSearch
                        && matchesBranch
                        && matchesType
                        && matchesStatus) {

                    addAdminReceivedNotificationCard(notification);

                    visibleNotificationCount++;
                }
            }
        }

        tvAdminNoNotification.setVisibility(
                visibleNotificationCount == 0
                        ? View.VISIBLE
                        : View.GONE
        );
    }
    private boolean containsText(
            String value,
            String searchText
    ) {

        return value != null
                && value.toLowerCase(Locale.ROOT)
                .contains(searchText);
    }

    private boolean matchesSelectedStatus(
            AdminNotificationItem notification
    ) {

        if ("All Status".equalsIgnoreCase(
                selectedAdminNotificationStatus
        )) {
            return true;
        }

        if ("Read".equalsIgnoreCase(
                selectedAdminNotificationStatus
        )) {

            return notification.unreadCount != null
                    && notification.unreadCount == 0;
        }

        if ("Unread".equalsIgnoreCase(
                selectedAdminNotificationStatus
        )) {

            return notification.unreadCount != null
                    && notification.unreadCount > 0;
        }

        return notification.status.equalsIgnoreCase(
                selectedAdminNotificationStatus
        );
    }

    private void addAdminReceivedNotificationCard(
            NotificationResponse notification
    ) {

        View notificationView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_admin_notification,
                        adminNotificationListContainer,
                        false
                );

        TextView tvTitle =
                notificationView.findViewById(
                        R.id.tvAdminNotificationTitle
                );

        TextView tvMessage =
                notificationView.findViewById(
                        R.id.tvAdminNotificationMessage
                );

        TextView tvType =
                notificationView.findViewById(
                        R.id.tvAdminNotificationType
                );

        TextView tvRecipient =
                notificationView.findViewById(
                        R.id.tvAdminNotificationRecipient
                );

        TextView tvBranch =
                notificationView.findViewById(
                        R.id.tvAdminNotificationBranch
                );

        TextView tvDate =
                notificationView.findViewById(
                        R.id.tvAdminNotificationDate
                );

        TextView tvStatus =
                notificationView.findViewById(
                        R.id.tvAdminNotificationStatus
                );

        boolean isRead = Boolean.TRUE.equals(
                notification.getIsRead()
        );

        String branch = branchNameMap.get(
                notification.getBranchId()
        );

        if (branch == null) {
            branch = notification.getBranchId() == null
                    ? "Unknown"
                    : "Branch #" + notification.getBranchId();
        }

        tvTitle.setText(notification.getTitle());
        tvMessage.setText(notification.getMessage());

        tvType.setText(
                "Type: " + convertNotificationType(
                        notification.getType()
                )
        );

        tvRecipient.setText("Received by: Admin");
        tvBranch.setText("Branch: " + branch);

        tvDate.setText(
                "Date: " + formatCampaignDate(
                        notification.getCreatedAt()
                )
        );

        tvStatus.setText(
                isRead ? "Status: Read" : "Status: Unread"
        );

        tvStatus.setTypeface(
                null,
                isRead
                        ? android.graphics.Typeface.NORMAL
                        : android.graphics.Typeface.BOLD
        );

        notificationView.setOnClickListener(
                v -> openReceivedNotificationDetails(notification)
        );

        adminNotificationListContainer.addView(notificationView);
    }

    private void openReceivedNotificationDetails(
            NotificationResponse notification
    ) {

        Intent intent = new Intent(
                AdminNotificationManagementActivity.this,
                AdminNotificationDetailsActivity.class
        );

        intent.putExtra("is_received_notification", true);

        intent.putExtra(
                "notification_id",
                notification.getId()
        );

        intent.putExtra(
                "notification_title",
                notification.getTitle()
        );

        startActivityForResult(intent, 9001);
    }

    // ==========================================
    // Notification Card
    // ==========================================

    private void addAdminNotificationCard(
            AdminNotificationItem notification
    ) {

        View notificationView =
                LayoutInflater.from(this).inflate(
                        R.layout.item_admin_notification,
                        adminNotificationListContainer,
                        false
                );

        TextView tvTitle =
                notificationView.findViewById(
                        R.id.tvAdminNotificationTitle
                );

        TextView tvMessage =
                notificationView.findViewById(
                        R.id.tvAdminNotificationMessage
                );

        TextView tvType =
                notificationView.findViewById(
                        R.id.tvAdminNotificationType
                );

        TextView tvRecipient =
                notificationView.findViewById(
                        R.id.tvAdminNotificationRecipient
                );

        TextView tvBranch =
                notificationView.findViewById(
                        R.id.tvAdminNotificationBranch
                );

        TextView tvDate =
                notificationView.findViewById(
                        R.id.tvAdminNotificationDate
                );

        TextView tvStatus =
                notificationView.findViewById(
                        R.id.tvAdminNotificationStatus
                );

        tvTitle.setText(notification.title);

        tvMessage.setText(notification.message);

        tvType.setText(
                "Type: " + notification.type
        );

        tvRecipient.setText(
                "To: " + notification.recipient
        );

        tvBranch.setText(
                "Branch: " + notification.branch
        );

        tvDate.setText(
                "Date: " + notification.date
        );

        int total = notification.totalRecipients != null
                ? notification.totalRecipients : 0;

        int read = notification.readCount != null
                ? notification.readCount : 0;

        int unread = notification.unreadCount != null
                ? notification.unreadCount : 0;

        tvStatus.setText(
                "Status: " + notification.status
                        + " | Read: " + read
                        + " | Unread: " + unread
                        + " | Total: " + total
        );

        tvStatus.setTypeface(
                null,
                unread > 0
                        ? android.graphics.Typeface.BOLD
                        : android.graphics.Typeface.NORMAL
        );

        notificationView.setOnClickListener(
                v -> openCampaignDetails(notification)
        );

        adminNotificationListContainer.addView(
                notificationView
        );
    }

    private void openCampaignDetails(
            AdminNotificationItem notification
    ) {

        Intent intent = new Intent(
                AdminNotificationManagementActivity.this,
                AdminNotificationDetailsActivity.class
        );

        intent.putExtra(
                "notification_id",
                String.valueOf(notification.id)
        );

        intent.putExtra(
                "campaign_id",
                notification.id
        );

        intent.putExtra(
                "notification_title",
                notification.title
        );

        intent.putExtra(
                "notification_message",
                notification.message
        );

        intent.putExtra(
                "notification_type",
                notification.type
        );

        intent.putExtra(
                "notification_recipient",
                notification.recipient
        );

        intent.putExtra(
                "notification_branch",
                notification.branch
        );

        intent.putExtra(
                "notification_date",
                notification.date
        );

        intent.putExtra(
                "notification_status",
                notification.status
        );

        intent.putExtra(
                "notification_total_recipients",
                notification.totalRecipients != null
                        ? notification.totalRecipients : 0
        );

        intent.putExtra(
                "notification_read_count",
                notification.readCount != null
                        ? notification.readCount : 0
        );

        intent.putExtra(
                "notification_unread_count",
                notification.unreadCount != null
                        ? notification.unreadCount : 0
        );

        startActivityForResult(
                intent,
                9001
        );
    }

    // ==========================================
    // Refresh After Returning From Other Screen
    // ==========================================

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

        if (requestCode == 8001
                && resultCode == RESULT_OK) {

            loadAdminNotificationCampaigns();
        }

        if (requestCode == 9001
                && resultCode == RESULT_OK) {

            loadAdminNotificationCampaigns();
            loadAdminReceivedNotifications();
        }
    }

    // ==========================================
    // Local Campaign Model
    // ==========================================

    private static class AdminNotificationEntry {

        AdminNotificationItem campaign;
        NotificationResponse received;
        Date createdAt;

        AdminNotificationEntry(
                AdminNotificationItem campaign,
                NotificationResponse received,
                Date createdAt
        ) {
            this.campaign = campaign;
            this.received = received;
            this.createdAt = createdAt;
        }
    }

    private Date parseNotificationDate(String dateValue) {

        if (dateValue == null || dateValue.trim().isEmpty()) {
            return null;
        }

        try {

            String normalized = dateValue.trim();

            // Remove fractional seconds for consistent parsing
            int dotIndex = normalized.indexOf(".");

            if (dotIndex != -1) {

                int timezoneIndex = normalized.indexOf(
                        "+", dotIndex
                );

                if (timezoneIndex == -1) {
                    timezoneIndex = normalized.indexOf(
                            "Z", dotIndex
                    );
                }

                if (timezoneIndex == -1) {
                    normalized = normalized.substring(0, dotIndex);
                } else {
                    normalized =
                            normalized.substring(0, dotIndex)
                                    + normalized.substring(timezoneIndex);
                }
            }

            SimpleDateFormat formatter =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.ENGLISH
                    );

            formatter.setLenient(false);

            // Existing backend LocalDateTime values are treated
            // consistently as local application timestamps.
            return formatter.parse(normalized);

        } catch (Exception e) {

            return null;
        }
    }

    private static class AdminNotificationItem {

        Long id;

        Long branchId;

        String title;

        String message;

        String type;

        String recipient;

        String branch;

        String date;

        String status;

        Integer totalRecipients;

        Integer readCount;

        Integer unreadCount;

        AdminNotificationItem(
                Long id,
                Long branchId,
                String title,
                String message,
                String type,
                String recipient,
                String branch,
                String date,
                String status,
                Integer totalRecipients,
                Integer readCount,
                Integer unreadCount
        ) {

            this.id = id;
            this.branchId = branchId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.recipient = recipient;
            this.branch = branch;
            this.date = date;
            this.status = status;
            this.totalRecipients = totalRecipients;
            this.readCount = readCount;
            this.unreadCount = unreadCount;
        }
    }

    @Override
    protected void onDestroy() {

        if (campaignCall != null) {
            campaignCall.cancel();
        }

        if (receivedNotificationsCall != null) {
            receivedNotificationsCall.cancel();
        }

        super.onDestroy();
    }
}


package com.autismcentre.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.model.NotificationResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private LinearLayout notificationContainer;
    private TextView tvNoNotifications;

    private ApiService apiService;

    private Call<List<NotificationResponse>> notificationCall;

    private final List<NotificationResponse> notifications =
            new ArrayList<>();

    private boolean isMarkingRead = false;
    private MaterialButton btnMarkAllAsRead;

    private Call<Void> markAllReadCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notifications);

        btnBack = findViewById(R.id.btnBack);
        notificationContainer = findViewById(R.id.notificationContainer);
        tvNoNotifications = findViewById(R.id.tvNoNotifications);
        btnMarkAllAsRead =
                findViewById(R.id.btnMarkAllAsRead);

        btnMarkAllAsRead.setOnClickListener(
                v -> confirmMarkAllAsRead()
        );

        btnBack.setOnClickListener(v -> finish());

        ApiClient.initialize(this);
        apiService = ApiClient.getApiService();

        loadParentNotifications();
    }

    // =====================================================
// CONFIRM MARK ALL AS READ
// =====================================================

    private void confirmMarkAllAsRead() {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Mark All as Read")
                .setMessage(
                        "Are you sure you want to mark all notifications as read?"
                )
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss()
                )
                .setPositiveButton("Yes, Mark All", (dialog, which) ->
                        markAllNotificationsAsRead()
                )
                .show();
    }

// =====================================================
// MARK ALL NOTIFICATIONS AS READ
// =====================================================

    private void markAllNotificationsAsRead() {

        if (markAllReadCall != null && !markAllReadCall.isExecuted()) {
            return;
        }

        btnMarkAllAsRead.setEnabled(false);
        btnMarkAllAsRead.setText("Updating...");

        markAllReadCall =
                apiService.markAllParentNotificationsAsRead();

        markAllReadCall.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnMarkAllAsRead.setEnabled(true);
                btnMarkAllAsRead.setText("Mark All as Read");

                if (response.isSuccessful()) {

                    Toast.makeText(
                            NotificationsActivity.this,
                            "All notifications marked as read.",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadParentNotifications();

                } else {

                    Toast.makeText(
                            NotificationsActivity.this,
                            "Unable to mark all as read. Code: "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnMarkAllAsRead.setEnabled(true);
                btnMarkAllAsRead.setText("Mark All as Read");

                if (call.isCanceled()) {
                    return;
                }

                Toast.makeText(
                        NotificationsActivity.this,
                        "Mark all as read failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void loadParentNotifications() {

        if (notificationCall != null) {
            notificationCall.cancel();
        }

        notificationCall = apiService.getParentNotifications();

        notificationCall.enqueue(
                new Callback<List<NotificationResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<NotificationResponse>> call,
                            Response<List<NotificationResponse>> response) {

                        if (call.isCanceled()) return;

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    NotificationsActivity.this,
                                    "Unable to load notifications. Code: "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();

                            showEmptyState();
                            return;
                        }

                        notifications.clear();
                        notifications.addAll(response.body());

                        displayNotifications();
                    }

                    @Override
                    public void onFailure(
                            Call<List<NotificationResponse>> call,
                            Throwable t) {

                        if (call.isCanceled()) return;

                        Toast.makeText(
                                NotificationsActivity.this,
                                "Notification loading failed: "
                                        + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                        showEmptyState();
                    }
                }
        );
    }

    private void displayNotifications() {

        notificationContainer.removeAllViews();

        if (notifications.isEmpty()) {

            btnMarkAllAsRead.setVisibility(View.GONE);

            showEmptyState();
            return;
        }

        tvNoNotifications.setVisibility(View.GONE);
        boolean hasUnreadNotifications = false;

        for (NotificationResponse notification : notifications) {

            if (notification != null
                    && !Boolean.TRUE.equals(notification.getIsRead())) {

                hasUnreadNotifications = true;
                break;
            }
        }

        btnMarkAllAsRead.setVisibility(
                hasUnreadNotifications ? View.VISIBLE : View.GONE
        );

        for (NotificationResponse notification : notifications) {

            View notificationView =
                    LayoutInflater.from(this).inflate(
                            R.layout.item_parent_notification,
                            notificationContainer,
                            false
                    );

            TextView tvTitle =
                    notificationView.findViewById(
                            R.id.tvParentNotificationTitle);

            TextView tvMessage =
                    notificationView.findViewById(
                            R.id.tvParentNotificationMessage);

            TextView tvDate =
                    notificationView.findViewById(
                            R.id.tvParentNotificationDate);

            TextView tvType =
                    notificationView.findViewById(
                            R.id.tvParentNotificationType);

            TextView tvReadStatus =
                    notificationView.findViewById(
                            R.id.tvParentNotificationReadStatus);

            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());

            tvDate.setText(
                    formatNotificationDate(notification.getCreatedAt()));

            tvType.setText(
                    notification.getType() != null
                            ? notification.getType()
                            : "General");

            boolean isRead =
                    Boolean.TRUE.equals(notification.getIsRead());

            tvReadStatus.setText(isRead ? "Read" : "Unread");
            tvReadStatus.setVisibility(View.VISIBLE);

            // Card ko explicitly clickable banaya gaya hai.
            MaterialCardView notificationCard =
                    (MaterialCardView) notificationView;

            notificationCard.setClickable(true);
            notificationCard.setFocusable(true);

            notificationCard.setOnClickListener(v -> {

                if (isMarkingRead) {
                    return;
                }

                if (!Boolean.TRUE.equals(notification.getIsRead())) {

                    markNotificationAsRead(
                            notification.getId(),
                            notificationCard,
                            tvReadStatus
                    );

                } else {
                    Toast.makeText(
                            NotificationsActivity.this,
                            "This notification is already read.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

            notificationContainer.addView(notificationView);
        }
    }

    private void markNotificationAsRead(
            Long notificationId,
            MaterialCardView card,
            TextView tvReadStatus) {

        if (notificationId == null) {
            Toast.makeText(
                    this,
                    "Invalid notification ID.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (isMarkingRead) return;

        isMarkingRead = true;

        Toast.makeText(
                this,
                "Updating notification...",
                Toast.LENGTH_SHORT
        ).show();

        apiService.markParentNotificationAsRead(
                notificationId
        ).enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(
                    Call<JsonObject> call,
                    Response<JsonObject> response) {

                isMarkingRead = false;

                if (response.isSuccessful()) {

                    tvReadStatus.setText("Read");

                    Toast.makeText(
                            NotificationsActivity.this,
                            "Notification marked as read.",
                            Toast.LENGTH_SHORT
                    ).show();

                    // Backend se latest notification list reload.
                    loadParentNotifications();

                } else {

                    Toast.makeText(
                            NotificationsActivity.this,
                            "Read update failed. Code: "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<JsonObject> call,
                    Throwable t) {

                isMarkingRead = false;

                Toast.makeText(
                        NotificationsActivity.this,
                        "Read status update failed: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String formatNotificationDate(String dateValue) {

        if (dateValue == null || dateValue.trim().isEmpty()) {
            return "";
        }

        try {

            String normalizedDate = dateValue;

            if (normalizedDate.contains(".")) {
                normalizedDate = normalizedDate.substring(
                        0,
                        normalizedDate.indexOf("."));
            }

            SimpleDateFormat inputFormat =
                    new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.ENGLISH);

            inputFormat.setTimeZone(
                    TimeZone.getTimeZone("Asia/Kolkata"));

            Date date = inputFormat.parse(normalizedDate);

            if (date == null) return dateValue;

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.ENGLISH);

            outputFormat.setTimeZone(
                    TimeZone.getTimeZone("Asia/Kolkata"));

            return outputFormat.format(date);

        } catch (Exception e) {
            return dateValue.replace("T", " ");
        }
    }

    private void showEmptyState() {

        notificationContainer.removeAllViews();
        tvNoNotifications.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (apiService != null) {
            loadParentNotifications();
        }
    }

    @Override
    protected void onDestroy() {

        if (notificationCall != null) {
            notificationCall.cancel();
        }
        if (markAllReadCall != null) {
            markAllReadCall.cancel();
        }

        super.onDestroy();
    }
}

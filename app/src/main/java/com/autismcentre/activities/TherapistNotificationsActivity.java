
package com.autismcentre.activities;

import android.app.AlertDialog;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.model.NotificationResponse;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistNotificationsActivity extends BaseTherapistActivity {

    private MaterialButton btnClearAllNotifications;
    private LinearLayout notificationContainer;
    private TextView tvNoNotifications;

    private ApiService apiService;

    private Call<List<NotificationResponse>> notificationsCall;
    private Call<Void> clearAllCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism Center");

        showPageContent(R.layout.activity_therapist_notifications);

        btnClearAllNotifications =
                findViewById(R.id.btnClearAllNotifications);

        notificationContainer =
                findViewById(R.id.notificationContainer);

        tvNoNotifications =
                findViewById(R.id.tvNoNotifications);

        ApiClient.initialize(getApplicationContext());
        apiService = ApiClient.getApiService();

        btnClearAllNotifications.setOnClickListener(v ->
                showClearAllConfirmation()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    // =====================================================
    // LOAD NOTIFICATIONS FROM BACKEND
    // =====================================================

    private void loadNotifications() {

        if (notificationsCall != null && !notificationsCall.isCanceled()) {
            notificationsCall.cancel();
        }

        notificationsCall = apiService.getTherapistNotifications();

        notificationsCall.enqueue(new Callback<List<NotificationResponse>>() {

            @Override
            public void onResponse(
                    Call<List<NotificationResponse>> call,
                    Response<List<NotificationResponse>> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    displayNotifications(response.body());

                } else {

                    Toast.makeText(
                            TherapistNotificationsActivity.this,
                            "Unable to load notifications.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<List<NotificationResponse>> call,
                    Throwable t) {

                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                Toast.makeText(
                        TherapistNotificationsActivity.this,
                        "Network error while loading notifications.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // =====================================================
    // DISPLAY DYNAMIC NOTIFICATION CARDS
    // =====================================================

    private void displayNotifications(List<NotificationResponse> notifications) {

        notificationContainer.removeAllViews();

        if (notifications == null || notifications.isEmpty()) {

            tvNoNotifications.setVisibility(View.VISIBLE);
            btnClearAllNotifications.setVisibility(View.GONE);

            return;
        }

        tvNoNotifications.setVisibility(View.GONE);

        boolean hasUnread = false;

        for (NotificationResponse notification : notifications) {

            if (notification == null) {
                continue;
            }

            Boolean readStatus = notification.getIsRead();

            if (readStatus == null || !readStatus) {
                hasUnread = true;
            }

            createNotificationCard(notification);
        }

        btnClearAllNotifications.setVisibility(
                hasUnread ? View.VISIBLE : View.GONE
        );
    }

    // =====================================================
    // CREATE NOTIFICATION CARD
    // =====================================================

    private void createNotificationCard(NotificationResponse notification) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dpToPx(14);

        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(18));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(Color.WHITE);
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(
                ColorStateList.valueOf(Color.LTGRAY)
        );

        LinearLayout content = new LinearLayout(this);

        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dpToPx(18),
                dpToPx(18),
                dpToPx(18),
                dpToPx(18)
        );

        // TITLE

        TextView title = new TextView(this);

        title.setText(
                notification.getTitle() != null
                        ? notification.getTitle()
                        : "Notification"
        );

        title.setTextColor(Color.rgb(23, 23, 23));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        content.addView(title);

        // MESSAGE

        TextView message = new TextView(this);

        message.setText(
                notification.getMessage() != null
                        ? notification.getMessage()
                        : ""
        );

        message.setTextColor(Color.rgb(85, 85, 85));
        message.setTextSize(15);

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        messageParams.topMargin = dpToPx(7);

        content.addView(message, messageParams);

        // DATE AND TIME

        TextView time = new TextView(this);

        time.setText(formatNotificationDate(notification.getCreatedAt()));
        time.setTextColor(Color.rgb(153, 153, 153));
        time.setTextSize(12);

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        timeParams.topMargin = dpToPx(10);

        content.addView(time, timeParams);

        // UNREAD VISUAL INDICATOR

        Boolean isRead = notification.getIsRead();

        if (isRead == null || !isRead) {

            card.setCardBackgroundColor(
                    Color.rgb(250, 247, 255)
            );
        }

        card.addView(content);

        card.setOnClickListener(v -> {

            Long notificationId = notification.getId();

            if (notificationId == null) {

                Toast.makeText(
                        this,
                        "Invalid notification ID.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            markNotificationAsRead(notification);
        });
        notificationContainer.addView(card);
    }

    // =====================================================
    // MARK INDIVIDUAL NOTIFICATION AS READ
    // =====================================================

    // =====================================================
// MARK AS READ AND NAVIGATE
// =====================================================

    private void markNotificationAsRead(
            NotificationResponse notification) {

        apiService.markTherapistNotificationAsRead(
                notification.getId()
        ).enqueue(new Callback<com.google.gson.JsonObject>() {

            @Override
            public void onResponse(
                    Call<com.google.gson.JsonObject> call,
                    Response<com.google.gson.JsonObject> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful()) {

                    loadNotifications();

                    navigateToNotificationDestination(
                            notification.getType()
                    );

                } else {

                    Toast.makeText(
                            TherapistNotificationsActivity.this,
                            "Unable to mark notification as read.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<com.google.gson.JsonObject> call,
                    Throwable t) {

                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                Toast.makeText(
                        TherapistNotificationsActivity.this,
                        "Network error.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // =====================================================
// NOTIFICATION TYPE BASED NAVIGATION
// =====================================================

    private void navigateToNotificationDestination(String type) {

        if (type == null) {
            return;
        }

        Intent intent = null;

        switch (type.toUpperCase(java.util.Locale.ROOT)) {

            case "SESSION":

                intent = new Intent(
                        this,
                        TherapistScheduleActivity.class
                );

                break;

            case "HOMEWORK":

                intent = new Intent(
                        this,
                        TherapistHomeworkActivity.class
                );

                break;

            case "COMMUNICATION":

                intent = new Intent(
                        this,
                        TherapistParentChatActivity.class
                );

                break;

            case "REPORT":

                intent = new Intent(
                        this,
                        TherapistReportsActivity.class
                );

                break;

            case "FEEDBACK":
            case "PAYMENT":
            case "SYSTEM":
            default:

                // No destination available currently.
                return;
        }

        startActivity(intent);
    }

    // =====================================================
    // CLEAR ALL CONFIRMATION
    // =====================================================

    private void showClearAllConfirmation() {

        new AlertDialog.Builder(this)
                .setTitle("Clear All Notifications")
                .setMessage(
                        "Are you sure you want to mark all notifications as read?"
                )
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss()
                )
                .setPositiveButton("Yes", (dialog, which) ->
                        clearAllNotifications()
                )
                .show();
    }

    // =====================================================
    // MARK ALL NOTIFICATIONS AS READ
    // =====================================================

    private void clearAllNotifications() {

        if (clearAllCall != null && !clearAllCall.isCanceled()) {
            clearAllCall.cancel();
        }

        clearAllCall = apiService.markAllTherapistNotificationsAsRead();

        clearAllCall.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response) {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful()) {

                    Toast.makeText(
                            TherapistNotificationsActivity.this,
                            "All notifications marked as read.",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadNotifications();

                } else {

                    Toast.makeText(
                            TherapistNotificationsActivity.this,
                            "Unable to clear notifications.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                Toast.makeText(
                        TherapistNotificationsActivity.this,
                        "Network error.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // =====================================================
    // FORMAT DATE AND TIME
    // =====================================================

    private String formatNotificationDate(String dateString) {

        if (dateString == null || dateString.trim().isEmpty()) {
            return "";
        }

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };

        for (String pattern : patterns) {

            try {

                SimpleDateFormat inputFormat =
                        new SimpleDateFormat(pattern, Locale.US);

                inputFormat.setLenient(false);

                Date date = inputFormat.parse(dateString);

                if (date != null) {

                    SimpleDateFormat outputFormat =
                            new SimpleDateFormat(
                                    "dd MMM yyyy • hh:mm a",
                                    Locale.getDefault()
                            );

                    return outputFormat.format(date);
                }

            } catch (ParseException ignored) {
                // Try next supported date format.
            }
        }

        return dateString;
    }

    // =====================================================
    // DP TO PIXELS
    // =====================================================

    private int dpToPx(int dp) {

        return (int) (dp * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    // =====================================================
    // CLEANUP
    // =====================================================

    @Override
    protected void onDestroy() {

        if (notificationsCall != null && !notificationsCall.isCanceled()) {
            notificationsCall.cancel();
        }

        if (clearAllCall != null && !clearAllCall.isCanceled()) {
            clearAllCall.cancel();
        }

        super.onDestroy();
    }

    // =====================================================
    // CURRENT BOTTOM NAVIGATION ITEM
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.therapist_nav_home;
    }
}

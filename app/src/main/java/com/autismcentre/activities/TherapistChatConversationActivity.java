
package com.autismcentre.activities;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.TherapistMessageRequest;
import com.autismcentre.model.TherapistMessageResponse;
import com.autismcentre.utils.MessageDateTimeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistChatConversationActivity
        extends BaseTherapistActivity {

    private TextView tvChatParentName;
    private TextView tvChatChildName;
    private TextInputEditText etMessage;
    private MaterialButton btnSendMessage;
    private LinearLayout messageContainer;
    private ScrollView chatScrollView;

    private Long childId;
    private String parentName;
    private String childName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism Center");

        showPageContent(
                R.layout.activity_therapist_chat_conversation
        );

        tvChatParentName = findViewById(
                R.id.tvChatParentName
        );

        tvChatChildName = findViewById(
                R.id.tvChatChildName
        );

        etMessage = findViewById(
                R.id.etMessage
        );

        btnSendMessage = findViewById(
                R.id.btnSendMessage
        );

        messageContainer = findViewById(
                R.id.messageContainer
        );

        chatScrollView = findViewById(
                R.id.chatScrollView
        );

        childId = getIntent().getLongExtra(
                "child_id", -1L
        );

        parentName = getIntent().getStringExtra(
                "parent_name"
        );

        childName = getIntent().getStringExtra(
                "child_name"
        );

        if (parentName == null || parentName.trim().isEmpty()) {
            parentName = "Parent";
        }

        if (childName == null || childName.trim().isEmpty()) {
            childName = "Child";
        }

        tvChatParentName.setText(parentName);
        tvChatChildName.setText("Parent of " + childName);

        if (childId == -1L || childId <= 0) {
            showMessage("Invalid child ID. Please reopen the conversation.");
            btnSendMessage.setEnabled(false);
            return;
        }

        loadConversation();

        // Sending will be connected in the next step.
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    // =====================================================
    // LOAD REAL CONVERSATION FROM BACKEND
    // =====================================================

    private void loadConversation() {

        messageContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading messages...");
        loadingText.setTextSize(14);
        loadingText.setPadding(
                dpToPx(12), dpToPx(16),
                dpToPx(12), dpToPx(16)
        );

        messageContainer.addView(loadingText);

        ApiClient.getApiService()
                .getTherapistConversation(childId)
                .enqueue(new Callback<List<TherapistMessageResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistMessageResponse>> call,
                            Response<List<TherapistMessageResponse>> response) {

                        messageContainer.removeAllViews();

                        if (!response.isSuccessful()) {
                            showEmptyState(
                                    "Unable to load messages. Error: "
                                            + response.code()
                            );
                            return;
                        }

                        List<TherapistMessageResponse> messages =
                                response.body();

                        if (messages == null || messages.isEmpty()) {
                            showEmptyState(
                                    "No messages yet. Your conversation will appear here."
                            );
                            return;
                        }

                        for (TherapistMessageResponse message : messages) {
                            if (message != null) {
                                addRealMessage(message);
                                markParentMessageAsRead(message);
                            }
                        }

                        if (messageContainer.getChildCount() == 0) {
                            showEmptyState("No messages available.");
                        }

                        scrollToLatestMessage();
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistMessageResponse>> call,
                            Throwable t) {

                        messageContainer.removeAllViews();

                        showEmptyState(
                                "Could not connect to the server. "
                                        + "Check your internet and try again."
                        );
                    }
                });
    }


    // =====================================================
    // MARK PARENT MESSAGE AS READ
    // =====================================================

    // =====================================================
// MARK RECEIVED THERAPIST MESSAGES AS READ
// =====================================================

    private void markParentMessageAsRead(
            TherapistMessageResponse message) {

        if (message == null
                || message.getMessageId() == null
                || message.getReceiverUserId() == null
                || message.getRead() == null
                || message.getRead()) {
            return;
        }

        // Only mark messages addressed to the logged-in Therapist.
        // Parent messages and messages addressed to Admin are ignored.
        com.autismcentre.utils.SessionManager sessionManager =
                new com.autismcentre.utils.SessionManager(this);

        Long currentUserId = sessionManager.getUserId();

        if (currentUserId == null
                || !message.getReceiverUserId().equals(currentUserId)) {
            return;
        }

        ApiClient.getApiService()
                .markTherapistMessageAsRead(
                        childId,
                        message.getMessageId()
                )
                .enqueue(new Callback<com.google.gson.JsonObject>() {

                    @Override
                    public void onResponse(
                            Call<com.google.gson.JsonObject> call,
                            Response<com.google.gson.JsonObject> response) {

                        if (!response.isSuccessful()) {
                            android.util.Log.w(
                                    "TherapistChat",
                                    "Could not mark message as read. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<com.google.gson.JsonObject> call,
                            Throwable t) {

                        android.util.Log.e(
                                "TherapistChat",
                                "Mark message read request failed",
                                t
                        );
                    }
                });
    }


    // =====================================================
    // SEND MESSAGE TO BACKEND
    // =====================================================

    private void sendMessage() {

        String messageText = etMessage.getText() == null
                ? ""
                : etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            etMessage.setError("Enter a message");
            etMessage.requestFocus();
            return;
        }

        if (messageText.length() > 3000) {
            etMessage.setError("Message cannot exceed 3000 characters");
            return;
        }

        if (childId == null || childId <= 0) {
            showMessage("Invalid child. Please reopen the conversation.");
            return;
        }

        btnSendMessage.setEnabled(false);

        ApiClient.getApiService()
                .sendTherapistMessage(
                        childId,
                        new TherapistMessageRequest(messageText)
                )
                .enqueue(new Callback<TherapistMessageResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistMessageResponse> call,
                            Response<TherapistMessageResponse> response) {

                        btnSendMessage.setEnabled(true);

                        if (response.isSuccessful()) {

                            etMessage.setText("");

                            // Reload server history so the saved
                            // message appears with its real timestamp.
                            loadConversation();

                            return;
                        }

                        String errorMessage =
                                "Message could not be sent. Error: "
                                        + response.code();

                        try {
                            if (response.errorBody() != null) {
                                String errorBody =
                                        response.errorBody().string();

                                if (!errorBody.trim().isEmpty()) {
                                    android.util.Log.e(
                                            "TherapistChat",
                                            "Send message error: " + errorBody
                                    );
                                }
                            }
                        } catch (java.io.IOException e) {
                            android.util.Log.e(
                                    "TherapistChat",
                                    "Could not read error response",
                                    e
                            );
                        }

                        showMessage(errorMessage);
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistMessageResponse> call,
                            Throwable t) {

                        btnSendMessage.setEnabled(true);

                        showMessage(
                                "Network error. Message was not confirmed as saved."
                        );

                        android.util.Log.e(
                                "TherapistChat",
                                "Send message failed",
                                t
                        );
                    }
                });
    }


    // =====================================================
    // DISPLAY A REAL MESSAGE
    // =====================================================


    private void addRealMessage(
            TherapistMessageResponse message) {

        String sender = message.getSenderName();

        com.autismcentre.utils.SessionManager sessionManager =
                new com.autismcentre.utils.SessionManager(this);

        Long currentUserId = sessionManager.getUserId();

        boolean isOwnMessage =
                message.getSenderUserId() != null
                        && currentUserId != null
                        && message.getSenderUserId().equals(currentUserId);

        boolean isParentMessage =
                sender != null
                        && sender.trim().equalsIgnoreCase(parentName.trim());

        LinearLayout messageWrapper = new LinearLayout(this);
        messageWrapper.setOrientation(LinearLayout.VERTICAL);

        messageWrapper.setGravity(
                isOwnMessage ? Gravity.END : Gravity.START
        );

        LinearLayout.LayoutParams wrapperParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        wrapperParams.setMargins(
                0, dpToPx(4), 0, dpToPx(10)
        );

        messageWrapper.setLayoutParams(wrapperParams);

        // Sender name
        TextView tvSender = new TextView(this);

        String senderLabel =
                sender == null || sender.trim().isEmpty()
                        ? (isParentMessage ? parentName : "Therapist")
                        : sender;

        tvSender.setText(senderLabel);
        tvSender.setTextSize(12);
        tvSender.setTextColor(Color.DKGRAY);

        LinearLayout.LayoutParams senderParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        senderParams.setMargins(
                dpToPx(6), 0, dpToPx(6), dpToPx(3)
        );

        messageWrapper.addView(tvSender, senderParams);

        // Message bubble
        MaterialCardView messageCard = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                isParentMessage ? 0 : dpToPx(40),
                0,
                isParentMessage ? dpToPx(40) : 0,
                0
        );

        messageCard.setLayoutParams(cardParams);
        messageCard.setRadius(dpToPx(16));
        messageCard.setCardElevation(0);

        messageCard.setCardBackgroundColor(
                isOwnMessage
                        ? Color.rgb(232, 224, 255)
                        : Color.WHITE
        );

        TextView tvMessage = new TextView(this);

        tvMessage.setText(message.getMessage());
        tvMessage.setTextColor(Color.rgb(30, 30, 30));
        tvMessage.setTextSize(15);

        tvMessage.setPadding(
                dpToPx(14), dpToPx(12),
                dpToPx(14), dpToPx(12)
        );

        messageCard.addView(tvMessage);
        messageWrapper.addView(messageCard);

        // Timestamp and Read Status
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTime = new TextView(this);

       // tvTime.setText(formatTimestamp(message.getCreatedAt()));
        tvTime.setText(
                MessageDateTimeUtils.formatTimestamp(message.getCreatedAt())
        );
        tvTime.setTextColor(Color.GRAY);
        tvTime.setTextSize(11);

        footer.addView(tvTime);

        // Show Read/Unread only on Therapist's own messages
        if (isOwnMessage) {

            TextView tvReadStatus = new TextView(this);

            boolean isRead = Boolean.TRUE.equals(message.getRead());

            tvReadStatus.setText(
                    isRead ? "  ✓✓ Read" : "  ✓ Unread"
            );

            tvReadStatus.setTextColor(
                    isRead
                            ? Color.rgb(22, 133, 216)
                            : Color.GRAY
            );

            tvReadStatus.setTextSize(11);

            footer.addView(tvReadStatus);
        }

        LinearLayout.LayoutParams footerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        footerParams.setMargins(
                dpToPx(6), dpToPx(3),
                dpToPx(6), 0
        );

        footer.setLayoutParams(footerParams);

        messageWrapper.addView(footer);
        messageContainer.addView(messageWrapper);
    }

    // =====================================================
    // EMPTY / LOADING ERROR STATE
    // =====================================================

    private void showEmptyState(String message) {

        TextView textView = new TextView(this);

        textView.setText(message);
        textView.setTextSize(14);
        textView.setTextColor(Color.GRAY);
        textView.setGravity(Gravity.CENTER);

        textView.setPadding(
                dpToPx(16), dpToPx(24),
                dpToPx(16), dpToPx(24)
        );

        messageContainer.addView(textView);
    }

    // =====================================================
    // FORMAT BACKEND TIMESTAMP
    // =====================================================

    private String formatTimestamp(String timestamp) {

        if (timestamp == null || timestamp.trim().isEmpty()) {
            return "";
        }

        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss"
        };

        for (String pattern : formats) {
            try {
                SimpleDateFormat parser =
                        new SimpleDateFormat(
                                pattern, Locale.US
                        );

                if (pattern.endsWith("'Z'")) {
                    parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                }

                Date date = parser.parse(timestamp);

                if (date != null) {
                    return new SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                    ).format(date);
                }

            } catch (ParseException ignored) {
                // Try the next supported timestamp format.
            }
        }

        return timestamp;
    }

    // =====================================================
    // SCROLL TO LATEST MESSAGE
    // =====================================================

    private void scrollToLatestMessage() {

        chatScrollView.post(() ->
                chatScrollView.fullScroll(
                        ScrollView.FOCUS_DOWN
                )
        );
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private int dpToPx(int dp) {
        return (int) (
                dp * getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_home;
    }
}

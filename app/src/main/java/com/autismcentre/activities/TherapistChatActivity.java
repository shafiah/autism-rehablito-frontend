package com.autismcentre.activities;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentMessageRequest;
import com.autismcentre.model.ParentMessageResponse;
import com.autismcentre.utils.MessageDateTimeUtils;
import com.autismcentre.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistChatActivity extends BaseParentActivity {

    private TextView tvTherapistName;
    private TextView tvSpecialization;

    private EditText etMessage;
    private MaterialButton btnSend;

    private LinearLayout chatContainer;
    private ScrollView scrollChat;

    private SessionManager sessionManager;

    private Long childId;
    private Long therapistId;
    private Long currentUserId;

    private String therapistName;
    private String specialization;

    private boolean isSending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_therapist_chat);

        sessionManager = new SessionManager(this);

        tvTherapistName = findViewById(R.id.tvTherapistName);
        tvSpecialization = findViewById(R.id.tvSpecialization);

        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        chatContainer = findViewById(R.id.chatContainer);
        scrollChat = findViewById(R.id.scrollChat);

        childId = getIntent().getLongExtra("childId", -1L);
        therapistId = getIntent().getLongExtra("therapistId", -1L);

        currentUserId = sessionManager.getUserId();

        therapistName = getIntent().getStringExtra("therapistName");
        specialization = getIntent().getStringExtra("specialization");

        if (therapistName != null) {
            tvTherapistName.setText(therapistName);
        }

        if (specialization != null) {
            tvSpecialization.setText(specialization);
        }

        if (childId == null || childId <= 0
                || therapistId == null || therapistId <= 0
                || currentUserId == null || currentUserId <= 0) {

            Toast.makeText(
                    this,
                    "Invalid chat details. Please open the therapist again.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        btnSend.setOnClickListener(v -> sendMessage());

        loadConversation();
    }

    // =====================================================
    // LOAD EXISTING PARENT, THERAPIST AND ADMIN MESSAGES
    // =====================================================

    private void loadConversation() {

        ApiClient.getApiService()
                .getParentConversation(childId, therapistId)
                .enqueue(new Callback<List<ParentMessageResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentMessageResponse>> call,
                            Response<List<ParentMessageResponse>> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {

                            chatContainer.removeAllViews();

                            for (ParentMessageResponse message : response.body()) {

                                TextView tvReadStatus = addMessageToChat(message);

                                markParentMessageAsRead(message, tvReadStatus);
                            }

                            scrollToBottom();

                        } else {
                            Toast.makeText(
                                    TherapistChatActivity.this,
                                    "Unable to load conversation.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentMessageResponse>> call,
                            Throwable t
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                TherapistChatActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
    // =====================================================
    // MARK RECEIVED PARENT MESSAGES AS READ
    // =====================================================

    private void markParentMessageAsRead(
            ParentMessageResponse message,
            TextView tvReadStatus) {

        if (message == null
                || message.getMessageId() == null
                || message.getReceiverUserId() == null
                || Boolean.TRUE.equals(message.getRead())) {
            return;
        }

        // Only messages received by the logged-in Parent.
        if (!message.getReceiverUserId().equals(currentUserId)) {
            return;
        }

        ApiClient.getApiService()
                .markParentMessageAsRead(
                        childId,
                        therapistId,
                        message.getMessageId()
                )
                .enqueue(new Callback<com.autismcentre.model.ParentMessageReadResponse>() {

                    @Override
                    public void onResponse(
                            Call<com.autismcentre.model.ParentMessageReadResponse> call,
                            Response<com.autismcentre.model.ParentMessageReadResponse> response) {

                        if (response.isSuccessful()) {

                            // Update UI only after backend confirms success.
                           // message.setRead(true);

                            if (tvReadStatus != null) {
                                tvReadStatus.setText("✓✓ Read");
                                tvReadStatus.setTextColor(
                                        Color.rgb(22, 133, 216)
                                );
                            }

                        } else {
                            android.util.Log.w(
                                    "ParentChat",
                                    "Unable to mark message as read. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<com.autismcentre.model.ParentMessageReadResponse> call,
                            Throwable t) {

                        android.util.Log.e(
                                "ParentChat",
                                "Mark message as read failed",
                                t
                        );
                    }
                });
    }
    // =====================================================
    // DISPLAY MESSAGE, TIMESTAMP AND READ/UNREAD STATUS
    // =====================================================

    private TextView addMessageToChat(ParentMessageResponse message) {

        if (message == null || message.getMessage() == null) {
            return null;
        }

        boolean isOwnMessage =
                message.getSenderUserId() != null
                        && message.getSenderUserId().equals(currentUserId);

        String senderName = message.getSenderName();

        if (senderName == null || senderName.trim().isEmpty()) {
            senderName = "Unknown";
        }

        LinearLayout messageGroup = new LinearLayout(this);
        messageGroup.setOrientation(LinearLayout.VERTICAL);
        messageGroup.setGravity(
                isOwnMessage ? Gravity.END : Gravity.START
        );

        LinearLayout.LayoutParams groupParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        groupParams.topMargin = dp(12);
        messageGroup.setLayoutParams(groupParams);

        // Sender name
        TextView tvSender = new TextView(this);
        tvSender.setText(senderName);
        tvSender.setTextSize(13);
        tvSender.setTextColor(Color.DKGRAY);

        LinearLayout.LayoutParams senderParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        senderParams.bottomMargin = dp(5);
        messageGroup.addView(tvSender, senderParams);

        // Message bubble
        TextView tvMessage = new TextView(this);
        tvMessage.setText(message.getMessage());
        tvMessage.setTextSize(15);
        tvMessage.setTextColor(Color.BLACK);
        tvMessage.setPadding(dp(16), dp(12), dp(16), dp(12));

        tvMessage.setMaxWidth(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.75)
        );

        GradientDrawable bubbleBackground = new GradientDrawable();

        if (isOwnMessage) {
            bubbleBackground.setColor(Color.rgb(232, 224, 255));
        } else {
            bubbleBackground.setColor(Color.WHITE);
        }

        bubbleBackground.setCornerRadius(dp(22));
        bubbleBackground.setStroke(dp(1), Color.rgb(210, 210, 215));

        tvMessage.setBackground(bubbleBackground);
        messageGroup.addView(tvMessage);

        // Timestamp and Read Status
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams footerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        footerParams.topMargin = dp(5);
        footer.setLayoutParams(footerParams);

        TextView tvTimestamp = new TextView(this);

        String timestamp = message.getCreatedAt();

        if (timestamp == null || timestamp.trim().isEmpty()) {
            timestamp = "";
        } else {
            timestamp = timestamp.replace("T", " ");
        }

       // tvTimestamp.setText(timestamp);
        tvTimestamp.setText(
                MessageDateTimeUtils.formatTimestamp(message.getCreatedAt())
        );
        tvTimestamp.setTextSize(11);
        tvTimestamp.setTextColor(Color.GRAY);

        footer.addView(tvTimestamp);

        // Show status for both sent and received messages.
        TextView tvReadStatus = new TextView(this);

        boolean isRead = Boolean.TRUE.equals(message.getRead());

        tvReadStatus.setText(
                isRead ? "  ✓✓ Read" : "  ✓ Unread"
        );

        tvReadStatus.setTextSize(11);

        tvReadStatus.setTextColor(
                isRead ? Color.rgb(22, 133, 216) : Color.GRAY
        );

        footer.addView(tvReadStatus);

        messageGroup.addView(footer);
        chatContainer.addView(messageGroup);

        return tvReadStatus;
    }
    // =====================================================
    // SEND PARENT MESSAGE TO THERAPIST
    // =====================================================

    private void sendMessage() {

        if (isSending) {
            return;
        }

        String messageText = etMessage.getText().toString().trim();

        if (messageText.isEmpty()) {
            etMessage.setError("Enter message");
            etMessage.requestFocus();
            return;
        }

        ParentMessageRequest request =
                new ParentMessageRequest(messageText);

        isSending = true;
        btnSend.setEnabled(false);

        ApiClient.getApiService()
                .sendParentMessage(childId, therapistId, request)
                .enqueue(new Callback<ParentMessageResponse>() {

                    @Override
                    public void onResponse(
                            Call<ParentMessageResponse> call,
                            Response<ParentMessageResponse> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        isSending = false;
                        btnSend.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {

                            etMessage.setText("");

                            // Display backend saved message.
                            // Initial status will be Unread until recipient reads it.
                            addMessageToChat(response.body());

                            scrollToBottom();

                        } else {

                            Toast.makeText(
                                    TherapistChatActivity.this,
                                    "Message could not be sent.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ParentMessageResponse> call,
                            Throwable t
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        isSending = false;
                        btnSend.setEnabled(true);

                        Toast.makeText(
                                TherapistChatActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density
        );
    }

    private void scrollToBottom() {

        if (scrollChat != null) {
            scrollChat.post(() ->
                    scrollChat.fullScroll(View.FOCUS_DOWN)
            );
        }
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_chat;
    }
}

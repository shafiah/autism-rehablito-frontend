
package com.autismcentre.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autismcentre.R;
import com.autismcentre.model.AdminCommunicationMessageResponse;
import com.autismcentre.model.AdminMessageRequest;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.autismcentre.utils.MessageDateTimeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminParentConversationActivity extends BaseAdminActivity {

    private TextView tvAdminConversationParent;
    private TextView tvAdminConversationChild;
    private TextView tvAdminConversationTherapist;
    private TextView tvAdminConversationBranch;

    private LinearLayout adminConversationMessagesContainer;
    private TextInputEditText etAdminConversationMessage;
    private MaterialButton btnAdminSendConversationMessage;
    private AutoCompleteTextView actAdminConversationRecipient;

    private ApiService apiService;

    private Long childId;
    private Long parentUserId;
    private Long therapistUserId;

    private String parentName;
    private String childName;
    private String therapistName;
    private String branchName;

    private Long selectedReceiverUserId;
    private String selectedRecipientRole;

    private Call<List<AdminCommunicationMessageResponse>>
            conversationCall;

    private Call<AdminCommunicationMessageResponse> sendCall;

    private boolean isSending = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_parent_conversation
        );

        apiService = ApiClient.getApiService();

        initializeAdminConversationViews();
        readIntentData();
        setupRecipientDropdown();
        loadAdminConversationData();
        setupAdminSendMessage();
    }

    private void initializeAdminConversationViews() {

        tvAdminConversationParent =
                findViewById(R.id.tvAdminConversationParent);

        tvAdminConversationChild =
                findViewById(R.id.tvAdminConversationChild);

        tvAdminConversationTherapist =
                findViewById(R.id.tvAdminConversationTherapist);

        tvAdminConversationBranch =
                findViewById(R.id.tvAdminConversationBranch);

        adminConversationMessagesContainer =
                findViewById(R.id.adminConversationMessagesContainer);

        etAdminConversationMessage =
                findViewById(R.id.etAdminConversationMessage);

        btnAdminSendConversationMessage =
                findViewById(R.id.btnAdminSendConversationMessage);

        actAdminConversationRecipient =
                findViewById(R.id.actAdminConversationRecipient);
    }

    private void readIntentData() {

        childId = getIntent().getLongExtra(
                "communication_child_id",
                -1L
        );

        parentUserId = getIntent().getLongExtra(
                "communication_parent_user_id",
                -1L
        );

        therapistUserId = getIntent().getLongExtra(
                "communication_therapist_user_id",
                -1L
        );

        if (childId <= 0L) {
            childId = null;
        }

        if (parentUserId <= 0L) {
            parentUserId = null;
        }

        if (therapistUserId <= 0L) {
            therapistUserId = null;
        }

        parentName = getIntent().getStringExtra(
                "communication_parent"
        );

        childName = getIntent().getStringExtra(
                "communication_child"
        );

        therapistName = getIntent().getStringExtra(
                "communication_therapist"
        );

        branchName = getIntent().getStringExtra(
                "communication_branch"
        );
    }

    private void loadAdminConversationData() {

        tvAdminConversationParent.setText(
                "Parent: " + safeText(parentName, "Unknown Parent")
        );

        tvAdminConversationChild.setText(
                "Child: " + safeText(childName, "Unknown Child")
        );

        tvAdminConversationTherapist.setText(
                "Therapist: "
                        + safeText(therapistName, "Not assigned")
        );

        tvAdminConversationBranch.setText(
                "Branch: " + safeText(branchName, "Unknown")
        );

        if (childId == null) {
            showConversationError(
                    "Child ID is missing. Please reopen this conversation."
            );
            btnAdminSendConversationMessage.setEnabled(false);
            return;
        }

        if (therapistUserId == null) {
            showConversationError(
                    "Selected therapist ID is missing. Please reopen this conversation."
            );
            btnAdminSendConversationMessage.setEnabled(false);
            return;
        }

        loadRealConversation();
    }
// Temporarily disabling recipient dropdown as per your request. You can uncomment and use it if needed in the future.
   /* private void setupRecipientDropdown() {

        List<String> recipients = new ArrayList<>();

        if (parentUserId != null) {
            recipients.add("Parent");
        }

        if (therapistUserId != null) {
            recipients.add("Therapist");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                recipients
        );

        actAdminConversationRecipient.setAdapter(adapter);

        String initialRecipient;

        if (parentUserId != null) {
            initialRecipient = "Parent";
        } else {
            initialRecipient = "Therapist";
        }

        actAdminConversationRecipient.setText(
                initialRecipient,
                false
        );

        selectRecipient(initialRecipient);

        actAdminConversationRecipient.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String recipient =
                            parent.getItemAtPosition(position).toString();

                    selectRecipient(recipient);
                }
        );
    } */


    private void setupRecipientDropdown() {

        List<String> recipients = new ArrayList<>();

        if (therapistUserId != null) {
            recipients.add("Therapist");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                recipients
        );

        actAdminConversationRecipient.setAdapter(adapter);

        if (therapistUserId != null) {

            actAdminConversationRecipient.setText(
                    "Therapist",
                    false
            );

            selectRecipient("Therapist");

        } else {

            actAdminConversationRecipient.setText("", false);

            selectedReceiverUserId = null;
            selectedRecipientRole = null;

            btnAdminSendConversationMessage.setEnabled(false);
        }

        actAdminConversationRecipient.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String recipient =
                            parent.getItemAtPosition(position).toString();

                    selectRecipient(recipient);
                }
        );
    }


    private void selectRecipient(String recipient) {

        if ("Parent".equalsIgnoreCase(recipient)) {

            selectedRecipientRole = "PARENT";
            selectedReceiverUserId = parentUserId;

        } else if ("Therapist".equalsIgnoreCase(recipient)) {

            selectedRecipientRole = "THERAPIST";
            selectedReceiverUserId = therapistUserId;

        } else {

            selectedRecipientRole = null;
            selectedReceiverUserId = null;
        }

        boolean validRecipient = selectedReceiverUserId != null;

        btnAdminSendConversationMessage.setEnabled(
                validRecipient && !isSending && childId != null
                        && therapistUserId != null
        );

        if (!validRecipient) {
            Toast.makeText(
                    this,
                    "Selected recipient is not available.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Therapist-specific conversation GET API
    private void loadRealConversation() {

        if (childId == null
                || therapistUserId == null
                || isFinishing()
                || isDestroyed()) {
            return;
        }

        if (conversationCall != null) {
            conversationCall.cancel();
        }

        conversationCall =
                apiService.getAdminTherapistConversation(
                        childId,
                        therapistUserId
                );

        conversationCall.enqueue(
                new Callback<List<AdminCommunicationMessageResponse>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<AdminCommunicationMessageResponse>> call,
                            @NonNull Response<List<AdminCommunicationMessageResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            renderConversation(response.body());

                        } else {

                            showConversationError(
                                    "Could not load messages. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<AdminCommunicationMessageResponse>> call,
                            @NonNull Throwable t) {

                        if (call.isCanceled()
                                || isFinishing()
                                || isDestroyed()) {
                            return;
                        }

                        showConversationError(
                                "Network error while loading messages."
                        );
                    }
                }
        );
    }

    private void renderConversation(
            List<AdminCommunicationMessageResponse> messages) {

        adminConversationMessagesContainer.removeAllViews();

        if (messages == null || messages.isEmpty()) {
            showConversationError("No messages in this conversation yet.");
            return;
        }

        for (AdminCommunicationMessageResponse message : messages) {

            if (message == null) {
                continue;
            }

            String role = safeText(
                    message.getSenderRole(),
                    ""
            );

            String sender = safeText(
                    message.getSenderUserName(),
                    "Unknown"
            );

            String body = safeText(
                    message.getMessage(),
                    ""
            );

            String time = formatTimestamp(
                    message.getCreatedAt()
            );

            if ("PARENT".equalsIgnoreCase(role)) {

                addMessageBubble(
                        R.layout.item_admin_parent_message,
                        "Parent • " + sender,
                        body,
                        time
                );

            } else if ("THERAPIST".equalsIgnoreCase(role)) {

                addMessageBubble(
                        R.layout.item_admin_therapist_message,
                        "Therapist • " + sender,
                        body,
                        time
                );

            } else if ("ADMIN".equalsIgnoreCase(role)) {

                addMessageBubble(
                        R.layout.item_admin_admin_message,
                        "Admin • " + sender,
                        body,
                        time
                );

            } else {

                addMessageBubble(
                        R.layout.item_admin_admin_message,
                        role.isEmpty() ? sender : role + " • " + sender,
                        body,
                        time
                );
            }
        }

        scrollAdminConversationToBottom();
    }

    private void addMessageBubble(
            int layoutId,
            String sender,
            String message,
            String time) {

        View messageView = LayoutInflater.from(this).inflate(
                layoutId,
                adminConversationMessagesContainer,
                false
        );

        TextView tvSender = messageView.findViewById(
                R.id.tvAdminMessageSender
        );

        TextView tvMessage = messageView.findViewById(
                R.id.tvAdminMessageText
        );

        TextView tvTime = messageView.findViewById(
                R.id.tvAdminMessageTime
        );

        tvSender.setText(sender);
        tvMessage.setText(message);
        tvTime.setText(time);


        adminConversationMessagesContainer.addView(messageView);
    }

    private void setupAdminSendMessage() {

        btnAdminSendConversationMessage.setOnClickListener(v -> {

            if (isSending) {
                return;
            }

            if (childId == null || therapistUserId == null) {
                Toast.makeText(
                        this,
                        "Child or therapist ID is missing.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (selectedReceiverUserId == null) {
                Toast.makeText(
                        this,
                        "Please select a valid recipient.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            if (etAdminConversationMessage.getText() == null) {
                return;
            }

            String message = etAdminConversationMessage
                    .getText()
                    .toString()
                    .trim();

            if (message.isEmpty()) {
                etAdminConversationMessage.setError(
                        "Enter a message"
                );
                return;
            }

            sendAdminMessage(message);
        });
    }

    private void sendAdminMessage(String message) {

        isSending = true;
        btnAdminSendConversationMessage.setEnabled(false);

        // Selected therapist User ID identifies this conversation.
        AdminMessageRequest request = new AdminMessageRequest(
                selectedReceiverUserId,
                therapistUserId,
                message
        );

        sendCall = apiService.sendAdminCommunicationMessage(
                childId,
                request
        );

        sendCall.enqueue(
                new Callback<AdminCommunicationMessageResponse>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<AdminCommunicationMessageResponse> call,
                            @NonNull Response<AdminCommunicationMessageResponse> response) {

                        isSending = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnAdminSendConversationMessage.setEnabled(
                                selectedReceiverUserId != null
                                        && childId != null
                                        && therapistUserId != null
                        );

                        if (response.isSuccessful()
                                && response.body() != null) {

                            etAdminConversationMessage.setText("");

                            Toast.makeText(
                                    AdminParentConversationActivity.this,
                                    "Message sent successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadRealConversation();

                        } else {

                            Toast.makeText(
                                    AdminParentConversationActivity.this,
                                    "Message failed. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<AdminCommunicationMessageResponse> call,
                            @NonNull Throwable t) {

                        isSending = false;

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        btnAdminSendConversationMessage.setEnabled(
                                selectedReceiverUserId != null
                                        && childId != null
                                        && therapistUserId != null
                        );

                        if (call.isCanceled()) {
                            return;
                        }

                        Toast.makeText(
                                AdminParentConversationActivity.this,
                                "Network error while sending message.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void showConversationError(String message) {

        adminConversationMessagesContainer.removeAllViews();

        TextView errorView = new TextView(this);
        errorView.setText(message);
        errorView.setTextSize(14);
        errorView.setPadding(16, 24, 16, 24);

        adminConversationMessagesContainer.addView(errorView);
    }

    private String formatTimestamp(String timestamp) {
        return MessageDateTimeUtils.formatTimestamp(timestamp);
    }
    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }

    private void scrollAdminConversationToBottom() {

        View scrollViewView = findViewById(
                R.id.adminConversationScrollView
        );

        if (scrollViewView instanceof ScrollView) {

            ScrollView scrollView = (ScrollView) scrollViewView;

            scrollView.post(() ->
                    scrollView.fullScroll(View.FOCUS_DOWN)
            );
        }
    }

    @Override
    protected void onDestroy() {

        if (conversationCall != null) {
            conversationCall.cancel();
        }

        if (sendCall != null) {
            sendCall.cancel();
        }

        super.onDestroy();
    }
}

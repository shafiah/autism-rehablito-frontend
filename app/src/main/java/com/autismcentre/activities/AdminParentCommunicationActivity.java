
package com.autismcentre.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.autismcentre.R;
import com.autismcentre.model.AdminAssignedTherapistResponse;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminCommunicationSummaryResponse;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminParentCommunicationActivity extends BaseAdminActivity {

    private TextView tvAdminNoCommunication;

    private android.widget.EditText etAdminSearchCommunication;
    private AutoCompleteTextView actAdminCommunicationBranch;
    private AutoCompleteTextView actAdminCommunicationStatus;
    private LinearLayout adminCommunicationListContainer;

    private final List<AdminCommunicationSummaryResponse> allCommunications =
            new ArrayList<>();

    private final List<AdminBranchResponse> activeBranches =
            new ArrayList<>();

    private final List<String> branchLabels = new ArrayList<>();

    private ApiService apiService;

    private Call<List<AdminBranchResponse>> branchesCall;
    private Call<List<AdminCommunicationSummaryResponse>> communicationsCall;
    private Call<List<AdminAssignedTherapistResponse>> therapistsCall;

    private boolean communicationsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAdminPageTitle("Rehablito Autism Center");

        showAdminPageContent(
                R.layout.activity_admin_parent_communication
        );

        apiService = ApiClient.getApiService();

        etAdminSearchCommunication =
                findViewById(R.id.etAdminSearchCommunication);

        actAdminCommunicationBranch =
                findViewById(R.id.actAdminCommunicationBranch);

        actAdminCommunicationStatus =
                findViewById(R.id.actAdminCommunicationStatus);

        adminCommunicationListContainer =
                findViewById(R.id.adminCommunicationListContainer);

        tvAdminNoCommunication =
                findViewById(R.id.tvAdminNoCommunication);

        setupBranchDropdown();
        setupStatusDropdown();
        setupSearch();
    }

    private void setupBranchDropdown() {

        branchLabels.clear();
        branchLabels.add("All Branches");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                branchLabels
        );

        actAdminCommunicationBranch.setAdapter(adapter);
        actAdminCommunicationBranch.setText("All Branches", false);

        actAdminCommunicationBranch.setOnItemClickListener(
                (parent, view, position, id) -> applyFilters()
        );
    }

    private void setupStatusDropdown() {

        List<String> statuses = Arrays.asList(
                "All",
                "Unread",
                "Read"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );

        actAdminCommunicationStatus.setAdapter(adapter);
        actAdminCommunicationStatus.setText("All", false);

        actAdminCommunicationStatus.setOnItemClickListener(
                (parent, view, position, id) -> applyFilters()
        );
    }

    private void setupSearch() {

        etAdminSearchCommunication.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int before,
                            int count,
                            int after) {
                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (apiService != null) {
            loadBranches();
            loadAdminCommunications();
        }
    }

    private void loadBranches() {

        if (branchesCall != null) {
            branchesCall.cancel();
        }

        branchesCall = apiService.getAdminBranches();

        branchesCall.enqueue(
                new Callback<>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<AdminBranchResponse>> call,
                            @NonNull Response<List<AdminBranchResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            activeBranches.clear();

                            for (AdminBranchResponse branch : response.body()) {

                                if (branch != null
                                        && branch.getId() != null
                                        && Boolean.TRUE.equals(branch.getActive())
                                        && branch.getBranchName() != null
                                        && !branch.getBranchName().trim().isEmpty()) {

                                    activeBranches.add(branch);
                                }
                            }

                            rebuildBranchDropdown();
                            applyFilters();

                        } else {
                            Toast.makeText(
                                    AdminParentCommunicationActivity.this,
                                    "Unable to load branches. HTTP "
                                            + response.code(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<AdminBranchResponse>> call,
                            @NonNull Throwable t) {

                        if (call.isCanceled()
                                || isFinishing()
                                || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                AdminParentCommunicationActivity.this,
                                "Branch loading failed. Check connection.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void rebuildBranchDropdown() {

        String selectedBranch =
                actAdminCommunicationBranch.getText()
                        .toString()
                        .trim();

        branchLabels.clear();
        branchLabels.add("All Branches");

        for (AdminBranchResponse branch : activeBranches) {

            String name = branch.getBranchName();

            if (!branchLabels.contains(name)) {
                branchLabels.add(name);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                branchLabels
        );

        actAdminCommunicationBranch.setAdapter(adapter);

        if (branchLabels.contains(selectedBranch)) {
            actAdminCommunicationBranch.setText(selectedBranch, false);
        } else {
            actAdminCommunicationBranch.setText("All Branches", false);
        }
    }

    private void loadAdminCommunications() {

        communicationsLoaded = false;

        if (communicationsCall != null) {
            communicationsCall.cancel();
        }

        communicationsCall =
                apiService.getAdminCommunicationSummaries();

        communicationsCall.enqueue(
                new Callback<>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<AdminCommunicationSummaryResponse>> call,
                            @NonNull Response<List<AdminCommunicationSummaryResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null) {

                            allCommunications.clear();
                            allCommunications.addAll(response.body());

                            communicationsLoaded = true;
                            applyFilters();

                        } else {
                            showEmptyState(
                                    "Unable to load communications. HTTP "
                                            + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<AdminCommunicationSummaryResponse>> call,
                            @NonNull Throwable t) {

                        if (call.isCanceled()
                                || isFinishing()
                                || isDestroyed()) {
                            return;
                        }

                        showEmptyState(
                                "Unable to connect to the server."
                        );

                        Toast.makeText(
                                AdminParentCommunicationActivity.this,
                                "Communication loading failed.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void applyFilters() {

        if (!communicationsLoaded) {
            return;
        }

        String search = etAdminSearchCommunication.getText() == null
                ? ""
                : etAdminSearchCommunication.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        String selectedBranch =
                actAdminCommunicationBranch.getText()
                        .toString()
                        .trim();

        String selectedStatus =
                actAdminCommunicationStatus.getText()
                        .toString()
                        .trim();

        Long selectedBranchId = getSelectedBranchId(selectedBranch);

        if (!selectedBranch.equalsIgnoreCase("All Branches")
                && selectedBranchId == null) {

            renderCommunications(new ArrayList<>());
            return;
        }

        List<AdminCommunicationSummaryResponse> filtered =
                new ArrayList<>();

        for (AdminCommunicationSummaryResponse item : allCommunications) {

            if (item == null) {
                continue;
            }

            if (!isActiveBranch(item.getBranchId())) {
                continue;
            }

            if (selectedBranchId != null
                    && !selectedBranchId.equals(item.getBranchId())) {
                continue;
            }

            if (!matchesStatus(item, selectedStatus)) {
                continue;
            }

            if (!matchesSearch(item, search)) {
                continue;
            }

            filtered.add(item);
        }

        // Latest communication first (descending order).
        filtered.sort((a, b) -> {

            String dateA = a.getLatestMessageAt();
            String dateB = b.getLatestMessageAt();

            if (dateA == null || dateA.trim().isEmpty()) {
                return (dateB == null || dateB.trim().isEmpty()) ? 0 : 1;
            }

            if (dateB == null || dateB.trim().isEmpty()) {
                return -1;
            }

            return dateB.compareTo(dateA);
        });

        renderCommunications(filtered);
    }

    private Long getSelectedBranchId(String branchName) {

        if (branchName == null
                || branchName.isEmpty()
                || branchName.equalsIgnoreCase("All Branches")) {
            return null;
        }

        for (AdminBranchResponse branch : activeBranches) {

            if (branch.getBranchName().equalsIgnoreCase(branchName)) {
                return branch.getId();
            }
        }

        return null;
    }

    private boolean isActiveBranch(Long branchId) {

        if (branchId == null) {
            return false;
        }

        for (AdminBranchResponse branch : activeBranches) {

            if (branchId.equals(branch.getId())) {
                return true;
            }
        }

        return false;
    }

    // Backend unread counts determine Read / Unread status.
    private boolean matchesStatus(
            AdminCommunicationSummaryResponse item,
            String status) {

        if (status == null
                || status.isEmpty()
                || status.equalsIgnoreCase("All")) {
            return true;
        }

        boolean hasUnread = hasUnreadMessages(item);

        if (status.equalsIgnoreCase("Unread")) {
            return hasUnread;
        }

        if (status.equalsIgnoreCase("Read")) {
            return !hasUnread;
        }

        return true;
    }

    private boolean hasUnreadMessages(
            AdminCommunicationSummaryResponse item) {

        long parentUnread = getParentUnreadCount(item);
        long therapistUnread = getTherapistUnreadCount(item);

        return parentUnread > 0 || therapistUnread > 0;
    }

    private long getParentUnreadCount(
            AdminCommunicationSummaryResponse item) {

        if (item == null || item.getParentUnreadCount() == null) {
            return 0;
        }

        return Math.max(0, item.getParentUnreadCount());
    }

    private long getTherapistUnreadCount(
            AdminCommunicationSummaryResponse item) {

        if (item == null || item.getTherapistUnreadCount() == null) {
            return 0;
        }

        return Math.max(0, item.getTherapistUnreadCount());
    }

    private boolean matchesSearch(
            AdminCommunicationSummaryResponse item,
            String search) {

        if (search.isEmpty()) {
            return true;
        }

        return contains(item.getParentName(), search)
                || contains(item.getChildName(), search)
                || contains(item.getTherapistName(), search)
                || contains(item.getBranchName(), search)
                || contains(item.getLatestMessage(), search);
    }

    private boolean contains(String value, String search) {

        return value != null
                && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private void renderCommunications(
            List<AdminCommunicationSummaryResponse> communications) {

        adminCommunicationListContainer.removeAllViews();

        if (communications.isEmpty()) {
            showEmptyState("No children found for this branch.");
            return;
        }

        tvAdminNoCommunication.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (AdminCommunicationSummaryResponse item : communications) {

            View itemView = inflater.inflate(
                    R.layout.item_admin_parent_communication,
                    adminCommunicationListContainer,
                    false
            );

            TextView tvParent =
                    itemView.findViewById(
                            R.id.tvAdminCommunicationParent
                    );

            TextView tvChild =
                    itemView.findViewById(
                            R.id.tvAdminCommunicationChild
                    );

            TextView tvTherapist =
                    itemView.findViewById(
                            R.id.tvAdminCommunicationTherapist
                    );

            TextView tvBranch =
                    itemView.findViewById(
                            R.id.tvAdminCommunicationBranch
                    );

            TextView tvLastMessage =
                    itemView.findViewById(
                            R.id.tvAdminCommunicationLastMessage
                    );

            TextView tvTime =
                    itemView.findViewById(
                            R.id.tvAdminCommunicationTime
                    );

            // Two separate unread count badges.
            TextView tvParentUnread =
                    itemView.findViewById(
                            R.id.tvAdminParentUnread
                    );

            TextView tvTherapistUnread =
                    itemView.findViewById(
                            R.id.tvAdminTherapistUnread
                    );

            tvParent.setText(
                    safeText(item.getParentName(), "Unknown Parent")
            );

            tvChild.setText(
                    safeText(item.getChildName(), "Unknown Child")
            );

            tvTherapist.setText(
                    "Therapist: "
                            + safeText(item.getTherapistName(), "Not assigned")
            );

            tvBranch.setText(
                    "Branch: "
                            + safeText(item.getBranchName(), "Unknown")
            );

            tvLastMessage.setText(
                    safeText(item.getLatestMessage(), "No messages yet")
            );

            tvTime.setText(
                    formatTimestamp(item.getLatestMessageAt())
            );

            long parentUnreadCount = getParentUnreadCount(item);
            long therapistUnreadCount = getTherapistUnreadCount(item);

            tvParentUnread.setText(
                    "Parent Unread: " + parentUnreadCount
            );

            tvTherapistUnread.setText(
                    "Therapist Unread: " + therapistUnreadCount
            );

            tvParentUnread.setVisibility(
                    parentUnreadCount > 0 ? View.VISIBLE : View.GONE
            );

            tvTherapistUnread.setVisibility(
                    therapistUnreadCount > 0 ? View.VISIBLE : View.GONE
            );

            itemView.setOnClickListener(
                    v -> loadAssignedTherapists(item)
            );

            adminCommunicationListContainer.addView(itemView);
        }
    }

    private void loadAssignedTherapists(
            AdminCommunicationSummaryResponse item) {

        Long childId = item.getChildId();

        if (childId == null) {

            Toast.makeText(
                    this,
                    "Child ID not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (therapistsCall != null) {
            therapistsCall.cancel();
        }

        therapistsCall = apiService.getAdminAssignedTherapists(childId);

        therapistsCall.enqueue(
                new Callback<>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<AdminAssignedTherapistResponse>> call,
                            @NonNull Response<List<AdminAssignedTherapistResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            showTherapistSelectionDialog(
                                    item,
                                    response.body()
                            );

                        } else {

                            Toast.makeText(
                                    AdminParentCommunicationActivity.this,
                                    "No assigned active therapist found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<AdminAssignedTherapistResponse>> call,
                            @NonNull Throwable t) {

                        if (call.isCanceled()
                                || isFinishing()
                                || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                AdminParentCommunicationActivity.this,
                                "Unable to load therapists: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private void showTherapistSelectionDialog(
            AdminCommunicationSummaryResponse item,
            List<AdminAssignedTherapistResponse> therapists) {

        String[] therapistNames = new String[therapists.size()];

        for (int i = 0; i < therapists.size(); i++) {

            AdminAssignedTherapistResponse therapist = therapists.get(i);

            therapistNames[i] =
                    therapist.getTherapistName()
                            + " (" + therapist.getTherapyType() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Therapist")
                .setItems(therapistNames, (dialog, which) -> {

                    AdminAssignedTherapistResponse selected =
                            therapists.get(which);

                    openAdminConversation(item, selected);

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAdminConversation(
            AdminCommunicationSummaryResponse item,
            AdminAssignedTherapistResponse selectedTherapist) {

        Long childId = item.getChildId();

        if (childId == null) {

            Toast.makeText(
                    this,
                    "Child ID is missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (selectedTherapist == null
                || selectedTherapist.getTherapistId() == null
                || selectedTherapist.getTherapistUserId() == null) {

            Toast.makeText(
                    this,
                    "Selected therapist details are missing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Admin is only an observer.
        // Do not mark Parent or Therapist messages as read here.

        Intent intent = new Intent(
                this,
                AdminParentConversationActivity.class
        );

        intent.putExtra(
                "communication_parent",
                safeText(item.getParentName(), "")
        );

        intent.putExtra(
                "communication_child",
                safeText(item.getChildName(), "")
        );

        intent.putExtra(
                "communication_therapist",
                safeText(selectedTherapist.getTherapistName(), "")
        );

        intent.putExtra(
                "communication_branch",
                safeText(item.getBranchName(), "")
        );

        intent.putExtra("communication_child_id", childId);

        intent.putExtra(
                "communication_therapist_id",
                selectedTherapist.getTherapistId()
        );

        intent.putExtra(
                "communication_therapist_user_id",
                selectedTherapist.getTherapistUserId()
        );

        if (item.getParentUserId() != null) {

            intent.putExtra(
                    "communication_parent_user_id",
                    item.getParentUserId()
            );
        }

        startActivity(intent);
    }

    private String formatTimestamp(String timestamp) {

        if (timestamp == null || timestamp.trim().isEmpty()) {
            return "No messages yet";
        }

        String formatted = timestamp.replace('T', ' ');

        if (formatted.length() > 19) {
            formatted = formatted.substring(0, 19);
        }

        return formatted;
    }

    private String safeText(String value, String fallback) {

        return value == null || value.trim().isEmpty()
                ? fallback
                : value;
    }

    private void showEmptyState(String message) {

        adminCommunicationListContainer.removeAllViews();

        tvAdminNoCommunication.setText(message);
        tvAdminNoCommunication.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {

        if (branchesCall != null) {
            branchesCall.cancel();
        }

        if (communicationsCall != null) {
            communicationsCall.cancel();
        }

        if (therapistsCall != null) {
            therapistsCall.cancel();
        }

        super.onDestroy();
    }
}

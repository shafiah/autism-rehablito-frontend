
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.TherapistChildResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistParentChatActivity extends BaseTherapistActivity {

    private LinearLayout layoutParentChatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageTitle("Rehablito Autism Center");
        showPageContent(R.layout.activity_therapist_parent_chat);

        layoutParentChatList = findViewById(R.id.layoutParentChatList);

        loadAssignedChildren();
    }

    private void loadAssignedChildren() {

        layoutParentChatList.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading assigned children...");
        loadingText.setTextSize(15);
        loadingText.setPadding(16, 24, 16, 24);
        layoutParentChatList.addView(loadingText);

        ApiClient.getApiService().getMyTherapistChildren()
                .enqueue(new Callback<List<TherapistChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<TherapistChildResponse>> call,
                            Response<List<TherapistChildResponse>> response) {

                        layoutParentChatList.removeAllViews();

                        if (!response.isSuccessful()) {
                            showMessage("Unable to load assigned children. Error: "
                                    + response.code());
                            showEmptyMessage("Could not load children. Please try again.");
                            return;
                        }

                        List<TherapistChildResponse> children = response.body();

                        if (children == null || children.isEmpty()) {
                            showEmptyMessage(
                                    "No children are currently assigned to you.");
                            return;
                        }

                        for (TherapistChildResponse child : children) {
                            if (child != null && child.getChildId() != null) {
                                addChildCard(child);
                            }
                        }

                        if (layoutParentChatList.getChildCount() == 0) {
                            showEmptyMessage("No valid assigned children found.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistChildResponse>> call,
                            Throwable t) {

                        layoutParentChatList.removeAllViews();

                        showEmptyMessage(
                                "Unable to connect. Check your internet and try again.");

                        showMessage("Communication error: " + t.getMessage());
                    }
                });
    }

    private void addChildCard(TherapistChildResponse child) {

        MaterialCardView card = new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        cardParams.setMargins(0, 0, 0, dp(16));

        card.setLayoutParams(cardParams);
        card.setRadius(dp(12));
        card.setCardElevation(dp(2));
        card.setUseCompatPadding(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView parentName = new TextView(this);
        String actualParentName = child.getParentName();

        parentName.setText(
                actualParentName == null || actualParentName.trim().isEmpty()
                        ? "Parent name unavailable"
                        : actualParentName);

        parentName.setTextSize(18);
        parentName.setTextColor(0xFF222222);

        TextView childName = new TextView(this);
        String actualChildName = child.getChildName();

        childName.setText(
                "Child: " + (actualChildName == null
                        || actualChildName.trim().isEmpty()
                        ? "Unknown"
                        : actualChildName));

        childName.setTextSize(14);
        childName.setPadding(0, dp(6), 0, dp(12));

        MaterialButton openButton = new MaterialButton(this);
        openButton.setText("Open Conversation");
        openButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.parent_navy)
                )
        );

        openButton.setOnClickListener(v -> openConversation(
                child.getChildId(),
                actualParentName,
                actualChildName
        ));

        content.addView(parentName);
        content.addView(childName);
        content.addView(openButton);

        card.addView(content);
        layoutParentChatList.addView(card);
    }

    private void openConversation(
            Long childId,
            String parentName,
            String childName) {

        Intent intent = new Intent(
                TherapistParentChatActivity.this,
                TherapistChatConversationActivity.class);

        intent.putExtra("child_id", childId);
        intent.putExtra("parent_name",
                parentName == null ? "Parent" : parentName);
        intent.putExtra("child_name",
                childName == null ? "Child" : childName);

        startActivity(intent);
    }

    private void showEmptyMessage(String message) {

        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(dp(16), dp(24), dp(16), dp(24));

        layoutParentChatList.addView(textView);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    // =====================================================
    // CURRENT BOTTOM NAVIGATION ITEM
    // =====================================================

    @Override
    protected int getCurrentBottomNavItem() {

        return R.id.therapist_nav_home;
    }
}


package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autismcentre.R;
import com.autismcentre.adapter.ParentHomeworkAdapter;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.model.ParentHomeworkResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeworkActivity extends BaseParentActivity {

    private static final String TAG = "HomeworkActivity";

    private TextView tvHomeworkChildName;
    private TextView tvHomeworkEmpty;
    private RecyclerView rvHomework;

    private ParentHomeworkAdapter homeworkAdapter;

    private Long currentChildId;

    private final List<ParentHomeworkResponse> homeworkList =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showPageContent(R.layout.activity_homework);
        setPageTitle("Autism Centre");

        ApiClient.initialize(getApplicationContext());

        initializeViews();
        loadChild();
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.nav_homework;
    }

    private void initializeViews() {

        tvHomeworkChildName = findViewById(R.id.tvHomeworkChildName);
        tvHomeworkEmpty = findViewById(R.id.tvHomeworkEmpty);
        rvHomework = findViewById(R.id.rvHomework);

        rvHomework.setLayoutManager(
                new LinearLayoutManager(this)
        );

        homeworkAdapter = new ParentHomeworkAdapter(
                this::openHomework
        );

        rvHomework.setAdapter(homeworkAdapter);

        tvHomeworkChildName.setText("Loading child...");
        showEmptyState(false);
    }

    private void loadChild() {

        ApiClient.getApiService().getMyChildren()
                .enqueue(new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<ParentChildResponse>> call,
                            @NonNull Response<List<ParentChildResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            showError("Unable to load child profile.");
                            showEmptyState(true);
                            return;
                        }

                        List<ParentChildResponse> children = response.body();

                        if (children.isEmpty()) {
                            tvHomeworkChildName.setText("No child linked");
                            showEmptyState(true);
                            return;
                        }

                        ParentChildResponse child = children.get(0);

                        currentChildId = child.getChildId();

                        String childName = child.getChildName();

                        tvHomeworkChildName.setText(
                                isBlank(childName)
                                        ? "Child's Homework"
                                        : childName + "'s Homework"
                        );

                        if (currentChildId == null) {
                            showError("Child ID is unavailable.");
                            showEmptyState(true);
                            return;
                        }

                        loadHomework(currentChildId);
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<ParentChildResponse>> call,
                            @NonNull Throwable t) {

                        Log.e(TAG, "Failed to load child", t);
                        showError("Network error while loading child.");
                        showEmptyState(true);
                    }
                });
    }

    private void loadHomework(Long childId) {

        ApiClient.getApiService()
                .getMyChildHomework(childId)
                .enqueue(new Callback<List<ParentHomeworkResponse>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<List<ParentHomeworkResponse>> call,
                            @NonNull Response<List<ParentHomeworkResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            Log.e(TAG, "Homework API failed: " + response.code());
                            showError("Unable to load homework.");
                            showEmptyState(true);
                            return;
                        }

                        homeworkList.clear();
                        homeworkList.addAll(response.body());

                        sortHomework();
                        displayHomework();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<ParentHomeworkResponse>> call,
                            @NonNull Throwable t) {

                        Log.e(TAG, "Failed to load homework", t);
                        showError("Network error while loading homework.");
                        showEmptyState(true);
                    }
                });
    }

    private void sortHomework() {

        Collections.sort(
                homeworkList,
                new Comparator<ParentHomeworkResponse>() {
                    @Override
                    public int compare(
                            ParentHomeworkResponse first,
                            ParentHomeworkResponse second) {

                        return getSortDate(second).compareTo(
                                getSortDate(first)
                        );
                    }
                }
        );
    }

    private void displayHomework() {

        if (homeworkList.isEmpty()) {
            showEmptyState(true);
            return;
        }

        List<Object> displayItems = new ArrayList<>();

        List<ParentHomeworkResponse> occupationalList = new ArrayList<>();
        List<ParentHomeworkResponse> speechList = new ArrayList<>();
        List<ParentHomeworkResponse> otherList = new ArrayList<>();

        for (ParentHomeworkResponse homework : homeworkList) {

            String therapyType = homework.getTherapyType();

            if (isBlank(therapyType)) {
                otherList.add(homework);
                continue;
            }

            String normalizedType = therapyType.trim()
                    .toLowerCase(Locale.ROOT);

            if (normalizedType.contains("occupational")) {
                occupationalList.add(homework);

            } else if (normalizedType.contains("speech")) {
                speechList.add(homework);

            } else {
                otherList.add(homework);
            }
        }

        addSection(
                displayItems,
                "Occupational Therapy",
                occupationalList
        );

        addSection(
                displayItems,
                "Speech Therapy",
                speechList
        );

        addSection(
                displayItems,
                "Other / Not Categorized",
                otherList
        );

        homeworkAdapter.setItems(displayItems);

        showEmptyState(false);
    }

    private void addSection(
            List<Object> displayItems,
            String sectionTitle,
            List<ParentHomeworkResponse> sectionHomework) {

        if (sectionHomework.isEmpty()) {
            return;
        }

        displayItems.add(sectionTitle);
        displayItems.addAll(sectionHomework);
    }

    private void openHomework(ParentHomeworkResponse homework) {

        if (homework == null
                || homework.getHomeworkId() == null
                || homework.getChildId() == null) {

            showError("This homework is not available.");
            return;
        }

        Intent intent = new Intent(
                HomeworkActivity.this,
                HomeworkDetailActivity.class
        );

        intent.putExtra("childId", homework.getChildId());
        intent.putExtra("homeworkId", homework.getHomeworkId());

        startActivity(intent);
    }

    private String getSortDate(ParentHomeworkResponse homework) {

        if (!isBlank(homework.getAssignedDate())) {
            return homework.getAssignedDate();
        }

        return homework.getCreatedAt() == null
                ? ""
                : homework.getCreatedAt();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void showEmptyState(boolean show) {

        tvHomeworkEmpty.setVisibility(
                show ? View.VISIBLE : View.GONE
        );

        rvHomework.setVisibility(
                show ? View.GONE : View.VISIBLE
        );
    }

    private void showError(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}

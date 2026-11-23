
package com.autismcentre.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.autismcentre.R;
import com.autismcentre.model.TherapistChildResponse;
import com.autismcentre.model.TherapistHomeworkCreateRequest;
import com.autismcentre.model.TherapistHomeworkResponse;
import com.autismcentre.model.TherapistHomeworkStatusUpdateRequest;
import com.autismcentre.model.TherapistHomeworkUpdateRequest;
import com.autismcentre.api.ApiClient;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.Collections;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TherapistHomeworkActivity extends BaseTherapistActivity {

    private TextView tvHomeworkChildren;
    private Button btnSelectChildHomework;
    private Button btnViewHomework;
    private Button btnAddHomework;
    private LinearLayout homeworkContainer;


    private final List<TherapistChildResponse> assignedChildren =
            new ArrayList<>();

    private final List<TherapistHomeworkResponse> homeworkList =
            new ArrayList<>();

    private TherapistChildResponse selectedChild;
    private String selectedDueDate = "";

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_therapist_homework);
        setPageTitle("Rehablito Autism Center");
        showPageContent(R.layout.activity_therapist_homework);


        tvHomeworkChildren = findViewById(R.id.tvHomeworkChildren);
        btnSelectChildHomework = findViewById(R.id.btnSelectChildHomework);
        btnViewHomework = findViewById(R.id.btnViewHomework);
        btnAddHomework = findViewById(R.id.btnAddHomework);
        homeworkContainer = findViewById(R.id.homeworkContainer);

        // Remove the static sample card from XML.
        homeworkContainer.removeAllViews();

        btnSelectChildHomework.setOnClickListener(
                v -> showAssignedChildrenDialog());

        btnViewHomework.setOnClickListener(v -> {
            selectedChild = null;
            tvHomeworkChildren.setText("All assigned children");
            loadHomework();
        });

        btnAddHomework.setOnClickListener(v -> {
            if (assignedChildren.isEmpty()) {
                loadAssignedChildren();
                Toast.makeText(
                        this,
                        "Loading assigned children. Please try again.",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                showAddHomeworkDialog();
            }
        });

        loadAssignedChildren();
        loadHomework();
    }

    private void loadAssignedChildren() {
        ApiClient.getApiService().getMyTherapistChildren()
                .enqueue(new Callback<List<TherapistChildResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<TherapistChildResponse>> call,
                            Response<List<TherapistChildResponse>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            return;
                        }

                        assignedChildren.clear();

                        for (TherapistChildResponse child : response.body()) {
                            if (child != null
                                    && child.getChildId() != null
                                    && Boolean.TRUE.equals(child.getActive())) {
                                assignedChildren.add(child);
                            }
                        }

                        tvHomeworkChildren.setText(
                                assignedChildren.size()
                                        + " assigned child(ren)");
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistChildResponse>> call,
                            Throwable t) {
                        Toast.makeText(
                                TherapistHomeworkActivity.this,
                                "Unable to load assigned children",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void showAssignedChildrenDialog() {
        if (assignedChildren.isEmpty()) {
            Toast.makeText(
                    this,
                    "No active assigned children found",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String[] names = new String[assignedChildren.size() + 1];
        names[0] = "All assigned children";

        for (int i = 0; i < assignedChildren.size(); i++) {
            names[i + 1] = assignedChildren.get(i).getChildName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Child")
                .setItems(names, (dialog, which) -> {
                    if (which == 0) {
                        selectedChild = null;
                        tvHomeworkChildren.setText("All assigned children");
                    } else {
                        selectedChild = assignedChildren.get(which - 1);
                        tvHomeworkChildren.setText(
                                selectedChild.getChildName());
                    }
                    renderHomework();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadHomework() {
        homeworkContainer.removeAllViews();
        addMessage("Loading homework...");

        ApiClient.getApiService().getMyTherapistHomework()
                .enqueue(new Callback<List<TherapistHomeworkResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<TherapistHomeworkResponse>> call,
                            Response<List<TherapistHomeworkResponse>> response) {

                        homeworkContainer.removeAllViews();

                        if (!response.isSuccessful()
                                || response.body() == null) {
                            addMessage("Unable to load homework. Please retry.");
                            return;
                        }

                        homeworkList.clear();
                        homeworkList.addAll(response.body());
                        renderHomework();
                    }

                    @Override
                    public void onFailure(
                            Call<List<TherapistHomeworkResponse>> call,
                            Throwable t) {
                        homeworkContainer.removeAllViews();
                        addMessage("Network error. Please try again.");
                    }
                });
    }

    private void renderHomework() {
        homeworkContainer.removeAllViews();

        List<TherapistHomeworkResponse> filtered = new ArrayList<>();

        for (TherapistHomeworkResponse homework : homeworkList) {
            if (homework == null) {
                continue;
            }

            if (selectedChild == null
                    || (homework.getChildId() != null
                    && homework.getChildId().equals(
                    selectedChild.getChildId()))) {
                filtered.add(homework);
            }
        }

        if (filtered.isEmpty()) {
            addMessage("No homework found for this selection.");
            return;
        }

        // Latest Due Date first (Descending Order)
        Collections.sort(filtered, (first, second) ->
                compareDueDates(
                        second.getDueDate(),
                        first.getDueDate()
                )
        );

        for (TherapistHomeworkResponse homework : filtered) {
            homeworkContainer.addView(createHomeworkCard(homework));
        }
    }

    private int compareDueDates(String firstDate, String secondDate) {

        try {
            Date first = parseHomeworkDate(firstDate);
            Date second = parseHomeworkDate(secondDate);

            if (first == null && second == null) return 0;
            if (first == null) return 1;
            if (second == null) return -1;

            return first.compareTo(second);

        } catch (Exception e) {
            return 0;
        }
    }

    private Date parseHomeworkDate(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String date = value.trim();

        if (date.length() >= 10) {
            date = date.substring(0, 10);
        }

        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
    private View createHomeworkCard(TherapistHomeworkResponse homework) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundColor(0xFFFFFFFF);
        card.setElevation(dp(3));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(dp(4), dp(6), dp(4), dp(12));
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText(safe(homework.getTitle()));
        title.setTextSize(18);
        title.setTextColor(0xFF25233A);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(title);

        TextView child = new TextView(this);
        child.setText("Child: " + safe(homework.getChildName()));
        child.setTextSize(14);
        child.setTextColor(0xFF666477);
        child.setPadding(0, dp(6), 0, 0);
        card.addView(child);

        TextView dueDate = new TextView(this);
        dueDate.setText("Due date: " + safe(homework.getDueDate()));
        dueDate.setTextSize(13);
        dueDate.setTextColor(0xFF666477);
        dueDate.setPadding(0, dp(4), 0, 0);
        card.addView(dueDate);

        TextView status = new TextView(this);
        status.setText("Status: " + formatStatus(homework.getStatus()));
        status.setTextSize(13);
        status.setTypeface(null, android.graphics.Typeface.BOLD);
        status.setTextColor(statusColor(homework.getStatus()));
        status.setPadding(0, dp(8), 0, dp(8));
        card.addView(status);

        TextView instructions = new TextView(this);
        String description = safe(homework.getInstructions());
        if (description.length() > 140) {
            description = description.substring(0, 137) + "...";
        }
        instructions.setText(description);
        instructions.setTextSize(14);
        instructions.setTextColor(0xFF48465A);
        card.addView(instructions);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(12), 0, 0);

        Button viewButton = makeButton("View");
        viewButton.setOnClickListener(v -> showHomeworkDetails(homework));
        actions.addView(viewButton, buttonParams());

        // Add/Edit Therapist Remarks button
        Button remarksButton = makeButton(
                homework.getTherapistRemarks() == null
                        || homework.getTherapistRemarks().trim().isEmpty()
                        ? "Add Remarks"
                        : "Edit Remarks"
        );

        remarksButton.setOnClickListener(
                v -> showTherapistRemarksDialog(homework)
        );

        actions.addView(remarksButton, buttonParams());

        boolean completed = isCompleted(homework);

        if (!completed) {
            Button editButton = makeButton("Edit");
            editButton.setOnClickListener(v -> showEditHomeworkDialog(homework));
            actions.addView(editButton, buttonParams());

            Button deleteButton = makeButton("Delete");
            deleteButton.setOnClickListener(v -> confirmDeleteHomework(homework));
            actions.addView(deleteButton, buttonParams());

            Button statusButton = makeButton(
                    isInProgress(homework) ? "Complete" : "Start");

            statusButton.setOnClickListener(v -> updateHomeworkStatus(
                    homework,
                    isInProgress(homework) ? "COMPLETED" : "IN_PROGRESS"));

            actions.addView(statusButton, buttonParams());
        }

        card.addView(actions);
        return card;
    }

    private void showAddHomeworkDialog() {
        if (assignedChildren.isEmpty()) {
            Toast.makeText(this, "No assigned children available",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout form = createFormLayout();

        EditText etChild = createInput("Select child", false);

        Spinner spinnerTherapyType = new Spinner(this);

        String[] therapyTypes = {
                "Select therapy type",
                "Occupational Therapy",
                "Speech Therapy"
        };

        ArrayAdapter<String> therapyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                therapyTypes
        );

        therapyAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTherapyType.setAdapter(therapyAdapter);

        EditText etTitle = createInput("Homework title", false);
        EditText etInstructions = createInput("Homework instructions", true);
        EditText etDueDate = createInput("Select due date", false);
        etChild.setFocusable(false);
        etDueDate.setFocusable(false);

        form.addView(etChild);
        form.addView(spinnerTherapyType);
        form.addView(etTitle);
        form.addView(etInstructions);
        form.addView(etDueDate);
        final TherapistChildResponse[] chosenChild = {null};
        Calendar initialDate = Calendar.getInstance();
        initialDate.add(Calendar.DAY_OF_MONTH, 7);
        final String[] dueDateValue = {
                dateFormat.format(initialDate.getTime())
        };

        etChild.setOnClickListener(v -> {
            String[] names = new String[assignedChildren.size()];
            for (int i = 0; i < assignedChildren.size(); i++) {
                names[i] = assignedChildren.get(i).getChildName();
            }

            new AlertDialog.Builder(this)
                    .setTitle("Select Child")
                    .setItems(names, (d, which) -> {
                        chosenChild[0] = assignedChildren.get(which);
                        etChild.setText(chosenChild[0].getChildName());
                    })
                    .show();
        });

        etDueDate.setText(dueDateValue[0]);
        etDueDate.setOnClickListener(v -> showDatePicker(
                dueDateValue[0], value -> {
                    dueDateValue[0] = value;
                    etDueDate.setText(value);
                }));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Assign Homework")
                .setView(wrapForm(form))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Assign", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(
                AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (chosenChild[0] == null) {
                etChild.setError("Select a child");
                return;
            }
            if (spinnerTherapyType.getSelectedItemPosition() == 0) {
                Toast.makeText(
                        TherapistHomeworkActivity.this,
                        "Please select therapy type",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String selectedTherapyType =
                    spinnerTherapyType.getSelectedItem().toString();

            if (etTitle.getText().toString().trim().isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }
            if (etInstructions.getText().toString().trim().isEmpty()) {
                etInstructions.setError("Instructions are required");
                return;
            }

            TherapistHomeworkCreateRequest request =
                    new TherapistHomeworkCreateRequest(
                            etTitle.getText().toString().trim(),
                            etInstructions.getText().toString().trim(),
                            dueDateValue[0],
                            null,
                            selectedTherapyType
                    );
            createHomework(chosenChild[0].getChildId(), request, dialog);
        }));

        dialog.show();
    }

    private void showEditHomeworkDialog(
            TherapistHomeworkResponse homework) {

        LinearLayout form = createFormLayout();

        EditText etTitle = createInput("Homework title", false);
        EditText etInstructions = createInput("Homework instructions", true);
        EditText etDueDate = createInput("Select due date", false);

        etTitle.setText(safe(homework.getTitle()));
        etInstructions.setText(safe(homework.getInstructions()));

        String initialDueDate = homework.getDueDate();
        if (initialDueDate == null || initialDueDate.trim().isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            initialDueDate = dateFormat.format(calendar.getTime());
        }

        final String[] dueDateValue = {initialDueDate};
        etDueDate.setText(initialDueDate);
        etDueDate.setFocusable(false);
        etDueDate.setOnClickListener(v -> showDatePicker(
                dueDateValue[0], value -> {
                    dueDateValue[0] = value;
                    etDueDate.setText(value);
                }));

        form.addView(etTitle);
        form.addView(etInstructions);
        form.addView(etDueDate);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Homework")
                .setView(wrapForm(form))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Changes", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(
                AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String instructions = etInstructions.getText().toString().trim();

            if (title.isEmpty()) {
                etTitle.setError("Title is required");
                return;
            }
            if (instructions.isEmpty()) {
                etInstructions.setError("Instructions are required");
                return;
            }

            TherapistHomeworkUpdateRequest request =
                    new TherapistHomeworkUpdateRequest(
                            title, instructions, dueDateValue[0]);

            ApiClient.getApiService().updateTherapistHomework(
                            homework.getHomeworkId(), request)
                    .enqueue(new Callback<TherapistHomeworkResponse>() {
                        @Override
                        public void onResponse(
                                Call<TherapistHomeworkResponse> call,
                                Response<TherapistHomeworkResponse> response) {
                            if (response.isSuccessful()) {
                                dialog.dismiss();
                                Toast.makeText(
                                        TherapistHomeworkActivity.this,
                                        "Homework updated successfully",
                                        Toast.LENGTH_SHORT).show();
                                loadHomework();
                            } else {
                                showApiError("Unable to update homework",
                                        response.code());
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<TherapistHomeworkResponse> call,
                                Throwable t) {
                            Toast.makeText(
                                    TherapistHomeworkActivity.this,
                                    "Network error while updating homework",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }));

        dialog.show();
    }

    private void confirmDeleteHomework(
            TherapistHomeworkResponse homework) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Homework")
                .setMessage("Delete \"" + safe(homework.getTitle())
                        + "\"? This action cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) ->
                        deleteHomework(homework))
                .show();
    }

    private void deleteHomework(TherapistHomeworkResponse homework) {
        ApiClient.getApiService().deleteTherapistHomework(
                        homework.getHomeworkId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    TherapistHomeworkActivity.this,
                                    "Homework deleted successfully",
                                    Toast.LENGTH_SHORT).show();
                            loadHomework();
                        } else {
                            showApiError("Unable to delete homework",
                                    response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(
                                TherapistHomeworkActivity.this,
                                "Network error while deleting homework",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createHomework(
            Long childId,
            TherapistHomeworkCreateRequest request,
            AlertDialog dialog) {

        ApiClient.getApiService().createTherapistHomework(childId, request)
                .enqueue(new Callback<TherapistHomeworkResponse>() {
                    @Override
                    public void onResponse(
                            Call<TherapistHomeworkResponse> call,
                            Response<TherapistHomeworkResponse> response) {
                        if (response.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(
                                    TherapistHomeworkActivity.this,
                                    "Homework assigned successfully",
                                    Toast.LENGTH_SHORT).show();
                            loadHomework();
                        } else {
                            showApiError("Unable to assign homework",
                                    response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistHomeworkResponse> call,
                            Throwable t) {
                        Toast.makeText(
                                TherapistHomeworkActivity.this,
                                "Network error while assigning homework",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateHomeworkStatus(
            TherapistHomeworkResponse homework,
            String newStatus) {

        TherapistHomeworkStatusUpdateRequest request =
                new TherapistHomeworkStatusUpdateRequest(newStatus);

        ApiClient.getApiService().updateTherapistHomeworkStatus(
                        homework.getHomeworkId(), request)
                .enqueue(new Callback<TherapistHomeworkResponse>() {
                    @Override
                    public void onResponse(
                            Call<TherapistHomeworkResponse> call,
                            Response<TherapistHomeworkResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    TherapistHomeworkActivity.this,
                                    "Homework status updated",
                                    Toast.LENGTH_SHORT).show();
                            loadHomework();
                        } else {
                            showApiError("Unable to update status",
                                    response.code());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistHomeworkResponse> call,
                            Throwable t) {
                        Toast.makeText(
                                TherapistHomeworkActivity.this,
                                "Network error while updating status",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showHomeworkDetails(
            TherapistHomeworkResponse homework) {

        String details =
                "Homework: " + safe(homework.getTitle())
                        + "\n\nChild: " + safe(homework.getChildName())
                        + "\nParent: " + safe(homework.getParentName())
                        + "\n\nInstructions:\n" + safe(homework.getInstructions())
                        + "\n\nAssigned date: " + safe(homework.getAssignedDate())
                        + "\nDue date: " + safe(homework.getDueDate())
                        + "\nStatus: " + formatStatus(homework.getStatus())
                        + "\n\nTherapist remarks: "
                        + safe(homework.getTherapistRemarks())
                        + "\n\nParent remarks: "
                        + safe(homework.getParentRemarks());

        TextView message = new TextView(this);
        message.setText(details);
        message.setTextSize(15);
        message.setTextColor(0xFF302E40);
        message.setPadding(dp(20), dp(12), dp(20), dp(12));

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(message);

        new AlertDialog.Builder(this)
                .setTitle("Homework Details")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .show();
    }


    private void showTherapistRemarksDialog(
            TherapistHomeworkResponse homework) {

        LinearLayout form = createFormLayout();

        EditText etRemarks = createInput(
                "Enter therapist remarks",
                true
        );

        etRemarks.setMinLines(4);
        etRemarks.setMaxLines(8);
        etRemarks.setText(
                homework.getTherapistRemarks() == null
                        ? ""
                        : homework.getTherapistRemarks()
        );

        form.addView(etRemarks);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Therapist Remarks")
                .setView(wrapForm(form))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Remarks", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String remarks = etRemarks.getText()
                                    .toString()
                                    .trim();

                            if (remarks.isEmpty()) {
                                etRemarks.setError("Please enter therapist remarks");
                                return;
                            }

                            if (remarks.length() > 2000) {
                                etRemarks.setError(
                                        "Remarks cannot exceed 2000 characters"
                                );
                                return;
                            }

                            if (homework.getHomeworkId() == null) {
                                Toast.makeText(
                                        TherapistHomeworkActivity.this,
                                        "Invalid homework ID",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            saveTherapistRemarks(homework, remarks, dialog);
                        }));

        dialog.show();
    }


    private void saveTherapistRemarks(
            TherapistHomeworkResponse homework,
            String remarks,
            AlertDialog dialog) {

        com.autismcentre.model.TherapistHomeworkRemarksRequest request =
                new com.autismcentre.model.TherapistHomeworkRemarksRequest(
                        remarks
                );

        ApiClient.getApiService().updateTherapistHomeworkRemarks(
                        homework.getHomeworkId(),
                        request
                )
                .enqueue(new Callback<TherapistHomeworkResponse>() {

                    @Override
                    public void onResponse(
                            Call<TherapistHomeworkResponse> call,
                            Response<TherapistHomeworkResponse> response) {

                        if (response.isSuccessful()) {

                            dialog.dismiss();

                            Toast.makeText(
                                    TherapistHomeworkActivity.this,
                                    "Therapist remarks saved successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadHomework();

                        } else {
                            showApiError(
                                    "Unable to save therapist remarks",
                                    response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TherapistHomeworkResponse> call,
                            Throwable t) {

                        Toast.makeText(
                                TherapistHomeworkActivity.this,
                                "Network error while saving remarks",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }


    private interface DateSelectedListener {
        void onDateSelected(String date);
    }

    private void showDatePicker(
            String currentDate,
            DateSelectedListener listener) {

        Calendar calendar = Calendar.getInstance();

        try {
            Date parsedDate = dateFormat.parse(currentDate);
            if (parsedDate != null) {
                calendar.setTime(parsedDate);
            }
        } catch (ParseException ignored) {
            // Keep today's date as the fallback.
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    listener.onDateSelected(
                            dateFormat.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000);

        picker.show();
    }

    private LinearLayout createFormLayout() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), dp(8));
        return form;
    }

    private View wrapForm(LinearLayout form) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(form);
        return scrollView;
    }

    private EditText createInput(String hint, boolean multiline) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(15);
        input.setSingleLine(!multiline);

        if (multiline) {
            input.setGravity(Gravity.TOP | Gravity.START);
            input.setMinLines(3);
            input.setInputType(
                    InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(4), 0, dp(8));
        input.setLayoutParams(params);

        return input;
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(11);
        button.setAllCaps(false);
        button.setPadding(dp(4), 0, dp(4), 0);
        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(2), 0, dp(2), 0);
        return params;
    }

    private void addMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(0xFF666477);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(dp(16), dp(24), dp(16), dp(24));
        homeworkContainer.addView(textView);
    }

    private String formatStatus(String status) {
        if (status == null) {
            return "Unknown";
        }

        switch (status.toUpperCase(Locale.US)) {
            case "IN_PROGRESS":
                return "In Progress";
            case "COMPLETED":
                return "Completed";
            case "ASSIGNED":
                return "Assigned";
            default:
                return status.replace('_', ' ');
        }
    }

    private boolean isCompleted(TherapistHomeworkResponse homework) {
        return homework.getStatus() != null
                && "COMPLETED".equalsIgnoreCase(homework.getStatus());
    }

    private boolean isInProgress(TherapistHomeworkResponse homework) {
        return homework.getStatus() != null
                && "IN_PROGRESS".equalsIgnoreCase(homework.getStatus());
    }

    private int statusColor(String status) {
        if (status == null) {
            return 0xFF666477;
        }

        if ("COMPLETED".equalsIgnoreCase(status)) {
            return 0xFF218838;
        }
        if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            return 0xFFB36B00;
        }
        return 0xFF6554C0;
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not available" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private void showApiError(String message, int statusCode) {
        Toast.makeText(
                this,
                message + " (HTTP " + statusCode + ")",
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected int getCurrentBottomNavItem() {
        return R.id.therapist_nav_home;
    }
}

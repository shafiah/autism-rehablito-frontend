
package com.autismcentre.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import com.autismcentre.R;
import com.autismcentre.model.AdminBranchResponse;
import com.autismcentre.model.AdminChildResponse;
import com.autismcentre.model.AdminTherapistResponse;
import com.autismcentre.model.AdminSessionCreateRequest;
import com.autismcentre.model.AdminSessionResponse;
import com.autismcentre.repository.AdminSessionRepository;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddSessionActivity extends BaseAdminActivity {

    private MaterialAutoCompleteTextView actBranch, actChild,
            actTherapist, actTherapy, actStatus;

    private TextInputEditText etDate, etTime, etEndTime, etRemarks;
    private MaterialButton btnSave;

    private final List<AdminBranchResponse> branches = new ArrayList<>();
    private final List<AdminChildResponse> allChildren = new ArrayList<>();
    private final List<AdminChildResponse> filteredChildren = new ArrayList<>();
    private final List<AdminTherapistResponse> therapists = new ArrayList<>();

    private AdminBranchResponse selectedBranch;
    private AdminChildResponse selectedChild;
    private AdminTherapistResponse selectedTherapist;

    private AdminSessionRepository sessionRepository;

    private String sessionDateForApi = "";
    private String startTimeForApi = "";
    private String endTimeForApi = "";

    private boolean loadingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_session);

        sessionRepository = new AdminSessionRepository();

        actBranch = findViewById(R.id.actAdminAddSessionBranch);
        actChild = findViewById(R.id.actAdminAddSessionChild);
        actTherapist = findViewById(R.id.actAdminAddSessionTherapist);
        actTherapy = findViewById(R.id.actAdminAddSessionTherapy);
        actStatus = findViewById(R.id.actAdminAddSessionStatus);

        etDate = findViewById(R.id.etAdminAddSessionDate);
        etTime = findViewById(R.id.etAdminAddSessionTime);
        etEndTime = findViewById(R.id.etAdminAddSessionEndTime);
        etRemarks = findViewById(R.id.etAdminAddSessionRemarks);

        btnSave = findViewById(R.id.btnAdminSaveSession);

        // Status is controlled by the backend on creation.
        if (actStatus != null) {
            actStatus.setVisibility(View.GONE);
        }

        if (actTherapy != null) {
            actTherapy.setKeyListener(null);
            actTherapy.setFocusable(false);
            actTherapy.setOnClickListener(null);
        }

        setupPickers();
        setupDropdownListeners();
        setupSaveButton();

        loadBranches();
        loadChildren();
    }

    private void setupPickers() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth);

                        sessionDateForApi = new SimpleDateFormat(
                                "yyyy-MM-dd", Locale.US
                        ).format(chosen.getTime());

                        etDate.setText(new SimpleDateFormat(
                                "dd MMM yyyy", Locale.getDefault()
                        ).format(chosen.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.show();
        });

        etTime.setOnClickListener(v ->
                showTimePicker(true));

        etEndTime.setOnClickListener(v ->
                showTimePicker(false));
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();

        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String apiTime = String.format(
                            Locale.US, "%02d:%02d:00",
                            hourOfDay, minute
                    );

                    Calendar chosen = Calendar.getInstance();
                    chosen.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    chosen.set(Calendar.MINUTE, minute);

                    String displayTime = new SimpleDateFormat(
                            "hh:mm a", Locale.getDefault()
                    ).format(chosen.getTime());

                    if (isStartTime) {
                        startTimeForApi = apiTime;
                        etTime.setText(displayTime);
                    } else {
                        endTimeForApi = apiTime;
                        etEndTime.setText(displayTime);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        ).show();
    }

    private void setupDropdownListeners() {
        actBranch.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= branches.size()) return;

            selectedBranch = branches.get(position);
            selectedChild = null;
            selectedTherapist = null;

            actChild.setText("", false);
            actTherapist.setText("", false);
            actTherapy.setText("", false);

            filteredChildren.clear();

            for (AdminChildResponse child : allChildren) {
                if (isActive(child)
                        && sameId(getChildBranchId(child),
                        selectedBranch.getId())) {
                    filteredChildren.add(child);
                }
            }

            List<String> childNames = new ArrayList<>();
            for (AdminChildResponse child : filteredChildren) {
                childNames.add(child.getChildName());
            }

            setAdapter(actChild, childNames);

            therapists.clear();
            setAdapter(actTherapist, new ArrayList<>());

            loadTherapists(selectedBranch.getId());
        });

        actChild.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= filteredChildren.size()) return;

            selectedChild = filteredChildren.get(position);

            // Clear therapist selection when child changes.
            selectedTherapist = null;
            actTherapist.setText("", false);
            actTherapy.setText("", false);
        });

        actTherapist.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= therapists.size()) return;

            selectedTherapist = therapists.get(position);

            String therapyType = readString(
                    selectedTherapist,
                    "getTherapyType",
                    "getTherapyName",
                    "getSpecialization"
            );

            actTherapy.setText(
                    therapyType == null ? "" : therapyType,
                    false
            );
        });
    }

    private void loadBranches() {
        loadingData = true;

        com.autismcentre.api.ApiClient.getApiService()
                .getAdminBranches()
                .enqueue(new Callback<List<AdminBranchResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<AdminBranchResponse>> call,
                            Response<List<AdminBranchResponse>> response) {

                        loadingData = false;

                        if (!response.isSuccessful() || response.body() == null) {
                            showToast("Branches load nahi ho sake.");
                            return;
                        }

                        branches.clear();

                        for (AdminBranchResponse branch : response.body()) {
                            if (branch.getActive() == null
                                    || Boolean.TRUE.equals(branch.getActive())) {
                                branches.add(branch);
                            }
                        }

                        List<String> names = new ArrayList<>();
                        for (AdminBranchResponse branch : branches) {
                            names.add(branch.getBranchName());
                        }

                        setAdapter(actBranch, names);
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminBranchResponse>> call,
                            Throwable t) {
                        loadingData = false;
                        showToast("Branch API error: " + t.getMessage());
                    }
                });
    }

    private void loadChildren() {
        com.autismcentre.api.ApiClient.getApiService()
                .getAdminChildren()
                .enqueue(new Callback<List<AdminChildResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<AdminChildResponse>> call,
                            Response<List<AdminChildResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            showToast("Children load nahi ho sake.");
                            return;
                        }

                        allChildren.clear();
                        allChildren.addAll(response.body());

                        // If a branch was selected before the response arrived,
                        // refresh the child list for that branch.
                        if (selectedBranch != null) {
                            refreshChildrenForSelectedBranch();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminChildResponse>> call,
                            Throwable t) {
                        showToast("Children API error: " + t.getMessage());
                    }
                });
    }

    private void refreshChildrenForSelectedBranch() {
        filteredChildren.clear();

        for (AdminChildResponse child : allChildren) {
            if (isActive(child)
                    && sameId(getChildBranchId(child),
                    selectedBranch.getId())) {
                filteredChildren.add(child);
            }
        }

        List<String> names = new ArrayList<>();
        for (AdminChildResponse child : filteredChildren) {
            names.add(child.getChildName());
        }

        setAdapter(actChild, names);
    }

    private void loadTherapists(Long branchId) {
        if (branchId == null) return;

        com.autismcentre.api.ApiClient.getApiService()
                .getAdminTherapists(branchId)
                .enqueue(new Callback<List<AdminTherapistResponse>>() {
                    @Override
                    public void onResponse(
                            Call<List<AdminTherapistResponse>> call,
                            Response<List<AdminTherapistResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) {
                            showToast("Therapists load nahi ho sake.");
                            return;
                        }

                        therapists.clear();

                        for (AdminTherapistResponse therapist : response.body()) {
                            Object activeValue = readValue(
                                    therapist, "getActive", "isActive"
                            );

                            boolean active = activeValue == null
                                    || Boolean.TRUE.equals(activeValue);

                            if (active) {
                                therapists.add(therapist);
                            }
                        }

                        List<String> names = new ArrayList<>();
                        for (AdminTherapistResponse therapist : therapists) {
                            String name = readString(
                                    therapist,
                                    "getTherapistName",
                                    "getName",
                                    "getFullName"
                            );

                            names.add(name == null
                                    ? "Therapist"
                                    : name);
                        }

                        setAdapter(actTherapist, names);
                    }

                    @Override
                    public void onFailure(
                            Call<List<AdminTherapistResponse>> call,
                            Throwable t) {
                        showToast("Therapist API error: " + t.getMessage());
                    }
                });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> saveSession());
    }

    private void saveSession() {
        if (loadingData) {
            showToast("Data load ho raha hai. Thoda wait karo.");
            return;
        }

        if (selectedBranch == null) {
            showToast("Branch select karo.");
            return;
        }

        if (selectedChild == null) {
            showToast("Child select karo.");
            return;
        }

        if (selectedTherapist == null) {
            showToast("Therapist select karo.");
            return;
        }

        if (sessionDateForApi.isEmpty()) {
            showToast("Session date select karo.");
            return;
        }

        if (startTimeForApi.isEmpty() || endTimeForApi.isEmpty()) {
            showToast("Start Time aur End Time select karo.");
            return;
        }

        if (startTimeForApi.compareTo(endTimeForApi) >= 0) {
            showToast("End Time, Start Time ke baad hona chahiye.");
            return;
        }

        Long therapistId = readLong(
                selectedTherapist,
                "getTherapistId",
                "getId",
                "getUserId"
        );

        if (therapistId == null) {
            showToast("Therapist ID nahi mili. Therapist model check karna hoga.");
            return;
        }

        String remarks = etRemarks.getText() == null
                ? ""
                : etRemarks.getText().toString().trim();

        AdminSessionCreateRequest request = new AdminSessionCreateRequest(
                selectedChild.getChildId(),
                therapistId,
                selectedBranch.getId(),
                sessionDateForApi,
                startTimeForApi,
                endTimeForApi,
                remarks
        );

        btnSave.setEnabled(false);

        sessionRepository.createSession(
                request,
                new Callback<AdminSessionResponse>() {
                    @Override
                    public void onResponse(
                            Call<AdminSessionResponse> call,
                            Response<AdminSessionResponse> response) {

                        btnSave.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            showToast("Session successfully create ho gaya.");
                            finish();
                        } else {
                            showToast(
                                    "Session create nahi hua. HTTP "
                                            + response.code()
                                            + ". Child/therapist assignment aur branch check karo."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AdminSessionResponse> call,
                            Throwable t) {
                        btnSave.setEnabled(true);
                        showToast("Network/API error: " + t.getMessage());
                    }
                }
        );
    }

    private void setAdapter(
            MaterialAutoCompleteTextView view,
            List<String> values) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                values
        );

        view.setAdapter(adapter);
    }

    private boolean isActive(AdminChildResponse child) {
        return child.getActive() == null
                || Boolean.TRUE.equals(child.getActive());
    }

    private Long getChildBranchId(AdminChildResponse child) {
        return child.getBranchId();
    }

    private boolean sameId(Long first, Long second) {
        return first != null && first.equals(second);
    }

    private Object readValue(Object object, String... getterNames) {
        if (object == null) return null;

        for (String getter : getterNames) {
            try {
                Method method = object.getClass().getMethod(getter);
                return method.invoke(object);
            } catch (Exception ignored) {
                // Try the next possible getter name.
            }
        }

        return null;
    }

    private String readString(Object object, String... getterNames) {
        Object value = readValue(object, getterNames);
        return value == null ? null : String.valueOf(value);
    }

    private Long readLong(Object object, String... getterNames) {
        Object value = readValue(object, getterNames);

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private void showToast(String message) {
        Toast.makeText(
                AdminAddSessionActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}

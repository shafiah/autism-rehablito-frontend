package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.autismcentre.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import android.view.View;
import android.widget.TextView;

import android.widget.Toast;

import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.autismcentre.utils.SessionManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseParentActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar topAppBar;
    protected BottomNavigationView bottomNavigation;
    protected ImageButton btnNotifications;
    private TextView tvUnreadNotificationBadge;

    private ApiService notificationApiService;

    private Call<JsonObject> unreadCountCall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================================================
        // COMMON PARENT LAYOUT
        // =====================================================

        setContentView(R.layout.app_bar_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnNotifications = findViewById(R.id.btnNotifications);
        tvUnreadNotificationBadge =
                findViewById(R.id.tvUnreadNotificationBadge);

        ApiClient.initialize(getApplicationContext());

        notificationApiService = ApiClient.getApiService();

        // =====================================================
        // TOOLBAR
        // =====================================================

        setSupportActionBar(topAppBar);

        // =====================================================
                // COMMON NOTIFICATION MENU
        // =====================================================

        // =====================================================
// COMMON NOTIFICATION
// =====================================================

        btnNotifications.setOnClickListener(v -> {

            startActivity(new Intent(
                    this,
                    NotificationsActivity.class
            ));

        });

        // =====================================================
        // COMMON HAMBURGER / DRAWER
        // =====================================================

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        topAppBar,
                        R.string.open_drawer,
                        R.string.close_drawer
                );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // =========================================
// PARENT DRAWER PROFILE DETAILS
// =========================================

        if (navigationView.getHeaderCount() > 0) {

            View headerView = navigationView.getHeaderView(0);

            TextView tvParentName =
                    headerView.findViewById(R.id.tvParentName);

            TextView tvParentEmail =
                    headerView.findViewById(R.id.tvParentEmail);

            SessionManager sessionManager =
                    new SessionManager(this);

            String parentName = sessionManager.getName();
            String parentEmail = sessionManager.getEmail();

            tvParentName.setText(
                    parentName != null && !parentName.trim().isEmpty()
                            ? parentName
                            : "Parent"
            );

            tvParentEmail.setText(
                    parentEmail != null && !parentEmail.trim().isEmpty()
                            ? parentEmail
                            : "Email not available"
            );
        }

        // =====================================================
        // COMMON BOTTOM NAVIGATION
        // =====================================================

        bottomNavigation.setLabelVisibilityMode(
                BottomNavigationView.LABEL_VISIBILITY_LABELED
        );

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            // ================= HOME =================

            if (id == R.id.nav_home) {

                if (getCurrentBottomNavItem() != R.id.nav_home) {
                    openParentHome();
                }
            }
            // ================= SCHEDULE =================

            if (id == R.id.nav_schedule) {

                if (getCurrentBottomNavItem() != R.id.nav_schedule) {

                    startActivity(new Intent(
                            this,
                            ScheduleActivity.class
                    ));

                    finish();
                }

                return true;
            }

            // ================= PROGRESS =================

            if (id == R.id.nav_progress) {

                if (getCurrentBottomNavItem() != R.id.nav_progress) {

                    startActivity(new Intent(
                            this,
                            ProgressActivity.class
                    ));

                    finish();
                }

                return true;
            }

            // ================= PROFILE =================

            if (id == R.id.nav_profile) {

                if (getCurrentBottomNavItem() != R.id.nav_profile) {
                    openChildProfile();
                }

                return true;
            }

            return false;
        });

        // =====================================================
        // SELECT CURRENT PAGE
        // =====================================================

        bottomNavigation.setSelectedItemId(
                getCurrentBottomNavItem()
        );

        // =====================================================
        // STATUS BAR
        // =====================================================

        getWindow().setStatusBarColor(
                getColor(R.color.primary)
        );

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.M) {

            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(0);
        }
    }

    // =====================================================
    // EACH CHILD ACTIVITY WILL TELL ITS CURRENT PAGE
    // =====================================================

    protected abstract int getCurrentBottomNavItem();

    // =====================================================
    // PAGE TITLE
    // =====================================================

    protected void setPageTitle(String title) {

        topAppBar.setTitle(title);
    }

    // =====================================================
    // PAGE CONTENT
    // =====================================================

    protected void showPageContent(@LayoutRes int layoutRes) {

        getLayoutInflater().inflate(
                layoutRes,
                findViewById(R.id.contentContainer),
                true
        );
    }

    private void openChildProfile() {

        ParentChildRepository repository =
                new ParentChildRepository();

        repository.getMyChildren(
                new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            ParentChildResponse child =
                                    response.body().get(0);

                            Intent intent = new Intent(
                                    BaseParentActivity.this,
                                    ChildProfileActivity.class
                            );

                            intent.putExtra(
                                    "childId",
                                    child.getChildId()
                            );

                            startActivity(intent);

                        } else {
                            Toast.makeText(
                                    BaseParentActivity.this,
                                    "Linked child details nahi mile.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t
                    ) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                BaseParentActivity.this,
                                "Child details load nahi ho paayi.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =====================================================
// OPEN PARENT HOME WITH LINKED CHILD ID
// =====================================================

    private void openParentHome() {

        ParentChildRepository repository =
                new ParentChildRepository();

        repository.getMyChildren(
                new Callback<List<ParentChildResponse>>() {

                    @Override
                    public void onResponse(
                            Call<List<ParentChildResponse>> call,
                            Response<List<ParentChildResponse>> response) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            ParentChildResponse child =
                                    response.body().get(0);

                            Long linkedChildId = child.getChildId();

                            if (linkedChildId == null || linkedChildId <= 0) {

                                Toast.makeText(
                                        BaseParentActivity.this,
                                        "Child details not available.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            Intent intent = new Intent(
                                    BaseParentActivity.this,
                                    ParentHomeDashboardActivity.class
                            );

                            intent.putExtra("childId", linkedChildId);

                            startActivity(intent);

                            finish();

                        } else {

                            Toast.makeText(
                                    BaseParentActivity.this,
                                    "Linked child details nahi mile.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<ParentChildResponse>> call,
                            Throwable t) {

                        if (isFinishing() || isDestroyed()) {
                            return;
                        }

                        Toast.makeText(
                                BaseParentActivity.this,
                                "Child details load nahi ho paayi.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =====================================================
    // DRAWER MENU
    // =====================================================

    @Override
    public boolean onNavigationItemSelected(
            @NonNull MenuItem item) {

        int id = item.getItemId();

        // ================= HOME =================

        if (id == R.id.nav_home) {

            if (getCurrentBottomNavItem() != R.id.nav_home) {
                openParentHome();
            }

            return true;
        }
        // ================= CHILD PROFILE =================

        else if (id == R.id.nav_child_profile) {

            openChildProfile();
        }
        // ================= SCHEDULE =================

        else if (id == R.id.nav_schedule) {

            startActivity(new Intent(
                    this,
                    ScheduleActivity.class
            ));
        }

        // ================= PROGRESS =================

        else if (id == R.id.nav_progress) {

            startActivity(new Intent(
                    this,
                    ProgressActivity.class
            ));
        }

        // ================= REPORTS =================

        else if (id == R.id.nav_reports) {

            startActivity(new Intent(
                    this,
                    ReportsActivity.class
            ));
        }

        // ================= HOMEWORK =================

        else if (id == R.id.nav_homework) {

            startActivity(new Intent(
                    this,
                    HomeworkActivity.class
            ));
        }

        // ================= PAYMENTS =================

        else if (id == R.id.nav_payments) {

            startActivity(new Intent(
                    this,
                    PaymentsActivity.class
            ));
        }

        // ================= CHAT =================

        else if (id == R.id.nav_chat) {

            startActivity(new Intent(
                    this,
                    TherapistListActivity.class
            ));
        }


        // ================= DOCTOR APPOINTMENT =================

        else if (id == R.id.nav_doctor_appointment) {

            startActivity(new Intent(
                    this,
                    DoctorAppointmentActivity.class
            ));
        }

// ================= FEEDBACK =================

        else if (id == R.id.nav_feedback) {

            startActivity(new Intent(
                    this,
                    FeedbackActivity.class
            ));
        }

        // ================= SETTINGS =================

        else if (id == R.id.nav_settings) {

            startActivity(new Intent(
                    this,
                    SettingsActivity.class
            ));
        }

        // ================= HELP =================

        else if (id == R.id.nav_help) {

            startActivity(new Intent(
                    this,
                    HelpSupportActivity.class
            ));
        }

        // ================= LOGOUT =================

        // ================= LOGOUT =================

        else if (id == R.id.nav_logout) {

            SessionManager sessionManager =
                    new SessionManager(this);

            sessionManager.clearSession();

            Intent intent = new Intent(
                    this,
                    LoginActivity.class
            );

            // Back button se parent screens par wapas na ja sake
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        }

        // =================================================
        // CLOSE DRAWER
        // =================================================

        drawerLayout.closeDrawer(
                GravityCompat.START
        );

        return true;
    }

    // =====================================================
// LOAD PARENT UNREAD NOTIFICATION COUNT
// =====================================================

    private void loadUnreadNotificationCount() {

        if (notificationApiService == null
                || tvUnreadNotificationBadge == null) {
            return;
        }

        if (unreadCountCall != null) {
            unreadCountCall.cancel();
        }

        unreadCountCall =
                notificationApiService.getParentUnreadNotificationCount();

        unreadCountCall.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(
                    Call<JsonObject> call,
                    Response<JsonObject> response) {

                if (call.isCanceled()
                        || isFinishing()
                        || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful()
                        && response.body() != null) {

                    JsonObject body = response.body();

                    // Backend response field will be verified
                    // in the next test.
                    if (body.has("unreadCount")
                            && !body.get("unreadCount").isJsonNull()) {

                        JsonElement countElement =
                                body.get("unreadCount");

                        int unreadCount =
                                countElement.getAsInt();

                        updateUnreadNotificationBadge(unreadCount);

                    } else {

                        android.util.Log.e(
                                "ParentNotification",
                                "Unread count field not found: " + body
                        );
                    }

                } else {

                    android.util.Log.e(
                            "ParentNotification",
                            "Unread count API failed: " + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<JsonObject> call,
                    Throwable t) {

                if (call.isCanceled()
                        || isFinishing()
                        || isDestroyed()) {
                    return;
                }

                android.util.Log.e(
                        "ParentNotification",
                        "Unread count request failed",
                        t
                );
            }
        });
    }

// =====================================================
// UPDATE NOTIFICATION BADGE
// =====================================================

    private void updateUnreadNotificationBadge(int count) {

        if (tvUnreadNotificationBadge == null) {
            return;
        }

        if (count <= 0) {

            tvUnreadNotificationBadge.setVisibility(View.GONE);

        } else {

            tvUnreadNotificationBadge.setText(
                    count > 99 ? "99+" : String.valueOf(count)
            );

            tvUnreadNotificationBadge.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadNotificationCount();
    }

    @Override
    protected void onDestroy() {

        if (unreadCountCall != null) {
            unreadCountCall.cancel();
        }

        super.onDestroy();
    }

    // =====================================================
    // BACK BUTTON
    // =====================================================



}
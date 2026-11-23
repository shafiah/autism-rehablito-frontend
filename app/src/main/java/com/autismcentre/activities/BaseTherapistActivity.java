package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.ImageButton;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.autismcentre.utils.SessionManager;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseTherapistActivity extends AppCompatActivity
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
        // COMMON THERAPIST LAYOUT
        // =====================================================

        setContentView(R.layout.app_bar_therapist);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        tvUnreadNotificationBadge = findViewById(R.id.tvUnreadNotificationBadge);

        ApiClient.initialize(getApplicationContext());
        notificationApiService = ApiClient.getApiService();

        // =====================================================
// COMMON THERAPIST NOTIFICATION BUTTON
// =====================================================

        btnNotifications = findViewById(R.id.btnNotifications);

        btnNotifications.setOnClickListener(v -> {

            startActivity(new Intent(
                    BaseTherapistActivity.this,
                    TherapistNotificationsActivity.class
            ));

        });

        // =====================================================
        // TOOLBAR
        // =====================================================

        setSupportActionBar(topAppBar);

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
        // Load logged-in therapist details in drawer header
        loadTherapistDrawerHeader();

        // =====================================================
        // COMMON BOTTOM NAVIGATION
        // =====================================================

        bottomNavigation.setLabelVisibilityMode(
                BottomNavigationView.LABEL_VISIBILITY_LABELED
        );

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            // ================= HOME =================

            if (id == R.id.therapist_nav_home) {

                if (getCurrentBottomNavItem()
                        != R.id.therapist_nav_home) {

                    startActivity(new Intent(
                            this,
                            TherapistHomeDashboardActivity.class
                    ));

                    finish();
                }

                return true;
            }

            // ================= CHILDREN =================

            if (id == R.id.therapist_nav_children) {

                if (getCurrentBottomNavItem()
                        != R.id.therapist_nav_children) {

                    startActivity(new Intent(
                            this,
                            TherapistChildrenActivity.class
                    ));

                    finish();
                }

                return true;
            }

            // ================= SCHEDULE =================

            if (id == R.id.therapist_nav_schedule) {

                if (getCurrentBottomNavItem()
                        != R.id.therapist_nav_schedule) {

                    startActivity(new Intent(
                            this,
                            TherapistScheduleActivity.class
                    ));

                    finish();
                }

                return true;
            }

            // ================= REPORTS =================

            if (id == R.id.therapist_nav_reports) {

                if (getCurrentBottomNavItem()
                        != R.id.therapist_nav_reports) {

                    startActivity(new Intent(
                            this,
                            TherapistReportsActivity.class
                    ));

                    finish();
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
    // EACH THERAPIST ACTIVITY DEFINES CURRENT PAGE
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

    // =====================================================
// LOAD THERAPIST DETAILS IN NAVIGATION DRAWER
// =====================================================

    private void loadTherapistDrawerHeader() {

        View headerView = navigationView.getHeaderView(0);

        TextView tvTherapistName =
                headerView.findViewById(R.id.tvTherapistName);

        TextView tvTherapistRole =
                headerView.findViewById(R.id.tvTherapistRole);

        SessionManager sessionManager =
                new SessionManager(this);

        String therapistName = sessionManager.getName();
        String therapistEmail = sessionManager.getEmail();

        tvTherapistName.setText(
                therapistName != null && !therapistName.trim().isEmpty()
                        ? therapistName
                        : "Therapist"
        );

        tvTherapistRole.setText(
                therapistEmail != null && !therapistEmail.trim().isEmpty()
                        ? therapistEmail
                        : "Email not available"
        );
    }

    // =====================================================
    // THERAPIST DRAWER MENU
    // =====================================================

    @Override
    public boolean onNavigationItemSelected(
            @NonNull MenuItem item) {

        int id = item.getItemId();

        // ================= HOME =================

        if (id == R.id.therapist_nav_home) {

            if (getCurrentBottomNavItem()
                    != R.id.therapist_nav_home) {

                startActivity(new Intent(
                        this,
                        TherapistHomeDashboardActivity.class
                ));

                finish();
            }
        }

        // ================= ASSIGNED CHILDREN =================

        else if (id == R.id.therapist_nav_children) {

            startActivity(new Intent(
                    this,
                    TherapistChildrenActivity.class
            ));
        }

        // ================= SCHEDULE =================

        else if (id == R.id.therapist_nav_schedule) {

            startActivity(new Intent(
                    this,
                    TherapistScheduleActivity.class
            ));
        }

        // ================= THERAPY REPORTS =================

        else if (id == R.id.therapist_nav_reports) {

            startActivity(new Intent(
                    this,
                    TherapistReportsActivity.class
            ));
        }

        // ================= HOMEWORK =================

        else if (id == R.id.therapist_nav_homework) {

            startActivity(new Intent(
                    this,
                    TherapistHomeworkActivity.class
            ));
        }

        // ================= PARENT COMMUNICATION =================

        else if (id == R.id.therapist_nav_parent_chat) {

            startActivity(new Intent(
                    this,
                    TherapistParentChatActivity.class
            ));
        }

        // ================= NOTIFICATIONS =================

        else if (id == R.id.therapist_nav_notifications) {

            startActivity(new Intent(
                    this,
                    TherapistNotificationsActivity.class
            ));
        }

        // ================= SETTINGS =================

        else if (id == R.id.therapist_nav_settings) {

            startActivity(new Intent(
                    this,
                    TherapistSettingsActivity.class
            ));
        }

        // ================= HELP & SUPPORT =================

        else if (id == R.id.therapist_nav_help) {

            startActivity(new Intent(
                    this,
                    TherapistHelpSupportActivity.class
            ));
        }

        // ================= LOGOUT =================

        // ================= LOGOUT =================

        else if (id == R.id.therapist_nav_logout) {

            // Clear saved therapist session
            SessionManager sessionManager =
                    new SessionManager(this);

            sessionManager.clearSession();

            Intent intent = new Intent(
                    this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();

            return true;
        }

        // =====================================================
        // CLOSE DRAWER
        // =====================================================

        drawerLayout.closeDrawer(
                GravityCompat.START
        );

        return true;
    }

    private void loadUnreadNotificationCount() {

        if (notificationApiService == null) {
            return;
        }

        if (unreadCountCall != null && !unreadCountCall.isCanceled()) {
            unreadCountCall.cancel();
        }

        unreadCountCall = notificationApiService.getTherapistUnreadNotificationCount();

        unreadCountCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(
                    Call<JsonObject> call,
                    Response<JsonObject> response
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {

                    int unreadCount = response.body()
                            .get("unreadCount")
                            .getAsInt();

                    if (tvUnreadNotificationBadge != null) {

                        if (unreadCount > 0) {
                            tvUnreadNotificationBadge.setText(
                                    unreadCount > 99 ? "99+" : String.valueOf(unreadCount)
                            );

                            tvUnreadNotificationBadge.setVisibility(View.VISIBLE);

                        } else {
                            tvUnreadNotificationBadge.setVisibility(View.GONE);
                        }
                    }

                } else {
                    if (tvUnreadNotificationBadge != null) {
                        tvUnreadNotificationBadge.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

                if (call.isCanceled() || isFinishing() || isDestroyed()) {
                    return;
                }

                // Network failure: existing badge state ko change nahi karenge.
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadNotificationCount();
    }

    @Override
    protected void onDestroy() {

        if (unreadCountCall != null && !unreadCountCall.isCanceled()) {
            unreadCountCall.cancel();
        }

        super.onDestroy();
    }

    // =====================================================
    // BACK BUTTON
    // =====================================================


}
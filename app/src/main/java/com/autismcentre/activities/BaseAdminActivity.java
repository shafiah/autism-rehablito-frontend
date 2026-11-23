package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.autismcentre.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.autismcentre.utils.SessionManager;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.View;

import com.autismcentre.api.ApiClient;
import com.autismcentre.api.ApiService;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.TextView;

public class BaseAdminActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout adminDrawerLayout;
    protected NavigationView adminNavigationView;
    protected Toolbar adminTopAppBar;
    protected BottomNavigationView adminBottomNavigation;
    // Admin Notification
    protected ImageButton adminBtnNotifications;

    private TextView adminTvUnreadNotificationBadge;

    private ApiService adminNotificationApiService;

    private Call<JsonObject> adminUnreadCountCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_bar_admin);

        initializeAdminViews();
        setupAdminToolbar();
        setupAdminNotificationButton();
        setupAdminDrawer();
        setupAdminBottomNavigation();
        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.primary)
        );
    }

    /**
     * Initialize common Admin views.
     */
    private void initializeAdminViews() {

        adminDrawerLayout = findViewById(R.id.adminDrawerLayout);
        adminNavigationView = findViewById(R.id.adminNavigationView);
        adminTopAppBar = findViewById(R.id.adminTopAppBar);
        adminBottomNavigation = findViewById(R.id.adminBottomNavigation);

        // Notification Views
        adminBtnNotifications =
                findViewById(R.id.adminBtnNotifications);

        adminTvUnreadNotificationBadge =
                findViewById(R.id.adminTvUnreadNotificationBadge);

        // Notification API
        ApiClient.initialize(getApplicationContext());

        adminNotificationApiService =
                ApiClient.getApiService();
    }

    /**
     * Setup Admin Toolbar.
     */
    private void setupAdminToolbar() {

        setSupportActionBar(adminTopAppBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    getString(R.string.admin_dashboard)
            );
        }
    }

    // =====================================================
// ADMIN NOTIFICATION BUTTON
// =====================================================

    private void setupAdminNotificationButton() {

        adminBtnNotifications.setOnClickListener(v -> {

            startActivity(new Intent(
                    BaseAdminActivity.this,
                    AdminNotificationManagementActivity.class
            ));

        });
    }

    /**
     * Setup Admin Navigation Drawer.
     */
    private void setupAdminDrawer() {

        ActionBarDrawerToggle adminDrawerToggle =
                new ActionBarDrawerToggle(
                        this,
                        adminDrawerLayout,
                        adminTopAppBar,
                        R.string.admin_open_drawer,
                        R.string.admin_close_drawer
                );

        adminDrawerLayout.addDrawerListener(adminDrawerToggle);
        adminDrawerToggle.syncState();

        adminNavigationView.setNavigationItemSelectedListener(this);
        loadAdminDrawerHeader();
    }

    // =====================================================
// LOAD ADMIN DETAILS IN NAVIGATION DRAWER
// =====================================================

    private void loadAdminDrawerHeader() {

        View headerView = adminNavigationView.getHeaderView(0);

        TextView tvAdminName =
                headerView.findViewById(R.id.tvAdminName);

        TextView tvAdminEmail =
                headerView.findViewById(R.id.tvAdminEmail);

        SessionManager sessionManager =
                new SessionManager(this);

        String adminName = sessionManager.getName();
        String adminEmail = sessionManager.getEmail();

        tvAdminName.setText(
                adminName != null && !adminName.trim().isEmpty()
                        ? adminName
                        : "Admin"
        );

        tvAdminEmail.setText(
                adminEmail != null && !adminEmail.trim().isEmpty()
                        ? adminEmail
                        : "Email not available"
        );
    }

    /**
     * Setup Admin Bottom Navigation.
     */
    private void setupAdminBottomNavigation() {

        adminBottomNavigation.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.admin_nav_home) {

                if (!(this instanceof AdminHomeDashboardActivity)) {

                    Intent adminHomeIntent = new Intent(
                            BaseAdminActivity.this,
                            AdminHomeDashboardActivity.class
                    );

                    startActivity(adminHomeIntent);
                    finish();
                }

                return true;
            }

            if (itemId == R.id.admin_nav_children) {

                if (!(this instanceof AdminChildManagementActivity)) {

                    Intent adminChildrenIntent = new Intent(
                            BaseAdminActivity.this,
                            AdminChildManagementActivity.class
                    );

                    startActivity(adminChildrenIntent);
                    finish();
                }

                return true;
            }

            if (itemId == R.id.admin_nav_schedule) {

                if (!(this instanceof AdminScheduleSessionsActivity)) {

                    Intent adminScheduleIntent =
                            new Intent(
                                    BaseAdminActivity.this,
                                    AdminScheduleSessionsActivity.class
                            );

                    startActivity(adminScheduleIntent);
                    finish();
                }

                return true;
            }

            if (itemId == R.id.admin_nav_payments) {

                if (!(this instanceof AdminPaymentManagementActivity)) {

                    Intent adminPaymentIntent =
                            new Intent(
                                    BaseAdminActivity.this,
                                    AdminPaymentManagementActivity.class
                            );

                    startActivity(adminPaymentIntent);
                    finish();
                }

                return true;
            }

            return false;
        });
    }

    /**
     * Handle Admin Navigation Drawer menu.
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.admin_nav_home) {

            openAdminHome();

        } else if (itemId == R.id.admin_nav_children) {

            // Admin Child Management.
            Intent adminChildrenIntent = new Intent(
                    BaseAdminActivity.this,
                    AdminChildManagementActivity.class
            );

            startActivity(adminChildrenIntent);
            finish();

        } else if (itemId == R.id.admin_nav_therapists) {

            // Admin Therapist Management .
            Intent adminTherapistIntent =
                    new Intent(
                            BaseAdminActivity.this,
                            AdminTherapistManagementActivity.class
                    );

            startActivity(adminTherapistIntent);
            finish();


        } else if (itemId == R.id.admin_nav_schedule) {

            Intent adminScheduleIntent =
                    new Intent(
                            BaseAdminActivity.this,
                            AdminScheduleSessionsActivity.class
                    );

            startActivity(adminScheduleIntent);
            finish();

            // Admin Schedule Management will be implemented later.

        } else if (itemId == R.id.admin_nav_reports) {

            Intent adminReportsIntent =
                    new Intent(
                            BaseAdminActivity.this,
                            AdminTherapyReportsActivity.class
                    );

            startActivity(adminReportsIntent);
            finish();
            // Admin Therapy Reports will be implemented later.

        } else if (itemId == R.id.admin_nav_homework) {

            Intent adminHomeworkIntent =
                    new Intent(
                            BaseAdminActivity.this,
                            AdminHomeworkManagementActivity.class
                    );

            startActivity(adminHomeworkIntent);
            finish();


            // Admin Homework Management will be implemented later.

        } else if (itemId == R.id.admin_nav_parent_communication) {

            Intent adminCommunicationIntent =
                    new Intent(
                            BaseAdminActivity.this,
                            AdminParentCommunicationActivity.class
                    );

            startActivity(adminCommunicationIntent);
            finish();

            // Admin Parent Communication will be implemented later.

        } else if (itemId == R.id.admin_nav_payments) {

            Intent adminPaymentIntent =
                    new Intent(
                            BaseAdminActivity.this,
                            AdminPaymentManagementActivity.class
                    );

            startActivity(adminPaymentIntent);
            finish();

            // Admin Payment Management will be implemented later.

        } else if (itemId == R.id.admin_nav_notifications) {

            Intent adminNotificationIntent =
                    new Intent(
                            BaseAdminActivity.this,
                            AdminNotificationManagementActivity.class
                    );

            startActivity(adminNotificationIntent);
            finish();

            // Admin Notifications will be implemented later.

        } else if (itemId == R.id.admin_nav_settings) {

            // Admin Settings will be implemented later.

        } else if (itemId == R.id.admin_nav_help) {

            // Admin Help & Support will be implemented later.

        } else if (itemId == R.id.admin_nav_logout) {

            performAdminLogout();
        }

        adminDrawerLayout.closeDrawers();

        return true;
    }

    /**
     * Open Admin Dashboard.
     */
    private void openAdminHome() {

        if (this instanceof AdminHomeDashboardActivity) {
            return;
        }

        Intent adminHomeIntent = new Intent(
                BaseAdminActivity.this,
                AdminHomeDashboardActivity.class
        );

        startActivity(adminHomeIntent);
        finish();
    }

    /**
     * Admin Logout.
     */

    private void performAdminLogout() {

        SessionManager sessionManager =
                new SessionManager(this);

        sessionManager.clearSession();

        Intent adminLogoutIntent =
                new Intent(
                        BaseAdminActivity.this,
                        LoginActivity.class
                );

        adminLogoutIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(adminLogoutIntent);
        finish();
    }

    /**
     * Set Admin page title.
     */
    protected void setAdminPageTitle(String title) {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Show Admin page content.
     */
    protected void showAdminPageContent(@LayoutRes int layoutRes) {

        ViewGroup adminContentContainer =
                findViewById(R.id.adminContentContainer);

        adminContentContainer.removeAllViews();

        getLayoutInflater().inflate(
                layoutRes,
                adminContentContainer,
                true
        );
    }

    // =====================================================
// LOAD ADMIN UNREAD NOTIFICATION COUNT
// =====================================================

    private void loadAdminUnreadNotificationCount() {

        if (adminNotificationApiService == null
                || adminTvUnreadNotificationBadge == null) {
            return;
        }

        if (adminUnreadCountCall != null) {
            adminUnreadCountCall.cancel();
        }

        adminUnreadCountCall =
                adminNotificationApiService.getAdminUnreadNotificationCount();

        adminUnreadCountCall.enqueue(new Callback<JsonObject>() {

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

                    if (body.has("unreadCount")
                            && !body.get("unreadCount").isJsonNull()) {

                        int unreadCount =
                                body.get("unreadCount").getAsInt();

                        updateAdminNotificationBadge(unreadCount);

                    } else {

                        android.util.Log.e(
                                "AdminNotification",
                                "Unread count field missing: " + body
                        );
                    }

                } else {

                    android.util.Log.e(
                            "AdminNotification",
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
                        "AdminNotification",
                        "Unread count request failed",
                        t
                );
            }
        });
    }

// =====================================================
// UPDATE ADMIN NOTIFICATION BADGE
// =====================================================

    private void updateAdminNotificationBadge(int count) {

        if (adminTvUnreadNotificationBadge == null) {
            return;
        }

        if (count <= 0) {

            adminTvUnreadNotificationBadge.setVisibility(View.GONE);

        } else {

            adminTvUnreadNotificationBadge.setText(
                    count > 99 ? "99+" : String.valueOf(count)
            );

            adminTvUnreadNotificationBadge.setVisibility(View.VISIBLE);
        }
    }

// =====================================================
// REFRESH NOTIFICATION COUNT
// =====================================================

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminUnreadNotificationCount();
    }

// =====================================================
// CANCEL NOTIFICATION API
// =====================================================

    @Override
    protected void onDestroy() {

        if (adminUnreadCountCall != null) {
            adminUnreadCountCall.cancel();
        }

        super.onDestroy();
    }
}
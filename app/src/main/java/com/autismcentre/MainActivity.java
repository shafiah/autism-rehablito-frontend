
package com.autismcentre;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.activities.LoginActivity;
import com.autismcentre.activities.NewParentHomeActivity;
import com.autismcentre.activities.RegisterActivity;
import com.autismcentre.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import com.autismcentre.activities.AdminHomeDashboardActivity;
import com.autismcentre.activities.TherapistHomeDashboardActivity;
import com.autismcentre.api.ApiClient;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnRegister;
    private MaterialButton btnLogin;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize API client whenever app process starts.
        ApiClient.initialize(getApplicationContext());

        sessionManager = new SessionManager(this);

        // Restore existing valid session according to role.
        if (sessionManager.isLoggedIn()) {

            String role = sessionManager.getRole();

            Class<?> dashboardActivity = null;

            if ("PARENT".equalsIgnoreCase(role)
                    && sessionManager.hasValidSessionData()) {

                dashboardActivity = NewParentHomeActivity.class;

            } else if ("ADMIN".equalsIgnoreCase(role)
                    && sessionManager.hasValidSessionForRole("ADMIN")) {

                dashboardActivity = AdminHomeDashboardActivity.class;

            } else if ("THERAPIST".equalsIgnoreCase(role)
                    && sessionManager.hasValidTherapistSession()) {

                dashboardActivity = TherapistHomeDashboardActivity.class;
            }

            if (dashboardActivity != null) {

                Intent intent = new Intent(
                        MainActivity.this,
                        dashboardActivity
                );

                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();
                return;
            }
        }

        // No valid session: show existing welcome screen.
        setContentView(R.layout.activity_main);

        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RegisterActivity.class
            );
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
        });
    }
}

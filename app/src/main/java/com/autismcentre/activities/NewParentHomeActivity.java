
package com.autismcentre.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.api.ApiClient;
import com.autismcentre.model.ParentChildResponse;
import com.autismcentre.repository.ParentChildRepository;
import com.autismcentre.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewParentHomeActivity extends AppCompatActivity {

    private static final String TAG = "ParentChildAPI";

    private ImageButton btnNotifications;

    private MaterialButton btnCallCentre;
    private MaterialButton btnWhatsApp;
   // private MaterialButton btnChatbot;
    private MaterialButton btnTemporaryActivate;

    private TextView tvWelcome;
    private TextView tvChildName;
    private TextView tvChildDob;
    private TextView tvChildGender;
    private TextView tvChildBranch;

    private MaterialCardView cardChildNotLinked;
    private MaterialCardView cardLinkedChild;

    private SessionManager sessionManager;
    private ParentChildRepository parentChildRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_parent_home);

        sessionManager = new SessionManager(this);
        ApiClient.initialize(getApplicationContext());
        parentChildRepository = new ParentChildRepository();

        tvWelcome = findViewById(R.id.tvWelcome);
        cardChildNotLinked = findViewById(R.id.cardChildNotLinked);
        cardLinkedChild = findViewById(R.id.cardLinkedChild);

        tvChildName = findViewById(R.id.tvChildName);
        tvChildDob = findViewById(R.id.tvChildDob);
        tvChildGender = findViewById(R.id.tvChildGender);
        tvChildBranch = findViewById(R.id.tvChildBranch);

        if (tvWelcome != null) {
            String name = sessionManager.getName();

            if (name == null || name.trim().isEmpty()) {
                name = "Parent";
            }

            tvWelcome.setText("Welcome, " + name + " 👋");
        }

        // Initially show the not-linked state until API responds.
        cardChildNotLinked.setVisibility(View.VISIBLE);
        cardLinkedChild.setVisibility(View.GONE);

        btnTemporaryActivate = findViewById(R.id.btnTemporaryActivate);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnCallCentre = findViewById(R.id.btnCallCentre);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        //btnChatbot = findViewById(R.id.btnChatbot);

        btnNotifications.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "No new notifications",
                        Toast.LENGTH_SHORT
                ).show()
        );

        btnCallCentre.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:9204786220")
            );
            startActivity(intent);
        });

        btnWhatsApp.setOnClickListener(v -> {
            String phoneNumber = "919204786220";

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/" + phoneNumber)
            );
            startActivity(intent);
        });

        //btnChatbot.setOnClickListener(v ->
          //      Toast.makeText(
            //            this,
              //          "Centre Chatbot opened",
                //        Toast.LENGTH_SHORT
                //).show()
        //);

        btnTemporaryActivate.setOnClickListener(v -> {
            Intent intent = new Intent(NewParentHomeActivity.this,
                    ParentHomeDashboardActivity.class
            );
            startActivity(intent);
        });

        loadLinkedChildren();
    }

    private void loadLinkedChildren() {

        parentChildRepository.getMyChildren(
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
                                && response.body() != null) {

                            List<ParentChildResponse> children =
                                    response.body();

                            if (children.isEmpty()) {
                                showChildNotLinked();
                            } else {
                                // Open dashboard with the first linked child's ID.
                                ParentChildResponse child = children.get(0);

                                Intent intent = new Intent(
                                        NewParentHomeActivity.this,
                                        ParentHomeDashboardActivity.class
                                );

                                intent.putExtra("childId", child.getChildId());

                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Log.e(
                                    TAG,
                                    "Child API failed. HTTP "
                                            + response.code()
                            );

                            // Do not claim the child is unlinked
                            // when the API itself failed.
                            Toast.makeText(
                                    NewParentHomeActivity.this,
                                    "Child details load nahi ho paayi.",
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

                        Log.e(TAG, "Child API request failed", t);

                        Toast.makeText(
                                NewParentHomeActivity.this,
                                "Server se child details nahi mil paayi.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void showChildNotLinked() {
        cardChildNotLinked.setVisibility(View.VISIBLE);
        cardLinkedChild.setVisibility(View.GONE);
    }

    private void showChildDetails(ParentChildResponse child) {

        cardChildNotLinked.setVisibility(View.GONE);
        cardLinkedChild.setVisibility(View.VISIBLE);

        String childName = child.getChildName();

        tvChildName.setText(
                "Name: " + (
                        childName != null && !childName.trim().isEmpty()
                                ? childName
                                : "Not available"
                )
        );

        String dob = child.getDateOfBirth();
        tvChildDob.setText(
                "Date of Birth: " +
                        (dob != null && !dob.isEmpty()
                                ? dob
                                : "Not available")
        );

        String gender = child.getGender();
        tvChildGender.setText(
                "Gender: " +
                        (gender != null && !gender.isEmpty()
                                ? gender
                                : "Not available")
        );

        String branch = child.getBranchName();
        tvChildBranch.setText(
                "Branch: " +
                        (branch != null && !branch.isEmpty()
                                ? branch
                                : "Not available")
        );
    }
}

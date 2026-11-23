package com.autismcentre.activities;

import android.os.Bundle;

import com.autismcentre.R;

public class DoctorAppointmentActivity extends BaseParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Doctor Appointment Content
        showPageContent(R.layout.activity_doctor_appointment);

        // Page Title
        setPageTitle("Autism Centre");
    }

    @Override
    protected int getCurrentBottomNavItem() {
        // Doctor Appointment bottom navigation ka part nahi hai.
        // Home ko current selection rakhenge.
        return R.id.nav_home;
    }
}
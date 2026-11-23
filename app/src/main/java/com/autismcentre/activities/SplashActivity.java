
package com.autismcentre.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.autismcentre.R;
import com.autismcentre.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 5000L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable navigationRunnable = () -> {
        Intent intent = new Intent(
                SplashActivity.this,
                MainActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        handler.postDelayed(navigationRunnable, SPLASH_DELAY);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigationRunnable);
        super.onDestroy();
    }
}

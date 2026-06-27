package com.andika.temucashflow.ui.dashboard;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.andika.temucashflow.R;
import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityDashboardBinding;
import com.andika.temucashflow.ui.BaseActivity;
import com.andika.temucashflow.ui.analytics.StatisticsFragment;
import com.andika.temucashflow.ui.settings.ProfileFragment;
import com.andika.temucashflow.ui.transaction.AddTransactionActivity;
import com.andika.temucashflow.ui.transaction.TransactionListFragment;

public class DashboardActivity extends BaseActivity implements SensorEventListener {

    private ActivityDashboardBinding binding;
    private SensorManager sensorManager;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge to Edge setup
        setupEdgeToEdge();

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // MATIKAN PRIVASI LAYAR
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setupWindowInsets();
        setupBottomNav();
        setupListeners();
        setupShakeSensor();

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupShakeSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (SharedPrefManager.getInstance(this).isShakeEnabled()) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = currentAcceleration - lastAcceleration;
            acceleration = acceleration * 0.9f + delta;

            if (acceleration > 12) {
                // Shake detected
                startActivity(new Intent(this, AddTransactionActivity.class));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void setupEdgeToEdge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinator, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            binding.bottomNav.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom
            );
            
            return insets;
        });
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_transactions) {
                loadFragment(new TransactionListFragment());
                return true;
            } else if (id == R.id.nav_analytics) {
                loadFragment(new StatisticsFragment());
                return true;
            } else if (id == R.id.nav_settings) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {
        binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.commit();
    }

    public void navigateToTransactions() {
        binding.bottomNav.setSelectedItemId(R.id.nav_transactions);
    }

    public void navigateToStatistics() {
        binding.bottomNav.setSelectedItemId(R.id.nav_analytics);
    }
}

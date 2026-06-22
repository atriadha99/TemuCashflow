package com.andika.temucashflow.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andika.temucashflow.data.SharedPrefManager;
import com.andika.temucashflow.databinding.ActivityAccessibilitySettingsBinding;

public class AccessibilitySettingsActivity extends AppCompatActivity {

    private ActivityAccessibilitySettingsBinding binding;
    private SharedPrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccessibilitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pref = SharedPrefManager.getInstance(this);

        setupToolbar();
        initViews();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Accessibility Settings");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        binding.switchHighContrast.setChecked(pref.isHighContrastEnabled());
        binding.switchTts.setChecked(pref.isTtsEnabled());
        binding.switchSpeechRecog.setChecked(pref.isSpeechRecognitionEnabled());
        binding.switchVibration.setChecked(pref.isVibrationEnabled());
        
        int fontSize = pref.getFontSize();
        if (fontSize == 0) binding.rbSmall.setChecked(true);
        else if (fontSize == 1) binding.rbMedium.setChecked(true);
        else binding.rbLarge.setChecked(true);
    }

    private void setupListeners() {
        binding.rgFontSize.setOnCheckedChangeListener((group, checkedId) -> {
            int size = 1; // Default medium
            if (checkedId == binding.rbSmall.getId()) size = 0;
            else if (checkedId == binding.rbLarge.getId()) size = 2;
            
            pref.setFontSize(size);
            Toast.makeText(this, "Font size updated. Restart app to apply.", Toast.LENGTH_SHORT).show();
        });

        binding.switchHighContrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pref.setHighContrastEnabled(isChecked);
            Toast.makeText(this, "High contrast updated.", Toast.LENGTH_SHORT).show();
        });

        binding.switchTts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pref.setTtsEnabled(isChecked);
        });

        binding.switchSpeechRecog.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pref.setSpeechRecognitionEnabled(isChecked);
        });

        binding.switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            pref.setVibrationEnabled(isChecked);
        });
    }
}

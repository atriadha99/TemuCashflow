package com.andika.temucashflow.ui.education;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.andika.temucashflow.databinding.ActivityEducationBinding;
import com.andika.temucashflow.ui.BaseActivity;

public class EducationActivity extends BaseActivity {

    private ActivityEducationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEducationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String page = getIntent().getStringExtra("page");
        if (page == null) page = "guide1.html";

        setupWebView();
        binding.webView.loadUrl("file:///android_asset/education/" + page);
    }

    private void setupWebView() {
        binding.webView.getSettings().setJavaScriptEnabled(false);
        binding.webView.setWebViewClient(new WebViewClient());
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}

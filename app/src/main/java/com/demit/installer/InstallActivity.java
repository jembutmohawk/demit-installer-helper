package com.demit.installer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class InstallActivity extends Activity {
    private static final int REQ_STORAGE = 71;
    private String apkPath;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        apkPath = getIntent().getStringExtra("apk_path");
        if (apkPath == null || apkPath.trim().isEmpty()) {
            Toast.makeText(this, "Lokasi APK tidak diberikan.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 32 &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_STORAGE);
        } else {
            launchInstaller();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQ_STORAGE) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) launchInstaller();
            else {
                Toast.makeText(this, "Izin membaca penyimpanan diperlukan.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void launchInstaller() {
        try {
            File apk = new File(apkPath);
            if (!apk.isFile() || !apk.canRead()) {
                Toast.makeText(this, "APK tidak ditemukan/tidak bisa dibaca: " + apkPath, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            Uri uri = FileProvider.getUriForFile(this, "com.demit.installer.fileprovider", apk);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(uri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(install);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membuka installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}

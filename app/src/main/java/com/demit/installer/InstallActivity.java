package com.demit.installer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class InstallActivity extends Activity {

    private static final int REQ_STORAGE = 71;
    private String apkPath;
    private boolean waitingPermission = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        apkPath = getIntent().getStringExtra("apk_path");

        if (apkPath == null || apkPath.trim().isEmpty()) {
            showError("Lokasi APK tidak diberikan.");
            return;
        }

        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQ_STORAGE
            );
            return;
        }

        checkInstallPermission();
    }

    private void checkInstallPermission() {
        if (Build.VERSION.SDK_INT >= 26 &&
            !getPackageManager().canRequestPackageInstalls()) {

            waitingPermission = true;

            Intent intent = new Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + getPackageName())
            );

            startActivity(intent);
            return;
        }

        launchInstaller();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (waitingPermission) {
            waitingPermission = false;

            if (Build.VERSION.SDK_INT >= 26 &&
                getPackageManager().canRequestPackageInstalls()) {
                launchInstaller();
            } else {
                showError("Izin instalasi aplikasi belum diberikan.");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode, String[] permissions, int[] results) {

        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode == REQ_STORAGE) {
            if (results.length > 0 &&
                results[0] == PackageManager.PERMISSION_GRANTED) {
                checkInstallPermission();
            } else {
                showError("Izin membaca penyimpanan ditolak.");
            }
        }
    }

    private void launchInstaller() {
        try {
            File apk = new File(apkPath);

            if (!apk.exists() || !apk.isFile() || !apk.canRead()) {
                showError("APK tidak ditemukan atau tidak bisa dibaca.");
                return;
            }

            Uri uri = FileProvider.getUriForFile(
                this,
                "com.demit.installer.fileprovider",
                apk
            );

            Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            install.setData(uri);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.putExtra(Intent.EXTRA_RETURN_RESULT, true);

            startActivity(install);

        } catch (Exception e) {
            showError("Gagal membuka installer: " + e.toString());
            return;
        }

        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}

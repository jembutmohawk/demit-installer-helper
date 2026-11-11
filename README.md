# DEMIT Installer Helper V1

A small Android bridge app for Termux. It receives an APK path and opens Android's package installer using a `content://` URI from `FileProvider`.

## Build
Push this folder to a GitHub repository, then run the included GitHub Actions workflow. Download the `demit-installer-debug-apk` artifact and install `app-debug.apk`.

## Termux command after installing the helper
```bash
am start -n com.demit.installer/.InstallActivity \
  -a com.demit.installer.INSTALL_APK \
  --es apk_path "/sdcard/Download/Demit-Island-V4.33.4.apk"
```

## Notes
- Android still displays its normal install confirmation. This helper does not bypass Android security or silently install apps.
- On first launch, it may request storage access.
- Built for the Redmi Note 7 / Android 10 target scenario. It has not been built or device-tested in this environment.

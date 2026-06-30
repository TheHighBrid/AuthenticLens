# AuthenticLens Android App Usage

AuthenticLens turns the image-realism framework in this repository into a direct Android image-audit app.

## What the app does

- Lets you choose an image from your Android device.
- Analyzes the selected image directly from pixel data.
- Grades the image with an AuthenticLens score out of 100.
- Produces a letter grade and verdict.
- Measures brightness, contrast, dynamic range, saturation, clipping, sharpness, grain/noise, edge density, JPEG blockiness, color cast, resolution, and aspect ratio.
- Provides mode-specific QA for apparel, mobile/iPhone realism, street realism, photographic physics, and AI artifact risk.
- Returns detected issues and a fix list inside the app.
- Lets you copy or share the completed audit.

## Current limitation

This version is offline and rule-based. It does not run a semantic AI vision model yet, so it cannot fully understand hands, faces, text, logos, garment identity, or scene meaning. It does, however, audit and grade the selected image directly inside the APK using AuthenticLens technical realism rules.

## Recommended workflow

1. Open AuthenticLens.
2. Choose the right audit mode.
3. Select an image.
4. Add a short note describing your goal if useful.
5. Tap **Run AuthenticLens audit**.
6. Review the score, grade, measured signals, detected issues, and fix list.
7. Correct the image based on the app's findings.
8. Re-run the audit before exporting.

## Best mode for Melato product images

Use **Apparel Product QA**.

Recommended note:

```text
Audit this as a premium Melato apparel product image. Check if it is product-page ready without changing the garment, embroidery, logo, color, zipper, pockets, silhouette, or artwork.
```

## APK build output

The GitHub Actions workflow builds a debug APK at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

When the workflow finishes, download the uploaded artifact named:

```text
authenticlens-debug-apk
```

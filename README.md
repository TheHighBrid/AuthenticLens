# AuthenticLens

AuthenticLens is an image realism analysis framework for grading photographic believability and improving visual production quality.

It began as a PDF playbook library. It now includes a lightweight Android app that directly audits selected images with an offline AuthenticLens scoring engine.

## What it helps with

- Image realism review
- Photographic quality checks
- Apparel product image QA
- Ghost mannequin and product-page consistency checks
- Mobile / iPhone realism evaluation
- Street and location realism checks
- Technical grading before publishing or editing

## Android app

The Android app is an offline image-audit tool. It lets you select an image, choose an audit mode, add your goal, then grades the image directly from pixel data and AuthenticLens rules.

It measures:

- Brightness and exposure balance
- Contrast and dynamic range
- Shadow and highlight clipping
- Saturation and color cast
- Sharpness and edge density
- Grain/noise estimate
- JPEG blockiness estimate
- Resolution and aspect ratio
- Mode-specific readiness for apparel, mobile, street, physics, and artifact-risk workflows

The app returns:

- Score out of 100
- Letter grade
- Verdict
- Score breakdown
- Measured signals
- Detected issues
- Fix list

### App modes

- General Realism Audit
- AI Artifact Scan
- Photographic Physics
- Apparel Product QA
- Mobile / iPhone Realism
- Contextual Street Realism

### Build the APK with GitHub Actions

1. Open the repository on GitHub.
2. Go to **Actions**.
3. Select **Build AuthenticLens APK**.
4. Run the workflow manually, or push to `main`.
5. Download the artifact named `authenticlens-debug-apk`.
6. Inside the artifact, install `app-debug.apk` on your Android device.

### Build locally

You need Android Studio or a local Android SDK.

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

The APK will be created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Recommended apparel workflow

1. Open AuthenticLens.
2. Choose **Apparel Product QA**.
3. Pick the product image.
4. Add a goal, for example:

```text
Audit this as a premium Melato apparel product image. Check if it is product-page ready without changing the garment, embroidery, logo, color, zipper, pockets, silhouette, or artwork.
```

5. Tap **Run AuthenticLens audit**.
6. Review the score, grade, measured signals, detected issues, and fix list.
7. Correct the image and re-run the audit before publishing.

## Repository structure

```text
app/                    Android app source
.github/workflows/      APK build workflow
docs/                   Usage documentation
*.pdf                   Original AuthenticLens playbooks
```

## Current limitation

The current Android version is offline and rule-based. It does not yet run a semantic vision model, so it cannot fully understand hands, faces, brand logos, text, garment identity, or scene meaning. It still audits and grades the selected image directly inside the app using AuthenticLens technical realism rules.

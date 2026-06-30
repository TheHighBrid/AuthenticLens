# AuthenticLens

AuthenticLens is an image realism and AI-artifact analysis framework for detecting generated-image tells, improving photographic believability, and producing stricter correction prompts for visual tools.

It began as a PDF playbook library. It now also includes a lightweight Android companion app that turns the framework into a practical mobile workflow.

## What it helps with

- AI-generated image artifact detection
- Photographic realism checks
- Apparel product image QA
- Ghost mannequin and product-page consistency
- Mobile / iPhone realism prompting
- Street and location realism checks
- Correction prompts for ChatGPT, Gemini, Photoshop, Midjourney, and other visual tools

## Android app

The Android app is an offline prompt-and-QA assistant. It lets you select an image, choose an audit mode, add your goal, then generate a structured AuthenticLens audit and a strict image-correction prompt.

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
Make this product image realistic and product-page ready without changing the garment, embroidery, logo, color, zipper, pockets, silhouette, or artwork.
```

5. Copy the generated audit and prompt.
6. Paste it into your image editing or generation tool.
7. Run the final QA checklist before publishing.

## Repository structure

```text
app/                    Android app source
.github/workflows/      APK build workflow
docs/                   Usage documentation
prompts/                Reusable AuthenticLens prompts
*.pdf                   Original AuthenticLens playbooks
```

## Current limitation

The first Android version does not run an on-device AI vision model. It is a structured realism-audit and prompt-generation companion. Use it beside a visual AI/editor for the actual image transformation step.

# AuthenticLens Android App Usage

AuthenticLens turns the original PDF playbooks in this repository into a practical Android companion for image realism work.

## What the app does

- Lets you choose an image from your Android device.
- Reads basic file metadata such as filename, file size, resolution, and aspect ratio when available.
- Generates a structured realism audit.
- Creates a strict correction prompt you can paste into ChatGPT, Gemini, Photoshop, or another image tool.
- Provides mode-specific QA for apparel, mobile/iPhone realism, street realism, photographic physics, and AI artifact detection.
- Lets you copy or share the generated audit.

## What the app does not do yet

The first APK is an offline prompt-and-QA assistant. It does not yet run a local AI vision model on-device. It prepares the diagnosis framework and correction prompt so you can use it with a visual AI tool.

## Recommended workflow

1. Open AuthenticLens.
2. Choose the right audit mode.
3. Select an image.
4. Add a short note describing your goal.
5. Tap **Generate audit + prompt**.
6. Copy the output.
7. Paste it into an image model or editing workflow.
8. Use the final QA checklist before exporting.

## Best mode for Melato product images

Use **Apparel Product QA**.

Recommended note:

```text
Make this Melato product image realistic and product-page ready without changing the garment, embroidery, logo, color, zipper, pockets, silhouette, or artwork.
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

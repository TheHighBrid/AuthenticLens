# AuthenticLens

AuthenticLens transforms and analyzes images by detecting AI-generated artifacts and applying real-world photographic physics, imperfections, and textures to make them look authentically human-captured.

## What the app does

- Uploads an image into a browser-based analysis workspace.
- Produces an artifact-risk report for edge halos, plastic texture, lighting mismatches, provenance issues, and repeated/warped details.
- Applies a canvas-based realism pass with sensor grain, vignette, micro-contrast, warmth, and subtle texture disruption.
- Exports the transformed image as a PNG.

## Knowledge base

This repo includes the AuthenticLens PDF corpus covering artifact lexicons, photographic physics, realism prompting formulas, diagnostic classification, enhancement protocols, mobile compression provenance, contextual street realism, and apparel/product QA.

## Local development

```bash
npm install
npm run dev
```

## Production build

```bash
npm run build
```

package com.authenticlens.app;

import java.util.Arrays;
import java.util.List;

public final class PromptEngine {
    public static final String MODE_GENERAL = "General Realism Audit";
    public static final String MODE_ARTIFACTS = "AI Artifact Scan";
    public static final String MODE_PHYSICS = "Photographic Physics";
    public static final String MODE_APPAREL = "Apparel Product QA";
    public static final String MODE_MOBILE = "Mobile / iPhone Realism";
    public static final String MODE_STREET = "Contextual Street Realism";

    public static final List<String> MODES = Arrays.asList(
            MODE_GENERAL,
            MODE_ARTIFACTS,
            MODE_PHYSICS,
            MODE_APPAREL,
            MODE_MOBILE,
            MODE_STREET
    );

    private PromptEngine() {}

    public static String buildAudit(String mode, String userNotes, String imageMeta) {
        String safeMode = normalizeMode(mode);
        String notes = clean(userNotes);
        String meta = clean(imageMeta);

        StringBuilder out = new StringBuilder();
        out.append("AUTHENTICLENS AUDIT\n");
        out.append("Mode: ").append(safeMode).append("\n\n");

        if (!meta.isEmpty()) {
            out.append("IMAGE CONTEXT\n").append(meta).append("\n\n");
        }
        if (!notes.isEmpty()) {
            out.append("USER INTENT\n").append(notes).append("\n\n");
        }

        out.append("CLASSIFICATION TARGET\n");
        out.append("Check whether the image reads as real camera capture, AI-generated, AI-edited, composite, over-smoothed render, or product-page asset.\n\n");

        out.append("REALISM SCORECARD / 100\n");
        out.append("- 20 lighting, shadow logic, and reflection physics\n");
        out.append("- 20 material behavior, texture, fabric/skin/object detail\n");
        out.append("- 15 lens behavior, focus, compression, grain, and camera imperfections\n");
        out.append("- 15 anatomy, scale, alignment, and object continuity\n");
        out.append("- 15 context realism, environment, signage, geography, and background believability\n");
        out.append("- 15 production polish without sterile AI perfection\n\n");

        out.append("MODE-SPECIFIC CHECKS\n");
        out.append(checksFor(safeMode)).append("\n");

        out.append("NON-NEGOTIABLE LOCKS\n");
        out.append("- Do not redesign the subject, garment, logo, embroidery, artwork, colorway, pattern, zipper, pocket layout, or silhouette.\n");
        out.append("- Preserve identity, pose, composition, and product details unless the user explicitly asks otherwise.\n");
        out.append("- Correct only realism, proportions, lighting, shadows, texture, framing, and export readiness.\n\n");

        out.append("CORRECTION PROMPT\n");
        out.append(buildPrompt(safeMode, notes, meta)).append("\n\n");

        out.append("FINAL QA BEFORE EXPORT\n");
        out.append("1. Shadows touch the ground/object naturally.\n");
        out.append("2. Texture is visible but not crunchy or over-sharpened.\n");
        out.append("3. Lens depth feels like a real camera, not a 3D render.\n");
        out.append("4. No extra limbs, warped seams, fake text, or impossible reflections.\n");
        out.append("5. Product scale and framing stay consistent across the full set.\n");
        out.append("6. The result still looks like the original subject, only more believable.\n");
        return out.toString();
    }

    private static String normalizeMode(String mode) {
        if (mode == null || !MODES.contains(mode)) {
            return MODE_GENERAL;
        }
        return mode;
    }

    private static String clean(String value) {
        if (value == null) return "";
        return value.trim();
    }

    private static String checksFor(String mode) {
        switch (mode) {
            case MODE_ARTIFACTS:
                return "- Inspect hands, seams, logos, teeth, hairlines, pockets, zippers, text, buttons, and edge transitions.\n"
                        + "- Flag repeating textures, melted details, plastic skin, symmetry that is too perfect, and inconsistent object geometry.\n"
                        + "- Look for AI fingerprints: floating accessories, fake stitching, duplicated folds, and impossible occlusion.";
            case MODE_PHYSICS:
                return "- Check whether light direction, softness, color temperature, bounce light, reflections, and contact shadows agree.\n"
                        + "- Add subtle real-camera imperfections: lens falloff, grain, mild chromatic aberration, imperfect focus, and realistic bokeh.\n"
                        + "- Remove hyper-gloss, over-saturation, plastic highlights, and weightless objects.";
            case MODE_APPAREL:
                return "- Check garment fidelity: collar shape, sleeve width, hem line, zipper depth, pocket placement, embroidery/logo accuracy, and fabric tension.\n"
                        + "- Compare front, back, 45-degree, and macro shots for consistent scale, color, shadow, and silhouette.\n"
                        + "- Fix ghost mannequin issues: hollow neck shape, shoulder balance, body volume, fabric gravity, and clean product-page alignment.";
            case MODE_MOBILE:
                return "- Simulate real phone capture: mild compression, sensor noise, imperfect white balance, handheld framing, micro blur, and believable sharpening.\n"
                        + "- Avoid sterile studio symmetry unless the shot is explicitly a studio product image.\n"
                        + "- Keep metadata-like visual cues plausible: real exposure, phone lens perspective, imperfect background depth.";
            case MODE_STREET:
                return "- Validate environment: local signage, road scale, climate, street furniture, license plates, storefronts, and crowd behavior.\n"
                        + "- Ensure the subject belongs in the scene through matching light, shadow angle, camera height, and atmospheric texture.\n"
                        + "- Remove generic city mush, fake shop text, impossible perspective, and pasted-on subject edges.";
            default:
                return "- Run a full realism pass across artifacts, materials, physics, lens behavior, context, and export quality.\n"
                        + "- Identify what breaks believability first, then correct the highest-impact flaws without changing the concept.\n"
                        + "- Prioritize believable imperfection over overly clean AI polish.";
        }
    }

    private static String buildPrompt(String mode, String notes, String meta) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Edit or regenerate this image as a realistic, human-captured photograph. ");
        if (!notes.isEmpty()) {
            prompt.append("User goal: ").append(notes).append(". ");
        }
        if (!meta.isEmpty()) {
            prompt.append("Image context: ").append(meta).append(". ");
        }
        prompt.append("Preserve the original subject, composition, garment/product design, color, artwork, logos, embroidery, pattern, and silhouette. ");

        switch (mode) {
            case MODE_ARTIFACTS:
                prompt.append("Remove AI artifacts including warped seams, duplicated folds, fake text, melted edges, inconsistent hands, and impossible geometry. ");
                break;
            case MODE_PHYSICS:
                prompt.append("Make the light, shadows, reflections, lens compression, depth of field, and material response obey real-world photographic physics. ");
                break;
            case MODE_APPAREL:
                prompt.append("Make the garment product-page ready with accurate fabric gravity, clean sleeve and hem alignment, realistic collar volume, believable stitching, consistent product scale, and natural shadow contact. ");
                break;
            case MODE_MOBILE:
                prompt.append("Make it feel captured on a modern phone camera with subtle sensor noise, natural compression, slightly imperfect focus, authentic sharpening, and realistic handheld framing. ");
                break;
            case MODE_STREET:
                prompt.append("Make the location feel geographically and socially real with coherent signage, weather, street scale, perspective, crowd behavior, and environmental lighting. ");
                break;
            default:
                prompt.append("Add subtle grain, natural imperfection, realistic texture, believable light falloff, grounded shadows, correct perspective, and non-sterile color grading. ");
                break;
        }

        prompt.append("Avoid over-smoothing, hyper-saturation, plastic lighting, fake gloss, broken anatomy, inconsistent scale, and sterile AI perfection.");
        return prompt.toString();
    }
}

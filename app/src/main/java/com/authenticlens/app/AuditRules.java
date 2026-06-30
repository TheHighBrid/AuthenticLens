package com.authenticlens.app;

import java.util.Arrays;
import java.util.List;

public final class AuditRules {
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

    private AuditRules() {}

    public static String normalizeMode(String mode) {
        if (mode == null || !MODES.contains(mode)) {
            return MODE_GENERAL;
        }
        return mode;
    }

    public static String modeDescription(String mode) {
        switch (normalizeMode(mode)) {
            case MODE_ARTIFACTS:
                return "Weights artifact-risk signals: extreme smoothing, weak micro-detail, strange edge behavior, heavy clipping, and compression traces.";
            case MODE_PHYSICS:
                return "Weights photographic physics: exposure, dynamic range, lens/detail behavior, light consistency, and realistic imperfection.";
            case MODE_APPAREL:
                return "Weights product readiness: detail clarity, color control, clipping, resolution, fabric-readable texture, and clean export consistency.";
            case MODE_MOBILE:
                return "Weights phone realism: believable sharpness, controlled noise, natural compression, handheld-photo imperfection, and non-sterile color.";
            case MODE_STREET:
                return "Weights contextual realism: environmental texture, natural contrast, non-generic edges, compression plausibility, and scene believability.";
            default:
                return "Runs a balanced AuthenticLens realism audit across light, texture, camera behavior, artifact risk, and production readiness.";
        }
    }
}

package com.authenticlens.app;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PromptEngineTest {
    @Test
    public void apparelModeKeepsGarmentLocked() {
        String result = PromptEngine.buildAudit(
                PromptEngine.MODE_APPAREL,
                "Make the product image realistic for a premium apparel page.",
                "1200 x 1600 px, 3:4"
        );

        assertTrue(result.contains("Do not redesign"));
        assertTrue(result.contains("garment"));
        assertTrue(result.contains("Apparel Product QA"));
        assertTrue(result.contains("CORRECTION PROMPT"));
    }

    @Test
    public void unknownModeFallsBackToGeneralAudit() {
        String result = PromptEngine.buildAudit("Unknown", "", "");
        assertTrue(result.contains(PromptEngine.MODE_GENERAL));
        assertTrue(result.contains("REALISM SCORECARD"));
    }

    @Test
    public void mobileModeMentionsPhoneRealism() {
        String result = PromptEngine.buildAudit(PromptEngine.MODE_MOBILE, "BTS street image", "1080 x 1920 px");
        assertTrue(result.contains("modern phone camera"));
        assertTrue(result.contains("sensor noise"));
    }
}

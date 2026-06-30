package com.authenticlens.app;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PromptEngineTest {
    @Test
    public void gradeBoundariesWork() {
        assertEquals("A+", ImageAnalyzer.gradeForScore(97));
        assertEquals("A", ImageAnalyzer.gradeForScore(91));
        assertEquals("B+", ImageAnalyzer.gradeForScore(82));
        assertEquals("C", ImageAnalyzer.gradeForScore(61));
        assertEquals("F", ImageAnalyzer.gradeForScore(42));
    }

    @Test
    public void unknownModeFallsBackToGeneralAudit() {
        assertEquals(AuditRules.MODE_GENERAL, AuditRules.normalizeMode("Unknown"));
        assertEquals(AuditRules.MODE_GENERAL, AuditRules.normalizeMode(null));
    }

    @Test
    public void auditModesIncludeApparelAndMobile() {
        assertTrue(AuditRules.MODES.contains(AuditRules.MODE_APPAREL));
        assertTrue(AuditRules.MODES.contains(AuditRules.MODE_MOBILE));
        assertTrue(AuditRules.modeDescription(AuditRules.MODE_APPAREL).contains("product"));
    }

    @Test
    public void clampProtectsScoreRange() {
        assertEquals(0.0, ImageAnalyzer.clamp(-20, 0, 100), 0.001);
        assertEquals(50.0, ImageAnalyzer.clamp(50, 0, 100), 0.001);
        assertEquals(100.0, ImageAnalyzer.clamp(140, 0, 100), 0.001);
    }
}

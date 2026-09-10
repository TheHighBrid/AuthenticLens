package com.authenticlens.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ImageAnalyzer {
    private static final int MAX_ANALYSIS_SIDE = 900;

    private ImageAnalyzer() {}

    public static String analyze(Context context, Uri uri, String mode, String userNotes) throws Exception {
        String safeMode = AuditRules.normalizeMode(mode);
        ImageMetrics m = measure(context, uri);
        score(m, safeMode);
        return report(m, safeMode, userNotes == null ? "" : userNotes.trim());
    }

    public static String gradeForScore(double score) {
        if (score >= 95) return "A+";
        if (score >= 90) return "A";
        if (score >= 85) return "A-";
        if (score >= 80) return "B+";
        if (score >= 75) return "B";
        if (score >= 70) return "B-";
        if (score >= 65) return "C+";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static ImageMetrics measure(Context context, Uri uri) throws Exception {
        ImageMetrics m = new ImageMetrics();
        readFileMeta(context, uri, m);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IllegalArgumentException("Could not decode image dimensions.");
        }
        m.originalWidth = bounds.outWidth;
        m.originalHeight = bounds.outHeight;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_ANALYSIS_SIDE);
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap bitmap;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(stream, null, opts);
        }
        if (bitmap == null) {
            throw new IllegalArgumentException("Could not decode image.");
        }

        m.analyzedWidth = bitmap.getWidth();
        m.analyzedHeight = bitmap.getHeight();
        computePixelStats(bitmap, m);
        bitmap.recycle();
        return m;
    }

    private static void readFileMeta(Context context, Uri uri, ImageMetrics m) {
        m.fileName = "selected image";
        m.fileSizeBytes = -1L;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex >= 0) m.fileName = cursor.getString(nameIndex);
                if (sizeIndex >= 0) m.fileSizeBytes = cursor.getLong(sizeIndex);
            }
        } catch (Exception ignored) {}
    }

    private static int sampleSize(int width, int height, int maxSide) {
        int sample = 1;
        while ((width / sample) > maxSide || (height / sample) > maxSide) {
            sample *= 2;
        }
        return sample;
    }

    private static void computePixelStats(Bitmap bitmap, ImageMetrics m) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int n = w * h;
        double[] luma = new double[n];
        float[] hsv = new float[3];

        double sumL = 0, sumL2 = 0, minL = 255, maxL = 0;
        double sumSat = 0, sumSat2 = 0;
        double sumR = 0, sumG = 0, sumB = 0;
        int shadow = 0, highlight = 0;

        int[] row = new int[w];
        for (int y = 0; y < h; y++) {
            bitmap.getPixels(row, 0, w, 0, y, w, 1);
            for (int x = 0; x < w; x++) {
                int color = row[x];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
                luma[y * w + x] = lum;
                sumL += lum;
                sumL2 += lum * lum;
                minL = Math.min(minL, lum);
                maxL = Math.max(maxL, lum);
                if (lum < 18) shadow++;
                if (lum > 238) highlight++;

                Color.RGBToHSV(r, g, b, hsv);
                double sat = hsv[1];
                sumSat += sat;
                sumSat2 += sat * sat;
                sumR += r;
                sumG += g;
                sumB += b;
            }
        }

        m.meanLuma = sumL / n;
        m.lumaStdDev = Math.sqrt(Math.max(0, sumL2 / n - m.meanLuma * m.meanLuma));
        m.dynamicRange = maxL - minL;
        m.meanSaturation = sumSat / n;
        m.saturationStdDev = Math.sqrt(Math.max(0, sumSat2 / n - m.meanSaturation * m.meanSaturation));
        m.shadowClipPercent = 100.0 * shadow / n;
        m.highlightClipPercent = 100.0 * highlight / n;
        m.redMean = sumR / n;
        m.greenMean = sumG / n;
        m.blueMean = sumB / n;
        m.colorCast = (Math.max(m.redMean, Math.max(m.greenMean, m.blueMean)) - Math.min(m.redMean, Math.min(m.greenMean, m.blueMean))) / 255.0;

        computeStructureStats(luma, w, h, m);
    }

    private static void computeStructureStats(double[] luma, int w, int h, ImageMetrics m) {
        double lapSum = 0, lapSq = 0;
        int lapCount = 0, edgeCount = 0, flatCount = 0;
        double residualSum = 0;
        double boundaryDiff = 0, normalDiff = 0;
        int boundaryCount = 0, normalCount = 0;

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int idx = y * w + x;
                double c = luma[idx];
                double left = luma[idx - 1];
                double right = luma[idx + 1];
                double up = luma[idx - w];
                double down = luma[idx + w];
                double lap = 4 * c - left - right - up - down;
                lapSum += lap;
                lapSq += lap * lap;
                lapCount++;

                double gx = Math.abs(right - left);
                double gy = Math.abs(down - up);
                double grad = gx + gy;
                if (grad > 38) edgeCount++;
                if (grad < 10) {
                    flatCount++;
                    residualSum += Math.abs(c - ((left + right + up + down) / 4.0));
                }

                double hd = Math.abs(c - right);
                double vd = Math.abs(c - down);
                if (x % 8 == 0) {
                    boundaryDiff += hd;
                    boundaryCount++;
                } else {
                    normalDiff += hd;
                    normalCount++;
                }
                if (y % 8 == 0) {
                    boundaryDiff += vd;
                    boundaryCount++;
                } else {
                    normalDiff += vd;
                    normalCount++;
                }
            }
        }

        double lapMean = lapCount == 0 ? 0 : lapSum / lapCount;
        m.sharpness = Math.sqrt(Math.max(0, lapSq / Math.max(1, lapCount) - lapMean * lapMean));
        m.edgeDensity = 100.0 * edgeCount / Math.max(1, lapCount);
        m.grainNoise = flatCount == 0 ? 0 : residualSum / flatCount;
        double bAvg = boundaryDiff / Math.max(1, boundaryCount);
        double nAvg = normalDiff / Math.max(1, normalCount);
        m.jpegBlockiness = nAvg <= 0.01 ? 0 : Math.max(0, (bAvg / nAvg) - 1.0);
    }

    private static void score(ImageMetrics m, String mode) {
        double brightnessPenalty = Math.abs(m.meanLuma - 128) / 128.0 * 8.0;
        double contrastPenalty = Math.abs(m.lumaStdDev - 55) / 55.0 * 5.0;
        double clipPenalty = (m.shadowClipPercent + m.highlightClipPercent) * 0.55;
        double castPenalty = m.colorCast * 8.0;
        m.lightScore = clamp(20 - brightnessPenalty - contrastPenalty - clipPenalty - castPenalty, 0, 20);

        double sharpnessScore = clamp((m.sharpness - 4) / 18.0 * 10.0, 0, 10);
        double grainPenalty = m.grainNoise > 12 ? (m.grainNoise - 12) * 0.35 : 0;
        double saturationPenalty = m.meanSaturation > 0.72 ? (m.meanSaturation - 0.72) * 18 : 0;
        double flatPenalty = m.edgeDensity < 3 ? (3 - m.edgeDensity) * 1.6 : 0;
        m.textureScore = clamp(8 + sharpnessScore - grainPenalty - saturationPenalty - flatPenalty, 0, 20);

        double cameraBase = 15;
        cameraBase -= m.sharpness < 6 ? (6 - m.sharpness) * 0.8 : 0;
        cameraBase -= m.jpegBlockiness > 0.35 ? (m.jpegBlockiness - 0.35) * 6 : 0;
        cameraBase -= m.dynamicRange < 140 ? (140 - m.dynamicRange) / 140.0 * 4 : 0;
        cameraBase -= m.grainNoise < 0.5 ? 1.5 : 0;
        m.cameraScore = clamp(cameraBase, 0, 15);

        double artifactBase = 15;
        artifactBase -= m.edgeDensity < 2.4 ? (2.4 - m.edgeDensity) * 2.2 : 0;
        artifactBase -= m.meanSaturation > 0.78 ? (m.meanSaturation - 0.78) * 12 : 0;
        artifactBase -= m.sharpness < 4 ? (4 - m.sharpness) * 1.0 : 0;
        artifactBase -= m.jpegBlockiness > 0.8 ? (m.jpegBlockiness - 0.8) * 4 : 0;
        artifactBase -= (m.shadowClipPercent + m.highlightClipPercent) > 18 ? 3 : 0;
        m.artifactScore = clamp(artifactBase, 0, 15);

        double contextBase = 15;
        contextBase -= m.lumaStdDev < 22 ? (22 - m.lumaStdDev) * 0.2 : 0;
        contextBase -= m.colorCast > 0.34 ? (m.colorCast - 0.34) * 10 : 0;
        contextBase -= m.edgeDensity < 1.8 ? 2 : 0;
        contextBase -= m.edgeDensity > 28 ? 2 : 0;
        m.contextScore = clamp(contextBase, 0, 15);

        double productionBase = 15;
        int minSide = Math.min(m.originalWidth, m.originalHeight);
        productionBase -= minSide < 900 ? (900 - minSide) / 900.0 * 4 : 0;
        productionBase -= m.highlightClipPercent > 6 ? (m.highlightClipPercent - 6) * 0.45 : 0;
        productionBase -= m.shadowClipPercent > 9 ? (m.shadowClipPercent - 9) * 0.35 : 0;
        productionBase -= m.meanSaturation > 0.8 ? 2.5 : 0;
        m.productionScore = clamp(productionBase, 0, 15);

        applyModeWeights(m, mode);
    }

    private static void applyModeWeights(ImageMetrics m, String mode) {
        double total = m.lightScore + m.textureScore + m.cameraScore + m.artifactScore + m.contextScore + m.productionScore;
        if (AuditRules.MODE_APPAREL.equals(mode)) {
            total += clamp(m.textureScore - 14, -3, 3);
            total += clamp(m.productionScore - 11, -2, 2);
            if (m.meanSaturation > 0.68) total -= 2;
            if (m.highlightClipPercent > 5) total -= 2;
        } else if (AuditRules.MODE_ARTIFACTS.equals(mode)) {
            total += clamp(m.artifactScore - 10, -5, 5);
            if (m.edgeDensity < 2.0) total -= 3;
            if (m.sharpness < 4.0) total -= 3;
        } else if (AuditRules.MODE_PHYSICS.equals(mode)) {
            total += clamp(m.lightScore - 14, -4, 4);
            total += clamp(m.cameraScore - 10, -3, 3);
        } else if (AuditRules.MODE_MOBILE.equals(mode)) {
            if (m.grainNoise > 0.7 && m.grainNoise < 9) total += 2;
            if (m.jpegBlockiness > 1.2) total -= 3;
            if (m.sharpness > 32) total -= 2;
        } else if (AuditRules.MODE_STREET.equals(mode)) {
            total += clamp(m.contextScore - 10, -4, 4);
            if (m.edgeDensity < 2.2) total -= 3;
        }
        m.totalScore = clamp(total, 0, 100);
    }

    private static String report(ImageMetrics m, String mode, String userNotes) {
        List<String> issues = issues(m, mode);
        List<String> fixes = fixes(m, mode);
        StringBuilder out = new StringBuilder();

        out.append("AUTHENTICLENS IMAGE AUDIT\n");
        out.append("Mode: ").append(mode).append("\n");
        out.append("Grade: ").append(gradeForScore(m.totalScore)).append("\n");
        out.append("Score: ").append(fmt(m.totalScore)).append(" / 100\n");
        out.append("Verdict: ").append(verdict(m.totalScore)).append("\n\n");

        out.append("MODE LOGIC\n").append(AuditRules.modeDescription(mode)).append("\n\n");
        if (!userNotes.isEmpty()) {
            out.append("USER GOAL\n").append(userNotes).append("\n\n");
        }

        out.append("IMAGE FACTS\n");
        out.append("- File: ").append(m.fileName).append("\n");
        out.append("- Original size: ").append(m.originalWidth).append(" x ").append(m.originalHeight).append(" px\n");
        out.append("- Analyzed sample: ").append(m.analyzedWidth).append(" x ").append(m.analyzedHeight).append(" px\n");
        if (m.fileSizeBytes > 0) out.append("- File weight: ").append(fmt(m.fileSizeBytes / 1024.0 / 1024.0)).append(" MB\n");
        out.append("- Aspect ratio: ").append(aspect(m.originalWidth, m.originalHeight)).append("\n\n");

        out.append("SCORE BREAKDOWN\n");
        out.append("- Light / shadow physics: ").append(fmt(m.lightScore)).append(" / 20\n");
        out.append("- Texture / material detail: ").append(fmt(m.textureScore)).append(" / 20\n");
        out.append("- Camera / lens believability: ").append(fmt(m.cameraScore)).append(" / 15\n");
        out.append("- AI artifact risk: ").append(fmt(m.artifactScore)).append(" / 15\n");
        out.append("- Context realism: ").append(fmt(m.contextScore)).append(" / 15\n");
        out.append("- Production readiness: ").append(fmt(m.productionScore)).append(" / 15\n\n");

        out.append("MEASURED SIGNALS\n");
        out.append("- Brightness mean: ").append(fmt(m.meanLuma)).append(" / 255\n");
        out.append("- Contrast std dev: ").append(fmt(m.lumaStdDev)).append("\n");
        out.append("- Dynamic range: ").append(fmt(m.dynamicRange)).append(" / 255\n");
        out.append("- Saturation mean: ").append(fmt(m.meanSaturation)).append("\n");
        out.append("- Shadow clipping: ").append(fmt(m.shadowClipPercent)).append("%\n");
        out.append("- Highlight clipping: ").append(fmt(m.highlightClipPercent)).append("%\n");
        out.append("- Sharpness: ").append(fmt(m.sharpness)).append("\n");
        out.append("- Grain/noise estimate: ").append(fmt(m.grainNoise)).append("\n");
        out.append("- Edge density: ").append(fmt(m.edgeDensity)).append("%\n");
        out.append("- JPEG blockiness estimate: ").append(fmt(m.jpegBlockiness)).append("\n");
        out.append("- Color cast index: ").append(fmt(m.colorCast)).append("\n\n");

        out.append("DETECTED ISSUES\n");
        if (issues.isEmpty()) {
            out.append("- No major low-level realism failures detected. Run a human visual pass for anatomy, text, logos, and scene logic.\n");
        } else {
            for (String issue : issues) out.append("- ").append(issue).append("\n");
        }

        out.append("\nFIX LIST\n");
        if (fixes.isEmpty()) {
            out.append("- Keep export settings consistent and compare against approved references before publishing.\n");
        } else {
            for (String fix : fixes) out.append("- ").append(fix).append("\n");
        }

        out.append("\nLIMITS\n");
        out.append("- This offline version measures photographic/technical realism from pixels. It cannot fully understand hands, garment identity, brand logos, or scene meaning without a vision model.\n");
        out.append("- It still grades the image directly inside the app. It is not a prompt-only tool.\n");
        return out.toString();
    }

    private static List<String> issues(ImageMetrics m, String mode) {
        List<String> list = new ArrayList<>();
        if (m.meanLuma < 72) list.add("Image is very dark; product/subject detail may collapse into black areas.");
        if (m.meanLuma > 188) list.add("Image is too bright; realism can feel washed out or overexposed.");
        if (m.lumaStdDev < 24) list.add("Low contrast; image may look flat, synthetic, or overly smoothed.");
        if (m.lumaStdDev > 92) list.add("Very aggressive contrast; shadows/highlights may feel crushed.");
        if (m.shadowClipPercent > 10) list.add("Shadow clipping is high; dark regions are losing recoverable detail.");
        if (m.highlightClipPercent > 7) list.add("Highlight clipping is high; bright zones are blown out.");
        if (m.sharpness < 5) list.add("Low sharpness; details may look smeared, soft, or AI-polished.");
        if (m.sharpness > 40) list.add("Over-sharpened edges; image may look crunchy or aggressively processed.");
        if (m.meanSaturation > 0.78) list.add("Saturation is high; color may look artificial or retail-rendered.");
        if (m.grainNoise < 0.35) list.add("Very low micro-noise; image may look sterile instead of camera-captured.");
        if (m.grainNoise > 14) list.add("Heavy noise/grain; image may look degraded or low-light compressed.");
        if (m.edgeDensity < 2.0) list.add("Low edge structure; possible over-smoothing, weak texture, or synthetic surface detail.");
        if (m.jpegBlockiness > 1.0) list.add("Compression/block artifacts detected; image may not feel clean enough for premium use.");
        if (m.colorCast > 0.38) list.add("Strong color cast; white balance may be unrealistic or inconsistent.");
        if (AuditRules.MODE_APPAREL.equals(mode) && Math.min(m.originalWidth, m.originalHeight) < 1200) list.add("Apparel image resolution is low for premium product-page use.");
        return list;
    }

    private static List<String> fixes(ImageMetrics m, String mode) {
        List<String> list = new ArrayList<>();
        if (m.meanLuma < 72) list.add("Raise exposure carefully while protecting black fabric and shadow depth.");
        if (m.meanLuma > 188) list.add("Lower exposure and recover highlight detail so the image stops feeling washed out.");
        if (m.lumaStdDev < 24) list.add("Add controlled local contrast and texture separation, not global harsh contrast.");
        if (m.lumaStdDev > 92) list.add("Soften contrast curve and restore midtones for more natural photographic depth.");
        if (m.shadowClipPercent > 10) list.add("Lift crushed shadows only where details should exist; keep real contact shadows grounded.");
        if (m.highlightClipPercent > 7) list.add("Recover highlights and reduce glossy hotspots.");
        if (m.sharpness < 5) list.add("Add realistic detail clarity around edges, seams, fabric texture, eyes, or product contours.");
        if (m.sharpness > 40) list.add("Reduce sharpening halos and edge crunch.");
        if (m.meanSaturation > 0.78) list.add("Pull saturation down and use a more natural color grade.");
        if (m.grainNoise < 0.35) list.add("Add subtle sensor grain or compression texture so it stops looking sterile.");
        if (m.grainNoise > 14) list.add("Apply denoise selectively while preserving real fabric/material detail.");
        if (m.edgeDensity < 2.0) list.add("Restore micro-texture and believable object boundaries without changing the design.");
        if (m.jpegBlockiness > 1.0) list.add("Re-export from a cleaner source at higher quality; avoid stacked social-media compression.");
        if (m.colorCast > 0.38) list.add("Correct white balance and channel tint so neutrals feel physically plausible.");
        if (AuditRules.MODE_APPAREL.equals(mode)) {
            list.add("For product QA, compare this result against approved front/back/45/macro references for scale, hem, sleeve, collar, and shadow consistency.");
        }
        return list;
    }

    private static String verdict(double score) {
        if (score >= 90) return "Premium-ready from a technical realism standpoint. Still do a human semantic check.";
        if (score >= 80) return "Strong, with a few realism or export issues to clean up.";
        if (score >= 70) return "Usable draft, but not premium-final yet.";
        if (score >= 60) return "Needs visible correction before serious use.";
        return "Fails AuthenticLens realism standards. Rework before publishing.";
    }

    private static String aspect(int width, int height) {
        if (width <= 0 || height <= 0) return "unknown";
        int g = gcd(width, height);
        return (width / g) + ":" + (height / g);
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return Math.abs(a);
    }

    private static String fmt(double value) {
        return String.format(Locale.US, "%.1f", value);
    }
}

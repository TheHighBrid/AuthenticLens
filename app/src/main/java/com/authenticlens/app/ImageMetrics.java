package com.authenticlens.app;

public final class ImageMetrics {
    public int originalWidth;
    public int originalHeight;
    public int analyzedWidth;
    public int analyzedHeight;
    public long fileSizeBytes;
    public String fileName;

    public double meanLuma;
    public double lumaStdDev;
    public double dynamicRange;
    public double meanSaturation;
    public double saturationStdDev;
    public double shadowClipPercent;
    public double highlightClipPercent;
    public double sharpness;
    public double grainNoise;
    public double edgeDensity;
    public double jpegBlockiness;
    public double colorCast;
    public double redMean;
    public double greenMean;
    public double blueMean;

    public double lightScore;
    public double textureScore;
    public double cameraScore;
    public double artifactScore;
    public double contextScore;
    public double productionScore;
    public double totalScore;
}

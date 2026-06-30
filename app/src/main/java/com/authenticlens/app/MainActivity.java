package com.authenticlens.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE_REQUEST = 4107;

    private final int black = Color.rgb(13, 13, 15);
    private final int surface = Color.rgb(23, 23, 27);
    private final int text = Color.rgb(244, 241, 234);
    private final int muted = Color.rgb(184, 176, 165);
    private final int gold = Color.rgb(216, 180, 106);

    private String selectedMode = AuditRules.MODE_GENERAL;
    private Uri selectedImageUri;
    private EditText notesInput;
    private TextView outputView;
    private TextView metaView;
    private ImageView preview;
    private LinearLayout modeRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("AuthenticLens");
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(black);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(22), dp(18), dp(32));
        scroll.addView(root);

        TextView title = textView("AuthenticLens", 32, gold, true);
        root.addView(title);

        TextView subtitle = textView("Offline image realism audits powered by the AuthenticLens rule base: score, grade, diagnose, and fix the selected image inside the app.", 15, muted, false);
        subtitle.setPadding(0, dp(8), 0, dp(18));
        root.addView(subtitle);

        root.addView(sectionLabel("Audit mode"));
        HorizontalScrollView modeScroller = new HorizontalScrollView(this);
        modeScroller.setHorizontalScrollBarEnabled(false);
        modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeScroller.addView(modeRow);
        root.addView(modeScroller);
        renderModeButtons();

        Button choose = button("Choose image");
        choose.setOnClickListener(v -> chooseImage());
        root.addView(choose);

        preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setMaxHeight(dp(320));
        preview.setBackgroundColor(surface);
        preview.setPadding(dp(8), dp(8), dp(8), dp(8));
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        previewParams.setMargins(0, dp(12), 0, dp(8));
        root.addView(preview, previewParams);

        metaView = smallCard("No image selected yet.");
        root.addView(metaView);

        root.addView(sectionLabel("Notes / goal"));
        notesInput = new EditText(this);
        notesInput.setTextColor(text);
        notesInput.setHintTextColor(muted);
        notesInput.setHint("Example: Audit this as a premium Melato apparel product image.");
        notesInput.setMinLines(3);
        notesInput.setGravity(Gravity.TOP);
        notesInput.setTextSize(15);
        notesInput.setBackgroundColor(surface);
        notesInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        root.addView(notesInput, matchWrapWithMargins(0, dp(8), 0, dp(12)));

        Button generate = button("Run AuthenticLens audit");
        generate.setOnClickListener(v -> runAudit());
        root.addView(generate);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setPadding(0, dp(10), 0, dp(10));
        Button copy = button("Copy audit");
        copy.setOnClickListener(v -> copyOutput());
        Button share = button("Share audit");
        share.setOnClickListener(v -> shareOutput());
        actionRow.addView(copy, weightWrap());
        actionRow.addView(share, weightWrap());
        root.addView(actionRow);

        outputView = smallCard("Choose an image, pick an audit mode, then run the AuthenticLens audit. The app will grade the image directly from pixel metrics and rule-based scoring.");
        outputView.setTextIsSelectable(true);
        root.addView(outputView);

        setContentView(scroll);
    }

    private void renderModeButtons() {
        modeRow.removeAllViews();
        for (String mode : AuditRules.MODES) {
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(mode);
            b.setTextSize(13);
            b.setTextColor(mode.equals(selectedMode) ? black : text);
            b.setBackgroundColor(mode.equals(selectedMode) ? gold : surface);
            b.setPadding(dp(12), dp(8), dp(12), dp(8));
            b.setOnClickListener(v -> {
                selectedMode = mode;
                renderModeButtons();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, dp(8), dp(12));
            modeRow.addView(b, params);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {
                // Some providers do not grant persistable permissions. The selected session still works.
            }
            preview.setImageURI(selectedImageUri);
            metaView.setText(describeSelectedFile(selectedImageUri));
            outputView.setText("Image loaded. Tap Run AuthenticLens audit to score and diagnose it.");
        }
    }

    private String describeSelectedFile(Uri uri) {
        String name = "selected image";
        long size = -1L;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIndex >= 0) name = cursor.getString(nameIndex);
                if (sizeIndex >= 0) size = cursor.getLong(sizeIndex);
            }
        } catch (Exception ignored) {}
        String sizeText = size > 0 ? String.format(Locale.US, "%.2f MB", size / 1024f / 1024f) : "file size unavailable";
        return "Selected: " + name + "\n" + sizeText + "\nReady for direct AuthenticLens analysis.";
    }

    private void runAudit() {
        if (selectedImageUri == null) {
            outputView.setText("Choose an image first. AuthenticLens needs actual pixels to grade, not a blank prompt.");
            return;
        }
        try {
            outputView.setText("Analyzing image pixels...");
            String result = ImageAnalyzer.analyze(this, selectedImageUri, selectedMode, notesInput.getText().toString());
            outputView.setText(result);
        } catch (Exception error) {
            outputView.setText("Audit failed: " + error.getMessage());
        }
    }

    private void copyOutput() {
        String value = outputView.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("AuthenticLens audit", value));
        Toast.makeText(this, "Audit copied", Toast.LENGTH_SHORT).show();
    }

    private void shareOutput() {
        String value = outputView.getText().toString();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "AuthenticLens Image Audit");
        send.putExtra(Intent.EXTRA_TEXT, value);
        startActivity(Intent.createChooser(send, "Share AuthenticLens audit"));
    }

    private TextView textView(String value, int sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private TextView sectionLabel(String value) {
        TextView tv = textView(value.toUpperCase(Locale.US), 12, gold, true);
        tv.setLetterSpacing(0.12f);
        tv.setPadding(0, dp(18), 0, dp(8));
        return tv;
    }

    private TextView smallCard(String value) {
        TextView tv = textView(value, 14, text, false);
        tv.setBackgroundColor(surface);
        tv.setPadding(dp(12), dp(12), dp(12), dp(12));
        tv.setLineSpacing(2f, 1.08f);
        return tv;
    }

    private Button button(String value) {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setText(value);
        b.setTextSize(14);
        b.setTextColor(black);
        b.setBackgroundColor(gold);
        return b;
    }

    private LinearLayout.LayoutParams matchWrapWithMargins(int l, int t, int r, int b) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(l, t, r, b);
        return params;
    }

    private LinearLayout.LayoutParams weightWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

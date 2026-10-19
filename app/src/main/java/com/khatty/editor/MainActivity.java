package com.khatty.editor;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private ImageView imageView;
    private EditText editText;
    private Bitmap currentBitmap;
    private Button btnPick, btnAddText, btnSave, btnSaveProject, btnLoadProject;
    private Uri currentImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        btnPick = findViewById(R.id.btnPick);
        btnAddText = findViewById(R.id.btnAddText);
        btnSave = findViewById(R.id.btnSave);
        btnSaveProject = findViewById(R.id.btnSaveProject);
        btnLoadProject = findViewById(R.id.btnLoadProject);

        btnPick.setOnClickListener(v -> openImagePicker());

        btnAddText.setOnClickListener(v -> {
            String text = editText.getText().toString();
            if (currentBitmap == null) {
                Toast.makeText(this, R.string.no_image, Toast.LENGTH_SHORT).show();
                return;
            }
            if (text.trim().isEmpty()) {
                Toast.makeText(this, R.string.enter_text, Toast.LENGTH_SHORT).show();
                return;
            }
            currentBitmap = drawTextOnBitmap(currentBitmap, text);
            imageView.setImageBitmap(currentBitmap);
        });

        btnSave.setOnClickListener(v -> {
            if (currentBitmap == null) {
                Toast.makeText(this, R.string.no_image, Toast.LENGTH_SHORT).show();
                return;
            }
            saveBitmapToGallery(currentBitmap);
        });

        btnSaveProject.setOnClickListener(v -> {
            if (currentBitmap == null || currentImageUri == null) {
                Toast.makeText(this, R.string.no_image, Toast.LENGTH_SHORT).show();
                return;
            }
            saveProject();
        });

        btnLoadProject.setOnClickListener(v -> loadLatestProject());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        // Persist permission
                        final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        }
                        currentImageUri = uri;
                        Bitmap bmp = loadBitmapFromUri(uri);
                        if (bmp != null) {
                            currentBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            imageView.setImageBitmap(currentBitmap);
                        } else {
                            Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, R.string.open_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private Bitmap loadBitmapFromUri(Uri uri) throws IOException {
        // Decode with sampling to avoid OOM on large images
        InputStream is = getContentResolver().openInputStream(uri);
        if (is == null) return null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
        } finally {
            is.close();
        }

        int maxDim = 2048; // limit
        is = getContentResolver().openInputStream(uri);
        if (is == null) return null;
        try {
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = calculateInSampleSize(options2, maxDim, maxDim);
            Bitmap bmp = BitmapFactory.decodeStream(is, null, options2);
            return bmp;
        } finally {
            is.close();
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Simple sample size calculation
        int inSampleSize = 1;
        int width = options.outWidth > 0 ? options.outWidth : reqWidth;
        int height = options.outHeight > 0 ? options.outHeight : reqHeight;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap drawTextOnBitmap(Bitmap bitmap, String text) {
        Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutable);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF000000);
        paint.setTextSize(Math.max(24, mutable.getWidth() / 15));
        paint.setTextAlign(Paint.Align.CENTER);
        // Draw text at center
        int x = mutable.getWidth() / 2;
        int y = mutable.getHeight() / 2;
        canvas.drawText(text, x, y, paint);
        return mutable;
    }

    private void saveBitmapToGallery(Bitmap bitmap) {
        String displayName = "khatty_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";
        OutputStream fos = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Khatty");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("Failed to create new MediaStore record.");
                fos = getContentResolver().openOutputStream(uri);
            } else {
                // Fallback for older devices - try to write to MediaStore
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                fos = getContentResolver().openOutputStream(uri);
            }

            if (fos == null) throw new IOException("Failed to get output stream.");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, R.string.save_success, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private void saveProject() {
        ProjectModel p = new ProjectModel();
        p.name = "Project";
        p.imageUri = currentImageUri != null ? currentImageUri.toString() : null;
        p.text = editText.getText().toString();
        p.textSize = currentBitmap != null ? currentBitmap.getWidth() / 15f : 48f;
        p.textColor = 0xFF000000;

        // create a thumbnail
        Bitmap thumb = null;
        if (currentBitmap != null) {
            thumb = Bitmap.createScaledBitmap(currentBitmap, Math.max(100, currentBitmap.getWidth()/10), Math.max(100, currentBitmap.getHeight()/10), true);
        }

        try {
            String path = ProjectManager.saveProject(this, p, thumb);
            Toast.makeText(this, getString(R.string.save_success) + ": " + path, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void loadLatestProject() {
        ProjectModel p = ProjectManager.loadLatestProject(this);
        if (p == null) {
            Toast.makeText(this, "لا يوجد مشاريع محفوظة", Toast.LENGTH_SHORT).show();
            return;
        }
        if (p.imageUri != null) {
            try {
                Uri uri = Uri.parse(p.imageUri);
                currentImageUri = uri;
                Bitmap bmp = loadBitmapFromUri(uri);
                if (bmp != null) {
                    currentBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                    imageView.setImageBitmap(currentBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "فشل تحميل صورة المشروع، الرجاء إعادة اختيار الصورة", Toast.LENGTH_LONG).show();
            }
        }
        if (p.text != null) {
            editText.setText(p.text);
        }
        Toast.makeText(this, "تم فتح المشروع: " + p.id, Toast.LENGTH_SHORT).show();
    }
}

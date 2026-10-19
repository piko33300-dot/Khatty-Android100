package com.khatty.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ProjectManager {
    private static final String TAG = "ProjectManager";

    public static File getProjectsDir(Context ctx) {
        File dir = new File(ctx.getFilesDir(), "projects");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static String saveProject(Context ctx, ProjectModel project, Bitmap thumbnail) throws IOException {
        File dir = getProjectsDir(ctx);
        String id = project.id != null ? project.id : new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        project.id = id;
        String filename = "project_" + id + ".json";
        File out = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            JSONObject o = project.toJson();
            fos.write(o.toString(2).getBytes("UTF-8"));
            fos.flush();
        }
        if (thumbnail != null) {
            File thumb = new File(dir, "thumb_" + id + ".png");
            try (OutputStream os = new FileOutputStream(thumb)) {
                thumbnail.compress(Bitmap.CompressFormat.PNG, 90, os);
                os.flush();
            }
        }
        return out.getAbsolutePath();
    }

    public static ProjectModel loadLatestProject(Context ctx) {
        File dir = getProjectsDir(ctx);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("project_") && s.endsWith(".json");
            }
        });
        if (files == null || files.length == 0) return null;
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o2.lastModified(), o1.lastModified());
            }
        });
        File latest = files[0];
        try {
            String content = new String(java.nio.file.Files.readAllBytes(latest.toPath()), "UTF-8");
            JSONObject o = new JSONObject(content);
            return ProjectModel.fromJson(o);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load project", e);
            return null;
        }
    }
}

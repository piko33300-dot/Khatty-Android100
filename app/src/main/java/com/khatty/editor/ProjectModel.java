package com.khatty.editor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ProjectModel implements Serializable {
    public String id;
    public String name;
    public String imageUri;
    public String text;
    public float textSize;
    public int textColor;

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("name", name);
        o.put("imageUri", imageUri);
        o.put("text", text);
        o.put("textSize", textSize);
        o.put("textColor", textColor);
        return o;
    }

    public static ProjectModel fromJson(JSONObject o) throws JSONException {
        ProjectModel p = new ProjectModel();
        p.id = o.optString("id");
        p.name = o.optString("name");
        p.imageUri = o.optString("imageUri");
        p.text = o.optString("text");
        p.textSize = (float) o.optDouble("textSize", 0);
        p.textColor = o.optInt("textColor", 0xFF000000);
        return p;
    }
}

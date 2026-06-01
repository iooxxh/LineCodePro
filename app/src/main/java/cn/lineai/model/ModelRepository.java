package cn.lineai.model;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

public final class ModelRepository {
    private static final String PREFS = "linecode_models";
    private static final String KEY_MODELS = "models";
    private static final String KEY_SELECTED_ID = "selected_id";

    private final SharedPreferences preferences;

    public ModelRepository(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public synchronized List<ModelConfig> getModels() {
        ArrayList<ModelConfig> models = new ArrayList<>();
        String raw = preferences.getString(KEY_MODELS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                models.add(ModelConfig.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException ignored) {
            preferences.edit().putString(KEY_MODELS, "[]").apply();
        }
        return models;
    }

    public synchronized ModelConfig save(ModelConfig model) {
        ArrayList<ModelConfig> models = new ArrayList<>(getModels());
        ModelConfig normalized = model.getId().length() == 0
                ? model.withId(String.valueOf(System.currentTimeMillis()))
                : model;
        boolean replaced = false;
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).getId().equals(normalized.getId())) {
                models.set(i, normalized);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            models.add(normalized);
        }
        persist(models);
        if (getSelectedModelId().length() == 0) {
            setSelectedModelId(normalized.getId());
        }
        return normalized;
    }

    public synchronized void setSelectedModelId(String id) {
        preferences.edit().putString(KEY_SELECTED_ID, id == null ? "" : id).apply();
    }

    public synchronized String getSelectedModelId() {
        return preferences.getString(KEY_SELECTED_ID, "");
    }

    public synchronized ModelConfig getSelectedModel() {
        String selectedId = getSelectedModelId();
        List<ModelConfig> models = getModels();
        for (ModelConfig model : models) {
            if (model.getId().equals(selectedId)) {
                return model;
            }
        }
        return models.isEmpty() ? null : models.get(0);
    }

    private void persist(List<ModelConfig> models) {
        JSONArray array = new JSONArray();
        for (ModelConfig model : models) {
            try {
                array.put(model.toJson());
            } catch (JSONException ignored) {
            }
        }
        preferences.edit().putString(KEY_MODELS, array.toString()).apply();
    }
}

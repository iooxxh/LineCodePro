package cn.lineai.model;

import org.json.JSONException;
import org.json.JSONObject;

public final class ModelConfig {
    private final String id;
    private final String name;
    private final ModelProtocolType protocolType;
    private final String providerLabel;
    private final String baseUrl;
    private final String apiKey;
    private final String modelId;

    public ModelConfig(String id, String name, ModelProtocolType protocolType, String providerLabel, String baseUrl, String apiKey, String modelId) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name;
        this.protocolType = protocolType == null ? ModelProtocolType.OPENAI_COMPATIBLE : protocolType;
        this.providerLabel = providerLabel == null ? this.protocolType.getLabel() : providerLabel;
        this.baseUrl = baseUrl == null ? "" : baseUrl;
        this.apiKey = apiKey == null ? "" : apiKey;
        this.modelId = modelId == null ? "" : modelId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ModelProtocolType getProtocolType() {
        return protocolType;
    }

    public String getProviderLabel() {
        return providerLabel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getModelId() {
        return modelId;
    }

    public ModelConfig withId(String nextId) {
        return new ModelConfig(nextId, name, protocolType, providerLabel, baseUrl, apiKey, modelId);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("name", name);
        object.put("protocolType", protocolType.name());
        object.put("providerLabel", providerLabel);
        object.put("baseUrl", baseUrl);
        object.put("apiKey", apiKey);
        object.put("modelId", modelId);
        return object;
    }

    public static ModelConfig fromJson(JSONObject object) {
        return new ModelConfig(
                object.optString("id"),
                object.optString("name"),
                ModelProtocolType.fromStorage(object.optString("protocolType")),
                object.optString("providerLabel"),
                object.optString("baseUrl"),
                object.optString("apiKey"),
                object.optString("modelId")
        );
    }
}

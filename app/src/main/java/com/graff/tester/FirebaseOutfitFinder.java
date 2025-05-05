package com.graff.tester;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.vertexai.FirebaseVertexAI;
import com.google.firebase.vertexai.GenerativeModel;
import com.google.firebase.vertexai.java.GenerativeModelFutures;
import com.google.firebase.vertexai.type.Content;
import com.google.firebase.vertexai.type.GenerateContentResponse;
import com.google.firebase.vertexai.type.GenerationConfig;
import com.google.firebase.vertexai.type.Schema;
import com.graff.tester.models.ClothingItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirebaseOutfitFinder implements OutfitFinder {
    private final String SHIRT_ID = "shirt_id";
    private final String PANTS_ID = "pants_id";
    private final String EXPLANATION = "explanation";
    private final String OUTFIT_FOUND = "outfit_found";


    @Override
    public void findOutfit(String outfitDescription,
                           List<ClothingItem> shirts, List<ClothingItem> pants,OnFindOutfitCallback callback) {
        String promptText = null;
        try {
            promptText = buildPrompt(shirts, pants, outfitDescription);
        } catch (JSONException e) {
            callback.onFindOutfitFailed("Failed to build prompt.");
        }

        assert promptText != null;
        Content prompt = new Content.Builder()
                .addText(promptText)
                .build();
        Executor executor = Executors.newSingleThreadExecutor();

        GenerativeModelFutures modelFutures = GenerativeModelFutures.from(getOutfitModel());
        ListenableFuture<GenerateContentResponse> response = modelFutures.generateContent(prompt);

        Futures.addCallback(response, new FutureCallback<>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String resultText = result.getText();
                    assert resultText != null;
                    JSONObject obj = new JSONObject(resultText);
                    boolean foundOutfit = obj.optBoolean(OUTFIT_FOUND, false);
                    String shirtId = obj.optString(SHIRT_ID, null);
                    String pantsId = obj.optString(PANTS_ID, null);
                    String explanation = obj.optString(EXPLANATION, null);
                    callback.onFindOutfitSuccess(shirtId, pantsId, foundOutfit, explanation);
                } catch (JSONException e) {
                    Log.w("VertexAI", "Failed to parse response: " + e);
                    callback.onFindOutfitFailed("Failed to parse response.");
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.w("VertexAI", "Failed to find outfit: " + t);
                callback.onFindOutfitFailed("Failed to find outfit.");
            }
        }, executor);
    }

    private GenerativeModel getOutfitModel() {
        // Provide a JSON schema object using a standard format.
        Schema jsonSchema = Schema.obj( /* properties */
                Map.of(SHIRT_ID, Schema.str(),
                        PANTS_ID, Schema.str(),
                        EXPLANATION, Schema.str(),
                        OUTFIT_FOUND, Schema.enumeration(List.of("true", "false"))
                )
        );
        // In the generation config, set the `responseMimeType` to `application/json`
        // and pass the JSON schema object into `responseSchema`.
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = jsonSchema;

        GenerationConfig generationConfig = configBuilder.build();

        // Initialize the Vertex AI service and the generative model.
        GenerativeModel gm = FirebaseVertexAI.getInstance().generativeModel(
                /* modelName */ "gemini-2.0-flash",
                /* generationConfig */ generationConfig);
        return gm;
    }

    private String buildPrompt(List<ClothingItem> shirts, List<ClothingItem> pants, String userInstructions) throws JSONException {
        StringBuilder prompt = new StringBuilder();

        // 1. General instruction
        prompt.append("You are an assistant helping users choose an outfit consisting of one shirt and one pair of pants.\n");
        prompt.append("You will receive a list of shirt and pant items, and the user's personal preferences.\n");
        prompt.append("Based on that, pick the best matching combination. If you don't find anything, return empty strings for the ids.\n\n");

        // 2. Insert user instructions
        prompt.append("User instructions:\n");
        prompt.append(userInstructions).append("\n\n");

        // 3. JSON data for shirts and pants
        prompt.append("Here is the available clothing:\n");

        JSONObject root = new JSONObject();
        root.put("shirts", toJSONArray(shirts, SHIRT_ID));
        root.put("pants", toJSONArray(pants, PANTS_ID));

        prompt.append(root.toString(2)).append("\n\n");

        // 4. Expected format
        prompt.append("Respond in the following JSON format:\n");
        // 4. Expected format
        prompt.append("Respond in the following JSON format:\n");
        prompt.append("{ \"" + SHIRT_ID + "\": \"\", ");
        prompt.append("\"" + PANTS_ID + "\": \"\", ");
        prompt.append("\"" + EXPLANATION + "\": \"\", ");
        prompt.append("\"" + OUTFIT_FOUND + "\": \"true\" or \"false\" }");

        return prompt.toString();
    }

    private JSONArray toJSONArray(List<ClothingItem> items, String id_name) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ClothingItem item : items) {
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                JSONObject obj = new JSONObject();
                obj.put(id_name, item.getId());
                obj.put("description", item.getDescription());
                jsonArray.put(obj);
            }
        }
        return jsonArray;
    }

}

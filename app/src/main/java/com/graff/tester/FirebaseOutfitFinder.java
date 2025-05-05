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
    @Override
    public void findOutfit(String outfitDescription,
                           List<ClothingItem> shirts, List<ClothingItem> pants) {
        try {
            String promptText = buildPrompt(shirts, pants, outfitDescription);
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
                        String shirtId = obj.optString("shirt_id", null);
                        String pantsId = obj.optString("pants_id", null);
                        String explanation = obj.optString("explanation", null);
                        //TODO - callback
                    } catch (JSONException e) {
                        Log.w("Firebase", "Failed to find outfit: " + e);
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    Log.w("Firebase", "Failed to find outfit: " + t);
                    //TODO - callback
                }
            }, executor);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.w("Firebase", "Error finding outfit", e);
        }
    }

    private GenerativeModel getOutfitModel() {
        // Provide a JSON schema object using a standard format.
        Schema jsonSchema = Schema.obj( /* properties */
                Map.of("shirt_id", Schema.str(),
                        "pants_id", Schema.str(),
                        "explanation", Schema.str()
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
        prompt.append("Based on that, pick the best matching combination.\n\n");

        // 2. Insert user instructions
        prompt.append("User instructions:\n");
        prompt.append(userInstructions).append("\n\n");

        // 3. JSON data for shirts and pants
        prompt.append("Here is the available clothing:\n");

        JSONObject root = new JSONObject();
        root.put("shirts", toJSONArray(shirts, "shirt_id"));
        root.put("pants", toJSONArray(pants, "pants_id"));

        prompt.append(root.toString(2)).append("\n\n");

        // 4. Expected format
        prompt.append("Respond in the following JSON format:\n");
        prompt.append("{ \"shirts_id\": \"\", \"pants_id\": \"\", \"explanation\": \"\" }");

        return prompt.toString();
    }

    private JSONArray toJSONArray(List<ClothingItem> items, String id_name) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ClothingItem item : items) {
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                JSONObject obj = new JSONObject();
                obj.put(id_name, item.getDocRef().getId());
                obj.put("description", item.getDescription());
                jsonArray.put(obj);
            }
        }
        return jsonArray;
    }

}

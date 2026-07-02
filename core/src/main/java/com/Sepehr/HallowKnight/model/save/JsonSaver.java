package com.Sepehr.HallowKnight.model.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class JsonSaver {
    public static void saveSlot(int slot, int masks, float soul, String mapPath, float x, float y) {
        SaveData data = new SaveData();
        data.masks = masks;
        data.soul = soul;
        data.currentMapPath = mapPath;
        data.playerX = x;
        data.playerY = y;

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        String fileName = "hollow_save_slot_" + slot + ".json";
        try {
            String jsonText = json.prettyPrint(data);
            Gdx.files.local(fileName).writeString(jsonText, false);
            System.out.println("SUCCESS: Saved state cleanly to slot file: " + fileName);
        } catch (Exception e) {
            System.err.println("ERROR: Could not write save file slot: " + e.getMessage());
        }
    }
}

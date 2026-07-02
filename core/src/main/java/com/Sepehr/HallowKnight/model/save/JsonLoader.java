package com.Sepehr.HallowKnight.model.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

public class JsonLoader {
    public static SaveData loadSlot(int slot) {
        String fileName = "hollow_save_slot_" + slot + ".json";

        if (!Gdx.files.local(fileName).exists()) {
            System.out.println("INFO: No historical data located for slot reference: " + slot);
            return null;
        }

        Json json = new Json();
        try {
            SaveData data = json.fromJson(SaveData.class, Gdx.files.local(fileName));
            System.out.println("SUCCESS: Save file mapped into memory context from slot: " + slot);
            return data;
        } catch (Exception e) {
            System.err.println("ERROR: Corrupted save data file processing exception: " + e.getMessage());
            return null;
        }
    }
}

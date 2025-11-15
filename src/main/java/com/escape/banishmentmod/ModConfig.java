package com.escape.banishmentmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {

    public static ModConfig INSTANCE = new ModConfig();

    // ------------------------------
    // CONFIG VALUES
    // ------------------------------

    public BlockPos location = new BlockPos(0, 100, 0);

    public int countdown = 5;
    public float highNotePitch = 1.5F;
    public float lowNotePitch = 0.5F;
    public int noteInterval = 5;

    public boolean useRandomOffset = true;
    public int minDistance = 50;
    public int maxDistance = 200;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File getConfigFile() {
        return new File("config/banishmentmod.json");
    }

    // ------------------------------
    // SAVE
    // ------------------------------
    public static void save() {
        try {
            File file = getConfigFile();
            file.getParentFile().mkdirs();

            FileWriter writer = new FileWriter(file);
            GSON.toJson(INSTANCE, writer);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------
    // LOAD SAFE
    // ------------------------------
    public static ModConfig loadSafe() {
        try {
            File file = getConfigFile();
            if (file.exists()) {

                FileReader reader = new FileReader(file);
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                reader.close();

            } else {
                save(); // create default
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return INSTANCE;
    }
}

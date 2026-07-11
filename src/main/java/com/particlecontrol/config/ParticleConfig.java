package com.particlecontrol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores which particle type ids are currently allowed to spawn.
 * Everything is disabled by default; only ids present in the saved
 * set (or turned on for the current session) are allowed through.
 */
public final class ParticleConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("particlecontrol");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("particles.json");

    private static Set<String> enabled = new HashSet<>();

    private ParticleConfig() {
    }

    public static void load() {
        enabled = new HashSet<>();
        if (!Files.exists(CONFIG_FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Set<String>>() {}.getType();
            Set<String> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                enabled = loaded;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(enabled, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isEnabled(Identifier id) {
        return enabled.contains(id.toString());
    }

    public static void setEnabled(Identifier id, boolean value) {
        if (value) {
            enabled.add(id.toString());
        } else {
            enabled.remove(id.toString());
        }
    }

    public static void enableAll(Iterable<Identifier> ids) {
        for (Identifier id : ids) {
            enabled.add(id.toString());
        }
    }

    public static void disableAll() {
        enabled.clear();
    }
}

package extraserverlist.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

public class TagManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("extra-serverlist-tags.json");
    private static final Type MAP_TYPE = new TypeToken<Map<String, List<TagData>>>() {}.getType();

    private static Map<String, List<TagData>> tagsByAddress = new HashMap<>();

    public static class TagData {
        public String name;
        public int color;

        public TagData(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    public static void load() {
        if (!Files.exists(FILE_PATH)) {
            tagsByAddress = new HashMap<>();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            Map<String, List<TagData>> loaded = GSON.fromJson(reader, MAP_TYPE);
            tagsByAddress = loaded != null ? loaded : new HashMap<>();
        } catch (Exception e) {
            ExtraServerList.LOGGER.error("Could not load tags file (resetting to empty)", e);
            tagsByAddress = new HashMap<>();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(tagsByAddress, MAP_TYPE, writer);
        } catch (IOException e) {
            ExtraServerList.LOGGER.error("Could not save tags file", e);
        }
    }

    public static List<TagData> getTags(String address) {
        return tagsByAddress.getOrDefault(address, new ArrayList<>());
    }

public enum AddResult {
    SUCCESS, DUPLICATE, MAX_REACHED
}

    public static AddResult addTag(String address, String name, int color) {
        List<TagData> tags = tagsByAddress.computeIfAbsent(address, k -> new ArrayList<>());
        boolean alreadyExists = tags.stream().anyMatch(t -> t.name.equalsIgnoreCase(name));
        if (alreadyExists) {
            return AddResult.DUPLICATE;
        }
        if (tags.size() >= 5) {
            return AddResult.MAX_REACHED;
        }
        tags.add(new TagData(name, color));
        save();
        return AddResult.SUCCESS;
    }

    public static void removeTag(String address, String name) {
        List<TagData> tags = tagsByAddress.get(address);
        if (tags != null) {
            tags.removeIf(t -> t.name.equalsIgnoreCase(name));
            save();
        }
    }
}
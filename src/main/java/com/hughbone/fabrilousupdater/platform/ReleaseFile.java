package com.hughbone.fabrilousupdater.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReleaseFile {
    public String fileName;
    public String fileDate;
    public String downloadUrl;
    public boolean isFabric = false;
    private String platform;
    private JsonObject json;

    public ReleaseFile(JsonObject json, String platform) {
        if (platform.equals("curseforge")) {
            this.platform = "CF";
            final JsonArray modulesArray = json.get("data").getAsJsonArray().get(0).getAsJsonObject().get("modules").getAsJsonArray();
            for (JsonElement j : modulesArray) {
                if (j.getAsJsonObject().get("name").getAsString().equals("fabric.mod.json")) {
                    isFabric = true;
                    break;
                }
            }

            if (isFabric) {
                this.fileDate = json.get("data").getAsJsonArray().get(0).getAsJsonObject().get("fileDate").getAsString();
                this.fileName = json.get("data").getAsJsonArray().get(0).getAsJsonObject().get("fileName").getAsString();
                this.downloadUrl = json.get("data").getAsJsonArray().get(0).getAsJsonObject().get("downloadUrl").getAsString();
            }
        } else if (platform.equals("modrinth")) {
            this.platform = "MR";
            final JsonArray loadersArray = json.getAsJsonArray("loaders");
            for (JsonElement j : loadersArray) {
                if (j.getAsJsonPrimitive().getAsString().contains("fabric")) {
                    isFabric = true;
                    break;
                }
            }
            if (isFabric) {
                this.fileDate = json.get("date_published").getAsString();

                final JsonArray filesArray = json.getAsJsonArray("files");
                for (JsonElement j : filesArray) {
                    String tempFile = j.getAsJsonObject().get("filename").getAsString();
                    if (!tempFile.contains("-sources") && !tempFile.contains("-dev")) {  // If multiple files uploaded, get rid of imposter ඞ ones
                        this.fileName = j.getAsJsonObject().get("filename").getAsString();
                        this.downloadUrl = j.getAsJsonObject().get("url").getAsString();
                        break;
                    }
                }
            }
        }

        // Save json object
        if (isFabric) {
            this.json = json;
        }
    }

    public boolean isCompatible(String mcVer) {
        JsonArray jsonVerArray = null;

        if (platform.equals("CF")) {
            //jsonVerArray = json.get("data").getAsJsonObject().getAsJsonArray("gameVersion");
            jsonVerArray = json.get("data").getAsJsonArray().get(0).getAsJsonObject().get("gameVersions").getAsJsonArray();
        } else if (platform.equals("MR")) {
            jsonVerArray = json.getAsJsonArray("game_versions");
        }

        if (jsonVerArray != null) {
            for (JsonElement j : jsonVerArray) {
                String ver = j.getAsString();
                if (ver.contains(mcVer)) {
                    return true;
                }
            }
        }

        return false;
    }
}

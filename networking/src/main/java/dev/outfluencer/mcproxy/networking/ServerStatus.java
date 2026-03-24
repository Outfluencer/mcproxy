package dev.outfluencer.mcproxy.networking;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lenni0451.mcstructs.text.TextComponent;

import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerStatus {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Version {
        private String name;
        private int protocol;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Players {
        private int max;
        private int online;
        private Player[] sample;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Player {
        private String name;
        private UUID uuid;
    }

    private Version version;
    private Players players;
    private TextComponent description;

    public String serialize(int protocolVersion) {
        JsonObject jsonObject = new JsonObject();

        if (version != null) {
            JsonObject versionObject = new JsonObject();
            versionObject.addProperty("name", version.name);
            versionObject.addProperty("protocol", version.protocol);
            jsonObject.add("version", versionObject);
        }
        if (players != null) {
            JsonObject playersObject = new JsonObject();
            playersObject.addProperty("max", players.max);
            playersObject.addProperty("online", players.online);
            if (players.sample != null) {
                JsonArray sampleArray = new JsonArray();
                for (Player player : players.sample) {
                    JsonObject playerEntry = new JsonObject();
                    playerEntry.addProperty("name", player.name);
                    playerEntry.addProperty("id", player.uuid.toString());
                    sampleArray.add(playerEntry);
                }
                playersObject.add("sample", sampleArray);
            }
            jsonObject.add("players", playersObject);
        }
        if (description != null) {
            jsonObject.addProperty("description", Util.textComponentSerializerByVersion(protocolVersion).serialize(description));
        }

        return jsonObject.toString();
    }

    public static ServerStatus deserialize(String json, int protocolVersion) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        ServerStatus status = new ServerStatus();

        if (jsonObject.has("version")) {
            JsonObject versionObject = jsonObject.getAsJsonObject("version");
            Version version = new Version();
            version.name = versionObject.get("name").getAsString();
            version.protocol = versionObject.get("protocol").getAsInt();
            status.version = version;
        }

        if (jsonObject.has("players")) {
            JsonObject playersObject = jsonObject.getAsJsonObject("players");
            Players players = new Players();
            players.max = playersObject.get("max").getAsInt();
            players.online = playersObject.get("online").getAsInt();
            if (playersObject.has("sample")) {
                JsonArray sampleArray = playersObject.getAsJsonArray("sample");
                players.sample = new Player[sampleArray.size()];
                for (int i = 0; i < sampleArray.size(); i++) {
                    JsonObject playerEntry = sampleArray.get(i).getAsJsonObject();
                    Player player = new Player();
                    player.name = playerEntry.get("name").getAsString();
                    player.uuid = UUID.fromString(playerEntry.get("id").getAsString());
                    players.sample[i] = player;
                }
            }
            status.players = players;
        }

        if (jsonObject.has("description")) {
            JsonElement descElement = jsonObject.get("description");
            status.description = Util.textComponentSerializerByVersion(protocolVersion).deserialize(descElement.toString());
        }

        return status;
    }
}

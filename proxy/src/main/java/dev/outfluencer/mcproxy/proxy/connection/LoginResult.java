package dev.outfluencer.mcproxy.proxy.connection;

import com.google.gson.Gson;
import dev.outfluencer.mcproxy.networking.Property;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult {
    public static final Gson GSON = new Gson();
    private String id;
    private String name;
    private Property[] properties;
}

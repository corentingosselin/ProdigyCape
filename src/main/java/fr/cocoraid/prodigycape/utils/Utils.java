package fr.cocoraid.prodigycape.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils {

    public static GameProfile createProfileWithTexture(String texture) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        PropertyMap propertyMap = profile.getProperties();
        propertyMap.put("textures", new Property("textures", texture));
        return profile;
    }

    public static void sendActionBar(Player p, String message) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static List<String> listReplacer(List<String> source, String toReplace, String newValue) {
        List<String> result = new ArrayList<>();
        for (String s : source) {
            result.add(s.replace(toReplace, newValue));
        }
        return result;
    }

    private static String a = Bukkit.getServer().getClass().getPackage().getName();
    private static String version = a.substring(a.lastIndexOf('.') + 1);
}

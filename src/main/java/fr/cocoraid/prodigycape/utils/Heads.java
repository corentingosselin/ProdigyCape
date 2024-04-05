package fr.cocoraid.prodigycape.utils;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.language.LanguageManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Heads {
    private static LanguageManager lm = ProdigyCape.getInstance().getLanguageManager();

    public final static ItemStack ARROW_LEFT = new ItemEditor(Material.PLAYER_HEAD).setDisplayName(lm.getLanguage().previous_button).setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ").getItem();
    public final static ItemStack ARROW_RIGHT = new ItemEditor(Material.PLAYER_HEAD).setDisplayName(lm.getLanguage().next_button).setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19").getItem();
    public final static ItemStack TRASH = new ItemEditor(Material.PLAYER_HEAD).setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjQ2NWY4MGJmMDJiNDA4ODg1OTg3YjAwOTU3Y2E1ZTllYjg3NGMzZmE4ODMwNTA5OTU5N2EzMzNhMzM2ZWUxNSJ9fX0=").getItem();
    public final static ItemStack ARROW_UP = new ItemEditor(Material.PLAYER_HEAD).setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA0MGZlODM2YTZjMmZiZDJjN2E5YzhlYzZiZTUxNzRmZGRmMWFjMjBmNTVlMzY2MTU2ZmE1ZjcxMmUxMCJ9fX0=").getItem();
    public final static ItemStack ARROW_DOWN = new ItemEditor(Material.PLAYER_HEAD).setTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzNzM0NmQ4YmRhNzhkNTI1ZDE5ZjU0MGE5NWU0ZTc5ZGFlZGE3OTVjYmM1YTEzMjU2MjM2MzEyY2YifX19").getItem();

}

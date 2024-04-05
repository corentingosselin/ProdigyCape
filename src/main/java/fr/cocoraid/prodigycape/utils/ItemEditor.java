package fr.cocoraid.prodigycape.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ItemEditor {

    private static Method metaSetProfileMethod;
    private static Field metaProfileField;

    private ItemStack itemStack;
    private String texture;

    private ItemMeta meta;

    public ItemEditor(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        meta = itemStack.getItemMeta();
    }

    public ItemEditor(Material material) {
        this(material, 0);
    }

    public ItemEditor(Material material, int damage) {
        this(new ItemStack(material, 1, (short) damage));
    }

    public short getDamage() {
        return itemStack.getDurability();
    }

    public ItemEditor setDamage(short damage) {
        itemStack.setDurability(damage);
        return this;
    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    public ItemEditor setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public boolean hasDisplayName() {
        return meta.hasDisplayName();
    }

    public String getDisplayName() {
        return meta.getDisplayName();
    }

    public ItemEditor setDisplayName(String displayName) {
        meta.setDisplayName(displayName);
        return this;
    }

    public ItemEditor setLore(String[] lore) {
        return setLore(lore != null && lore.length > 0 ? Arrays.asList(lore) : null);
    }

    public ItemEditor setLore(String lore) {
        return setLore(lore.isEmpty() ? null : Arrays.asList(lore.split("\n")));
    }

    public ItemEditor addLoreLine(String lore) {
        if (lore == null)
            return this;
        List<String> itemLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        itemLore.addAll(Arrays.asList(lore.split("\n")));
        meta.setLore(itemLore);
        return this;
    }

    public ItemEditor addLoreLines(List<String> lore) {
        if (lore == null)
            return this;
        List<String> itemLore = hasLore() ? getLore() : new ArrayList<>();
        itemLore.addAll(lore);
        setLore(itemLore);
        return this;
    }

    public ItemEditor removeLoreLine(int line) {
        if (!hasLore()) return this;
        List<String> itemLore = meta.getLore();
        itemLore.remove(line);
        setLore(itemLore);
        return this;
    }

    public boolean hasLore() {
        return meta.hasLore();
    }

    public ItemEditor insertLoreLine(String lore, int line) {
        List<String> itemLore = hasLore() ? getLore() : new ArrayList<>();
        itemLore.add(line, lore);
        setLore(itemLore);
        return this;
    }

    public List<String> getLore() {
        return meta.getLore();
    }

    public ItemEditor setLore(List<String> itemLore) {
        meta.setLore(itemLore);
        return this;
    }

    public String getLoreAsString() {
        if (getLore().isEmpty()) return "";
        StringBuilder b = new StringBuilder();
        for (String l : getLore()) {
            b.append(l).append("\n");
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

    private static Reflection.ConstructorInvoker craftPlayerProfileConstructor = Reflection.getConstructor("{obc}.profile.CraftPlayerProfile", GameProfile.class);
    public ItemEditor setTexture(String texture) {
        if (itemStack.getType() != Material.PLAYER_HEAD)
            throw new IllegalArgumentException("ItemStack is not an Player Head");
        GameProfile profile = Utils.createProfileWithTexture(texture);

        PlayerProfile ownerProfile = (PlayerProfile) craftPlayerProfileConstructor.invoke(profile);
        ((SkullMeta) meta).setOwnerProfile(ownerProfile);
        this.texture = texture;
        return this;
    }

    public ItemEditor setPotion(PotionType potionType) {
        if (itemStack.getType() != Material.POTION)
            throw new IllegalArgumentException("ItemStack is not an Potion");
        ((PotionMeta) meta).setBasePotionData(new PotionData(potionType));
        return this;
    }

    public ItemEditor setHead(OfflinePlayer op) {
        if (itemStack.getType() != Material.PLAYER_HEAD)
            throw new IllegalArgumentException("ItemStack is not an Player Head");
        ((SkullMeta) meta).setOwningPlayer(op);
        return this;
    }

    public ItemEditor addEnchantment(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemEditor removeEnchantment(Enchantment enchantment) {
        meta.removeEnchant(enchantment);
        return this;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return meta.getEnchants();
    }

    public ItemEditor hideAllFlags() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemStack getItem() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public String getTexture() {
        return texture;
    }
}
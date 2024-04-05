package fr.cocoraid.prodigycape.contributors;

import fr.cocoraid.prodigycape.cape.Cape;

import java.util.HashMap;

import java.util.Map;
import java.util.UUID;

public class CapeContributors {

    private Map<UUID, Cape> capeContributors = new HashMap<>();


    public CapeContributors() {
       /* capeContributors.put(
                UUID.fromString("8b38bba6-0f1e-4e2b-ab0e-15911c851261"),
                new Cape("cocoraid_cape", false, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTIzNGE1NDFjNTg2NzlmYjU1Y2NjYTNjM2EwYzYyNjAzZGVhODE2MDE2ZDE1OGFjYzJhMjJhNGVlMjA4NWRkNyJ9fX0=",
                        "§6cocoraid's cape", "cocoraid", "Cape of the plugin owner")
        );*/

        capeContributors.put(
                UUID.fromString("395562db-d832-4061-bec0-c3b6e60f3c36"),
                new Cape("jarfiles_cape",
                        false,
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU1NTUyMmI3NzhjZGQwMjU3M2Y0MjUyYWY3MzcxY2NjMTRiMmI0NjczYzhjYWU4ZWFiMWVhM2ZkN2Y1ZTZkZSJ9fX0=",
                        "JarFiles",
                        "Cape of the plugin owner",
                        0,
                        0)
        );
    }

    public Map<UUID, Cape> getCapeContributors() {
        return capeContributors;
    }

    public Cape getCape(UUID uuid) {
        return capeContributors.get(uuid);
    }
}

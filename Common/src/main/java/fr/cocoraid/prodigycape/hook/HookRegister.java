package fr.cocoraid.prodigycape.hook;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.hook.vault.EconomyManager;

public class HookRegister {


    private ProdigyCape instance;
    public HookRegister(ProdigyCape instance) {
        this.instance = instance;
    }


    public EconomyManager loadEconomyManager() {
        return new EconomyManager(instance);
    }
}

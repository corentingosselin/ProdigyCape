package fr.cocoraid.prodigycape.commands;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandContexts;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import fr.cocoraid.prodigycape.cape.Cape;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.ProdigyCape;

public class CapeContext {

    private final ProdigyCape instance;
    private final PaperCommandManager manager;

    public CapeContext(ProdigyCape instance) {
        this.instance = instance;
        this.manager = instance.getCommandManager();
    }

    public void register() {
        CommandContexts<BukkitCommandExecutionContext> commandContexts = manager.getCommandContexts();
        commandContexts.registerContext(Cape.class, c -> {
            String name = c.popFirstArg();
            CapeManager capeManager = instance.getCapeManager();
            Cape cape = capeManager.getCape(name);
            if (cape == null) {
                throw new InvalidCommandArgument("Cape with name " + name + " not found...");
            }
            return cape;
        });

    }
}

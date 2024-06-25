package fr.cocoraid.prodigycape.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.PaperCommandManager;
import fr.cocoraid.prodigycape.manager.CapeManager;
import fr.cocoraid.prodigycape.ProdigyCape;

import java.util.Set;
import java.util.stream.Collectors;

public class CapeCompletion {

    private ProdigyCape instance;
    private CapeManager capeManager;
    private PaperCommandManager manager;

    public CapeCompletion(ProdigyCape instance) {
        this.instance = instance;
        this.capeManager = instance.getCapeManager();
        this.manager = instance.getCommandManager();
    }

    public void register() {
        CommandCompletions<BukkitCommandCompletionContext> commandCompletions = manager.getCommandCompletions();
        commandCompletions.registerAsyncCompletion("capes", c -> {
            Set<String> list = instance.getCapeManager().getCapes().keySet().stream().filter(
                    cape -> capeManager.ownsCape(c.getPlayer(), capeManager.getCape(cape))
            ).collect(Collectors.toSet());
            if (list.isEmpty()) return null;
            else return list;
        });


    }
}

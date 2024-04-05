package fr.cocoraid.prodigycape.language;

import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.language.abstraction.LanguageManagerAbstract;

public class LanguageManager extends LanguageManagerAbstract {

    public LanguageManager(ProdigyCape instance) {
        super(instance,new Language(),
                "languages",
                instance.getDataFolder().getPath());
    }

    public Language getLanguage() {
        return (Language) language;
    }

}

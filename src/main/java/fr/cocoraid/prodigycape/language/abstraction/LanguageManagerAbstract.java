package fr.cocoraid.prodigycape.language.abstraction;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import fr.cocoraid.prodigycape.ProdigyCape;
import fr.cocoraid.prodigycape.utils.Utils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class LanguageManagerAbstract {


    private List<String> availableLanguages = new ArrayList<>();
    private ProdigyCape instance;
    protected LanguageAbstract language;

    private String languageStoredPath;
    private String outputFolder;
    public LanguageManagerAbstract(ProdigyCape instance, LanguageAbstract languageAbstract, String languagesStoredPath, String outputFolder) {
        this.instance = instance;
        this.language = languageAbstract;
        this.languageStoredPath = languagesStoredPath;
        this.outputFolder = outputFolder;
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if (jarFile.isFile()) {  // Run with JAR file
            try {
                JarFile jar = new JarFile(jarFile);
                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                while (entries.hasMoreElements()) {
                    final String name = entries.nextElement().getName();
                    if (name.startsWith(languagesStoredPath + "/") && name.endsWith(".json")) {
                        availableLanguages.add(name.replace(languagesStoredPath + "/", "").replace(".json", ""));
                    }
                }
                jar.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void init() {
        File file = new File(outputFolder, "/language.json");
        if (!file.exists()) {
            if (availableLanguages.contains(instance.getConfiguration().getLanguage())) {
                InputStream is = instance.getClass().getClassLoader()
                        .getResourceAsStream(languageStoredPath + "/" + instance.getConfiguration().getLanguage() + ".json");
                try {
                    OutputStream os = new FileOutputStream(outputFolder + "/language.json");
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        os.write(buffer, 0, length);
                    }
                    os.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
                try {
                    replace("§", "&");
                    FileWriter writer = new FileWriter(file);
                    String s = gson.toJson(language);
                    writer.write(s);
                    writer.flush();
                    writer.close();
                } catch (IOException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void replace(String code, String code2) throws IllegalAccessException {
        for (Field field : language.getClass().getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                String s = (String) field.get(language);
                field.setAccessible(true);
                field.set(language, s.replace(code, code2));
            } else if (field.getType().equals(List.class)) {
                List<String> list = (List<String>) field.get(language);
                List<String> newList = Utils.listReplacer(list, code, code2);
                field.setAccessible(true);
                field.set(language, newList);
            }
        }

    }

    public void load() {
        init();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        LanguageAbstract defaultLanguage = language;

        //get existing fields json, check which are null and add them
        try {
            File file = new File(outputFolder, "/language.json");
            JsonReader reader = new JsonReader(new FileReader(file));
            JsonParser p = new JsonParser();
            JsonObject result = p.parse(reader).getAsJsonObject();
            this.language = gson.fromJson(result, language.getClass());
            //add missing key or remove missing key due to plugin update

            List<String> keys = new ArrayList<>();
            //collect all keys from file
            Set<Map.Entry<String, JsonElement>> entries = result.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                keys.add(entry.getKey());
            }

            // if one key is missing at least
            //collect all necessary keys
            List<String> necessaryKeys = new ArrayList<>();
            boolean keyMissing = false;
            JsonElement pluginLanguage = gson.toJsonTree(defaultLanguage);

            Set<Map.Entry<String, JsonElement>> entrySet = pluginLanguage.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (!keys.contains(entry.getKey())) {
                    keyMissing = true;
                }
                necessaryKeys.add(entry.getKey());
            }

            // 1 list keys needed
            // 1 list of all keys
            // compare both list, if key are found
            // jsonObj.getAsJsonObject("accounts").remove("email");
            List<String> differences = new ArrayList<>(keys);
            differences.removeAll(necessaryKeys);

            if (!differences.isEmpty()) {
                differences.forEach(result::remove);
                this.language = gson.fromJson(result, language.getClass());
            }
            //generate new json file of language
            if (keyMissing || !differences.isEmpty()) {
                try {
                    replace("§", "&");
                    FileWriter writer = new FileWriter(file);
                    String s = gson.toJson(language);
                    writer.write(s);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //translate chat
            replace("&", "§");
        } catch (IllegalAccessException | IOException e) {
            e.printStackTrace();
        }
    }

}

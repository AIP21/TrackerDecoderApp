package com.anipgames.WAT_Vis.config;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.anipgames.WAT_Vis.WatVis;
import com.anipgames.WAT_Vis.util.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

public abstract class Config {

    public static class LocaleAgnosticFieldNamingStrategy implements FieldNamingStrategy {
        @Override
        public String translateName(Field field) {
            return separateCamelCase(field.getName(), "_").toLowerCase(Locale.ROOT);
        }

        private static String separateCamelCase(String name, String separator) {
            StringBuilder translation = new StringBuilder();
            for (int i = 0; i < name.length(); i++) {
                char character = name.charAt(i);
                if (Character.isUpperCase(character) && translation.length() != 0) {
                    translation.append(separator);
                }
                translation.append(character);
            }
            return translation.toString();
        }
    }

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingStrategy(new LocaleAgnosticFieldNamingStrategy())
            .excludeFieldsWithoutExposeAnnotation()
            .excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT)
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();
    protected String root = WatVis.DIR_CONFIG;
    protected String extension = ".json";

    public void generateConfig() {
        this.resetConfig();

        try {
            this.writeConfig();
        } catch (IOException e) {
            Logger.err("Error generating config:\n "+ e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }
    }

    private File getConfigFile() {
        return new File(this.root + File.separator + this.getName() + this.extension);
    }

    public abstract String getName();

    public Config readConfig() {
        try {
            Config config = GSON.fromJson(new FileReader(this.getConfigFile()), this.getClass());
            if (config == null) throw new Exception("WTF");
            return config;
        } catch (Exception e) {
            Logger.warn(e.getMessage());
            this.generateConfig();
        }

        return this;
    }

    public Config forceGenerateConfig() {
        this.generateConfig();
        return this;
    }

    public void updateConfig() {
        this.maintainConfig();

        try {
            this.writeConfig();
        } catch (IOException e) {
            Logger.err("Error updating config:\n "+ e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }
    }

    public abstract void maintainConfig();

    protected abstract void resetConfig();

    public void writeConfig() throws IOException {
        File dir = new File(this.root);
        if (!dir.exists() && !dir.mkdirs()) return;
        if (!this.getConfigFile().exists() && !this.getConfigFile().createNewFile()) return;
        FileWriter writer = new FileWriter(this.getConfigFile());
        GSON.toJson(this, writer);
        writer.flush();
        writer.close();
    }

}

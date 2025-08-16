package com.theus452.walkietalkie.fabric.config;

import com.theus452.walkietalkie.WalkieTalkieMod;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class FabricModConfigs {
    public static final Logger LOGGER = LoggerFactory.getLogger(WalkieTalkieMod.MOD_ID);
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("walkietalkie.properties");
    private static final Properties properties = new Properties();

    private static double chatRange = 35.0;
    private static final String CHAT_RANGE_KEY = "chatDistance";

    public static void register() {
        
        try {
            if (!Files.exists(CONFIG_PATH)) {
                LOGGER.info("Criando arquivo de configuração padrão para o Walkie-Talkie.");
                save(); 
            }
            load(); 
        } catch (IOException e) {
            LOGGER.error("Falha ao criar ou carregar o arquivo de configuração do Walkie-Talkie.", e);
        }
    }

    public static void load() {
        try (FileInputStream stream = new FileInputStream(CONFIG_PATH.toFile())) {
            properties.load(stream);

            
            String chatRangeValue = properties.getProperty(CHAT_RANGE_KEY, String.valueOf(35.0));
            try {
                chatRange = Double.parseDouble(chatRangeValue);
            } catch (NumberFormatException e) {
                LOGGER.warn("Valor inválido para '{}' no arquivo de configuração. Usando o valor padrão.", CHAT_RANGE_KEY);
                chatRange = 35.0;
            }

            LOGGER.info("Configuração do Walkie-Talkie carregada. Distância do chat: {}", chatRange);

        } catch (IOException e) {
            LOGGER.error("Não foi possível carregar o arquivo de configuração.", e);
        }
    }

    public static void save() throws IOException {
        try (FileOutputStream stream = new FileOutputStream(CONFIG_PATH.toFile())) {
            
            properties.setProperty(CHAT_RANGE_KEY, String.valueOf(chatRange));

            
            properties.store(stream, "Walkie-Talkie Mod Configurations");
        }
    }

    public static double getChatRange() {
        return chatRange;
    }

    public static void setChatRange(double newRange) {
        chatRange = newRange;
        try {
            save(); 
        } catch (IOException e) {
            LOGGER.error("Falha ao salvar a configuração após alteração via comando.", e);
        }
    }
}
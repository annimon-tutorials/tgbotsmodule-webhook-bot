package com.example.bot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TestBotHandler extends BotHandler {

    private final TestBotConfig botConfig;

    public TestBotHandler() {
        final var configLoader = new YamlConfigLoaderService();
        botConfig = configLoader.loadResource("/testbot.yaml", TestBotConfig.class);
    }

    @Override
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {
        final var msg = update.getMessage();
        if (msg != null && msg.hasText()) {
            System.out.println(msg.getChatId());
            Methods.sendMessage(msg.getChatId(), msg.getText().toUpperCase(Locale.ROOT))
                    .callAsync(this);
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}

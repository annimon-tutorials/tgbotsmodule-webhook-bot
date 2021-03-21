package com.example.bot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.CommandRegistry;
import com.annimon.tgbotsmodule.commands.SimpleCommand;
import com.annimon.tgbotsmodule.commands.authority.For;
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TestBotHandler extends BotHandler {

    private final TestBotConfig botConfig;
    private final CommandRegistry<For> commands;

    public TestBotHandler() {
        final var configLoader = new YamlConfigLoaderService();
        botConfig = configLoader.loadResource("/testbot.yaml", TestBotConfig.class);

        final var authority = new SimpleAuthority(this, botConfig.getAdminId());
        commands = new CommandRegistry<>(this, authority);
        commands.register(new SimpleCommand("/start", ctx -> {
            ctx.reply("Hi, " + ctx.user().getFirstName())
                    .callAsync(ctx.sender);
        }));
    }

    @Override
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {
        if (commands.handleUpdate(update)) {
            return null;
        }

        final var msg = update.getMessage();
        if (msg != null && msg.hasText()) {
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

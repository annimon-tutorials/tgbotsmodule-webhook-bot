package com.example.bot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.commands.CommandRegistry;
import com.annimon.tgbotsmodule.commands.SimpleCommand;
import com.annimon.tgbotsmodule.commands.authority.For;
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority;
import com.annimon.tgbotsmodule.services.YamlConfigLoaderService;
import com.example.bot.commands.English2Kana;
import com.example.bot.commands.GuessNumberGame;
import com.example.bot.commands.YouTubeThumbnail;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public class TestBotHandler extends BotHandler {

    private final TestBotConfig botConfig;
    private final CommandRegistry<For> commands;

    public TestBotHandler() {
        final var configLoader = new YamlConfigLoaderService();
        botConfig = configLoader.loadResource("/testbot.yaml", TestBotConfig.class);

        // A command registry requires a SimpleAuthority instance
        // Authority system is based on roles. By default all commands available for all users
        // For the CREATOR role you need to pass a telegram user id, or 0 if you don't have creators-only commands.
        final var authority = new SimpleAuthority(this, botConfig.getAdminId());
        commands = new CommandRegistry<>(this, authority);
        commands.register(new SimpleCommand("/start", ctx -> {
            ctx.reply("Hi, " + ctx.user().getFirstName() + "\n" +
                      "This bot is an example for the tgbots-module library.\n" +
                      "  https://github.com/aNNiMON/tgbots-module/\n" +
                      "Source code\n" +
                      "  https://github.com/annimon-tutorials/tgbotsmodule-webhook-bot\n\n" +
                      "ℹ️ Available commands:\n" +
                      " - /kana word — convert English word to Katakana\n" +
                      " - /game — start a guess number game\n" +
                      "\nAlso, you can send me a link to YouTube video and I'll send you a video thumbnail as a photo.")
                    .disableWebPagePreview()
                    .callAsync(ctx.sender);
        }));
        commands.register(new YouTubeThumbnail());
        commands.register(new English2Kana());
        // Register commands bundle (multiple commands)
        commands.registerBundle(new GuessNumberGame());
    }

    @Override
    protected BotApiMethod<?> onUpdate(@NotNull Update update) {
        if (commands.handleUpdate(update)) {
            return null;
        }

        // Process other messages
        //final var msg = update.getMessage();
        //if (msg != null && msg.hasText()) {
        //    Methods.sendMessage(msg.getChatId(), msg.getText().toUpperCase(Locale.ROOT))
        //            .callAsync(this);
        //}
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

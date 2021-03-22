package com.example.bot.commands;

import com.annimon.tgbotsmodule.commands.CommandBundle;
import com.annimon.tgbotsmodule.commands.CommandRegistry;
import com.annimon.tgbotsmodule.commands.SimpleCallbackQueryCommand;
import com.annimon.tgbotsmodule.commands.SimpleCommand;
import com.annimon.tgbotsmodule.commands.authority.For;
import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

/**
 * CommandBundle example.
 * You can register multiple commands here.
 */
public class GuessNumberGame implements CommandBundle<For> {

    private final Random random;
    private final Map<Long, GameData> games;

    public GuessNumberGame() {
        random = new Random();
        games = new ConcurrentHashMap<>();
    }

    @Override
    public void register(@NotNull CommandRegistry<For> commands) {
        commands.register(new SimpleCommand("/game", this::startGame));
        commands.register(new SimpleCallbackQueryCommand("guess", this::checkGuess));
    }

    private void startGame(MessageContext ctx) {
        if (!ctx.message().isUserMessage()) return;

        final var userId = ctx.chatId();
        final var game = new GameData(random.nextInt(10));
        var msg = ctx.reply()
                .setText("Game started.\n" + game.formatMessage())
                .enableMarkdown()
                .setReplyMarkup(game.keyboard(userId))
                .call(ctx.sender);
        game.messageId = msg.getMessageId();
        games.put(userId, game);
    }

    private void checkGuess(CallbackQueryContext ctx) {
        final var userId = Long.parseLong(ctx.argument(0));
        final var guess = Integer.parseInt(ctx.argument(1));
        final var game = games.get(userId);
        if (game == null) return;
        if (guess < 0 || guess >= 10) return;
        if (ctx.message() == null) return;
        if (game.messageId != ctx.message().getMessageId()) return;

        if (guess == game.number) {
            ctx.editMessage("\uD83C\uDF89").callAsync(ctx.sender);
            return;
        }

        String status = "";
        InlineKeyboardMarkup keyboard = null;
        game.attempts++;
        if (game.attempts <= 3) {
            game.numbersAvailable[guess] = false;
            game.historyLines.add(String.format(
                    "  _My number is %s than %d_",
                    (game.number > guess) ? "greater" : "less",
                    guess));
            keyboard = game.keyboard(userId);
        } else {
            status = "\n\uD83D\uDE14 You lose. My number was " + game.number;
        }

        ctx.editMessage(game.formatMessage() + status)
                .setReplyMarkup(keyboard)
                .enableMarkdown()
                .callAsync(ctx.sender);
    }

    private static class GameData {
        long messageId;
        int number;
        int attempts;
        final boolean[] numbersAvailable;
        final List<String> historyLines;

        public GameData(int number) {
            this.number = number;
            numbersAvailable = new boolean[10];
            Arrays.fill(numbersAvailable, true);
            historyLines = new ArrayList<>();
        }

        public String formatMessage() {
            return "Guess the number from `0` to `9`.\n" +
                   "*Attempts*: `" + attempts + " / 4`\n" +
                   String.join("\n", historyLines);
        }

        public InlineKeyboardMarkup keyboard(long userId) {
            return new InlineKeyboardMarkup(rows(userId));
        }

        private List<List<InlineKeyboardButton>> rows(long userId) {
            // Create two rows of buttons
            // 0 1 2 3 4
            // 5 6 7 8 9
            return Stream.of(IntStream.range(0, 5), IntStream.range(5, 10))
                    .map(stream -> stream
                            .filter(i -> numbersAvailable[i])
                            .mapToObj(i -> InlineKeyboardButton.builder()
                                    .text(Integer.toString(i))
                                    .callbackData(String.format("guess:%d %d", userId, i))
                                    .build())
                    )
                    .map(stream -> stream.collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }
    }
}

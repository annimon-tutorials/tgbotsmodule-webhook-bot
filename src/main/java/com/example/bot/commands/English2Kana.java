package com.example.bot.commands;

import com.annimon.tgbotsmodule.commands.RegexCommand;
import com.annimon.tgbotsmodule.commands.authority.For;
import com.annimon.tgbotsmodule.commands.context.RegexMessageContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/**
 * Regex command with arguments.
 */
public class English2Kana implements RegexCommand {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Pattern pattern() {
        return Pattern.compile("/kana ([a-zA-Z']{1,30})");
    }

    @SuppressWarnings("unchecked")
    @Override
    public EnumSet<For> authority() {
        return For.all();
    }

    @Override
    public void accept(@NotNull RegexMessageContext ctx) {
        final var word = URLEncoder.encode(ctx.group(1), StandardCharsets.UTF_8);
        final var request = new Request.Builder()
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:86.0) Gecko/20100101 Firefox/86.0")
                .url("https://www.sljfaq.org/cgi/e2k.cgi?o=json&lang=en&word=" + word)
                .build();
        client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        ctx.replyToMessage("Unable to precess this query").callAsync(ctx.sender);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try (final var body = response.body()) {
                            if (body == null) return;
                            var kanaResp = mapper.readValue(body.string(), Kana.class);

                            var text = kanaResp.words.stream()
                                    .map(word -> word.j_pron_spell)
                                    .collect(Collectors.joining("・"));
                            if (kanaResp.romaji2kana != null) {
                                text += "\nThis looks like it might be romanized Japanese:\n" + kanaResp.romaji2kana;
                            }
                            ctx.replyToMessage(text).callAsync(ctx.sender);
                        }
                    }
                });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Kana {
        public List<Word> words;
        public String romaji2kana;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Word {
        public String type;
        public String j_pron_spell;
    }
}

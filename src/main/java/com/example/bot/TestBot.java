package com.example.bot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.BotModule;
import com.annimon.tgbotsmodule.Runner;
import com.annimon.tgbotsmodule.beans.Config;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TestBot implements BotModule {

    public static void main(String[] args) {
        Runner.run(List.of(new TestBot()));
    }

    @Override
    public @NotNull BotHandler botHandler(@NotNull Config config) {
        return new TestBotHandler();
    }
}

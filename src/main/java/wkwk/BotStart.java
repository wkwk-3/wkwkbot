package wkwk;

import wkwk.Command.Processing;
import wkwk.Command.WkwkSlashCommand;
import wkwk.botSystem.*;
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.twitterSystem.AutoTweet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BotStart extends BotLogin {
    public static void main(String[] args) {
        try {
            DiscordDAO dao = new DiscordDAO();
            WkwkSlashCommand wkwkSlashCommand = new WkwkSlashCommand();
            Processing processing = new Processing();
            AutoTweet autoTweet = new AutoTweet(dao.getAutoTweetApis());
            autoTweet.start();

            new AutoDeleteMessageSystem();
            new AutoDeleteRegisterSystem();
            new GuideSystem();
            new LoggingSystem();
            new PresetSystem();
            new ReactionRoleSystem();
            new SelectDeleteSystem();
            new SlashCommandSystem();
            new TempChannelSystem();
            new WatchingSystem();
            new HelpSystem();

            processing.upDataBotActivity();

            System.out.println(processing.getBotInviteUrl());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String cmd = br.readLine();
                switch (cmd) {
                    case "stop" -> {
                        System.out.println("システムを終了します");
                        System.exit(0);
                    }
                    case "reload" -> {
                        new ReloadSystem();
                        processing.upDataBotActivity();
                    }
                    case "ts" -> autoTweet.start();
                    case "tp" -> autoTweet.stop();
                    case "commandCreate" -> {
                        wkwkSlashCommand.createCommand();
                        System.out.println("Command新規作成完了");
                    }
                    case "AllCommandDelete" -> {
                        wkwkSlashCommand.allDeleteCommands();
                        System.out.println("全削除完了");
                    }
                    case "AllCommandReload" -> {
                        wkwkSlashCommand.allDeleteCommands();
                        System.out.println("全削除完了");
                        wkwkSlashCommand.createCommand();
                        System.out.println("リロード完了");
                    }
                    case "commandShow" -> {
                        System.out.println("\n");
                        wkwkSlashCommand.commandShow();
                    }
                    case "invite" -> System.out.println(processing.getBotInviteUrl());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

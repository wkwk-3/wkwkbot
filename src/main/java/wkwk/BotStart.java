package wkwk;


import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import wkwk.Command.WkwkSlashCommand;
import wkwk.botSystem.*;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.record.ChannelRecord;
import wkwk.parameter.record.ServerDataRecord;
import wkwk.twitterSystem.AutoTweet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

public class BotStart {
    public static void main(String[] args) {
        try {
            DiscordDAO dao = new DiscordDAO();
            DiscordApi api = new DiscordApiBuilder().setAllIntents().setToken(dao.BotGetToken()).login().join();
            WkwkSlashCommand wkwkSlashCommand = new WkwkSlashCommand(api);
            AutoTweet autoTweet = new AutoTweet(dao.getAutoTweetApis());
            autoTweet.start();

            new AutoDeleteMessageSystem(api).run();
            new AutoDeleteRegisterSystem(api).run();
            new GuideSystem(api).run();
            new LoggingSystem(api).run();
            new PresetSystem(api).run();
            new ReactionRoleSystem(api).run();
            new SelectDeleteSystem(api).run();
            new SlashCommandSystem(api).run();
            new TempChannelSystem(api).run();
            new WatchingSystem(api).run();
            new HelpSystem(api).run();

            System.out.println("URL : " + api.createBotInvite(new PermissionsBuilder().setAllowed(PermissionType.ADMINISTRATOR).build()).replaceAll("scope=bot", "scope=bot+applications.commands"));
            api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String cmd = br.readLine();
                if ("stop".equals(cmd)) {
                    System.out.println("システムを終了します");
                    System.exit(0);
                } else if ("reload".equals(cmd)) {
                    new ReloadSystem(api).run();
                    api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
                } else if ("ts".equals(cmd)) {
                    autoTweet.start();
                } else if ("tp".equals(cmd)) {
                    autoTweet.stop();
                } else if ("commandCreate".equals(cmd)) {
                    wkwkSlashCommand.createCommand();
                    System.out.println("Command新規作成完了");
                } else if ("AllCommandDelete".equals(cmd)) {
                    wkwkSlashCommand.allDeleteCommands();
                    System.out.println("全削除完了");
                } else if ("AllCommandReload".equals(cmd)) {
                    wkwkSlashCommand.allDeleteCommands();
                    System.out.println("全削除完了");
                    wkwkSlashCommand.createCommand();
                    System.out.println("リロード完了");
                } else if ("commandShow".equals(cmd)) {
                    System.out.println("\n");
                    wkwkSlashCommand.commandShow();
                } else if ("updateAnnounce".equals(cmd)) {
                    System.out.println("送信開始");
                    for (ServerDataRecord list : dao.getNoSlashCommandServer()) {
                        if (api.getServerById(list.getServer()).isPresent() && api.getServerById(list.getServer()).get().getOwner().isPresent()) {
                            new MessageBuilder().setContent("wkwkBOTをお使いいただき誠にありがとうございます。" +
                                    "\n当BOTは4/12 をもしまして全機能が\nスラッシュコマンドに移行いたしました" +
                                    "\nそれに伴い、BOTの招待リンクがスラッシュコマンド対応の物に変更されました" +
                                    "\n※新招待リンク(https://wkb.page.link/bot)" +
                                    "\nお手数ですが、機能を使うにはBOTを再導入して頂く必要があります。" +
                                    "\n対象サーバー -> " + api.getServerById(list.getServer()).get().getName() +
                                    "\n複数回送られている場合があります。申し訳ございません。" +
                                    "\n今後とも当BOTをよろしくお願いします。").send(api.getServerById(list.getServer()).get().getOwner().get()
                            ).join();
                        }
                    }
                    System.out.println("送信完了");
                }
            }
        } catch (DatabaseException | SystemException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

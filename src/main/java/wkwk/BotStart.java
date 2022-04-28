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
import wkwk.record.ChannelRecord;
import wkwk.record.ServerDataRecord;
import wkwk.twitterSystem.AutoTweet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

            System.out.println("URL : " + api.createBotInvite(new PermissionsBuilder().setAllowed(PermissionType.ADMINISTRATOR).build()).replaceAll("scope=bot", "scope=bot+applications.commands"));
            api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String cmd = br.readLine();
                if ("stop".equals(cmd)) {
                    System.out.println("システムを終了します");
                    System.exit(0);
                } else if ("reload".equals(cmd)) {
                    int i = 0;
                    int k = 0;
                    int j = 0;
                    String outServer = "削除するサーバーデータがありませんでした";
                    String outMention = "削除するメンションデータがありませんでした";
                    String outTemp = "削除する一時データがありませんでした";
                    for (String serverId : dao.getServerList())
                        if (!api.getServerById(serverId).isPresent()) {
                            i++;
                            dao.serverLeaveAllDataDelete(serverId);
                            System.out.println("右のサーバーデーターを削除しました -> " + serverId);
                        }
                    for (String text : dao.getAllMentionText().getTextID())
                        if (!api.getServerTextChannelById(text).isPresent()) {
                            k++;
                            dao.deleteMentions(text);
                            System.out.println("右のメンションデータを削除しました -> " + text);
                        } else {
                            ChannelRecord list = dao.TempGetChannelList(api.getServerTextChannelById(text).get().getIdAsString(), "t");
                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent() && api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().size() < 1) {
                                k++;
                                api.getServerVoiceChannelById(list.getVoiceID()).get().delete();
                                if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                    api.getServerTextChannelById(list.getTextID()).get().delete();
                                }
                                dao.TempDeleteData(list.getServerID());
                                if (api.getTextChannelById(dao.getMentionChannel(list.getServerID())).isPresent()) {
                                    for (String message : dao.getMentionMessage(list.getTextID()).getMessages()) {
                                        api.getMessageById(message, api.getTextChannelById(dao.getMentionChannel(list.getServerID())).get()).join().delete();
                                    }
                                    dao.deleteMentions(list.getTextID());
                                }
                                System.out.println("右の一時通話群を削除しました -> " + list.getVoiceID());
                            }
                        }
                    for (String voice : dao.TempVoiceIds()) {
                        if (!api.getServerVoiceChannelById(voice).isPresent()) {
                            j++;
                            dao.TempDeleteChannelList(voice, "v");
                            System.out.println("右の一時データを削除しました -> " + voice);
                        }
                    }
                    if (i > 0) outServer = "サーバーデータ削除完了";
                    if (k > 0) outMention = "メンションデータ削除完了";
                    if (j > 0) outTemp = "一時データ削除完了";
                    System.out.println(outServer + "\n" + outMention + "\n" + outTemp);
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
                                    "\n当BOTは4/12 12:00 を持って全機能が\nスラッシュコマンドに移行いたしました" +
                                    "\nそれに伴い、BOTの招待リンクがスラッシュコマンド対応の物に変更されました" +
                                    "\n※新招待リンク(https://wkb.page.link/bot)" +
                                    "\nお手数ですが、機能を使うにはBOTを再導入して頂く必要があります。" +
                                    "\n対象サーバー -> " + api.getServerById(list.getServer()).get().getName() +
                                    "\n複数回送られている場合があります。申し訳ございません。" +
                                    "\n今後とも当BOTをよろしくお願いします。").send(api.getServerById(list.getServer()).get().getOwner().get()).join();
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

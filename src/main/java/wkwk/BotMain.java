package wkwk;

import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.paramater.ServerPropertyParameters;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BotMain extends Thread {
    @Override
    public void run() {
        try {
            DiscordDAO dao = new DiscordDAO();
            String token = dao.BotGetToken();
            DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
            for (String serverid : dao.getServerList())
                if (!api.getServerById(serverid).isPresent()) dao.TempDeleteData(serverid);

            dao.getAllMentionText().getTextid().stream().filter(text -> !api.getServerTextChannelById(text).isPresent()).forEach(dao::deleteMentions);

            for (String voice : dao.TempVoiceids())
                if (!api.getServerVoiceChannelById(voice).isPresent()) dao.TempDeleteChannelList(voice, "v");

            api.addMessageCreateListener(e -> {
                try {
                    if (e.getMessage().getUserAuthor().isPresent() && !e.getMessage().getUserAuthor().get().isBot() && e.getServer().isPresent()) {
                        String server = e.getServer().get().getIdAsString();
                        String prefix = dao.BotGetPrefix(server);
                        long serverl = e.getServer().get().getId();
                        String mes = e.getMessageContent();
                        String response = null;
                        if (prefix == null) {
                            if (mes.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX + "setup") && e.getMessage().getUserAuthor().isPresent() && e.getMessage().getUserAuthor().get().isBotOwner()) {
                                dao.TempNewServer(server);
                                ServerDataList old = dao.TempGetData(server);
                                Server serverem = e.getServer().get();
                                ServerDataList data = new ServerDataList();
                                data.setServer(server);
                                data.setFstchannel(new ServerVoiceChannelBuilder(serverem).setName("NewTEMP").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(e.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.SEND_MESSAGES).build()).create().join().getIdAsString());
                                data.setMentioncal(new ServerTextChannelBuilder(serverem).setName("Mention").setRawPosition(1).create().join().getIdAsString());
                                data.setVoicecate(new ChannelCategoryBuilder(serverem).setName("Voice").setRawPosition(0).create().join().getIdAsString());
                                data.setTextcate(new ChannelCategoryBuilder(serverem).setName("Text").setRawPosition(0).create().join().getIdAsString());
                                dao.TempDataUpData(data);
                                if (api.getChannelCategoryById(old.getVoicecate()).isPresent())
                                    api.getChannelCategoryById(old.getVoicecate()).get().delete();
                                if (api.getChannelCategoryById(old.getTextcate()).isPresent())
                                    api.getChannelCategoryById(old.getTextcate()).get().delete();
                                if (api.getServerVoiceChannelById(old.getFstchannel()).isPresent())
                                    api.getServerVoiceChannelById(old.getFstchannel()).get().delete();
                                if (api.getServerTextChannelById(old.getMentioncal()).isPresent())
                                    api.getServerTextChannelById(old.getMentioncal()).get().delete();
                                response = "セットアップ完了";
                            }
                        } else {
                            ChannelList list = dao.TempGetChannelList(e.getChannel().getIdAsString(), "t");
                            if (mes.split(prefix).length > 1 && e.getMessage().getUserAuthor().isPresent()) {
                                String[] cmd = mes.split(prefix)[1].split(" ");
                                boolean sw = false;
                                for (Role role : e.getMessage().getUserAuthor().get().getRoles(e.getServer().get()))
                                    if (role.getAllowedPermissions().stream().anyMatch(permission -> permission.equals(PermissionType.ADMINISTRATOR)))
                                        sw = true;

                                if (sw || e.getMessage().getUserAuthor().get().isBotOwner())
                                    if (cmd[0].equalsIgnoreCase("ping")) response = "pong!";
                                    else if (cmd[0].equalsIgnoreCase("set")) {
                                        try {
                                            if (cmd[1].equalsIgnoreCase("prefix")) if (cmd[2].length() == 1) {
                                                dao.BotSetDate("p", server, cmd[2]);
                                                response = "Prefix更新完了 --> " + cmd[2];
                                            } else response = "一文字だけにしてください";
                                            else if (cmd[1].equalsIgnoreCase("vcat"))
                                                if (api.getChannelCategoryById(cmd[2]).isPresent())
                                                    if (api.getChannelCategoryById(cmd[2]).get().getServer().getId() == serverl) {
                                                        dao.BotSetDate("v", server, cmd[2]);
                                                        response = "VoiceCategory更新可能";
                                                    } else response = "このサーバーのカテゴリを設定してください";
                                                else response = "カテゴリIDを入力してください";
                                            else if (cmd[1].equalsIgnoreCase("tcat"))
                                                if (api.getChannelCategoryById(cmd[2]).isPresent())
                                                    if (api.getChannelCategoryById(cmd[2]).get().getServer().getId() == serverl) {
                                                        dao.BotSetDate("t", server, cmd[2]);
                                                        response = "TextCategory更新可能";
                                                    } else response = "このサーバーのカテゴリを設定してください";
                                                else response = "カテゴリIDを入力してください";
                                            else if (cmd[1].equalsIgnoreCase("men"))
                                                if (api.getServerTextChannelById(cmd[2]).isPresent() && api.getServerTextChannelById(cmd[2]).get().getServer().getId() == e.getServer().get().getId()) {
                                                    dao.setMentionChannel(cmd[2], e.getServer().get().getIdAsString());
                                                    response = "メンション送信チャンネルを更新しました";
                                                } else {
                                                    response = "テキストチャンネルを設定してください";
                                                }
                                            else if (cmd[1].equalsIgnoreCase("1stc"))
                                                if (api.getServerVoiceChannelById(cmd[2]).isPresent())
                                                    if (api.getServerVoiceChannelById(cmd[2]).get().getServer().getId() == serverl) {
                                                        dao.BotSetDate("f", server, cmd[2]);
                                                        response = "FirstChannel更新可能";
                                                    } else response = "このサーバーの通話チャンネルを設定してください";
                                                else response = "通話チャンネルのIDを入力してください";
                                            else if (cmd[1].equalsIgnoreCase("role")) {
                                                StringBuilder str = new StringBuilder();
                                                try {
                                                    ReactionRoleRecord record = dao.getReactMessageData(server);
                                                    if (api.getRoleById(cmd[2]).isPresent() && EmojiManager.isEmoji(cmd[3].split("️")[0]) && record.getServerid().equalsIgnoreCase(server) && api.getServerTextChannelById(record.getTextchannelid()).isPresent()) {
                                                        dao.setReactRoleData(record.getMessageid(), cmd[2], cmd[3]);
                                                        api.getMessageById(record.getMessageid(), api.getServerTextChannelById(record.getTextchannelid()).get()).join().addReaction(cmd[3]).join();
                                                        response = "リアクションロール設定完了";
                                                    } else if (!EmojiManager.isEmoji(cmd[3]))
                                                        str.append("それは絵文字ではない\n");
                                                    else if (!api.getRoleById(cmd[2]).isPresent())
                                                        str.append("それはロールではない\n");
                                                } catch (NumberFormatException ignored) {
                                                    str.append("それは数字じゃない");
                                                }
                                                response = str.toString();
                                            } else if (cmd[1].equalsIgnoreCase("mess") && cmd.length > 3)
                                                if (api.getServerTextChannelById(cmd[3]).isPresent() && serverl == api.getServerTextChannelById(cmd[3]).get().getServer().getId())
                                                    if (api.getMessageById(cmd[2], api.getServerTextChannelById(cmd[3]).get()).join().getServer().isPresent() && api.getMessageById(cmd[2], api.getServerTextChannelById(cmd[3]).get()).join().getServer().get().getId() == serverl) {
                                                        dao.setReactMessageData(server, cmd[3], cmd[2]);
                                                        response = "メッセージ設定完了";
                                                    }
                                        } catch (SystemException | DatabaseException ignored) {
                                        }
                                    } else if (cmd[0].equalsIgnoreCase("remove")) {
                                        if (cmd[1].equalsIgnoreCase("role") && EmojiManager.isEmoji(cmd[2])) {
                                            ReactionRoleRecord record = dao.getReactMessageData(server);
                                            if (api.getServerTextChannelById(record.getTextchannelid()).isPresent()) {
                                                dao.deleteRoles(cmd[2], record.getMessageid());
                                                api.getMessageById(record.getMessageid(), api.getServerTextChannelById(record.getTextchannelid()).get()).join().removeReactionByEmoji(cmd[2]).join();
                                            }
                                        }
                                    } else if (cmd[0].equalsIgnoreCase("setup")) {
                                        ServerDataList old = dao.TempGetData(server);
                                        Server serverem = e.getServer().get();
                                        ServerDataList data = new ServerDataList();
                                        data.setServer(server);
                                        data.setFstchannel(new ServerVoiceChannelBuilder(serverem).setName("NewTEMP").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(e.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.SEND_MESSAGES).build()).create().join().getIdAsString());
                                        data.setMentioncal(new ServerTextChannelBuilder(serverem).setName("Mention").setRawPosition(1).create().join().getIdAsString());
                                        data.setVoicecate(new ChannelCategoryBuilder(serverem).setName("Voice").setRawPosition(0).create().join().getIdAsString());
                                        data.setTextcate(new ChannelCategoryBuilder(serverem).setName("Text").setRawPosition(0).create().join().getIdAsString());
                                        dao.TempDataUpData(data);
                                        if (api.getChannelCategoryById(old.getVoicecate()).isPresent())
                                            api.getChannelCategoryById(old.getVoicecate()).get().delete();
                                        if (api.getChannelCategoryById(old.getTextcate()).isPresent())
                                            api.getChannelCategoryById(old.getTextcate()).get().delete();
                                        if (api.getServerVoiceChannelById(old.getFstchannel()).isPresent())
                                            api.getServerVoiceChannelById(old.getFstchannel()).get().delete();
                                        if (api.getServerTextChannelById(old.getMentioncal()).isPresent())
                                            api.getServerTextChannelById(old.getMentioncal()).get().delete();
                                        response = "セットアップ完了";
                                    } else if (cmd[0].equalsIgnoreCase("mess")) {
                                        e.getMessage().delete();
                                        ArrayList<String> mess = new ArrayList<>(Arrays.asList(cmd));
                                        mess.remove("mess");
                                        String str = mess.stream().map(st -> st + "\n").collect(Collectors.joining());
                                        new MessageBuilder().setContent(str).send(e.getChannel()).join();
                                    } else if (cmd[0].equalsIgnoreCase("show")) {
                                        ServerDataList tempdata = dao.TempGetData(server);
                                        ReactionRoleRecord react = dao.getReactAllData(tempdata.getServer());
                                        e.getMessage().delete();
                                        String[] emojis = react.getEmoji().toArray(new String[0]);
                                        String[] roles = react.getRoleid().toArray(new String[0]);
                                        StringBuilder reacts = new StringBuilder();
                                        if (api.getServerTextChannelById(react.getTextchannelid()).isPresent())
                                            reacts.append("・リアクションロールメッセージ : ").append(api.getMessageById(react.getMessageid(), api.getServerTextChannelById(react.getTextchannelid()).get()).join().getLink()).append("\n");
                                        for (int i = 0; i < emojis.length; i++)
                                            if (api.getRoleById(roles[i]).isPresent())
                                                reacts.append("・リアクションロール : ").append("@").append(api.getRoleById(roles[i]).get().getName()).append(" >>>> ").append(emojis[i]).append("\n");
                                        EmbedBuilder embed = new EmbedBuilder()
                                                .setTitle("一覧情報表示")
                                                .setAuthor(e.getMessage().getUserAuthor().get())
                                                .addField("サーバー情報一覧", "・メンション送信チャンネルID : <#" + tempdata.getMentioncal() + ">\n" +
                                                        "・一時作成チャネル : <#" + tempdata.getFstchannel() + ">\n" +
                                                        "・通話カテゴリ : <#" + tempdata.getVoicecate() + ">\n" +
                                                        "・テキストカテゴリ : <#" + tempdata.getTextcate() + ">\n" +
                                                        "・prefix : " + tempdata.getPrefix() + "\n" +
                                                        reacts)
                                                .setColor(Color.cyan)
                                                .setThumbnail(new File("src/main/resources/s.png"));
                                        e.getMessage().getUserAuthor().get().sendMessage(embed);
                                    }
                                if (e.getMessage().getUserAuthor().get().getConnectedVoiceChannel(e.getServer().get()).isPresent() && list.getVoiceid() != null) {
                                    String requestvoiceid = dao.TempGetChannelList(e.getChannel().getIdAsString(), "t").getVoiceid();
                                    if (requestvoiceid.equalsIgnoreCase(e.getMessage().getUserAuthor().get().getConnectedVoiceChannel(e.getServer().get()).get().getIdAsString()) && api.getServerVoiceChannelById(requestvoiceid).isPresent() && api.getServerVoiceChannelById(requestvoiceid).get().getEffectivePermissions(e.getMessage().getUserAuthor().get()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS))
                                        if (cmd[0].equalsIgnoreCase("name") || cmd[0].equalsIgnoreCase("n")) {//ここから
                                            if (api.getServerVoiceChannelById(list.getVoiceid()).isPresent())
                                                api.getServerVoiceChannelById(list.getVoiceid()).get().createUpdater().setName(cmd[1]).update();
                                            if (api.getServerTextChannelById(list.getTextid()).isPresent())
                                                api.getServerTextChannelById(list.getTextid()).get().createUpdater().setName(cmd[1]).update();
                                            response = "NAME更新完了";
                                        } else if (cmd[0].equalsIgnoreCase("size") || cmd[0].equalsIgnoreCase("s")) {
                                            if (api.getServerVoiceChannelById(list.getVoiceid()).isPresent())
                                                api.getServerVoiceChannelById(list.getVoiceid()).get().createUpdater().setUserLimit(Integer.parseInt(cmd[1])).update();
                                            response = "人数制限を" + cmd[1] + "に設定しました";
                                            if (Integer.parseInt(cmd[1]) == 0) response = "人数制限を0(limitless)に設定しました";
                                        } else if (cmd[0].equalsIgnoreCase("men") || cmd[0].equalsIgnoreCase("m")) {
                                            StringBuilder strb = new StringBuilder("@here <#" + list.getVoiceid() + ">");
                                            for (int i = 1; i < cmd.length; i++) strb.append("\n").append(cmd[i]);
                                            ServerDataList serverlist = dao.TempGetData(server);
                                            if (api.getServerTextChannelById(serverlist.getMentioncal()).isPresent()) {
                                                ServerTextChannel mention = api.getServerTextChannelById(serverlist.getMentioncal()).get();
                                                dao.addMentionMessage(list.getTextid(), new MessageBuilder().setContent(strb.toString()).send(mention).join().getIdAsString(), server);
                                            }
                                            response = "募集メッセを送信しました";
                                        }
                                }
                                if (cmd[0].equalsIgnoreCase("help")) {
                                    e.getMessage().delete();
                                    EmbedBuilder embed = new EmbedBuilder()
                                            .setTitle("BOT情報案内 With" + e.getServer().get().getName())
                                            .setAuthor(e.getMessage().getUserAuthor().get())
                                            .addField("[ADMIN]確認用コマンド一覧", "・`" + prefix + ("help` -> コマンド一覧を表示\n" +
                                                    "・`" + prefix + "show` -> サーバーの設定状況を確認\n"))
                                            .addField("[ADMIN]設定コマンド一覧", "・`" + prefix + ("setup` -> 必要なチャンネルとカテゴリを自動作成\n" +
                                                    "・`" + prefix + "set prefix <prefix>` -> コマンドの前に打つ文字を変更\n" +
                                                    "・`" + prefix + "set vcat <カテゴリID>` -> 一時通話の作成先を変更\n" +
                                                    "・`" + prefix + "set tcat <カテゴリID>` -> 一時チャットの作成先を変更\n" +
                                                    "・`" + prefix + "set 1stc <チャンネルID>` -> 最初に入るチャンネルを変更\n" +
                                                    "・`" + prefix + "set men <チャンネルID>` -> 募集送信チャンネル変更\n" +
                                                    "・`" + prefix + "set role <ロールID> <絵文字>`↓\n　リアクションロールの付与ロールと絵文字を変更\n" +
                                                    "・`" + prefix + "set mess <メッセージID>　<チャンネルID>`↓\n　リアクションロールの対象メッセージを変更\n" +
                                                    "・`" + prefix + "remove role <絵文字>`↓\n　リアクションロールの絵文字を削除\n"))
                                            .addField("[USER]一時チャネルコマンド一覧", "・`" + prefix + "name <文字>`or`" + prefix + "n <文字>` -> チャンネルの名前を変更\n" +
                                                    "・`" + prefix + "size <数字>`or`" + prefix + "s <数字>` -> 通話参加人数を変更\n" +
                                                    "・`" + prefix + "men <募集内容>`or`" + prefix + "m <募集内容>`↓\n　募集チャットの内容を書いて送信\n")
                                            .setColor(Color.BLUE)
                                            .setThumbnail(new File("src/main/resources/q.png"));
                                    e.getMessage().getUserAuthor().get().sendMessage(embed);
                                }
                            }
                        }
                        if (response != null) {
                            e.getMessage().delete();
                            e.getChannel().sendMessage(response);
                        }
                    }
                } catch (DatabaseException | SystemException | IOException ignored) {
                }
            });

            api.addServerVoiceChannelMemberJoinListener(e -> {
                if (!e.getUser().isBot()) {
                    ServerDataList data;
                    ChannelCategory cata = null;
                    User user = e.getUser();
                    String server = e.getServer().getIdAsString();
                    String voicecalid = e.getChannel().getIdAsString();
                    if (e.getChannel().getCategory().isPresent()) cata = e.getChannel().getCategory().get();
                    try {
                        data = dao.TempGetData(server);
                        String fstc = data.getFstchannel();
                        String vcat = data.getVoicecate();
                        String tcat = data.getTextcate();
                        if (voicecalid.equalsIgnoreCase(fstc)) {
                            if (e.getServer().getChannelCategoryById(tcat).isPresent() && e.getServer().getChannelCategoryById(vcat).isPresent()) {
                                ChannelCategory Tcat = e.getServer().getChannelCategoryById(tcat).get();
                                ChannelCategory Vcat = e.getServer().getChannelCategoryById(vcat).get();
                                ServerTextChannel text = new ServerTextChannelBuilder(e.getServer()).setName(user.getName() + " channel").setCategory(Tcat).addPermissionOverwrite(e.getServer().getEveryoneRole(), new PermissionsBuilder().setAllDenied().build()).create().get();
                                ServerVoiceChannel voice = new ServerVoiceChannelBuilder(e.getServer()).setName(user.getName() + " channel").setCategory(Vcat).setUserlimit(0).setBitrate(64000).create().get();
                                String prefix = data.getPrefix();
                                new MessageBuilder().setContent("・`" + prefix + "name <文字>` か `" + prefix + "n <文字>` -> チャンネルの名前を変更\n" +
                                        "・`" + prefix + "size <数字>` か `" + prefix + "s <数字>` -> 通話参加人数を変更\n" +
                                        "・`" + prefix + "men <募集内容>` か `" + prefix + "m <募集内容>` -> 募集チャットの内容を書いて送信\n").addComponents(
                                        ActionRow.of(Button.success("claim", "管理権限獲得"),
                                                Button.success("hide", "非表示切替"),
                                                Button.success("lock", "参加許可切替"),
                                                Button.success("mention", "募集文送信"))).send(text);
                                ChannelList list = new ChannelList();
                                list.setVoiceid(voice.getIdAsString());
                                list.setTextid(text.getIdAsString());
                                list.setServerid(server);
                                dao.TempSetChannelList(list);
                                user.move(voice);
                            }
                        } else if (cata != null && cata.getIdAsString().equalsIgnoreCase(vcat)) {
                            ChannelList list = dao.TempGetChannelList(voicecalid, "v");
                            if (api.getServerTextChannelById(list.getTextid()).isPresent()) {
                                ServerTextChannel tx = api.getServerTextChannelById(list.getTextid()).get();
                                Permissions per = new PermissionsBuilder()
                                        .setAllowed(PermissionType.READ_MESSAGES,
                                                PermissionType.READ_MESSAGE_HISTORY,
                                                PermissionType.SEND_MESSAGES,
                                                PermissionType.ADD_REACTIONS).build();
                                tx.createUpdater().addPermissionOverwrite(user, per).update();
                                if (api.getServerVoiceChannelById(list.getVoiceid()).isPresent())
                                    if (api.getServerVoiceChannelById(list.getVoiceid()).get().getConnectedUserIds().size() == 1) {
                                        api.getServerVoiceChannelById(list.getVoiceid()).get().createUpdater().addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                        api.getServerTextChannelById(list.getTextid()).get().createUpdater().addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.READ_MESSAGE_HISTORY, PermissionType.SEND_MESSAGES, PermissionType.MANAGE_CHANNELS, PermissionType.ADD_REACTIONS).build()).update();
                                    }
                            }
                        }
                    } catch (SystemException | DatabaseException | ExecutionException | InterruptedException ignored) {
                    }
                }
            });

            api.addServerVoiceChannelMemberLeaveListener(e -> {
                ServerDataList data;
                User user = e.getUser();
                ChannelCategory cata = null;
                String server = e.getServer().getIdAsString();
                if (e.getChannel().getCategory().isPresent()) cata = e.getChannel().getCategory().get();
                try {
                    data = dao.TempGetData(server);
                    String vcat = data.getVoicecate();
                    if (api.getChannelCategoryById(vcat).isPresent())
                        for (RegularServerChannel voicelist : api.getChannelCategoryById(vcat).get().getChannels())
                            if (voicelist.asServerVoiceChannel().isPresent()) {
                                ServerVoiceChannel voice = voicelist.asServerVoiceChannel().get();
                                if (voice.getConnectedUserIds().size() == 0) {
                                    ChannelList list = dao.TempGetChannelList(voice.getIdAsString(), "v");
                                    if (api.getServerVoiceChannelById(list.getVoiceid()).isPresent()) {
                                        dao.TempDeleteChannelList(api.getServerVoiceChannelById(list.getVoiceid()).get().getIdAsString(), "v");
                                        api.getServerVoiceChannelById(list.getVoiceid()).get().delete();
                                    }
                                    if (api.getServerTextChannelById(list.getTextid()).isPresent()) {
                                        dao.TempDeleteChannelList(api.getServerTextChannelById(list.getTextid()).get().getIdAsString(), "t");
                                        api.getServerTextChannelById(list.getTextid()).get().delete();
                                    }
                                    if (api.getTextChannelById(dao.getMentionCannel(list.getServerid())).isPresent()) {
                                        for (String message : dao.getMentionMessage(list.getTextid()).getMessages()) {
                                            api.getMessageById(message, api.getTextChannelById(dao.getMentionCannel(list.getServerid())).get()).join().delete();
                                        }
                                        dao.deleteMentions(list.getTextid());
                                    }
                                }
                            }
                    if (cata != null && cata.getIdAsString().equalsIgnoreCase(vcat)) {
                        String chal = e.getChannel().getIdAsString();
                        ChannelList list = dao.TempGetChannelList(chal, "v");
                        if (e.getChannel().getConnectedUserIds().size() > 0)
                            if (api.getServerTextChannelById(list.getTextid()).isPresent()) {
                                ServerTextChannel tx = api.getServerTextChannelById(list.getTextid()).get();
                                tx.createUpdater().removePermissionOverwrite(user).update();
                            }
                    }
                } catch (SystemException | DatabaseException ignored) {
                }
            });

            api.addReactionAddListener(e -> {
                if (!api.getUserById(e.getUserId()).join().isBot()) {
                    Emoji emoji = e.getEmoji();
                    if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                        String server = e.getServer().get().getIdAsString();
                        String textchannel = e.getChannel().getIdAsString();
                        String message = Long.toString(e.getMessageId());
                        ReactionRoleRecord record = dao.getReactAllData(server);
                        if (record.getTextchannelid() != null && record.getMessageid() != null && record.getTextchannelid().equalsIgnoreCase(textchannel) && record.getMessageid().equalsIgnoreCase(message)) {
                            ArrayList<String> emojis = record.getEmoji();
                            ArrayList<String> roles = record.getRoleid();
                            for (int i = 0; i < record.getEmoji().size(); i++)
                                if (emoji.asUnicodeEmoji().get().equalsIgnoreCase(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent())
                                    api.getUserById(e.getUserId()).join().addRole(api.getRoleById(roles.get(i)).get()).join();
                        }
                    }
                }
            });

            api.addReactionRemoveListener(e -> {
                if (!api.getUserById(e.getUserId()).join().isBot()) {
                    Emoji emoji = e.getEmoji();
                    if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                        String server = e.getServer().get().getIdAsString();
                        String textchannel = e.getChannel().getIdAsString();
                        String message = Long.toString(e.getMessageId());
                        ReactionRoleRecord record = dao.getReactAllData(server);
                        if (record.getTextchannelid() != null && record.getMessageid() != null && record.getTextchannelid().equalsIgnoreCase(textchannel) && record.getMessageid().equalsIgnoreCase(message)) {
                            ArrayList<String> emojis = record.getEmoji();
                            ArrayList<String> roles = record.getRoleid();
                            for (int i = 0; i < record.getEmoji().size(); i++)
                                if (emoji.asUnicodeEmoji().get().equalsIgnoreCase(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent())
                                    api.getUserById(e.getUserId()).join().removeRole(api.getRoleById(roles.get(i)).get()).join();
                        }
                    }
                }
            });

            api.addButtonClickListener(e -> {
                String response = null;
                ButtonInteraction buttonInteraction = e.getButtonInteraction();
                String id = buttonInteraction.getCustomId();
                String server = buttonInteraction.getServer().get().getIdAsString();
                if (buttonInteraction.getChannel().isPresent()) {
                    String textchlid = buttonInteraction.getChannel().get().getIdAsString();
                    try {
                        ChannelList list = dao.TempGetChannelList(textchlid, "t");
                        int sw;
                        String requestvoiceid = list.getVoiceid();
                        if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                                requestvoiceid.equalsIgnoreCase(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                                api.getServerVoiceChannelById(requestvoiceid).isPresent() && api.getServerVoiceChannelById(requestvoiceid).get().getEffectivePermissions(buttonInteraction.getUser()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                            if (buttonInteraction.getServer().isPresent())
                                if ("hide".equals(id)) {
                                    if (api.getServerVoiceChannelById(list.getVoiceid()).isPresent()) {
                                        sw = dao.GetChannelHide(textchlid);
                                        if (sw == 0) {
                                            sw = 1;
                                            api.getServerVoiceChannelById(list.getVoiceid()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build()).update();
                                            response = "通話非表示完了";
                                        } else if (sw == 1) {
                                            sw = 0;
                                            api.getServerVoiceChannelById(list.getVoiceid()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                                            response = "通話非表示解除完了";
                                        }
                                        dao.UpdateChannelHide(textchlid, sw);
                                    }
                                } else if ("lock".equals(id)) {
                                    if (api.getServerVoiceChannelById(list.getVoiceid()).isPresent()) {
                                        sw = dao.GetChannelLock(textchlid);
                                        if (sw == 0) {
                                            sw = 1;
                                            api.getServerVoiceChannelById(list.getVoiceid()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.CONNECT).build()).update();
                                            response = "通話ロック完了";
                                        } else if (sw == 1) {
                                            sw = 0;
                                            api.getServerVoiceChannelById(list.getVoiceid()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setAllowed(PermissionType.CONNECT).build()).update();
                                            response = "通話ロック解除完了";
                                        }
                                        dao.UpdateChannelLock(textchlid, sw);
                                    }
                                } else if ("mention".equals(id)) {
                                    ServerDataList serverlist = dao.TempGetData(server);
                                    if (api.getServerTextChannelById(serverlist.getMentioncal()).isPresent()) {
                                        ServerTextChannel mention = api.getServerTextChannelById(serverlist.getMentioncal()).get();
                                        dao.addMentionMessage(list.getTextid(), new MessageBuilder().setContent("@here <#" + list.getVoiceid() + ">").send(mention).join().getIdAsString(), server);
                                    }
                                    response = "募集メッセを送信しました";
                                }
                        }
                        if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                                requestvoiceid.equalsIgnoreCase(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                                api.getServerVoiceChannelById(requestvoiceid).isPresent()) {
                            if ("claim".equals(id)) {
                                boolean claimsw = true;
                                for (Map.Entry<Long, Permissions> entry : api.getServerVoiceChannelById(requestvoiceid).get().getOverwrittenUserPermissions().entrySet())
                                    if (entry.getValue().getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                        for (Long conectid : api.getServerVoiceChannelById(requestvoiceid).get().getConnectedUserIds())
                                            if (Objects.equals(conectid, entry.getKey())) {
                                                claimsw = false;
                                                break;
                                            }
                                        break;
                                    }
                                if (claimsw) {
                                    api.getServerVoiceChannelById(requestvoiceid).get().createUpdater().addPermissionOverwrite(buttonInteraction.getUser(), new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                    response = buttonInteraction.getUser().getName() + "が新しく通話管理者になりました";
                                } else response = "通話管理者が通話にいらっしゃいます";
                            }
                        }
                        e.getInteraction().createImmediateResponder().setContent(response).respond();
                    } catch (DatabaseException | SystemException ignored) {
                    }
                }
            });

            api.addServerJoinListener(e -> {
                try {
                    if (e.getServer().getSystemChannel().isPresent())
                        e.getServer().getSystemChannel().get().sendMessage(">setup を打つと\nチャンネルとカテゴリを作成されます");
                    dao.TempNewServer(e.getServer().getIdAsString());
                } catch (DatabaseException ignored) {
                }
            });
            api.addServerLeaveListener(e -> {
                try {
                    dao.TempDeleteData(e.getServer().getIdAsString());
                } catch (DatabaseException ignored) {
                }
            });
            System.out.println("URL : " + api.createBotInvite());
            System.out.println();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String cmd = br.readLine();
                if (cmd.equalsIgnoreCase("stop")) {
                    System.out.println("システムを終了します");
                    System.exit(0);
                } else if (cmd.equalsIgnoreCase("reload")) {
                    int i = 0;
                    int k = 0;
                    int j = 0;
                    String out = "削除するサーバーデータがありませんでした";
                    String outs = "削除するメンションデータがありませんでした";
                    String outm = "削除する一時データがありませんでした";
                    for (String serverid : dao.getServerList())
                        if (!api.getServerById(serverid).isPresent()) {
                            i++;
                            dao.TempDeleteData(serverid);
                            System.out.println("右のサーバーデーターを削除しました -> " + serverid);
                        }
                    for (String text : dao.getAllMentionText().getTextid())
                        if (!api.getServerTextChannelById(text).isPresent()) {
                            k++;
                            dao.deleteMentions(text);
                            System.out.println("右のメンションデータを削除しました -> " + text);
                        }
                    for (String voice : dao.TempVoiceids())
                        if (!api.getServerVoiceChannelById(voice).isPresent()) {
                            j++;
                            dao.TempDeleteChannelList(voice, "v");
                            System.out.println("右の一時データを削除しました -> " + voice);
                        }
                    if (i > 0) out = "サーバーデータ削除完了";
                    if (k > 0) outs = "メンションデータ削除完了";
                    if (j > 0) outm = "一時データ削除完了";
                    System.out.println(out + "\n" + outs + "\n" + outm);
                }
            }
        } catch (DatabaseException | SystemException | IOException ignored) {
        }
    }
}

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
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
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

    private EmbedBuilder createShow(String serverId, User sendUser, MessageCreateEvent e, DiscordDAO dao, DiscordApi api) throws SystemException, DatabaseException {
        ServerDataList tempData = dao.TempGetData(serverId);
        ReactionRoleRecord react = dao.getReactAllData(tempData.getServer());
        e.getMessage().delete();
        String[] emojis = react.getEmoji().toArray(new String[0]);

        String[] roles = react.getRoleID().toArray(new String[0]);
        StringBuilder reacts = new StringBuilder();
        if (api.getServerTextChannelById(react.getTextChannelID()).isPresent()) {
            reacts.append("・リアクションロールメッセージ : ").append(api.getMessageById(react.getMessageID(), api.getServerTextChannelById(react.getTextChannelID()).get()).join().getLink()).append("\n");
        }
        for (int i = 0; i < emojis.length; i++) {
            if (api.getRoleById(roles[i]).isPresent()) {
                reacts.append("・リアクションロール : ").append("@").append(api.getRoleById(roles[i]).get().getName()).append(" >>>> ").append(emojis[i]).append("\n");
            }
        }
        return new EmbedBuilder()
                .setTitle("一覧情報表示")
                .setAuthor(sendUser)
                .addField("サーバー情報一覧", "・メンション送信チャンネルID : <#" + tempData.getMentioncal() + ">\n" +
                        "・一時作成チャネル : <#" + tempData.getFstchannel() + ">\n" +
                        "・通話カテゴリ : <#" + tempData.getVoicecate() + ">\n" +
                        "・テキストカテゴリ : <#" + tempData.getTextcate() + ">\n" +
                        "・prefix : " + tempData.getPrefix() + "\n" +
                        reacts)
                .setColor(Color.cyan)
                .setThumbnail(new File("src/main/resources/s.png"));
    }

    private EmbedBuilder createHelp(String prefix, String serverName, User user) {
        return new EmbedBuilder()
                .setTitle("BOT情報案内 With" + serverName)
                .setAuthor(user)
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
    }

    @Override
    public void run() {
        try {
            DiscordDAO dao = new DiscordDAO();
            String token = dao.BotGetToken();
            DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
            for (String serverId : dao.getServerList()) {
                if (!api.getServerById(serverId).isPresent()) dao.TempDeleteData(serverId);
            }
            for (String s : dao.getAllMentionText().getTextID()) {
                if (!api.getServerTextChannelById(s).isPresent()) dao.deleteMentions(s);
            }
            for (String voice : dao.TempVoiceids()) {
                if (!api.getServerVoiceChannelById(voice).isPresent()) dao.TempDeleteChannelList(voice, "v");
            }

            api.addMessageCreateListener(e -> {
                try {
                    if (e.getMessageAuthor().asUser().isPresent() && !e.getMessageAuthor().asUser().get().isBot() && e.getServer().isPresent()) {
                        User sendUser = e.getMessageAuthor().asUser().get();
                        Server server = e.getServer().get();
                        String serverId = server.getIdAsString();
                        String prefix = dao.BotGetPrefix(serverId);
                        String messageContent = e.getMessageContent();
                        String responseMessage = null;
                        boolean isAdmin = server.getAllowedPermissions(sendUser).contains(PermissionType.ADMINISTRATOR);
                        if (prefix == null) {
                            if (isAdmin) {
                                if (messageContent.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX + "setup")) {
                                    dao.TempNewServer(serverId);
                                    ServerDataList data = new ServerDataList();
                                    data.setServer(serverId);
                                    data.setFstchannel(new ServerVoiceChannelBuilder(server).setName("NewTEMP").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(e.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.SEND_MESSAGES).build()).addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS).build()).create().join().getIdAsString());
                                    ServerTextChannel mentionChannel = new ServerTextChannelBuilder(server).setName("Mention").setRawPosition(1).create().join();
                                    mentionChannel.createUpdater().addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MENTION_EVERYONE).build()).update().join();
                                    data.setMentioncal(mentionChannel.getIdAsString());
                                    data.setVoicecate(new ChannelCategoryBuilder(server).setName("Voice").setRawPosition(0).create().join().getIdAsString());
                                    data.setTextcate(new ChannelCategoryBuilder(server).setName("Text").setRawPosition(0).create().join().getIdAsString());
                                    dao.TempDataUpData(data);
                                    responseMessage = "セットアップ完了";
                                } else if (messageContent.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX + "show")) {
                                    e.getMessage().delete();
                                    sendUser.sendMessage(createShow(serverId, sendUser, e, dao, api));
                                }
                            }
                            if (messageContent.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX + "help")) {
                                sendUser.sendMessage(createHelp(ServerPropertyParameters.DEFAULT_PREFIX.getParameter(), server.getName(), sendUser));
                            }
                        } else {

                            if (messageContent.split(prefix).length > 1) {
                                String[] cmd = messageContent.split(prefix)[1].split(" ");
                                if (cmd[0].equalsIgnoreCase("help")) {
                                    e.getMessage().delete();
                                    sendUser.sendMessage(createHelp(prefix, server.getName(), sendUser));
                                }
                                if (isAdmin) {
                                    if (cmd[0].equalsIgnoreCase("ping")) {
                                        responseMessage = "これ一覧に乗ってないよ";
                                    } else if (cmd[0].equalsIgnoreCase("set")) {
                                        if (cmd[1].equalsIgnoreCase("prefix")) {
                                            if (cmd[2].length() == 1) {
                                                dao.BotSetDate("p", serverId, cmd[2]);
                                                responseMessage = "Prefix更新完了 ⇒ " + cmd[2];
                                            } else {
                                                responseMessage = "一文字だけ入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("vcat")) {
                                            if (api.getChannelCategoryById(cmd[2]).isPresent() && api.getChannelCategoryById(cmd[2]).get().getIdAsString().equalsIgnoreCase(serverId)) {
                                                dao.BotSetDate("v", serverId, cmd[2]);
                                                responseMessage = "VoiceCategory更新可能";
                                            } else if (!api.getChannelCategoryById(cmd[2]).get().getIdAsString().equalsIgnoreCase(serverId)) {
                                                responseMessage = "このサーバーのカテゴリを設定してください";
                                            } else if (!api.getChannelCategoryById(cmd[2]).isPresent()) {
                                                responseMessage = "カテゴリIDを入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("tcat")) {
                                            if (api.getChannelCategoryById(cmd[2]).isPresent() && api.getChannelCategoryById(cmd[2]).get().getIdAsString().equalsIgnoreCase(serverId)) {
                                                dao.BotSetDate("t", serverId, cmd[2]);
                                                responseMessage = "TextCategory更新可能";
                                            } else if (!api.getChannelCategoryById(cmd[2]).get().getIdAsString().equalsIgnoreCase(serverId)) {
                                                responseMessage = "このサーバーのカテゴリを設定してください";
                                            } else if (!api.getChannelCategoryById(cmd[2]).isPresent()) {
                                                responseMessage = "カテゴリIDを入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("men")) {
                                            if (api.getServerTextChannelById(cmd[2]).isPresent() && api.getServerTextChannelById(cmd[2]).get().getServer().getId() == e.getServer().get().getId()) {
                                                dao.setMentionChannel(cmd[2], e.getServer().get().getIdAsString());
                                                responseMessage = "メンション送信チャンネルを更新しました";
                                            } else {
                                                responseMessage = "テキストチャンネルを設定してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("1stc")) {
                                            if (api.getServerVoiceChannelById(cmd[2]).isPresent()) {
                                                if (api.getServerVoiceChannelById(cmd[2]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                    dao.BotSetDate("f", serverId, cmd[2]);
                                                    responseMessage = "FirstChannel更新可能";
                                                } else {
                                                    responseMessage = "このサーバーの通話チャンネルを設定してください";
                                                }
                                            } else {
                                                responseMessage = "通話チャンネルのIDを入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("role")) {
                                            StringBuilder str = new StringBuilder();
                                            try {
                                                ReactionRoleRecord record = dao.getReactMessageData(serverId);
                                                if (api.getRoleById(cmd[2]).isPresent() && EmojiManager.isEmoji(cmd[3].split("️")[0]) && record.getServerID().equalsIgnoreCase(serverId) && api.getServerTextChannelById(record.getTextChannelID()).isPresent()) {
                                                    dao.setReactRoleData(record.getMessageID(), cmd[2], cmd[3]);
                                                    api.getMessageById(record.getMessageID(), api.getServerTextChannelById(record.getTextChannelID()).get()).join().addReaction(cmd[3]).join();
                                                    str.append("リアクションロール設定完了");
                                                } else if (!EmojiManager.isEmoji(cmd[3]))
                                                    str.append("それは絵文字ではない\n");
                                                else if (!api.getRoleById(cmd[2]).isPresent())
                                                    str.append("それはロールではない\n");
                                            } catch (NumberFormatException ignored) {
                                                str.append("それは数字じゃない");
                                            }
                                            responseMessage = str.toString();
                                        } else if (cmd[1].equalsIgnoreCase("mess")) {
                                            if (cmd.length > 3 && api.getServerTextChannelById(cmd[3]).isPresent() && api.getServerTextChannelById(cmd[3]).get().getServer().getIdAsString().equalsIgnoreCase(serverId) && api.getMessageById(cmd[2], api.getServerTextChannelById(cmd[3]).get()).join().getServer().isPresent()) {
                                                dao.setReactMessageData(serverId, cmd[3], cmd[2]);
                                                responseMessage = "メッセージ設定完了";
                                            } else if (!api.getServerTextChannelById(cmd[3]).isPresent()) {
                                                responseMessage = "チャンネルIDを入力してください";
                                            } else if (!api.getServerTextChannelById(cmd[3]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                responseMessage = "このサーバーのチャンネルIDを入力してください";
                                            } else if (!api.getMessageById(cmd[2], api.getServerTextChannelById(cmd[3]).get()).join().getServer().isPresent()) {
                                                responseMessage = "このサーバーのメッセージIDを入力してください";
                                            }
                                        }
                                    } else if (cmd[0].equalsIgnoreCase("remove")) {
                                        if (cmd[1].equalsIgnoreCase("role")) {
                                            ReactionRoleRecord record = dao.getReactMessageData(serverId);
                                            if (api.getServerTextChannelById(record.getTextChannelID()).isPresent() && EmojiManager.isEmoji(cmd[2])) {
                                                dao.deleteRoles(cmd[2], record.getMessageID());
                                                api.getMessageById(record.getMessageID(), api.getServerTextChannelById(record.getTextChannelID()).get()).join().removeReactionByEmoji(cmd[2]).join();
                                            }
                                        }
                                    } else if (cmd[0].equalsIgnoreCase("setup")) {
                                        ServerDataList old = dao.TempGetData(serverId);
                                        ServerDataList data;
                                        data = new ServerDataList();
                                        data.setServer(serverId);
                                        data.setFstchannel(new ServerVoiceChannelBuilder(server).setName("NewTEMP").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(e.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.SEND_MESSAGES).build()).addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS).build()).create().join().getIdAsString());
                                        ServerTextChannel mentionChannel = new ServerTextChannelBuilder(server).setName("Mention").setRawPosition(1).create().join();
                                        mentionChannel.createUpdater().addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MENTION_EVERYONE).build()).update().join();
                                        data.setMentioncal(mentionChannel.getIdAsString());
                                        data.setVoicecate(new ChannelCategoryBuilder(server).setName("Voice").setRawPosition(0).create().join().getIdAsString());
                                        data.setTextcate(new ChannelCategoryBuilder(server).setName("Text").setRawPosition(0).create().join().getIdAsString());
                                        dao.TempDataUpData(data);
                                        if (api.getChannelCategoryById(old.getVoicecate()).isPresent())
                                            api.getChannelCategoryById(old.getVoicecate()).get().delete();
                                        if (api.getChannelCategoryById(old.getTextcate()).isPresent())
                                            api.getChannelCategoryById(old.getTextcate()).get().delete();
                                        if (api.getServerVoiceChannelById(old.getFstchannel()).isPresent())
                                            api.getServerVoiceChannelById(old.getFstchannel()).get().delete();
                                        if (api.getServerTextChannelById(old.getMentioncal()).isPresent())
                                            api.getServerTextChannelById(old.getMentioncal()).get().delete();
                                        responseMessage = "セットアップ完了";
                                    } else if (cmd[0].equalsIgnoreCase("mess")) {
                                        ArrayList<String> mess = new ArrayList<>(Arrays.asList(cmd));
                                        mess.remove("mess");
                                        responseMessage = mess.stream().map(st -> st + "\n").collect(Collectors.joining());
                                    } else if (cmd[0].equalsIgnoreCase("show")) {
                                        sendUser.sendMessage(createShow(serverId, sendUser, e, dao, api));
                                    }
                                }
                                ChannelList list = dao.TempGetChannelList(e.getChannel().getIdAsString(), "t");
                                if (sendUser.getConnectedVoiceChannel(server).isPresent() && list.getVoiceID() != null) {
                                    String requestVoiceId = dao.TempGetChannelList(e.getChannel().getIdAsString(), "t").getVoiceID();
                                    if (requestVoiceId.equalsIgnoreCase(sendUser.getConnectedVoiceChannel(server).get().getIdAsString()) && api.getServerVoiceChannelById(requestVoiceId).isPresent() && api.getServerVoiceChannelById(requestVoiceId).get().getEffectivePermissions(sendUser).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                        if (cmd[0].equalsIgnoreCase("name") || cmd[0].equalsIgnoreCase("n")) {//ここから
                                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                                api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().setName(cmd[1]).update();
                                            }
                                            if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                                api.getServerTextChannelById(list.getTextID()).get().createUpdater().setName(cmd[1]).update();
                                            }
                                            responseMessage = "NAME更新完了";
                                        } else if (cmd[0].equalsIgnoreCase("size") || cmd[0].equalsIgnoreCase("s")) {
                                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                                api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().setUserLimit(Integer.parseInt(cmd[1])).update();
                                            }
                                            responseMessage = "人数制限を" + cmd[1] + "に設定しました";
                                            if (Integer.parseInt(cmd[1]) == 0) {
                                                responseMessage = "人数制限を0(limitless)に設定しました";
                                            }
                                        } else if (cmd[0].equalsIgnoreCase("men") || cmd[0].equalsIgnoreCase("m")) {
                                            StringBuilder mentionText = new StringBuilder("@here <#" + list.getVoiceID() + ">");
                                            for (int i = 1; i < cmd.length; i++) {
                                                mentionText.append("\n").append(cmd[i]);
                                            }
                                            ServerDataList serverList = dao.TempGetData(serverId);
                                            if (api.getServerTextChannelById(serverList.getMentioncal()).isPresent()) {
                                                ServerTextChannel mention = api.getServerTextChannelById(serverList.getMentioncal()).get();
                                                dao.addMentionMessage(list.getTextID(), new MessageBuilder().setContent(mentionText.toString()).send(mention).join().getIdAsString(), serverId);
                                            }
                                            responseMessage = "募集メッセを送信しました";
                                        }
                                    }
                                }
                            }
                        }
                        if (responseMessage != null) {
                            e.getChannel().sendMessage(responseMessage).join();
                            e.getMessage().delete();
                        }
                    }
                } catch (SystemException | DatabaseException | IOException ex) {
                    ex.printStackTrace();
                }
            });

            api.addServerVoiceChannelMemberJoinListener(e -> {
                if (!e.getUser().isBot()) {
                    User joinUser = e.getUser();
                    ChannelCategory joinChannelCategory = null;
                    Server server = e.getServer();
                    String serverId = server.getIdAsString();
                    String joinVoiceId = e.getChannel().getIdAsString();
                    if (e.getChannel().getCategory().isPresent()) {
                        joinChannelCategory = e.getChannel().getCategory().get();
                    }
                    try {
                        ServerDataList data = dao.TempGetData(serverId);
                        String firstChannel = data.getFstchannel();
                        String vcatId = data.getVoicecate();
                        String tcatId = data.getTextcate();
                        if (joinVoiceId.equalsIgnoreCase(firstChannel)) {
                            if (server.getChannelCategoryById(tcatId).isPresent() && server.getChannelCategoryById(vcatId).isPresent()) {
                                ChannelCategory tcat = server.getChannelCategoryById(tcatId).get();
                                ChannelCategory vcat = server.getChannelCategoryById(vcatId).get();
                                ServerTextChannel text = new ServerTextChannelBuilder(server).setName(joinUser.getName() + " channel").setCategory(tcat).addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setAllDenied().build()).create().get();
                                ServerVoiceChannel voice = new ServerVoiceChannelBuilder(server).setName(joinUser.getName() + " channel").setCategory(vcat).setUserlimit(0).setBitrate(64000).create().get();
                                String prefix = data.getPrefix();
                                new MessageBuilder().setContent("・`" + prefix + "name <文字>` か `" + prefix + "n <文字>` -> チャンネルの名前を変更\n" +
                                        "・`" + prefix + "size <数字>` か `" + prefix + "s <数字>` -> 通話参加人数を変更\n" +
                                        "・`" + prefix + "men <募集内容>` か `" + prefix + "m <募集内容>` -> 募集チャットの内容を書いて送信\n").addComponents(
                                        ActionRow.of(Button.success("claim", "管理権限獲得"),
                                                Button.success("hide", "非表示切替"),
                                                Button.success("lock", "参加許可切替"),
                                                Button.success("mention", "募集文送信"))).send(text);
                                ChannelList list = new ChannelList();
                                list.setVoiceID(voice.getIdAsString());
                                list.setTextID(text.getIdAsString());
                                list.setServerID(serverId);
                                dao.TempSetChannelList(list);
                                joinUser.move(voice);
                            }
                        } else if (joinChannelCategory != null && joinChannelCategory.getIdAsString().equalsIgnoreCase(vcatId)) {
                            ChannelList list = dao.TempGetChannelList(joinVoiceId, "v");
                            if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                ServerTextChannel tx = api.getServerTextChannelById(list.getTextID()).get();
                                Permissions per = new PermissionsBuilder()
                                        .setAllowed(PermissionType.READ_MESSAGES,
                                                PermissionType.READ_MESSAGE_HISTORY,
                                                PermissionType.SEND_MESSAGES,
                                                PermissionType.ADD_REACTIONS).build();
                                tx.createUpdater().addPermissionOverwrite(joinUser, per).update();
                                if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent())
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().size() == 1) {
                                        api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(joinUser, new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                        api.getServerTextChannelById(list.getTextID()).get().createUpdater().addPermissionOverwrite(joinUser, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.READ_MESSAGE_HISTORY, PermissionType.SEND_MESSAGES, PermissionType.MANAGE_CHANNELS, PermissionType.ADD_REACTIONS).build()).update();
                                    }
                            }
                        }
                    } catch (SystemException | DatabaseException | ExecutionException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            api.addServerVoiceChannelMemberLeaveListener(e -> {
                ServerDataList data;
                User user = e.getUser();
                ChannelCategory leaveChannelCategory = null;
                String serverId = e.getServer().getIdAsString();
                if (e.getChannel().getCategory().isPresent()) {
                    leaveChannelCategory = e.getChannel().getCategory().get();
                }
                try {
                    data = dao.TempGetData(serverId);
                    String voiceCategory = data.getVoicecate();
                    if (api.getChannelCategoryById(voiceCategory).isPresent())
                        for (RegularServerChannel voiceList : api.getChannelCategoryById(voiceCategory).get().getChannels()) {
                            if (voiceList.asServerVoiceChannel().isPresent()) {
                                ServerVoiceChannel voiceChannel = voiceList.asServerVoiceChannel().get();
                                if (voiceChannel.getConnectedUserIds().size() == 0) {
                                    ChannelList list = dao.TempGetChannelList(voiceChannel.getIdAsString(), "v");
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                        dao.TempDeleteChannelList(api.getServerVoiceChannelById(list.getVoiceID()).get().getIdAsString(), "v");
                                        api.getServerVoiceChannelById(list.getVoiceID()).get().delete();
                                    }
                                    if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                        dao.TempDeleteChannelList(api.getServerTextChannelById(list.getTextID()).get().getIdAsString(), "t");
                                        api.getServerTextChannelById(list.getTextID()).get().delete();
                                    }
                                    if (api.getTextChannelById(dao.getMentionCannel(list.getServerID())).isPresent()) {
                                        for (String message : dao.getMentionMessage(list.getTextID()).getMessages()) {
                                            api.getMessageById(message, api.getTextChannelById(dao.getMentionCannel(list.getServerID())).get()).join().delete();
                                        }
                                        dao.deleteMentions(list.getTextID());
                                    }
                                }
                            }
                        }
                    if (leaveChannelCategory != null && leaveChannelCategory.getIdAsString().equalsIgnoreCase(voiceCategory)) {
                        String leaveVoiceChannel = e.getChannel().getIdAsString();
                        ChannelList list = dao.TempGetChannelList(leaveVoiceChannel, "v");
                        if (e.getChannel().getConnectedUserIds().size() > 0) {
                            if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                ServerTextChannel tx = api.getServerTextChannelById(list.getTextID()).get();
                                tx.createUpdater().removePermissionOverwrite(user).update();
                            }
                        }
                    }
                } catch (SystemException | DatabaseException ignored) {
                }
            });

            api.addReactionAddListener(e -> {
                if (e.getUser().isPresent() && !e.getUser().get().isBot()) {
                    Emoji emoji = e.getEmoji();
                    if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                        String serverId = e.getServer().get().getIdAsString();
                        String textChannel = e.getChannel().getIdAsString();
                        String messageId = Long.toString(e.getMessageId());
                        ReactionRoleRecord record = dao.getReactAllData(serverId);
                        if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equalsIgnoreCase(textChannel) && record.getMessageID().equalsIgnoreCase(messageId)) {
                            ArrayList<String> emojis = record.getEmoji();
                            ArrayList<String> roles = record.getRoleID();
                            for (int i = 0; i < record.getEmoji().size(); i++) {
                                if (emoji.asUnicodeEmoji().get().equalsIgnoreCase(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                    e.getUser().get().addRole(api.getRoleById(roles.get(i)).get()).join();
                                }
                            }
                        }
                    }
                }
            });

            api.addReactionRemoveListener(e -> {
                if (e.getUser().isPresent() && !e.getUser().get().isBot()) {
                    Emoji emoji = e.getEmoji();
                    if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                        String serverId = e.getServer().get().getIdAsString();
                        String textChannel = e.getChannel().getIdAsString();
                        String messageId = Long.toString(e.getMessageId());
                        ReactionRoleRecord record = dao.getReactAllData(serverId);
                        if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equalsIgnoreCase(textChannel) && record.getMessageID().equalsIgnoreCase(messageId)) {
                            ArrayList<String> emojis = record.getEmoji();
                            ArrayList<String> roles = record.getRoleID();
                            for (int i = 0; i < record.getEmoji().size(); i++) {
                                if (emoji.asUnicodeEmoji().get().equalsIgnoreCase(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                    e.getUser().get().removeRole(api.getRoleById(roles.get(i)).get()).join();
                                }
                            }
                        }
                    }
                }
            });

            api.addButtonClickListener(e -> {
                String response = null;
                ButtonInteraction buttonInteraction = e.getButtonInteraction();
                String id = buttonInteraction.getCustomId();
                String serverId = buttonInteraction.getServer().get().getIdAsString();
                if (buttonInteraction.getChannel().isPresent()) {
                    String textChannelId = buttonInteraction.getChannel().get().getIdAsString();
                    try {
                        ChannelList list = dao.TempGetChannelList(textChannelId, "t");
                        int sw;
                        String requestVoiceId = list.getVoiceID();
                        if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                                requestVoiceId.equalsIgnoreCase(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                                api.getServerVoiceChannelById(requestVoiceId).isPresent() && api.getServerVoiceChannelById(requestVoiceId).get().getEffectivePermissions(buttonInteraction.getUser()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                            if (buttonInteraction.getServer().isPresent())
                                if (id.equalsIgnoreCase("hide")) {
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                        sw = dao.GetChannelHide(textChannelId);
                                        if (sw == 0) {
                                            sw = 1;
                                            api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build()).update();
                                            response = "通話非表示完了";
                                        } else if (sw == 1) {
                                            sw = 0;
                                            api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build()).update();
                                            response = "通話非表示解除完了";
                                        }
                                        dao.UpdateChannelHide(textChannelId, sw);
                                    }
                                } else if (id.equalsIgnoreCase("lock")) {
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                        sw = dao.GetChannelLock(textChannelId);
                                        if (sw == 0) {
                                            sw = 1;
                                            api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.CONNECT).build()).update();
                                            response = "通話ロック完了";
                                        } else if (sw == 1) {
                                            sw = 0;
                                            api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getServer().get().getEveryoneRole(), new PermissionsBuilder().setAllowed(PermissionType.CONNECT).build()).update();
                                            response = "通話ロック解除完了";
                                        }
                                        dao.UpdateChannelLock(textChannelId, sw);
                                    }
                                } else if (id.equalsIgnoreCase("mention")) {
                                    ServerDataList serverList = dao.TempGetData(serverId);
                                    if (api.getServerTextChannelById(serverList.getMentioncal()).isPresent()) {
                                        ServerTextChannel mention = api.getServerTextChannelById(serverList.getMentioncal()).get();
                                        dao.addMentionMessage(list.getTextID(), new MessageBuilder().setContent("@here <#" + list.getVoiceID() + ">").send(mention).join().getIdAsString(), serverId);
                                    }
                                    response = "募集メッセを送信しました";
                                }
                        }
                        if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                                requestVoiceId.equalsIgnoreCase(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                                api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                            if ("claim".equals(id)) {
                                boolean claimSw = true;
                                for (Map.Entry<Long, Permissions> entry : api.getServerVoiceChannelById(requestVoiceId).get().getOverwrittenUserPermissions().entrySet()) {
                                    if (entry.getValue().getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                        for (Long connectId : api.getServerVoiceChannelById(requestVoiceId).get().getConnectedUserIds()) {
                                            if (Objects.equals(connectId, entry.getKey())) {
                                                claimSw = false;
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                                if (claimSw) {
                                    api.getServerVoiceChannelById(requestVoiceId).get().createUpdater().addPermissionOverwrite(buttonInteraction.getUser(), new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                    response = buttonInteraction.getUser().getName() + "が新しく通話管理者になりました";
                                } else {
                                    response = "通話管理者が通話にいらっしゃいます";
                                }
                            }
                        }
                        e.getInteraction().createImmediateResponder().setContent(response).respond();
                    } catch (DatabaseException | SystemException ignored) {
                    }
                }
            });

            api.addServerJoinListener(e -> {
                try {
                    if (e.getServer().getSystemChannel().isPresent()) {
                        e.getServer().getSystemChannel().get().sendMessage(">setup を打つと\nチャンネルとカテゴリを作成されます");
                    }
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
            Permissions per = new PermissionsBuilder().setAllowed(PermissionType.MANAGE_EMOJIS, PermissionType.MANAGE_CHANNELS, PermissionType.READ_MESSAGES, PermissionType.SEND_MESSAGES, PermissionType.EMBED_LINKS, PermissionType.ATTACH_FILE, PermissionType.READ_MESSAGE_HISTORY, PermissionType.MENTION_EVERYONE, PermissionType.ADD_REACTIONS, PermissionType.MOVE_MEMBERS).build();
            System.out.println("URL : " + api.createBotInvite(per));
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
                    String outServer = "削除するサーバーデータがありませんでした";
                    String outMention = "削除するメンションデータがありませんでした";
                    String outTemp = "削除する一時データがありませんでした";
                    for (String serverId : dao.getServerList())
                        if (!api.getServerById(serverId).isPresent()) {
                            i++;
                            dao.TempDeleteData(serverId);
                            System.out.println("右のサーバーデーターを削除しました -> " + serverId);
                        }
                    for (String text : dao.getAllMentionText().getTextID())
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
                    if (i > 0) outServer = "サーバーデータ削除完了";
                    if (k > 0) outMention = "メンションデータ削除完了";
                    if (j > 0) outTemp = "一時データ削除完了";
                    System.out.println(outServer + "\n" + outMention + "\n" + outTemp);
                }
            }
        } catch (DatabaseException | SystemException | IOException ignored) {
        }
    }
}
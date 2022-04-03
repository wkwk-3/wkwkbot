package wkwk;

import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.paramater.ServerPropertyParameters;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class BotMain extends Thread {

    private EmbedBuilder createShow(String serverName, String serverId, User sendUser, DiscordDAO dao, DiscordApi api) throws SystemException, DatabaseException {
        ServerDataList tempData = dao.TempGetData(serverId);
        ReactionRoleRecord react = dao.getReactAllData(tempData.getServer());
        String[] emojis = react.getEmoji().toArray(new String[0]);
        String[] roles = react.getRoleID().toArray(new String[0]);
        String bys = "";
        if (tempData.getTempBy()) {
            bys += "・一時作成切り替え : 有効化\n";
        } else {
            bys += "・一時作成切り替え : 無効化\n";
        }
        if (tempData.getTextBy()) {
            bys += "・一時テキストチェンネル作成切り替え : 有効化\n";
        } else {
            bys += "・一時テキストチェンネル作成切り替え : 無効化\n";
        }
        String size = tempData.getDefaultSize();
        if (size.equals("0")) {
            bys += "・一時通話初期人数 : " + size + "(limitless)\n";
        } else {
            bys += "・一時通話初期人数 : " + size + "人\n";
        }
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
                .setTitle("一覧情報表示 With " + serverName)
                .setAuthor(sendUser)
                .addField("サーバー情報一覧", "・メンション送信チャンネルID : <#" + tempData.getMentioncal() + ">\n" +
                        "・一時作成チャネル : <#" + tempData.getFstchannel() + ">\n" +
                        "・通話カテゴリ : <#" + tempData.getVoicecate() + ">\n" +
                        "・テキストカテゴリ : <#" + tempData.getTextcate() + ">\n" +
                        "・prefix : " + tempData.getPrefix() + "\n・カスタム募集 : " + tempData.getStereotyped() + "\n" + bys +
                        reacts)
                .setColor(Color.cyan)
                .setThumbnail("https://i.imgur.com/KHpjoiu.png");
    }

    private EmbedBuilder createHelp(String prefix, String serverName, User user, boolean admin) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("BOT情報案内 With " + serverName)
                .setAuthor(user);
        if (admin) {
            embed.addField("[ADMIN]確認用コマンド一覧", "・`" + prefix + "help` -> コマンド一覧を表示\n" +
                            "・`" + prefix + "show` -> サーバーの設定状況を確認\n")
                    .addField("[ADMIN]設定コマンド一覧", "・`" + prefix + "setup` -> 必要なチャンネルとカテゴリを自動作成\n" +
                            "・`" + prefix + "set prefix <1~100文字>` -> コマンドの前に打つ文字を変更\n" +
                            "・`" + prefix + "set vcat <カテゴリID>` -> 一時通話の作成先を変更\n" +
                            "・`" + prefix + "set tcat <カテゴリID>` -> 一時チャットの作成先を変更\n" +
                            "・`" + prefix + "set first <チャンネルID>` -> 最初に入るチャンネルを変更\n" +
                            "・`" + prefix + "set men <チャンネルID>` -> 募集送信チャンネル変更\n" +
                            "・`" + prefix + "set enable <true or false>`↓\n　一時通話チャンネル作成切替\n" +
                            "・`" + prefix + "set text <true or false>`↓\n　一時テキストチャンネル作成切替\n" +
                            "・`" + prefix + "set size <0~99の数字>` -> 一時通話初期人数変更\n" +
                            "・`" + prefix + "set role <ロールID> <絵文字>`↓\n　リアクションロールの付与ロールと絵文字を変更\n" +
                            "・`" + prefix + "set mess <メッセージID> <チャンネルID>`↓\n　リアクションロールの対象メッセージを変更\n" +
                            "・`" + prefix + "set namepreset <100文字以内>`->　チャンネルネーム候補を追加\n" +
                            "・`" + prefix + "remove role <絵文字>`↓\n　リアクションロールの絵文字を削除\n" +
                            "・`" + prefix + "remove namepreset`->　名前を選んで削除\n")
                    .addField("[ADMIN]募集テンプレ設定", "・`" + prefix + "set stereo <テンプレ内容>` : テンプレ内で使える置換！\n" +
                            "　　-`&user&` : 送信を選択したユーザーのメンションに置換\n" +
                            "　　-`&text&` : 募集コマンドの募集内容で入力した内容に置換\n"+
                            "　　-`&channel&` : 募集したい通話チャネルに置換\n" +
                            "　　-`&everyone&` : Everyoneメンションに置換\n" +
                            "　　-`&here&` : Hereメンションに置換\n" +
                            "　　-`/n` : 改行")
                    .addField("[ADMIN]一時通話初期ネーム設定", "・`" + prefix + "set defname <名前>` : 名前内で使える置換！\n" +
                            "　　-`&user&` : 通話を作ったユーザーの名前に置換")
                    .addField("[ADMIN]ユーティリティ", "・`" + prefix + "mess <文字><画像>` -> メッセージをBOTに送信させなおす\n");


        }
        embed.addField("[USER]一時チャネルコマンド一覧", "・`" + prefix + "name <文字>`or`" + prefix + "n <文字>` -> チャンネルの名前を変更\n" +
                        "・`" + prefix + "size <数字>`or`" + prefix + "s <数字>` -> 通話参加人数を変更\n" +
                        "・`" + prefix + "men <募集内容>`or`" + prefix + "m <募集内容>`↓\n　募集チャットの内容を書いて送信\n")
                .setColor(Color.BLUE)
                .setThumbnail("https://i.imgur.com/oRw9ePg.png");
        return embed;
    }

    @Override
    public void run() {
        try {
            DiscordDAO dao = new DiscordDAO();
            String token = dao.BotGetToken();
            DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
            api.updateActivity(ActivityType.PLAYING , dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");

            api.addMessageCreateListener(e -> {
                try {
                    if (e.getMessageAuthor().asUser().isPresent() && !e.getMessageAuthor().asUser().get().isBot() && e.getServer().isPresent()) {
                        User sendUser = e.getMessageAuthor().asUser().get();
                        Server server = e.getServer().get();
                        String serverId = server.getIdAsString();
                        String prefix = dao.BotGetPrefix(serverId);
                        String messageContent = e.getMessageContent();
                        String responseMessageString = null;
                        MessageBuilder responseMessage = null;
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
                                    responseMessageString = "セットアップ完了";
                                } else if (messageContent.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX + "show")) {
                                    sendUser.sendMessage(createShow(server.getName(), serverId, sendUser, dao, api));
                                }
                            }
                            if (messageContent.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX + "help")) {
                                sendUser.sendMessage(createHelp(ServerPropertyParameters.DEFAULT_PREFIX.getParameter(), server.getName(), sendUser, isAdmin));
                            }
                        } else {
                            boolean sw = true;
                            if (messageContent.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX.getParameter() + "help")) {
                                sw = false;
                                sendUser.sendMessage(createHelp(prefix, server.getName(), sendUser, isAdmin));
                            }
                            if (messageContent.equalsIgnoreCase(ServerPropertyParameters.DEFAULT_PREFIX.getParameter() + "show") && isAdmin) {
                                sw = false;
                                sendUser.sendMessage(createShow(server.getName(), serverId, sendUser, dao, api));
                            }
                            if (messageContent.startsWith(prefix)) {
                                String commandHeadless = messageContent.substring(prefix.length());
                                String[] cmd = commandHeadless.split(" ");
                                if (cmd[0].equalsIgnoreCase("help") && sw) {
                                    sendUser.sendMessage(createHelp(prefix, server.getName(), sendUser, isAdmin));
                                }
                                if (isAdmin) {
                                    if (cmd[0].equalsIgnoreCase("ping")) {
                                        responseMessageString = "これ一覧に乗ってないよ";
                                    } else if (cmd[0].equalsIgnoreCase("set")) {
                                        if (cmd[1].equalsIgnoreCase("prefix")) {
                                            int prefixLen = cmd[2].length();
                                            if (0 < prefixLen && prefixLen <= 100) {
                                                dao.BotSetDate("p", serverId, cmd[2]);
                                                responseMessageString = "Prefix更新完了 ⇒ " + cmd[2];
                                            } else {
                                                responseMessageString = "100文字以内で入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("vcat")) {
                                            if (api.getChannelCategoryById(cmd[2]).isPresent() && api.getChannelCategoryById(cmd[2]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                dao.BotSetDate("v", serverId, cmd[2]);
                                                responseMessageString = "VoiceCategory更新可能";
                                            } else if (!api.getChannelCategoryById(cmd[2]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                responseMessageString = "このサーバーのカテゴリを設定してください";
                                            } else if (!api.getChannelCategoryById(cmd[2]).isPresent()) {
                                                responseMessageString = "カテゴリIDを入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("tcat")) {
                                            if (api.getChannelCategoryById(cmd[2]).isPresent() && api.getChannelCategoryById(cmd[2]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                dao.BotSetDate("t", serverId, cmd[2]);
                                                responseMessageString = "TextCategory更新可能";
                                            } else if (!api.getChannelCategoryById(cmd[2]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                responseMessageString = "このサーバーのカテゴリを設定してください";
                                            } else if (!api.getChannelCategoryById(cmd[2]).isPresent()) {
                                                responseMessageString = "カテゴリIDを入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("men")) {
                                            if (api.getServerTextChannelById(cmd[2]).isPresent() && api.getServerTextChannelById(cmd[2]).get().getServer().getId() == e.getServer().get().getId()) {
                                                dao.BotSetDate("m", serverId, cmd[2]);
                                                responseMessageString = "メンション送信チャンネルを更新しました";
                                            } else {
                                                responseMessageString = "テキストチャンネルを設定してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("first")) {
                                            if (api.getServerVoiceChannelById(cmd[2]).isPresent()) {
                                                if (api.getServerVoiceChannelById(cmd[2]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                    dao.BotSetDate("f", serverId, cmd[2]);
                                                    responseMessageString = "FirstChannel更新可能";
                                                } else {
                                                    responseMessageString = "このサーバーの通話チャンネルを設定してください";
                                                }
                                            } else {
                                                responseMessageString = "通話チャンネルのIDを入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("role")) {
                                            StringBuilder str = new StringBuilder();
                                            try {
                                                cmd[3] = cmd[3].replaceFirst("️","");
                                                ReactionRoleRecord record = dao.getReactMessageData(serverId);
                                                if (record.getServerID() != null && api.getRoleById(cmd[2]).isPresent() && EmojiManager.isEmoji(cmd[3]) && record.getServerID().equalsIgnoreCase(serverId) && api.getServerTextChannelById(record.getTextChannelID()).isPresent()) {
                                                    dao.setReactRoleData(record.getMessageID(), cmd[2], cmd[3]);
                                                    api.getMessageById(record.getMessageID(), api.getServerTextChannelById(record.getTextChannelID()).get()).join().addReaction(cmd[3]).join();
                                                    str.append("リアクションロール設定完了");
                                                }
                                                if (!EmojiManager.isEmoji(cmd[3])) {
                                                    str.append(cmd[3]).append("は絵文字ではない\n");
                                                }
                                                if (!api.getRoleById(cmd[2]).isPresent()) {
                                                    str.append(cmd[2]).append("はロールではない\n");
                                                }
                                                if (record.getServerID() == null) {
                                                    str.append("リアクションロールの対象メッセージを設定してください\n");
                                                }
                                            } catch (NumberFormatException ignored) {
                                                str.append("それは数字じゃない");
                                            }
                                            responseMessageString = str.toString();
                                        } else if (cmd[1].equalsIgnoreCase("mess")) {
                                            if (cmd.length > 3 && api.getServerTextChannelById(cmd[3]).isPresent() && api.getServerTextChannelById(cmd[3]).get().getServer().getIdAsString().equalsIgnoreCase(serverId) && api.getMessageById(cmd[2], api.getServerTextChannelById(cmd[3]).get()).join().getServer().isPresent()) {
                                                dao.setReactMessageData(serverId, cmd[3], cmd[2]);
                                                responseMessageString = "メッセージ設定完了";
                                            } else if (!api.getServerTextChannelById(cmd[3]).isPresent()) {
                                                responseMessageString = "チャンネルIDを入力してください";
                                            } else if (!api.getServerTextChannelById(cmd[3]).get().getServer().getIdAsString().equalsIgnoreCase(serverId)) {
                                                responseMessageString = "このサーバーのチャンネルIDを入力してください";
                                            } else if (!api.getMessageById(cmd[2], api.getServerTextChannelById(cmd[3]).get()).join().getServer().isPresent()) {
                                                responseMessageString = "このサーバーのメッセージIDを入力してください";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("enable")) {
                                            if (cmd[2].equalsIgnoreCase("true")) {
                                                dao.BotSetDate("tmpby", serverId, "1");
                                                responseMessageString = "通話作成を有効化しました";
                                            } else if (cmd[2].equalsIgnoreCase("false")) {
                                                dao.BotSetDate("tmpby", serverId, "0");
                                                responseMessageString = "通話作成を無効化しました";
                                            } else {
                                                responseMessageString = "'true'か'false'だけを入力して下さい";
                                            }

                                        } else if (cmd[1].equalsIgnoreCase("text")) {
                                            if (cmd[2].equalsIgnoreCase("true")) {
                                                dao.BotSetDate("txtby", serverId, "1");
                                                responseMessageString = "チャット作成を有効化しました";
                                            } else if (cmd[2].equalsIgnoreCase("false")) {
                                                dao.BotSetDate("txtby", serverId, "0");
                                                responseMessageString = "チャット作成を無効化しました";
                                            } else {
                                                responseMessageString = "'true'か'false'だけを入力して下さい";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("size")) {
                                            try {
                                                int size = Integer.parseInt(cmd[2]);
                                                if (0 <= size && size < 100) {
                                                    dao.BotSetDate("size", serverId, cmd[2]);
                                                    responseMessageString = "初期人数制限を" + cmd[2] + "人に設定しました";
                                                } else {
                                                    responseMessageString = "0~99の範囲で入力して下さい";
                                                }
                                            } catch (NumberFormatException ex) {
                                                responseMessageString = "0~99の数字を入力して下さい";
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("stereo")) {
                                            StringBuilder setText = new StringBuilder();
                                            for (int i = 2; i < cmd.length; i++) {
                                                setText.append(cmd[i]);
                                            }
                                            dao.BotSetDate("stereo", serverId, setText.toString());
                                            responseMessageString = "募集テンプレを編集しました\n"+setText;
                                        } else if (cmd[1].equalsIgnoreCase("defname")) {
                                            StringBuilder setText = new StringBuilder();
                                            for (int i = 2; i < cmd.length; i++) {
                                                setText.append(cmd[i]);
                                            }
                                            dao.BotSetDate("defname", serverId, setText.toString());
                                            responseMessageString = "一時通話の初期ネームを編集しました\n"+setText;
                                        } else if (cmd[1].equalsIgnoreCase("namepreset")) {
                                            if (cmd[2].length() <= 100) {
                                                dao.addNamePreset(serverId,cmd[2]);
                                                responseMessageString = "名前変更候補を追加しました";
                                            } else {
                                                responseMessageString = "100文字以内にしてください。";
                                            }
                                        }
                                    } else if (cmd[0].equalsIgnoreCase("remove")) {
                                        if (cmd[1].equalsIgnoreCase("role")) {
                                            ReactionRoleRecord record = dao.getReactMessageData(serverId);
                                            if (api.getServerTextChannelById(record.getTextChannelID()).isPresent() && EmojiManager.isEmoji(cmd[2])) {
                                                dao.deleteRoles(cmd[2], record.getMessageID());
                                                api.getMessageById(record.getMessageID(), api.getServerTextChannelById(record.getTextChannelID()).get()).join().removeReactionByEmoji(cmd[2]).join();
                                            }
                                        } else if (cmd[1].equalsIgnoreCase("namepreset")) {
                                            ArrayList<String> namepreset = dao.GetNamePreset(serverId);
                                            SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("removeName").setPlaceholder("削除したい名前を設定してください").setMaximumValues(1).setMinimumValues(1);
                                            for (String name : namepreset) {
                                                selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(name).setValue(name).build());
                                            }
                                            responseMessage = new MessageBuilder()
                                                    .setContent("通話名前削除")
                                                    .addComponents(ActionRow.of(selectMenuBuilder.build()));
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
                                        responseMessageString = "セットアップ完了";
                                    } else if (cmd[0].equalsIgnoreCase("mess")) {
                                        String response = commandHeadless.substring(4);
                                        responseMessage = new MessageBuilder().setContent(response);
                                        for (MessageAttachment attachment : e.getMessageAttachments()) {
                                            responseMessage.addAttachment(attachment.getUrl());
                                        }
                                    } else if (cmd[0].equalsIgnoreCase("show") && sw) {
                                        sendUser.sendMessage(createShow(server.getName(), serverId, sendUser, dao, api));
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
                                            responseMessageString = "NAME更新完了";
                                        } else if (cmd[0].equalsIgnoreCase("size") || cmd[0].equalsIgnoreCase("s")) {
                                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                                api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().setUserLimit(Integer.parseInt(cmd[1])).update();
                                            }
                                            responseMessageString = "人数制限を" + cmd[1] + "に設定しました";
                                            if (Integer.parseInt(cmd[1]) == 0) {
                                                responseMessageString = "人数制限を0(limitless)に設定しました";
                                            }

                                        } else if (cmd[0].equalsIgnoreCase("men") || cmd[0].equalsIgnoreCase("m")) {
                                            StringBuilder mentionText = new StringBuilder();
                                            for (int i = 1; i < cmd.length; i++) {
                                                mentionText.append(cmd[i]).append("\n");
                                            }
                                            ServerDataList serverList = dao.TempGetData(serverId);
                                            if (api.getServerTextChannelById(serverList.getMentioncal()).isPresent()) {
                                                ServerTextChannel mention = api.getServerTextChannelById(serverList.getMentioncal()).get();
                                                String mentionMessage = serverList.getStereotyped();
                                                mentionMessage = mentionMessage.replaceAll("&user&", "<@"+e.getMessageAuthor().asUser().get().getIdAsString()+">")
                                                        .replaceAll("&text&",mentionText.toString())
                                                        .replaceAll("/n","\n")
                                                        .replaceAll("&channel&","<#"+list.getVoiceID()+"> ")
                                                        .replaceAll("&everyone&","@everyone ")
                                                        .replaceAll("&here&","@here ");
                                                dao.addMentionMessage(list.getTextID(), new MessageBuilder().setContent(mentionMessage).send(mention).join().getIdAsString(), serverId);
                                            }
                                            responseMessageString = "募集メッセを送信しました";
                                        }
                                    }
                                }
                            }
                        }
                        if (responseMessageString != null) {
                            e.getMessage().reply(responseMessageString).join();
                        } else if (responseMessage != null) {
                            responseMessage.send(e.getChannel()).join();
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
                        if (joinVoiceId.equalsIgnoreCase(firstChannel) && data.getTempBy()) {
                            if (server.getChannelCategoryById(tcatId).isPresent() && server.getChannelCategoryById(vcatId).isPresent()) {
                                ChannelCategory tcat = server.getChannelCategoryById(tcatId).get();
                                ChannelCategory vcat = server.getChannelCategoryById(vcatId).get();
                                ChannelList list = new ChannelList();
                                String defaultName = data.getDefaultName().replaceAll("&user&", joinUser.getName());
                                if (server.getNickname(joinUser).isPresent()) {
                                    defaultName = defaultName.replaceAll("&nick&", server.getNickname(joinUser).get());
                                } else {
                                    defaultName = defaultName.replaceAll("&nick&", joinUser.getName());
                                }
                                if (data.getTextBy()) {
                                    ServerTextChannel text = new ServerTextChannelBuilder(server).setName(defaultName).setCategory(tcat).addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setAllDenied().build()).create().get();
                                    list.setTextID(text.getIdAsString());
                                    String prefix = data.getPrefix();
                                    new MessageBuilder().setContent("・`" + prefix + "name <文字>` か `" + prefix + "n <文字>` -> チャンネルの名前を変更\n" +
                                            "・`" + prefix + "size <数字>` か `" + prefix + "s <数字>` -> 通話参加人数を変更\n" +
                                            "・`" + prefix + "men <募集内容>` か `" + prefix + "m <募集内容>` -> 募集チャットの内容を書いて送信\n").addComponents(
                                            ActionRow.of(Button.success("claim", "管理権限獲得"),
                                                    Button.success("hide", "非表示切替"),
                                                    Button.success("lock", "参加許可切替"),
                                                    Button.success("transfer", "通話権限移譲"),
                                                    Button.success("name", "通話名前変更"))).send(text);
                                } else {
                                    list.setTextID("NULL");
                                }
                                ServerVoiceChannel voice = new ServerVoiceChannelBuilder(server).setName(defaultName).setCategory(vcat).setUserlimit(Integer.parseInt(data.getDefaultSize())).setBitrate(64000).create().get();
                                list.setVoiceID(voice.getIdAsString());
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
                                                PermissionType.ADD_REACTIONS,
                                                PermissionType.ATTACH_FILE,
                                                PermissionType.USE_APPLICATION_COMMANDS,
                                                PermissionType.USE_EXTERNAL_STICKERS,
                                                PermissionType.USE_EXTERNAL_EMOJIS).build();
                                tx.createUpdater().addPermissionOverwrite(joinUser, per).update();
                                if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent())
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().size() == 1) {
                                        api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(joinUser, new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                        api.getServerTextChannelById(list.getTextID()).get().createUpdater().addPermissionOverwrite(joinUser, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES,
                                                PermissionType.READ_MESSAGE_HISTORY,
                                                PermissionType.SEND_MESSAGES,
                                                PermissionType.ADD_REACTIONS,
                                                PermissionType.ATTACH_FILE,
                                                PermissionType.USE_APPLICATION_COMMANDS,
                                                PermissionType.USE_EXTERNAL_STICKERS,
                                                PermissionType.USE_EXTERNAL_EMOJIS,
                                                PermissionType.MANAGE_MESSAGES,
                                                PermissionType.MANAGE_CHANNELS).build()).update();
                                    }
                            }
                        }
                    } catch (SystemException | DatabaseException | ExecutionException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
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
                api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
            });

            api.addReactionAddListener(e -> {
                if (!e.requestUser().join().isBot()) {
                    Emoji emoji = e.getEmoji();
                    if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                        String serverId = e.getServer().get().getIdAsString();
                        String textChannel = e.getChannel().getIdAsString();
                        String messageId = e.requestMessage().join().getIdAsString();
                        ReactionRoleRecord record = dao.getReactAllData(serverId);
                        if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equalsIgnoreCase(textChannel) && record.getMessageID().equalsIgnoreCase(messageId)) {
                            ArrayList<String> emojis = record.getEmoji();
                            ArrayList<String> roles = record.getRoleID();
                            for (int i = 0; i < record.getEmoji().size(); i++) {
                                if (emoji.asUnicodeEmoji().get().equalsIgnoreCase(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                    e.requestUser().join().addRole(api.getRoleById(roles.get(i)).get()).join();
                                }
                            }
                        }
                    }
                }
            });

            api.addReactionRemoveListener(e -> {
                if (!e.requestUser().join().isBot()) {
                    Emoji emoji = e.getEmoji();
                    if (emoji.asUnicodeEmoji().isPresent() && e.getServer().isPresent()) {
                        String serverId = e.getServer().get().getIdAsString();
                        String textChannel = e.getChannel().getIdAsString();
                        String messageId = e.requestMessage().join().getIdAsString();
                        ReactionRoleRecord record = dao.getReactAllData(serverId);
                        if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equalsIgnoreCase(textChannel) && record.getMessageID().equalsIgnoreCase(messageId)) {
                            ArrayList<String> emojis = record.getEmoji();
                            ArrayList<String> roles = record.getRoleID();
                            for (int i = 0; i < record.getEmoji().size(); i++) {
                                if (emoji.asUnicodeEmoji().get().equalsIgnoreCase(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                    e.requestUser().join().removeRole(api.getRoleById(roles.get(i)).get()).join();
                                }
                            }
                        }
                    }
                }
            });

            api.addSelectMenuChooseListener(e -> {
                SelectMenuInteraction menuInteraction = e.getSelectMenuInteraction();
                String cmd = menuInteraction.getCustomId();
                String response = null;
                try {
                    if (menuInteraction.getChannel().isPresent()) {
                        ChannelList list = dao.TempGetChannelList(menuInteraction.getChannel().get().getIdAsString(), "t");
                        String requestVoiceId = list.getVoiceID();
                        if (cmd.equalsIgnoreCase("transSelect")) {
                            if (api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                                boolean claimSw = false;
                                long oldAdminId = 0L;
                                for (Map.Entry<Long, Permissions> entry : api.getServerVoiceChannelById(requestVoiceId).get().getOverwrittenUserPermissions().entrySet()) {
                                    if (entry.getValue().getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                        for (Long connectId : api.getServerVoiceChannelById(requestVoiceId).get().getConnectedUserIds()) {
                                            if (Objects.equals(connectId, entry.getKey())) {
                                                oldAdminId = entry.getKey();
                                                claimSw = true;
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                                if (claimSw) {
                                    User selectUser = api.getUserById(menuInteraction.getChosenOptions().get(0).getValue()).join();
                                    User oldAdmin = api.getUserById(oldAdminId).join();
                                    api.getServerVoiceChannelById(requestVoiceId).get().createUpdater().addPermissionOverwrite(selectUser, new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).addPermissionOverwrite(oldAdmin, new PermissionsBuilder().setUnset(PermissionType.MANAGE_CHANNELS).build()).update();
                                    response = selectUser.getName() + "が新しく通話管理者になりました";
                                } else {
                                    response = "あなたは管理者ではありません";
                                }
                            }

                        } else if (cmd.equalsIgnoreCase("name")) {
                            if (api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                                if (api.getServerTextChannelById(list.getTextID()).isPresent() && api.getServerTextChannelById(list.getTextID()).get().getOverwrittenUserPermissions().get(menuInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)){

                                    String name = menuInteraction.getChosenOptions().get(0).getValue();
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                        api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().setName(name).update();
                                    }
                                    if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                        api.getServerTextChannelById(list.getTextID()).get().createUpdater().setName(name).update();
                                    }
                                    response = "チャンネル名を" + name + "に変更しました";
                                }
                            }
                        } else if (cmd.equalsIgnoreCase("removeName")) {
                            if (api.getServerById(list.getServerID()).isPresent() && api.getServerById(list.getServerID()).get().getPermissions(menuInteraction.getUser()).getAllowedPermission().contains(PermissionType.ADMINISTRATOR) || menuInteraction.getUser().isBotOwner()) {
                                String name = menuInteraction.getChosenOptions().get(0).getValue();
                                dao.deleteNamePreset(menuInteraction.getServer().get().getIdAsString(),name);
                                response = name + "を削除しました";
                            }
                        }
                        if (response != null) {
                            menuInteraction.getMessage().delete();
                            menuInteraction.createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(response).respond();
                        }
                    }
                } catch (DatabaseException ex) {
                    ex.printStackTrace();
                }
            });

            api.addButtonClickListener(e -> {
                MessageBuilder messageBuilder = null;
                ButtonInteraction buttonInteraction = e.getButtonInteraction();
                String response = "<@"+buttonInteraction.getUser().getIdAsString()+">\n";
                String id = buttonInteraction.getCustomId();
                if (buttonInteraction.getChannel().isPresent()) {
                    String textChannelId = buttonInteraction.getChannel().get().getIdAsString();
                    try {
                        ChannelList list = dao.TempGetChannelList(textChannelId, "t");
                        String requestVoiceId = list.getVoiceID();
                        if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                                requestVoiceId.equalsIgnoreCase(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                                api.getServerVoiceChannelById(requestVoiceId).isPresent() && api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()) != null &&
                                api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                            if (buttonInteraction.getServer().isPresent())
                                if (id.equalsIgnoreCase("hide")) {
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                        PermissionsBuilder permissions = new PermissionsBuilder();
                                        boolean lockIs = dao.GetChannelLock(textChannelId);
                                        boolean hideIs = dao.GetChannelHide(textChannelId);
                                        if (hideIs) {
                                            hideIs = false;
                                            permissions.setUnset(PermissionType.READ_MESSAGES);
                                            response += "通話非表示解除完了";
                                        } else {
                                            hideIs = true;
                                            permissions.setDenied(PermissionType.READ_MESSAGES);
                                            response += "通話非表示完了";
                                        }
                                        if (lockIs) permissions.setDenied(PermissionType.CONNECT);
                                        else permissions.setUnset(PermissionType.CONNECT);
                                        ArrayList<Role> targetRole = new ArrayList<>();

                                        for (Map.Entry<Long, Permissions> permissionMap : api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenRolePermissions().entrySet()) {
                                            if (hideIs) {
                                                for (PermissionType allowType : permissionMap.getValue().getAllowedPermission()) {
                                                    if (allowType.equals(PermissionType.READ_MESSAGES) && api.getRoleById(permissionMap.getKey()).isPresent()) {
                                                        targetRole.add(api.getRoleById(permissionMap.getKey()).get());
                                                        break;
                                                    }
                                                }
                                            } else {
                                                for (PermissionType deniType : permissionMap.getValue().getDeniedPermissions()) {
                                                    if (deniType.equals(PermissionType.READ_MESSAGES) && api.getRoleById(permissionMap.getKey()).isPresent()) {
                                                        targetRole.add(api.getRoleById(permissionMap.getKey()).get());
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        ServerVoiceChannelUpdater updater = api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater();
                                        targetRole.add(buttonInteraction.getServer().get().getEveryoneRole());
                                        for (Role target : targetRole) {
                                            updater.addPermissionOverwrite(target, permissions.build());
                                        }
                                        updater.update();
                                        dao.UpdateChannelHide(textChannelId, hideIs);
                                    }
                                } else if (id.equalsIgnoreCase("lock")) {
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                        PermissionsBuilder permissions = new PermissionsBuilder();
                                        boolean lockIs = dao.GetChannelLock(textChannelId);
                                        boolean hideIs = dao.GetChannelHide(textChannelId);
                                        if (lockIs) {
                                            lockIs = false;
                                            permissions.setUnset(PermissionType.CONNECT);
                                            response += "通話ロック解除完了";
                                        } else {
                                            lockIs = true;
                                            permissions.setDenied(PermissionType.CONNECT);
                                            response += "通話ロック完了";
                                        }
                                        if (hideIs) permissions.setDenied(PermissionType.READ_MESSAGES);
                                        else permissions.setUnset(PermissionType.READ_MESSAGES);
                                        ArrayList<Role> targetRole = new ArrayList<>();

                                        for (Map.Entry<Long, Permissions> permissionMap : api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenRolePermissions().entrySet()) {
                                            if (lockIs) {
                                                for (PermissionType allowType : permissionMap.getValue().getAllowedPermission()) {
                                                    if (allowType.equals(PermissionType.CONNECT) && api.getRoleById(permissionMap.getKey()).isPresent()) {
                                                        targetRole.add(api.getRoleById(permissionMap.getKey()).get());
                                                        break;
                                                    }
                                                }
                                            } else {
                                                for (PermissionType deniType : permissionMap.getValue().getDeniedPermissions()) {
                                                    if (deniType.equals(PermissionType.CONNECT) && api.getRoleById(permissionMap.getKey()).isPresent()) {
                                                        targetRole.add(api.getRoleById(permissionMap.getKey()).get());
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        ServerVoiceChannelUpdater updater = api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater();
                                        targetRole.add(buttonInteraction.getServer().get().getEveryoneRole());
                                        for (Role target : targetRole) {
                                            updater.addPermissionOverwrite(target, permissions.build());
                                        }
                                        updater.update();
                                        dao.UpdateChannelLock(textChannelId, lockIs);
                                    }
                                } else if (id.equalsIgnoreCase("transfer")) {
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().size() > 1 && api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                        SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("transSelect").setPlaceholder("移譲ユーザーを選択してください").setMaximumValues(1).setMinimumValues(1);
                                        for (Long userId : api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds()) {
                                            selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(api.getUserById(userId).join().getName()).setValue(String.valueOf(userId)).build());
                                        }
                                        messageBuilder = new MessageBuilder()
                                                .setContent("通話管理権限移譲")
                                                .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                    } else if (api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().size() <= 1) {
                                        response = "通話内に一人しか居ません。";
                                    } else if (!api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                        response = "あなたは管理権限を持っていません。";
                                    }
                                } else if (id.equalsIgnoreCase("name")) {
                                    ArrayList<String> namepreset = dao.GetNamePreset(list.getServerID());
                                    if (namepreset.size() > 0) {
                                        SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("name").setPlaceholder("変更したい名前を設定してください").setMaximumValues(1).setMinimumValues(1);
                                        for (String name : namepreset) {
                                            selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(name).setValue(name).build());
                                        }
                                        messageBuilder = new MessageBuilder()
                                                .setContent("通話名前変更")
                                                .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                    } else {
                                        response = "名前の選択肢がありません";
                                    }
                                }
                        } else if (api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()) == null ||
                                !api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                            response += "あなたは管理者ではありません";
                        }
                        if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                                requestVoiceId.equalsIgnoreCase(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                                api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                            if (id.equalsIgnoreCase("claim")) {
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
                        if (!response.equalsIgnoreCase("<@"+buttonInteraction.getUser().getIdAsString()+">\n")) {
                            e.getInteraction().createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(response).respond();
                        } else if (messageBuilder != null) {
                            messageBuilder.send(buttonInteraction.getChannel().get());
                            buttonInteraction.createImmediateResponder().respond();
                        }
                    } catch (DatabaseException ignored) {
                    }
                }
            });

            api.addServerJoinListener(e -> {
                try {
                    if (e.getServer().getSystemChannel().isPresent()) {
                        e.getServer().getSystemChannel().get().sendMessage(">setup を打つと\nチャンネルとカテゴリを作成されます");
                        e.getServer().getSystemChannel().get().sendMessage("困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttps://discord.gg/6Z7jabh983");
                    }else if (e.getServer().getOwner().isPresent()) {
                        e.getServer().getOwner().get().sendMessage(">setup を打つと\nチャンネルとカテゴリを作成されます");
                        e.getServer().getOwner().get().sendMessage("困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttps://discord.gg/6Z7jabh983").join();
                    } else {
                        for (Channel channel : e.getServer().getChannels()) {
                            if (channel.asServerTextChannel().isPresent()) {
                                channel.asServerTextChannel().get().sendMessage(">setup を打つと\nチャンネルとカテゴリを作成されます");
                                channel.asServerTextChannel().get().sendMessage("困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttps://discord.gg/6Z7jabh983").join();
                                break;
                            }
                        }
                    }

                    dao.TempNewServer(e.getServer().getIdAsString());
                } catch (DatabaseException ignored) {
                }
                api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
            });
            api.addServerLeaveListener(e -> {
                try {
                    dao.TempDeleteData(e.getServer().getIdAsString());
                } catch (DatabaseException ignored) {
                }
                api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
            });
            Permissions per = new PermissionsBuilder().setAllowed(PermissionType.ADMINISTRATOR).build();
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
                        } else {
                            ChannelList list = dao.TempGetChannelList(api.getServerTextChannelById(text).get().getIdAsString(), "t");
                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent() && api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().size() < 1) {
                                k++;
                                api.getServerVoiceChannelById(list.getVoiceID()).get().delete();
                                if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                    api.getServerTextChannelById(list.getTextID()).get().delete();
                                }
                                dao.TempDeleteData(list.getServerID());
                                if (api.getTextChannelById(dao.getMentionCannel(list.getServerID())).isPresent()) {
                                    for (String message : dao.getMentionMessage(list.getTextID()).getMessages()) {
                                        api.getMessageById(message, api.getTextChannelById(dao.getMentionCannel(list.getServerID())).get()).join().delete();
                                    }
                                    dao.deleteMentions(list.getTextID());
                                }
                                System.out.println("右の一時通話群を削除しました -> " + list.getVoiceID());
                            }
                        }
                    for (String voice : dao.TempVoiceids()) {
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
                }
            }
        } catch (DatabaseException | SystemException | IOException ignored) {
        }
    }
}
package wkwk;

import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import org.yaml.snakeyaml.tokens.StreamEndToken;
import wkwk.Command.Help;
import wkwk.Command.Processing;
import wkwk.Command.Show;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class BotMain {
    DiscordDAO dao;
    DiscordApi api;
    public BotMain(DiscordApi api, DiscordDAO dao) {
        this.dao = dao;
        this.api = api;
        api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        User wkwk = api.getYourself();
        Help help = new Help();
        Show show = new Show();
        Processing processing = new Processing();
        api.addServerMemberJoinListener(event -> {
            ArrayList<LoggingRecord> logRecord = dao.getLogging("s", event.getServer().getIdAsString());
            logRecord.forEach(record -> {
                if (record.getLogType().equals("USER") && api.getServerTextChannelById(record.getChannelId()).isPresent()) {
                    ServerTextChannel textChannel = api.getServerTextChannelById(record.getChannelId()).get();
                    Date date = Date.from(event.getUser().getCreationTimestamp());
                    textChannel.sendMessage(new EmbedBuilder()
                            .setTitle(event.getUser().getName() + " が加入")
                            .setAuthor(event.getUser())
                            .addInlineField("ID : " + event.getUser().getIdAsString(), event.getUser().getMentionTag())
                            .addInlineField("アカウント作成日時", sd.format(date))
                            .setColor(Color.BLACK)).join();
                }
            });
        });
        api.addServerMemberLeaveListener(event -> {
            ArrayList<LoggingRecord> logRecord = dao.getLogging("s", event.getServer().getIdAsString());
            logRecord.forEach(record -> {
                if (record.getLogType().equals("USER") && api.getServerTextChannelById(record.getChannelId()).isPresent()) {
                    ServerTextChannel textChannel = api.getServerTextChannelById(record.getChannelId()).get();
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle(event.getUser().getName() + "が脱退")
                            .setAuthor(event.getUser())
                            .addInlineField("@" + event.getUser().getIdAsString(), event.getUser().getMentionTag())
                            .setColor(Color.BLACK);
                    textChannel.sendMessage(embed).join();
                }
            });
        });
        api.addMessageCreateListener(event -> {
            if (event.isPrivateMessage() && event.getMessageAuthor().asUser().isPresent()) {
                if (api.getServerTextChannelById(966824808622993462L).isPresent()) {
                    MessageBuilder messageBuilder = new MessageBuilder();
                    User privateUser = null;
                    if (event.getMessageAuthor().asUser().get().getId() == wkwk.getId() && event.getPrivateChannel().isPresent() && event.getPrivateChannel().get().getRecipient().isPresent()) {
                        privateUser = event.getPrivateChannel().get().getRecipient().get();
                    }
                    StringBuilder builder = new StringBuilder();
                    if (privateUser != null) {
                        builder.append(privateUser.getDiscriminatedName()).append("のDM\n");
                    }
                    builder.append(event.getMessageAuthor().asUser().get().getDiscriminatedName()).append("\n ID : ").append(event.getMessageAuthor().asUser().get().getIdAsString()).append("\n本文:\n").append(event.getMessageContent());
                    messageBuilder.setContent(builder.toString());
                    for (MessageAttachment attachment : event.getMessageAttachments()) {
                        messageBuilder.addAttachment(attachment.getUrl());
                    }
                    messageBuilder.send(api.getServerTextChannelById(966824808622993462L).get()).join();
                }
            }
        });
        api.addMessageDeleteListener(event -> {
            dao.deleteMessage("m", event.getMessageId());
            if (event.getMessageAuthor().isPresent() && event.getMessageAuthor().get().asUser().isPresent() && !event.getMessageAuthor().get().asUser().get().isBot() && event.getServer().isPresent()) {
                TextChannel channel = event.getChannel();
                User user = event.getMessageAuthor().get().asUser().get();
                dao.getLogging("c", channel.getIdAsString()).forEach(record -> {
                    if (api.getTextChannelById(record.getChannelId()).isPresent() && record.getLogType().equals("CHAT") && channel.getIdAsString().equals(record.getTargetChannelId()) && event.getMessageContent().isPresent()) {
                        api.getTextChannelById(record.getChannelId()).get().sendMessage(new EmbedBuilder()
                                .setAuthor(user)
                                .addField("削除LOG", event.getMessageContent().get())
                                .setColor(Color.BLACK)).join();
                    }
                });
            }
        });
        api.addSlashCommandCreateListener(event -> {
            try {
                SlashCommandInteraction interaction = event.getSlashCommandInteraction();
                StringBuilder responseString = new StringBuilder();
                MessageBuilder responseMessage = null;
                TextChannel channel;
                String cmd;
                if (interaction.getServer().isPresent() && interaction.getChannel().isPresent()) {
                    Server server = interaction.getServer().get();
                    User sendUser = interaction.getUser();
                    String serverId = server.getIdAsString();
                    boolean isAdmin = server.getAllowedPermissions(sendUser).contains(PermissionType.ADMINISTRATOR);
                    Role everyone = server.getEveryoneRole();
                    channel = interaction.getChannel().get();
                    cmd = interaction.getCommandName();
                    if (dao.getNoSlashCommandServer(serverId)) {
                        dao.BotSetDate("p", serverId, "NULL");
                    }
                    if (isAdmin) {
                        switch (cmd) {
                            case "setup":
                                ServerDataList old;
                                old = dao.TempGetData(serverId);
                                ServerTextChannel mentionChannel = new ServerTextChannelBuilder(server).setName("Mention").setRawPosition(1).create().join();
                                mentionChannel.createUpdater().addPermissionOverwrite(wkwk, new PermissionsBuilder().setAllowed(PermissionType.MENTION_EVERYONE).build()).update().join();
                                ServerDataList data = new ServerDataList();
                                data.setServer(serverId);
                                data.setFstChannel(new ServerVoiceChannelBuilder(server).setName("NewTEMP").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(everyone, new PermissionsBuilder().setDenied(PermissionType.SEND_MESSAGES).build()).addPermissionOverwrite(wkwk, new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS).build()).create().join().getIdAsString());
                                data.setMentionChannel(mentionChannel.getIdAsString());
                                data.setVoiceCategory(new ChannelCategoryBuilder(server).setName("Voice").setRawPosition(0).create().join().getIdAsString());
                                data.setTextCategory(new ChannelCategoryBuilder(server).setName("Text").setRawPosition(0).create().join().getIdAsString());
                                dao.TempDataUpData(data);
                                if (api.getChannelCategoryById(old.getVoiceCategory()).isPresent())
                                    api.getChannelCategoryById(old.getVoiceCategory()).get().delete();
                                if (api.getChannelCategoryById(old.getTextCategory()).isPresent())
                                    api.getChannelCategoryById(old.getTextCategory()).get().delete();
                                if (api.getServerVoiceChannelById(old.getFstChannel()).isPresent())
                                    api.getServerVoiceChannelById(old.getFstChannel()).get().delete();
                                if (api.getServerTextChannelById(old.getMentionChannel()).isPresent())
                                    api.getServerTextChannelById(old.getMentionChannel()).get().delete();
                                responseString = new StringBuilder("セットアップ完了");

                                break;
                            case "set":
                                if (interaction.getOptionByIndex(0).isPresent()) {
                                    String subCommandGroup = interaction.getOptionByIndex(0).get().getName();
                                    switch (subCommandGroup) {
                                        case "vcat":
                                            if (interaction.getOptionByIndex(0).get().getOptionChannelValueByName("category").isPresent()) {
                                                String category = interaction.getOptionByIndex(0).get().getOptionChannelValueByName("category").get().getIdAsString();
                                                if (api.getChannelCategoryById(category).isPresent()) {
                                                    if (api.getChannelCategoryById(category).get().getServer().getIdAsString().equals(serverId)) {
                                                        dao.BotSetDate("v", serverId, category);
                                                        responseString = new StringBuilder("VoiceCategory更新完了");
                                                    } else responseString = new StringBuilder("このサーバーのカテゴリを入力してください");
                                                } else responseString = new StringBuilder("カテゴリを入力してください");
                                            }
                                            break;
                                        case "tcat":
                                            if (interaction.getOptionByIndex(0).get().getOptionChannelValueByName("category").isPresent()) {
                                                String category = interaction.getOptionByIndex(0).get().getOptionChannelValueByName("category").get().getIdAsString();
                                                if (api.getChannelCategoryById(category).isPresent()) {
                                                    if (api.getChannelCategoryById(category).get().getServer().getIdAsString().equals(serverId)) {
                                                        dao.BotSetDate("t", serverId, category);
                                                        responseString = new StringBuilder("TextCategory更新完了");
                                                    } else responseString = new StringBuilder("このサーバーのカテゴリを入力してください");
                                                } else responseString = new StringBuilder("カテゴリを入力してください");
                                            }
                                            break;
                                        case "first":
                                            if (interaction.getOptionByIndex(0).get().getOptionChannelValueByName("voiceChannel").isPresent()) {
                                                String voiceChannel = interaction.getOptionByIndex(0).get().getOptionChannelValueByName("voiceChannel").get().getIdAsString();
                                                if (api.getServerVoiceChannelById(voiceChannel).isPresent()) {
                                                    if (api.getServerVoiceChannelById(voiceChannel).get().getServer().getIdAsString().equals(serverId)) {
                                                        dao.BotSetDate("f", serverId, voiceChannel);
                                                        responseString = new StringBuilder("FirstChannel更新完了");
                                                    } else {
                                                        responseString = new StringBuilder("このサーバーの通話チャンネルを入力してください");
                                                    }
                                                } else responseString = new StringBuilder("通話チャンネルを入力してください");
                                            }
                                            break;
                                        case "mention":
                                            if (interaction.getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").isPresent()) {
                                                String textChannel = interaction.getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").get().getIdAsString();
                                                if (api.getServerTextChannelById(textChannel).isPresent()) {
                                                    if (api.getServerTextChannelById(textChannel).get().getServer().getIdAsString().equals(serverId)) {
                                                        dao.BotSetDate("m", serverId, textChannel);
                                                        responseString = new StringBuilder("メンション送信チャンネル更新完了");
                                                    } else responseString = new StringBuilder("このサーバーのテキストチャンネルを入力してください");
                                                } else responseString = new StringBuilder("テキストチャンネルを入力してください");
                                            }
                                            break;
                                        case "enable":
                                            if (interaction.getOptionByIndex(0).get().getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionBooleanValueByName("enable").isPresent()) {
                                                String subCommand = interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getName();
                                                boolean enable = interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionBooleanValueByName("enable").get();
                                                switch (subCommand) {
                                                    case "temp":
                                                        if (enable) {
                                                            dao.BotSetDate("tempBy", serverId, "1");
                                                            responseString = new StringBuilder("通話作成を有効化しました");
                                                        } else {
                                                            dao.BotSetDate("tempBy", serverId, "0");
                                                            responseString = new StringBuilder("通話作成を無効化しました");
                                                        }
                                                        break;
                                                    case "text":
                                                        if (enable) {
                                                            dao.BotSetDate("txtBy", serverId, "1");
                                                            responseString = new StringBuilder("チャット作成を有効化しました");
                                                        } else {
                                                            dao.BotSetDate("txtBy", serverId, "0");
                                                            responseString = new StringBuilder("チャット作成を無効化しました");
                                                        }
                                                        break;
                                                }
                                            }
                                            break;
                                        case "size":
                                            if (interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionLongValueByName("num").isPresent()) {
                                                try {
                                                    int size = Math.toIntExact(interaction.getOptionByIndex(0).get().getOptionLongValueByName("num").get());
                                                    if (0 <= size && size < 100) {
                                                        dao.BotSetDate("size", serverId, Integer.toString(size));
                                                        responseString = new StringBuilder("初期人数制限を" + size + "人に設定しました");
                                                    } else responseString = new StringBuilder("0~99の範囲で入力して下さい");

                                                } catch (ArithmeticException ex) {
                                                    responseString = new StringBuilder("0~99の範囲で入力して下さい");
                                                }
                                            }
                                            break;
                                        case "role":
                                            StringBuilder str = new StringBuilder();
                                            if (interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionRoleValueByName("role").isPresent() && interaction.getOptionByIndex(0).get().getOptionStringValueByName("emoji").isPresent()) {
                                                String emoji = interaction.getOptionByIndex(0).get().getOptionStringValueByName("emoji").get().replaceFirst("️", "");
                                                String roleId = interaction.getOptionByIndex(0).get().getOptionRoleValueByName("role").get().getIdAsString();
                                                ReactionRoleRecord record = dao.getReactMessageData(serverId);
                                                if (record.getServerID() != null && api.getRoleById(roleId).isPresent() && EmojiManager.isEmoji(emoji) && record.getServerID().equals(serverId) && api.getServerTextChannelById(record.getTextChannelID()).isPresent()) {
                                                    dao.setReactRoleData(serverId, record.getMessageID(), roleId, emoji);
                                                    api.getMessageById(record.getMessageID(), api.getServerTextChannelById(record.getTextChannelID()).get()).join().addReaction(emoji).join();
                                                    str.append("リアクションロール設定完了");
                                                } else if (record.getServerID() == null) str.append("リアクションロールの対象メッセージを設定してください\n");
                                                if (!EmojiManager.isEmoji(emoji)) str.append(emoji).append("は絵文字ではない\n");
                                                responseString = new StringBuilder(str.toString());

                                            }
                                            break;
                                        case "mess":
                                            if (interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionStringValueByName("messageId").isPresent() && interaction.getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").isPresent()) {
                                                String messageId = interaction.getOptionByIndex(0).get().getOptionStringValueByName("messageId").get();
                                                ServerChannel target = interaction.getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").get();
                                                if (api.getServerTextChannelById(target.getId()).isPresent() && api.getServerTextChannelById(target.getId()).get().getServer().getIdAsString().equals(serverId) && api.getMessageById(messageId, api.getServerTextChannelById(target.getId()).get()).join().getServer().isPresent()) {
                                                    dao.setReactMessageData(serverId, target.getIdAsString(), messageId);
                                                    responseString = new StringBuilder("メッセージ設定完了");

                                                } else if (!api.getServerTextChannelById(target.getId()).isPresent()) {
                                                    responseString = new StringBuilder("チャンネルIDを入力してください");

                                                } else if (!api.getServerTextChannelById(target.getId()).get().getServer().getIdAsString().equals(serverId)) {
                                                    responseString = new StringBuilder("このサーバーのチャンネルIDを入力してください");

                                                } else if (!api.getMessageById(messageId, api.getServerTextChannelById(target.getId()).get()).join().getServer().isPresent()) {
                                                    responseString = new StringBuilder("このサーバーのメッセージIDを入力してください");

                                                }
                                            }
                                            break;
                                        case "namepreset":
                                            if (interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionStringValueByName("name").isPresent()) {
                                                String name = interaction.getOptionByIndex(0).get().getOptionStringValueByName("name").get();
                                                if (name.length() <= 100) {
                                                    dao.addNamePreset(serverId, name);
                                                    responseString = new StringBuilder("名前変更候補を追加しました");
                                                } else responseString = new StringBuilder("100文字以内にしてください。");

                                            }
                                            break;
                                        case "logging":
                                            if (interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionByIndex(0).isPresent()) {
                                                String subCommand = interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getName();
                                                ArrayList<LoggingRecord> records = new ArrayList<>();
                                                LoggingRecord logging = new LoggingRecord();
                                                logging.setServerId(serverId);
                                                logging.setChannelId(channel.getIdAsString());
                                                logging.setLogType(subCommand);
                                                ArrayList<String> targets;
                                                if (subCommand.equals("USER")) {
                                                    logging.setTargetChannelId(channel.getIdAsString());
                                                    records.add(logging);
                                                    dao.addLogging(records);
                                                    responseString = new StringBuilder("ユーザー加入・脱退履歴を設定");

                                                } else if (subCommand.equals("CHAT") && interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").isPresent()) {
                                                    targets = new ArrayList<>();
                                                    targets.add(interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").get().getIdAsString());
                                                    targets.forEach(id -> {
                                                        logging.setTargetChannelId(id);
                                                        records.add(logging);
                                                    });
                                                    dao.addLogging(records);
                                                    responseString = new StringBuilder("チャット履歴を設定");

                                                }
                                            }
                                            break;
                                        case "stereo":
                                            if (interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionStringValueByName("template").isPresent()) {
                                                String template = interaction.getOptionByIndex(0).get().getOptionStringValueByName("template").get();
                                                dao.BotSetDate("stereo", serverId, template);
                                                responseString = new StringBuilder("募集テンプレを編集しました\n" + template);

                                            }
                                            break;
                                    }
                                }
                                break;
                            case "remove":
                                if (interaction.getOptionByIndex(0).isPresent()) {
                                    String subCommandGroup = interaction.getOptionByIndex(0).get().getName();
                                    switch (subCommandGroup) {
                                        case "role":
                                            ReactionRoleRecord record = dao.getReactAllData(serverId);
                                            if (record.getEmoji().size() > 0) {
                                                SelectMenuBuilder roleMenuBuilder = new SelectMenuBuilder().setCustomId("removeRole").setPlaceholder("削除したいリアクション").setMaximumValues(1).setMinimumValues(1);
                                                for (String emoji : record.getEmoji()) {
                                                    roleMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(emoji).setValue(emoji).build());
                                                }
                                                responseMessage = new MessageBuilder()
                                                        .setContent("リアクションロール削除")
                                                        .addComponents(ActionRow.of(roleMenuBuilder.build()));
                                                responseString = new StringBuilder("選択形式を送信");
                                            } else responseString = new StringBuilder("候補がありません");
                                            break;
                                        case "namepreset":
                                            ArrayList<String> namePreset = dao.GetNamePreset(serverId);
                                            if (namePreset.size() > 0) {
                                                SelectMenuBuilder nameMenuBuilder = new SelectMenuBuilder().setCustomId("removeName").setPlaceholder("削除したい名前").setMaximumValues(1).setMinimumValues(1);
                                                for (String name : namePreset) {
                                                    nameMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(name).setValue(name).build());
                                                }
                                                responseMessage = new MessageBuilder()
                                                        .setContent("通話名前削除")
                                                        .addComponents(ActionRow.of(nameMenuBuilder.build()));
                                                responseString = new StringBuilder("選択形式を送信");
                                            } else responseString = new StringBuilder("候補がありません");
                                            break;
                                        case "logging":
                                            ArrayList<LoggingRecord> logRecord = dao.getLogging("s", serverId);
                                            if (logRecord.size() > 0) {
                                                SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("removeLogging").setPlaceholder("削除したいlog設定を選んでください").setMaximumValues(1).setMinimumValues(1);
                                                for (LoggingRecord log : logRecord) {
                                                    if (api.getServerTextChannelById(log.getTargetChannelId()).isPresent() && log.getLogType().equals("CHAT")) {
                                                        selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(log.getLogType() + ":" + api.getServerTextChannelById(log.getTargetChannelId()).get().getName()).setValue(log.getTargetChannelId() + " " + log.getLogType() + " " + log.getChannelId()).build());
                                                    } else if (api.getServerTextChannelById(log.getTargetChannelId()).isPresent() && log.getLogType().equals("USER")) {
                                                        selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(log.getLogType() + ":" + api.getServerTextChannelById(log.getTargetChannelId()).get().getName()).setValue(log.getTargetChannelId() + " " + log.getLogType() + " " + log.getChannelId()).build());
                                                    } else if (!api.getServerTextChannelById(log.getTargetChannelId()).isPresent()) {
                                                        selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(log.getLogType() + ":" + log.getTargetChannelId()).setValue(log.getChannelId() + " " + log.getLogType() + " " + log.getChannelId()).build());
                                                    }
                                                }
                                                responseMessage = new MessageBuilder()
                                                        .setContent("履歴チャンネル削除")
                                                        .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                                responseString = new StringBuilder("選択形式を送信");
                                            } else responseString = new StringBuilder("候補がありません");
                                            break;
                                    }
                                }
                                break;
                            case "start":
                                if (interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionStringValueByName("unit").isPresent() && interaction.getOptionByIndex(0).get().getOptionLongValueByName("time").isPresent()) {
                                    String subCommandGroup = interaction.getOptionByIndex(0).get().getName();
                                    String unit = interaction.getOptionByIndex(0).get().getOptionStringValueByName("unit").get();
                                    long lTime = interaction.getOptionByIndex(0).get().getOptionLongValueByName("time").get();
                                    if ("delete".equals(subCommandGroup)) {
                                        try {
                                            int time = Math.toIntExact(lTime);
                                            DeleteTimeRecord times = new DeleteTimeRecord();
                                            times.setServerId(serverId);
                                            times.setTextChannelId(channel.getIdAsString());
                                            times.setDeleteTime(time);
                                            times.setTimeUnit(unit);
                                            if (dao.addDeleteTimes(times)) {
                                                responseString = new StringBuilder(Integer.toString(time));
                                                switch (unit) {
                                                    case "s":
                                                    case "S":
                                                        responseString.append("秒");
                                                        break;
                                                    case "m":
                                                    case "M":
                                                        responseString.append("分");
                                                        break;
                                                    case "h":
                                                    case "H":
                                                        responseString.append("時間");
                                                        break;
                                                    case "d":
                                                    case "D":
                                                        responseString.append("日");
                                                        break;
                                                }
                                                responseString.append("後削除に設定しました");
                                            } else responseString = new StringBuilder("設定に失敗しました");

                                        } catch (ArithmeticException ex) {
                                            responseString = new StringBuilder("桁数が多すぎます");
                                        }
                                    }
                                }
                                break;
                            case "stop":
                                if (interaction.getOptionByIndex(0).isPresent()) {
                                    String subCommandGroup = interaction.getOptionByIndex(0).get().getName();
                                    if ("delete".equals(subCommandGroup)) {
                                        dao.removeDeleteTimes(channel.getIdAsString());
                                        responseString = new StringBuilder("自動削除を停止しました");
                                    }
                                }
                                break;
                            case "show":
                                sendUser.sendMessage(show.create(server.getName(), serverId, sendUser, dao, api));
                                responseString = new StringBuilder("個チャに送信");
                                break;
                            case "mess":
                                if (interaction.getOptionStringValueByName("text").isPresent()) {
                                    String[] splitMessage = interaction.getOptionStringValueByName("text").get().split(" ");
                                    StringBuilder message = new StringBuilder();
                                    for (String simple : splitMessage) message.append(simple).append("\n");
                                    responseMessage = new MessageBuilder().setContent(message.toString());
                                    if (interaction.getOptionStringValueByName("url").isPresent()) {
                                        String[] splitUrls = interaction.getOptionStringValueByName("url").get().split(" ");
                                        for (String url : splitUrls) {
                                            try {
                                                responseMessage.addAttachment(new URL(url));
                                            } catch (MalformedURLException ex) {
                                                responseString.append(url).append("は画像URLではない");
                                            }
                                        }
                                    }
                                    responseString = new StringBuilder("送信代行成功");
                                }
                                break;
                            case "r":
                                if (interaction.getOptionStringValueByName("ID").isPresent()) {
                                    User user = api.getUserById(interaction.getOptionStringValueByName("ID").get()).join();
                                    MessageBuilder messageBuilder = new MessageBuilder();
                                    if (interaction.getOptionStringValueByName("TEXT").isPresent()) {
                                        String text = interaction.getOptionStringValueByName("TEXT").get().replaceAll(" ", "\n");
                                        messageBuilder.setContent(text);
                                    }
                                    if (interaction.getOptionStringValueByName("IMAGE").isPresent()) {
                                        String[] images = interaction.getOptionStringValueByName("IMAGE").get().split(" ");
                                        for (String url : images) {
                                            try {
                                                messageBuilder.addAttachment(new URL(url));
                                            } catch (MalformedURLException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }
                                    messageBuilder.send(user);
                                    responseString = new StringBuilder("個人チャットに送信しました");
                                }
                                break;
                        }
                    } else {
                        if ("setup".equals(cmd) || "set".equals(cmd) || "remove".equals(cmd) || "start".equals(cmd) || "stop".equals(cmd) || "show".equals(cmd) || "mess".equals(cmd)) {
                            responseString = new StringBuilder("そのコマンドは管理者用です");
                        }
                    }
                    switch (cmd) {
                        case "ping":
                            long ping = 0L;
                            try {
                                InetAddress address = InetAddress.getByName("8.8.8.8");
                                for (int n = 0; n < 5; n++) {
                                    long start = System.currentTimeMillis();
                                    boolean ena = address.isReachable(100);
                                    long end = System.currentTimeMillis();
                                    if (ena) ping += (end - start);
                                }
                                ping /= 5L;
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            responseString = new StringBuilder(ping + "ms");
                            break;
                        case "help":
                            sendUser.sendMessage(help.Create(server.getName(), sendUser, isAdmin));
                            responseString = new StringBuilder("個チャに送信");
                            break;
                        case "invite":
                            responseString = new StringBuilder("https://wkb.page.link/bot");
                            break;
                        case "guild":
                            responseString = new StringBuilder("https://wkb.page.link/guild");
                            break;
                    }
                    if (cmd.equals("name") || cmd.equals("size") || cmd.equals("men") || cmd.equals("n") || cmd.equals("s") || cmd.equals("m") || cmd.equals("add") || cmd.equals("delete")) {
                        ChannelList list = dao.TempGetChannelList(channel.getIdAsString(), "t");
                        if (sendUser.getConnectedVoiceChannel(server).isPresent() && list.getVoiceID() != null) {
                            String requestVoiceId = dao.TempGetChannelList(channel.getIdAsString(), "t").getVoiceID();
                            if (requestVoiceId.equals(sendUser.getConnectedVoiceChannel(server).get().getIdAsString()) && api.getServerVoiceChannelById(requestVoiceId).isPresent() && api.getServerVoiceChannelById(requestVoiceId).get().getEffectivePermissions(sendUser).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                if (cmd.equals("n") || cmd.equals("name") && interaction.getOptionStringValueByName("name").isPresent()) {
                                    String name = interaction.getOptionStringValueByName("name").get();
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                        api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().setName(name).update();
                                        responseString = new StringBuilder("NAME更新完了");

                                    }
                                    if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                        api.getServerTextChannelById(list.getTextID()).get().createUpdater().setName(name).update();
                                        responseString = new StringBuilder("NAME更新完了");

                                    }
                                } else if (cmd.equals("s") || cmd.equals("size") && interaction.getOptionLongValueByName("size").isPresent()) {
                                    int size = Math.toIntExact(interaction.getOptionLongValueByName("size").get());
                                    if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent() && size >= 0 && size < 100) {
                                        api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().setUserLimit(size).update();
                                        responseString = new StringBuilder("人数制限を" + size + "に設定しました");
                                        if (size == 0L) {
                                            responseString = new StringBuilder("人数制限を0(limitless)に設定しました");
                                        }
                                    } else if (size > 99) responseString = new StringBuilder("99 以内で入力してください");
                                } else if (cmd.equals("m") || cmd.equals("men")) {
                                    StringBuilder mentionText = new StringBuilder();
                                    if (interaction.getOptionStringValueByName("text").isPresent()) {
                                        mentionText.append(interaction.getOptionStringValueByName("text").get());
                                    }
                                    ServerDataList serverList = dao.TempGetData(serverId);
                                    if (api.getServerTextChannelById(serverList.getMentionChannel()).isPresent()) {
                                        ServerTextChannel mention = api.getServerTextChannelById(serverList.getMentionChannel()).get();
                                        String mentionMessage = serverList.getStereotyped();
                                        mentionMessage = processing.RecruitingTextRePress(mentionMessage, sendUser, list, mentionText.toString());
                                        Message message = new MessageBuilder().setContent(mentionMessage).send(mention).join();
                                        message.addReaction("❌");
                                        dao.addMentionMessage(list.getTextID(), message.getIdAsString(), serverId);
                                        BotSendMessageRecord record = new BotSendMessageRecord();
                                        record.setMESSAGEID(message.getIdAsString());
                                        record.setCHANNELID(mention.getIdAsString());
                                        record.setUSERID(sendUser.getIdAsString());
                                        record.setSERVERID(serverId);
                                        dao.addBotSendMessage(record);
                                        responseString = new StringBuilder("募集メッセを送信しました");
                                    }
                                } else if (cmd.equals("add")) {
                                    String subCommand = interaction.getOptionByIndex(0).get().getName();
                                    if ("user".equals(subCommand)) {
                                        Collection<User> users = new ArrayList<>();
                                        if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                            users = server.getMembers();
                                            users.removeAll(api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUsers());
                                            users.removeIf(User::isBot);
                                        }
                                        if (users.isEmpty()) responseString = new StringBuilder("対象ユーザーが0人です");
                                        else {
                                            SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("addUser").setPlaceholder("ユーザー追加").setMinimumValues(1);
                                            selectMenuBuilder.setMaximumValues(Math.min(users.size(), 24));
                                            users.forEach(user -> selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(user.getName()).setValue(String.valueOf(user.getIdAsString())).build()));
                                            responseMessage = new MessageBuilder()
                                                    .setContent("特定ユーザー追加")
                                                    .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                            responseString = new StringBuilder("送信しました");
                                        }
                                    }
                                } else if (cmd.equals("delete")) {
                                    String subCommand = interaction.getOptionByIndex(0).get().getName();
                                    if ("user".equals(subCommand)) {
                                        Collection<User> users = server.getMembers();
                                        users.removeIf(User::isBot);
                                        if (users.isEmpty()) responseString = new StringBuilder("対象ユーザーが0人です");
                                        else {
                                            SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("deleteUser").setPlaceholder("ユーザー排除").setMinimumValues(1);
                                            selectMenuBuilder.setMaximumValues(Math.min(users.size(), 24));
                                            users.forEach(user -> selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(user.getName()).setValue(String.valueOf(user.getIdAsString())).build()));
                                            responseMessage = new MessageBuilder()
                                                    .setContent("特定ユーザー排除")
                                                    .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                            responseString = new StringBuilder("送信しました");
                                        }
                                    }
                                }
                            } else if (api.getServerVoiceChannelById(requestVoiceId).isPresent() && !api.getServerVoiceChannelById(requestVoiceId).get().getEffectivePermissions(sendUser).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                responseString = new StringBuilder("通話管理権限が無いと使用できません");
                            }
                        } else if (!sendUser.getConnectedVoiceChannel(server).isPresent()) {
                            responseString = new StringBuilder("一時通話に接続していないと使用できません");
                        }
                    }
                    if (responseMessage != null) {
                        Message message = responseMessage.send(channel).join();
                        message.addReaction("❌");
                        BotSendMessageRecord record = new BotSendMessageRecord();
                        record.setMESSAGEID(message.getIdAsString());
                        record.setCHANNELID(channel.getIdAsString());
                        record.setUSERID(sendUser.getIdAsString());
                        record.setSERVERID(serverId);
                        dao.addBotSendMessage(record);
                    }
                    interaction.createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(responseString.toString()).respond();
                }
            } catch (SystemException | DatabaseException ex) {
                ex.printStackTrace();
            }
        });
        api.addMessageCreateListener(e -> {
            if (e.getMessageAuthor().asUser().isPresent() && !e.getMessageAuthor().asUser().get().isBot() && e.getServer().isPresent()) {
                String serverId = e.getServer().get().getIdAsString();
                TextChannel channel = e.getChannel();
                ArrayList<DeleteTimeRecord> deleteList = dao.getDeleteTimes(serverId);
                if (e.getServer().get().getOwner().isPresent() && e.getMessageContent().startsWith(">") && e.getMessageAuthor().asUser().isPresent() && e.getServer().get().getPermissions(e.getMessageAuthor().asUser().get()).getAllowedPermission().contains(PermissionType.ADMINISTRATOR) || e.getServer().get().getOwner().get().getId() == e.getMessageAuthor().asUser().get().getId()) {
                    if (dao.getNoSlashCommandServer(serverId)) {
                        StringBuilder response;
                        String[] cmd = e.getMessageContent().replaceFirst(">", "").split(" ");
                        if ("help".equals(cmd[0]) || "show".equals(cmd[0]) || "ping".equals(cmd[0]) || "setup".equals(cmd[0]) || "set".equals(cmd[0]) || "remove".equals(cmd[0]) || "start".equals(cmd[0]) || "stop".equals(cmd[0]) || "mess".equals(cmd[0]) || "name".equals(cmd[0]) || "size".equals(cmd[0]) || "men".equals(cmd[0]) || "n".equals(cmd[0]) || "s".equals(cmd[0]) || "m".equals(cmd[0])) {
                            e.getMessage().delete();
                            response = new StringBuilder().append("wkwkBOTはスラッシュコマンドのみの対応になりました。\n以下のリンクから").append(e.getServer().get().getName()).append("を選ばれますと、設定はそのままにすぐにスラッシュコマンドお使い頂けます。\n(https://wkb.page.link/bot)\n\nお手数ですが").append(e.getServer().get().getName()).append("でwkwkBOTに対して `/ping`をお使いください。\nすると以降このメッセージは表示されません");
                            new MessageBuilder().setContent(response.toString()).send(e.getMessageAuthor().asUser().get()).join();
                            if (api.getTextChannelById(966824808622993461L).isPresent()) {
                                new MessageBuilder().setContent(e.getServer().get().getName() + " : " + e.getServer().get().getIdAsString()).send(api.getTextChannelById(966824808622993461L).get()).join();
                            }
                        }
                    }
                }
                for (DeleteTimeRecord record : deleteList) {
                    if (record.getTextChannelId().equals(channel.getIdAsString())) {
                        DeleteMessage message = new DeleteMessage();
                        message.setServerId(serverId);
                        message.setChannelId(channel.getIdAsString());
                        message.setMessageId(e.getMessage().getIdAsString());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(Date.from(e.getMessage().getCreationTimestamp()));
                        if ("s".equals(record.getTimeUnit()) || "S".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.SECOND, record.getDeleteTime());
                        } else if ("m".equals(record.getTimeUnit()) || "M".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.MINUTE, record.getDeleteTime());
                        } else if ("h".equals(record.getTimeUnit()) || "H".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.HOUR, record.getDeleteTime());
                        } else if ("d".equals(record.getTimeUnit()) || "D".equals(record.getTimeUnit())) {
                            calendar.add(Calendar.DAY_OF_MONTH, record.getDeleteTime());
                        }
                        message.setDeleteTime(sd.format(calendar.getTime()));
                        dao.addDeleteMessage(message);
                        break;
                    }
                }
            }
        });
        api.addServerVoiceChannelMemberJoinListener(e -> {
            if (!e.getUser().isBot()) {
                User joinUser = e.getUser();
                ChannelCategory joinChannelCategory = null;
                Server server = e.getServer();
                String serverId = server.getIdAsString();
                String joinVoiceId = e.getChannel().getIdAsString();
                if (e.getChannel().getCategory().isPresent()) joinChannelCategory = e.getChannel().getCategory().get();
                try {
                    ServerDataList data = dao.TempGetData(serverId);
                    String firstChannel = data.getFstChannel();
                    String vcatId = data.getVoiceCategory();
                    String tcatId = data.getTextCategory();
                    if (joinVoiceId.equals(firstChannel) && data.getTempBy()) {
                        if (server.getChannelCategoryById(tcatId).isPresent() && server.getChannelCategoryById(vcatId).isPresent()) {
                            ChannelCategory tcat = server.getChannelCategoryById(tcatId).get();
                            ChannelCategory vcat = server.getChannelCategoryById(vcatId).get();
                            ChannelList list = new ChannelList();
                            String defaultName = data.getDefaultName().replaceAll("&user&", joinUser.getName());
                            if (server.getNickname(joinUser).isPresent()) {
                                defaultName = defaultName.replaceAll("&nick&", server.getNickname(joinUser).get());
                            } else defaultName = defaultName.replaceAll("&nick&", joinUser.getName());
                            if (data.getTextBy()) {
                                ServerTextChannel text = new ServerTextChannelBuilder(server).setName(defaultName).setCategory(tcat).addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setAllDenied().build()).create().get();
                                list.setTextID(text.getIdAsString());
                                new MessageBuilder().setContent("通話名前変更、通話最大人数変更、募集メッセージ送信は\nスラッシュコマンドになりました").addComponents(
                                        ActionRow.of(Button.success("name", "通話名前変更"),
                                                Button.success("size", "通話人数変更"),
                                                Button.success("send-recruiting", "募集送信"),
                                                Button.success("claim", "通話権限獲得"),
                                                Button.danger("next", "次の項目"))).send(text);
                            } else list.setTextID("NULL");
                            ServerVoiceChannel voice = new ServerVoiceChannelBuilder(server).setName(defaultName).setCategory(vcat).setUserlimit(Integer.parseInt(data.getDefaultSize())).setBitrate(64000).create().get();
                            list.setVoiceID(voice.getIdAsString());
                            list.setServerID(serverId);
                            dao.TempSetChannelList(list);
                            joinUser.move(voice);
                        }
                    } else if (joinChannelCategory != null && joinChannelCategory.getIdAsString().equals(vcatId)) {
                        ChannelList list = dao.TempGetChannelList(joinVoiceId, "v");
                        if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                            ServerTextChannel tx = api.getServerTextChannelById(list.getTextID()).get();
                            PermissionsBuilder per = new PermissionsBuilder()
                                    .setAllowed(PermissionType.READ_MESSAGES,
                                            PermissionType.READ_MESSAGE_HISTORY,
                                            PermissionType.SEND_MESSAGES,
                                            PermissionType.ADD_REACTIONS,
                                            PermissionType.ATTACH_FILE,
                                            PermissionType.USE_APPLICATION_COMMANDS,
                                            PermissionType.USE_EXTERNAL_STICKERS,
                                            PermissionType.USE_EXTERNAL_EMOJIS);
                            tx.createUpdater().addPermissionOverwrite(joinUser, per.build()).update();
                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent())
                                if (api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().size() == 1) {
                                    api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(joinUser, new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                    per.setAllowed(PermissionType.MANAGE_CHANNELS, PermissionType.MANAGE_MESSAGES);
                                    api.getServerTextChannelById(list.getTextID()).get().createUpdater().addPermissionOverwrite(joinUser, per.build()).update();
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
            if (e.getChannel().getCategory().isPresent()) leaveChannelCategory = e.getChannel().getCategory().get();
            try {
                data = dao.TempGetData(serverId);
                String voiceCategory = data.getVoiceCategory();
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
                                if (api.getTextChannelById(dao.getMentionChannel(list.getServerID())).isPresent()) {
                                    for (String message : dao.getMentionMessage(list.getTextID()).getMessages()) {
                                        api.getMessageById(message, api.getTextChannelById(dao.getMentionChannel(list.getServerID())).get()).join().delete();
                                    }
                                    dao.deleteMentions(list.getTextID());
                                }
                            }
                        }
                    }
                if (leaveChannelCategory != null && leaveChannelCategory.getIdAsString().equals(voiceCategory)) {
                    String leaveVoiceChannel = e.getChannel().getIdAsString();
                    ChannelList list = dao.TempGetChannelList(leaveVoiceChannel, "v");
                    if (e.getChannel().getConnectedUserIds().size() > 0) {
                        if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                            ServerTextChannel tx = api.getServerTextChannelById(list.getTextID()).get();
                            tx.createUpdater().removePermissionOverwrite(user).update();
                        }
                        if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                            ServerVoiceChannel vx = api.getServerVoiceChannelById(list.getVoiceID()).get();
                            vx.createUpdater().removePermissionOverwrite(user).update();
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
                    if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
                        ArrayList<String> emojis = record.getEmoji();
                        ArrayList<String> roles = record.getRoleID();
                        for (int i = 0; i < record.getEmoji().size(); i++) {
                            if (emoji.asUnicodeEmoji().get().equals(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                                e.requestUser().join().addRole(api.getRoleById(roles.get(i)).get()).join();
                            }
                        }
                    }
                    if(emoji.asUnicodeEmoji().isPresent() && emoji.asUnicodeEmoji().get().equals("❌")) {
                        BotSendMessageRecord messageRecord = dao.getBotSendMessage(messageId);
                        if (!messageRecord.getMESSAGEID().equalsIgnoreCase("NULL") && messageRecord.getMESSAGEID().equalsIgnoreCase(messageId) && messageRecord.getUSERID().equalsIgnoreCase(e.getUser().get().getIdAsString())) {
                            e.getMessage().get().delete();
                            dao.deleteBotSendMessage(messageId);
                            dao.deleteMentionMessage(messageId);
                        } else {
                            e.removeReaction();
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
                    if (record.getTextChannelID() != null && record.getMessageID() != null && record.getTextChannelID().equals(textChannel) && record.getMessageID().equals(messageId)) {
                        ArrayList<String> emojis = record.getEmoji();
                        ArrayList<String> roles = record.getRoleID();
                        for (int i = 0; i < record.getEmoji().size(); i++) {
                            if (emoji.asUnicodeEmoji().get().equals(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
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
            User user = menuInteraction.getUser();
            try {
                if (menuInteraction.getChannel().isPresent()) {
                    ChannelList list = dao.TempGetChannelList(menuInteraction.getChannel().get().getIdAsString(), "t");
                    boolean isManage = api.getServerTextChannelById(list.getTextID()).get().getOverwrittenUserPermissions().get(menuInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS);
                    String requestVoiceId = list.getVoiceID();
                    if (isManage) {
                        switch (cmd) {
                            case "transSelect":
                                long oldManege = menuInteraction.getUser().getId();
                                if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                    User selectUser = api.getUserById(menuInteraction.getChosenOptions().get(0).getValue()).join();
                                    api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(selectUser, new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS, PermissionType.READ_MESSAGES).build()).removePermissionOverwrite(api.getUserById(oldManege).join()).update();
                                    if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                        api.getServerTextChannelById(list.getTextID()).get().createUpdater().addPermissionOverwrite(selectUser, new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS, PermissionType.READ_MESSAGES,
                                                PermissionType.READ_MESSAGE_HISTORY,
                                                PermissionType.SEND_MESSAGES,
                                                PermissionType.ADD_REACTIONS,
                                                PermissionType.ATTACH_FILE,
                                                PermissionType.USE_APPLICATION_COMMANDS,
                                                PermissionType.USE_EXTERNAL_STICKERS,
                                                PermissionType.USE_EXTERNAL_EMOJIS).build()).removePermissionOverwrite(api.getUserById(oldManege).join()).addPermissionOverwrite(api.getUserById(oldManege).join(), new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES,
                                                PermissionType.READ_MESSAGE_HISTORY,
                                                PermissionType.SEND_MESSAGES,
                                                PermissionType.ADD_REACTIONS,
                                                PermissionType.ATTACH_FILE,
                                                PermissionType.USE_APPLICATION_COMMANDS,
                                                PermissionType.USE_EXTERNAL_STICKERS,
                                                PermissionType.USE_EXTERNAL_EMOJIS).build()).update();
                                    }
                                    response = selectUser.getName() + "が新しく通話管理者になりました";
                                }
                                break;
                            case "name":
                                if (api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                                    if (api.getServerTextChannelById(list.getTextID()).isPresent() && api.getServerTextChannelById(list.getTextID()).get().getOverwrittenUserPermissions().get(menuInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
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
                                break;
                            case "size":
                                int size = Integer.parseInt(menuInteraction.getChosenOptions().get(0).getValue());
                                if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                    api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().setUserLimit(size).update();
                                    response = "通話人数を" + size + "に変更しました";
                                }
                                break;
                            case "addUser":
                                if (api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                                    ServerVoiceChannel voiceChannel = api.getServerVoiceChannelById(requestVoiceId).get();
                                    ServerVoiceChannelUpdater updater = voiceChannel.createUpdater();
                                    for (SelectMenuOption option : menuInteraction.getChosenOptions()) {
                                        updater.addPermissionOverwrite(api.getUserById(option.getValue()).join(), new PermissionsBuilder().setAllowed(PermissionType.CONNECT, PermissionType.READ_MESSAGES).build());
                                    }
                                    updater.update();
                                    response = menuInteraction.getChosenOptions().size() + "人を追加しました。";
                                }
                                break;
                            case "deleteUser":
                                if (api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                                    ServerVoiceChannel voiceChannel = api.getServerVoiceChannelById(requestVoiceId).get();
                                    ServerVoiceChannelUpdater updater = voiceChannel.createUpdater();
                                    for (SelectMenuOption option : menuInteraction.getChosenOptions()) {
                                        updater.addPermissionOverwrite(api.getUserById(option.getValue()).join(), new PermissionsBuilder().setDenied(PermissionType.CONNECT, PermissionType.READ_MESSAGES).build());
                                    }
                                    updater.update();
                                    response = menuInteraction.getChosenOptions().size() + "人を排除しました。";
                                }
                                break;
                        }
                    }
                    if (api.getServerById(list.getServerID()).get().getPermissions(user).getAllowedPermission().contains(PermissionType.ADMINISTRATOR)) {
                        switch (cmd) {
                            case "removeName":
                                if (api.getServerById(list.getServerID()).isPresent() && api.getServerById(list.getServerID()).get().getPermissions(menuInteraction.getUser()).getAllowedPermission().contains(PermissionType.ADMINISTRATOR) || menuInteraction.getUser().isBotOwner()) {
                                    String name = menuInteraction.getChosenOptions().get(0).getValue();
                                    if (menuInteraction.getServer().isPresent()) {
                                        dao.deleteNamePreset(menuInteraction.getServer().get().getIdAsString(), name);
                                        response = name + "を削除しました";
                                    } else response = "削除に失敗しました";
                                }
                                break;
                            case "removeLogging":
                                String[] inputs = menuInteraction.getChosenOptions().get(0).getValue().split(" ");
                                dao.deleteLogging(inputs[0], inputs[1], inputs[2]);
                                response = "削除しました";
                                break;
                            case "removeRole":
                                String selectEmoji = menuInteraction.getChosenOptions().get(0).getValue();
                                ReactionRoleRecord record = dao.getReactAllData(menuInteraction.getServer().get().getIdAsString());
                                for (String emoji : record.getEmoji()) {
                                    if (selectEmoji.equals(emoji)) {
                                        dao.deleteRoles(emoji, record.getMessageID());
                                        response = emoji + "を削除しました";
                                        break;
                                    }
                                }

                                break;
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
            String response = "";
            String id = buttonInteraction.getCustomId();
            if (buttonInteraction.getChannel().isPresent()) {
                String textChannelId = buttonInteraction.getChannel().get().getIdAsString();
                try {
                    ChannelList list = dao.TempGetChannelList(textChannelId, "t");
                    String requestVoiceId = list.getVoiceID();
                    if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                            requestVoiceId.equals(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                            api.getServerVoiceChannelById(requestVoiceId).isPresent() && api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()) != null &&
                            api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                        if (buttonInteraction.getServer().isPresent())
                            if (id.equals("hide")) {
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
                            } else if (id.equals("lock")) {
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
                            } else if (id.equals("transfer")) {
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
                            } else if (id.equals("name")) {
                                ArrayList<String> namePreset = dao.GetNamePreset(list.getServerID());
                                if (namePreset.size() > 0) {
                                    SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("name").setPlaceholder("変更したい名前を設定してください").setMaximumValues(1).setMinimumValues(1);
                                    for (String name : namePreset) {
                                        selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(name).setValue(name).build());
                                    }
                                    messageBuilder = new MessageBuilder()
                                            .setContent("通話名前変更")
                                            .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                } else response = "名前の選択肢がありません";
                            } else if (id.equals("next")) {
                                buttonInteraction.getMessage().createUpdater().removeAllComponents().addComponents(
                                        ActionRow.of(Button.danger("back", "前の項目"),
                                                Button.success("transfer", "通話権限移譲"),
                                                Button.success("remove-recruiting", "募集文削除"),
                                                Button.success("hide", "非表示切替"),
                                                Button.success("lock", "参加許可切替"))).applyChanges();
                            } else if (id.equals("back")) {
                                buttonInteraction.getMessage().createUpdater().removeAllComponents().addComponents(
                                        ActionRow.of(Button.success("name", "通話名前変更"),
                                                Button.success("size", "通話人数変更"),
                                                Button.success("send-recruiting", "募集送信"),
                                                Button.success("claim", "通話権限獲得"),
                                                Button.danger("next", "次の項目"))).applyChanges();
                            } else if (id.equals("size")) {
                                SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("size").setPlaceholder("変更したい人数を選択してください").setMaximumValues(1).setMinimumValues(1);
                                for (int n = 2; n < 7; n++) {
                                    selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(Integer.toString(n)).setValue(Integer.toString(n)).build());
                                }
                                messageBuilder = new MessageBuilder()
                                        .setContent("通話人数変更")
                                        .addComponents(ActionRow.of(selectMenuBuilder.build()));
                            } else if (id.equals("send-recruiting")) {
                                String serverId = buttonInteraction.getServer().get().getIdAsString();
                                User sendUser = buttonInteraction.getUser();
                                ServerDataList serverList = dao.TempGetData(serverId);
                                if (api.getServerTextChannelById(serverList.getMentionChannel()).isPresent()) {
                                    ServerTextChannel mention = api.getServerTextChannelById(serverList.getMentionChannel()).get();
                                    String mentionMessage = serverList.getStereotyped();
                                    mentionMessage = processing.RecruitingTextRePress(mentionMessage, sendUser, list, "");
                                    Message message = new MessageBuilder().setContent(mentionMessage).send(mention).join();
                                    message.addReaction("❌");
                                    BotSendMessageRecord record = new BotSendMessageRecord();
                                    record.setMESSAGEID(message.getIdAsString());
                                    record.setCHANNELID(buttonInteraction.getChannel().get().getIdAsString());
                                    record.setUSERID(buttonInteraction.getUser().getIdAsString());
                                    record.setSERVERID(buttonInteraction.getServer().get().getIdAsString());
                                    dao.addBotSendMessage(record);
                                    dao.addMentionMessage(list.getTextID(), message.getIdAsString(), serverId);
                                }
                            } else if (id.equals("remove-recruiting")) {
                                if (api.getTextChannelById(dao.getMentionChannel(list.getServerID())).isPresent()) {
                                    for (String message : dao.getMentionMessage(list.getTextID()).getMessages()) {
                                        api.getMessageById(message, api.getTextChannelById(dao.getMentionChannel(list.getServerID())).get()).join().delete();
                                    }
                                    dao.deleteMentions(list.getTextID());
                                }
                            }
                    } else if (api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()) == null ||
                            !api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                        response += "あなたは通話管理者ではありません";
                    }
                    if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                            requestVoiceId.equals(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                            api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                        if (id.equals("claim")) {
                            boolean claimSw = api.getServerVoiceChannelById(requestVoiceId).get().getOverwrittenUserPermissions().entrySet().stream().filter(entry -> entry.getValue().getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)).findFirst().map(entry -> api.getServerVoiceChannelById(requestVoiceId).get().getConnectedUserIds().stream().noneMatch(connectId -> Objects.equals(connectId, entry.getKey()))).orElse(true);
                            if (claimSw) {
                                api.getServerVoiceChannelById(requestVoiceId).get().createUpdater().addPermissionOverwrite(buttonInteraction.getUser(), new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                    api.getServerTextChannelById(list.getTextID()).get().createUpdater().addPermissionOverwrite(buttonInteraction.getUser(),
                                            new PermissionsBuilder().setAllowed(
                                                    PermissionType.MANAGE_CHANNELS,
                                                    PermissionType.READ_MESSAGES,
                                                    PermissionType.READ_MESSAGE_HISTORY,
                                                    PermissionType.SEND_MESSAGES,
                                                    PermissionType.ADD_REACTIONS,
                                                    PermissionType.ATTACH_FILE,
                                                    PermissionType.USE_APPLICATION_COMMANDS,
                                                    PermissionType.USE_EXTERNAL_STICKERS,
                                                    PermissionType.USE_EXTERNAL_EMOJIS).build()).update();
                                }
                                response = buttonInteraction.getUser().getName() + "が新しく通話管理者になりました";
                            } else response = "通話管理者が通話にいらっしゃいます";
                        }
                    }
                    e.getInteraction().createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(response).respond();
                    if (messageBuilder != null) {
                        Message message = messageBuilder.send(buttonInteraction.getChannel().get()).join();
                        message.addReaction("❌");
                        BotSendMessageRecord record = new BotSendMessageRecord();
                        record.setMESSAGEID(message.getIdAsString());
                        record.setCHANNELID(buttonInteraction.getChannel().get().getIdAsString());
                        record.setUSERID(buttonInteraction.getUser().getIdAsString());
                        record.setSERVERID(buttonInteraction.getServer().get().getIdAsString());
                        dao.addBotSendMessage(record);
                        buttonInteraction.createImmediateResponder().respond();
                    }
                } catch (DatabaseException | SystemException ignored) { }
            }
        });
        api.addServerJoinListener(e -> {
            Server server = e.getServer();
            try {
                if (server.getSystemChannel().isPresent()) {
                    server.getSystemChannel().get().sendMessage("/setup を打つと\nチャンネルとカテゴリを作成されます");
                    server.getSystemChannel().get().sendMessage("困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttps://discord.gg/6Z7jabh983");
                } else {
                    server.getOwner().get().sendMessage("/setup を打つと\nチャンネルとカテゴリを作成されます");
                    server.getOwner().get().sendMessage("困ったことがありましたら、下記リンクからサポートサーバーに入り、お聞きください。\nhttps://discord.gg/6Z7jabh983").join();
                }
                dao.TempNewServer(server.getIdAsString());
            } catch (DatabaseException ignored) { }
            api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
        });
        api.addServerLeaveListener(e -> {
            dao.serverLeaveAllDataDelete(e.getServer().getIdAsString());
            api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
        });
        System.out.println("URL : " + api.createBotInvite(new PermissionsBuilder().setAllowed(PermissionType.ADMINISTRATOR).build()).replaceAll("scope=bot", "scope=bot+applications.commands"));
    }
}
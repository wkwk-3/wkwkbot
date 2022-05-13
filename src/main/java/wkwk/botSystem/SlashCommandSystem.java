package wkwk.botSystem;

import com.vdurmont.emoji.EmojiManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import wkwk.Command.Processing;
import wkwk.Command.Show;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.record.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class SlashCommandSystem {
    DiscordApi api;

    public SlashCommandSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {
        User wkwk = api.getYourself();

        Processing processing = new Processing(api);
        DiscordDAO dao = new DiscordDAO();
        Show show = new Show();

        api.addSlashCommandCreateListener(event -> {
            try {
                SlashCommandInteraction interaction = event.getSlashCommandInteraction();
                StringBuilder responseString = new StringBuilder("インタラクションに失敗");
                MessageBuilder responseMessage = null;
                TextChannel channel;
                String cmd = interaction.getCommandName();
                Server server = null;
                if (interaction.getServer().isPresent()) {
                    server = interaction.getServer().get();
                }
                if (server != null) {
                    if (interaction.getChannel().isPresent()) {
                        User sendUser = interaction.getUser();
                        String serverId = server.getIdAsString();
                        boolean isAdmin = server.getAllowedPermissions(sendUser).contains(PermissionType.ADMINISTRATOR);
                        Role everyone = server.getEveryoneRole();
                        channel = interaction.getChannel().get();
                        ChannelRecord list = dao.TempGetChannelList(channel.getIdAsString(), "t");
                        cmd = interaction.getCommandName();
                        if (dao.getNoSlashCommandServer(serverId)) {
                            dao.BotSetDate("p", serverId, "NULL");
                        }
                        if (isAdmin) {
                            switch (cmd) {
                                case "setup":
                                    ServerDataRecord old;
                                    old = dao.TempGetData(serverId);
                                    ServerTextChannel mentionChannel = new ServerTextChannelBuilder(server).setName("Mention").setRawPosition(1).create().join();
                                    mentionChannel.createUpdater().addPermissionOverwrite(wkwk, new PermissionsBuilder().setAllowed(PermissionType.MENTION_EVERYONE).build()).update().join();
                                    ServerDataRecord data = new ServerDataRecord();
                                    data.setServer(serverId);
                                    data.setFstChannel(new ServerVoiceChannelBuilder(server).setName("NewTemp").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(everyone, new PermissionsBuilder().setAllowed(PermissionType.SEND_MESSAGES).build()).addPermissionOverwrite(wkwk, new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS).build()).create().join().getIdAsString());
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
                                                        } else
                                                            responseString = new StringBuilder("このサーバーのテキストチャンネルを入力してください");
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
                                                int roleSize = dao.getReactAllData(serverId).getEmoji().size();
                                                if (roleSize < 25 && interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionRoleValueByName("role").isPresent() && interaction.getOptionByIndex(0).get().getOptionStringValueByName("emoji").isPresent()) {
                                                    String emoji = interaction.getOptionByIndex(0).get().getOptionStringValueByName("emoji").get().replaceFirst("️", "");
                                                    String roleId = interaction.getOptionByIndex(0).get().getOptionRoleValueByName("role").get().getIdAsString();
                                                    ReactionRoleRecord record = dao.getReactMessageData(serverId);
                                                    if (record.getServerID() != null && api.getRoleById(roleId).isPresent() && EmojiManager.isEmoji(emoji) && record.getServerID().equals(serverId) && api.getServerTextChannelById(record.getTextChannelID()).isPresent()) {
                                                        dao.setReactRoleData(serverId, record.getMessageID(), roleId, emoji);
                                                        api.getMessageById(record.getMessageID(), api.getServerTextChannelById(record.getTextChannelID()).get()).join().addReaction(emoji).join();
                                                        str.append("リアクションロール設定完了");
                                                    } else if (record.getServerID() == null)
                                                        str.append("リアクションロールの対象メッセージを設定してください\n");
                                                    if (!EmojiManager.isEmoji(emoji))
                                                        str.append(emoji).append("は絵文字ではない\n");
                                                    responseString = new StringBuilder(str.toString());
                                                } else if (roleSize == 25) {
                                                    responseString = new StringBuilder("リアクションロールが25件あります");
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
                                                    int nameSize = dao.GetNamePreset(list.getServerID()).size();
                                                    if (name.length() <= 100 && nameSize < 25) {
                                                        dao.addNamePreset(serverId, name);
                                                        responseString = new StringBuilder("名前変更候補を追加しました");
                                                    } else if (name.length() > 100) {
                                                        responseString = new StringBuilder("100文字以内にしてください。");
                                                    } else if (nameSize == 25){
                                                        responseString = new StringBuilder("登録されているプリセットが25件あります");
                                                    }
                                                }
                                                break;

                                            case "logging":
                                                int logSize = dao.getLogging("s", interaction.getServer().get().getIdAsString()).size();
                                                if (logSize < 25 && interaction.getOptionByIndex(0).isPresent() && interaction.getOptionByIndex(0).get().getOptionByIndex(0).isPresent()) {
                                                    String subCommand = interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getName();
                                                    ArrayList<LoggingRecord> records = new ArrayList<>();
                                                    LoggingRecord logging = new LoggingRecord();
                                                    logging.setServerId(serverId);
                                                    logging.setChannelId(channel.getIdAsString());
                                                    logging.setLogType(subCommand);
                                                    ArrayList<String> targets;
                                                    if (subCommand.equals("user")) {
                                                        logging.setTargetChannelId(channel.getIdAsString());
                                                        records.add(logging);
                                                        dao.addLogging(records);
                                                        responseString = new StringBuilder("ユーザー加入・脱退履歴を設定");

                                                    } else if (subCommand.equals("chat") && interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").isPresent()) {
                                                        targets = new ArrayList<>();
                                                        targets.add(interaction.getOptionByIndex(0).get().getOptionByIndex(0).get().getOptionChannelValueByName("textChannel").get().getIdAsString());
                                                        targets.forEach(id -> {
                                                            logging.setTargetChannelId(id);
                                                            records.add(logging);
                                                        });
                                                        dao.addLogging(records);
                                                        responseString = new StringBuilder("チャット履歴を設定");
                                                    }
                                                } else if (logSize == 25) {
                                                    responseString = new StringBuilder("Log設定がすでに25件あります");
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
                                                    SelectMenuBuilder roleMenuBuilder = new SelectMenuBuilder().setCustomId("removeRole").setPlaceholder("削除したいリアクション").setMaximumValues(record.getEmoji().size()).setMinimumValues(1);
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
                                                    SelectMenuBuilder nameMenuBuilder = new SelectMenuBuilder().setCustomId("removeName").setPlaceholder("削除したい名前").setMaximumValues(namePreset.size()).setMinimumValues(1);
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
                                                    SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("removeLogging").setPlaceholder("削除したいlog設定を選んでください").setMaximumValues(logRecord.size()).setMinimumValues(1);
                                                    for (LoggingRecord log : logRecord) {
                                                        if (api.getServerTextChannelById(log.getTargetChannelId()).isPresent() && log.getLogType().equals("chat")) {
                                                            selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(log.getLogType() + ":" + api.getServerTextChannelById(log.getTargetChannelId()).get().getName()).setValue(log.getTargetChannelId() + " " + log.getLogType() + " " + log.getChannelId()).build());
                                                        } else if (api.getServerTextChannelById(log.getTargetChannelId()).isPresent() && log.getLogType().equals("user")) {
                                                            selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(log.getLogType() + ":" + api.getServerTextChannelById(log.getTargetChannelId()).get().getName()).setValue(log.getTargetChannelId() + " " + log.getLogType() + " " + log.getChannelId()).build());
                                                        } else if (!api.getServerTextChannelById(log.getTargetChannelId()).isPresent()) {
                                                            selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(log.getLogType() + ":" + log.getTargetChannelId()).setValue(log.getChannelId() + " " + log.getLogType() + " " + log.getChannelId()).build());
                                                        }
                                                    }
                                                    responseMessage = new MessageBuilder()
                                                            .setContent("履歴チャンネル削除")
                                                            .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                                    responseString = new StringBuilder("選択形式を送信");
                                                } else {
                                                    responseString = new StringBuilder("候補がありません");
                                                }
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
                                    sendUser.sendMessage(show.create(server.getName(), serverId, sendUser, api));
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
                        if (list.getVoiceID() != null) {
                            boolean isManage = sendUser.getConnectedVoiceChannel(server).get().getOverwrittenPermissions(sendUser).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS);
                            if (isManage) {
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
                                    ServerDataRecord serverList = dao.TempGetData(list.getServerID());
                                    if (api.getServerTextChannelById(serverList.getMentionChannel()).isPresent()) {
                                        ServerTextChannel mention = api.getServerTextChannelById(serverList.getMentionChannel()).get();
                                        String mentionMessage = serverList.getStereotyped();
                                        mentionMessage = processing.RecruitingTextRePress(mentionMessage, sendUser, list, mentionText.toString());
                                        Message message = new MessageBuilder().setContent(mentionMessage).send(mention).join();
                                        message.addReaction("❌");
                                        dao.addMentionMessage(list.getTextID(), message.getIdAsString(), list.getServerID());
                                        BotSendMessageRecord record = new BotSendMessageRecord();
                                        record.setMESSAGEID(message.getIdAsString());
                                        record.setCHANNELID(mention.getIdAsString());
                                        record.setUSERID(sendUser.getIdAsString());
                                        record.setSERVERID(list.getServerID());
                                        dao.addBotSendMessage(record);
                                        responseString = new StringBuilder("募集メッセを送信しました");
                                    }
                                } else if (cmd.equals("add")) {
                                    String subCommand = interaction.getOptionByIndex(0).get().getName();
                                    if ("user".equals(subCommand)) {
                                        if (interaction.getOptionByIndex(0).get().getOptionUserValueByName("selectUser").isPresent()) {
                                            User user = interaction.getOptionByIndex(0).get().getOptionUserValueByName("selectUser").get();
                                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                                ServerVoiceChannel voiceChannel = api.getServerVoiceChannelById(list.getVoiceID()).get();
                                                ServerVoiceChannelUpdater updater = voiceChannel.createUpdater();
                                                updater.addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.CONNECT, PermissionType.READ_MESSAGES).build());
                                                updater.update();
                                                responseString = new StringBuilder(user.getName() + "を追加しました。");
                                            }
                                        } else {
                                            responseString = new StringBuilder("ユーザーを選択してください");
                                        }
                                    }
                                } else if (cmd.equals("delete")) {
                                    String subCommand = interaction.getOptionByIndex(0).get().getName();
                                    if ("user".equals(subCommand)) {
                                        if (interaction.getOptionByIndex(0).get().getOptionUserValueByName("selectUser").isPresent()) {
                                            User user = interaction.getOptionByIndex(0).get().getOptionUserValueByName("selectUser").get();
                                            if (api.getServerVoiceChannelById(list.getVoiceID()).isPresent()) {
                                                ServerVoiceChannel voiceChannel = api.getServerVoiceChannelById(list.getVoiceID()).get();
                                                ServerVoiceChannelUpdater updater = voiceChannel.createUpdater();
                                                updater.addPermissionOverwrite(user, new PermissionsBuilder().setDenied(PermissionType.CONNECT, PermissionType.READ_MESSAGES).build());
                                                updater.update();
                                                responseString = new StringBuilder(user.getName() + "を削除しました。");
                                            }
                                        } else {
                                            responseString = new StringBuilder("ユーザーを選択してください");
                                        }
                                    }
                                }
                            } else {
                                if (cmd.equals("claim")) {
                                    boolean claimSw = api.getServerVoiceChannelById(list.getVoiceID()).get().getOverwrittenUserPermissions().entrySet().stream().filter(entry -> entry.getValue().getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)).findFirst().map(entry -> api.getServerVoiceChannelById(list.getVoiceID()).get().getConnectedUserIds().stream().noneMatch(connectId -> Objects.equals(connectId, entry.getKey()))).orElse(true);
                                    if (claimSw) {
                                        api.getServerVoiceChannelById(list.getVoiceID()).get().createUpdater().addPermissionOverwrite(sendUser, processing.getAdminPermission().build()).update();
                                        if (api.getServerTextChannelById(list.getTextID()).isPresent()) {
                                            api.getServerTextChannelById(list.getTextID()).get().createUpdater().addPermissionOverwrite(sendUser, processing.getAdminPermission().build()).update();
                                        }
                                        responseString = new StringBuilder(sendUser.getName() + "が新しく通話管理者になりました");
                                    } else responseString = new StringBuilder("通話管理者が通話にいらっしゃいます");
                                }
                            }
                        }
                        if (responseMessage != null) {
                            Message message = responseMessage.send(channel).join();
                            message.addReaction("❌");
                            BotSendMessageRecord record = new BotSendMessageRecord();
                            record.setMESSAGEID(message.getIdAsString());
                            record.setCHANNELID(channel.getIdAsString());
                            record.setUSERID(interaction.getUser().getIdAsString());
                            record.setSERVERID(serverId);
                            dao.addBotSendMessage(record);
                        }
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
                    case "invite":
                        responseString = new StringBuilder("https://wkb.page.link/bot");
                        break;
                    case "guild":
                        responseString = new StringBuilder("https://wkb.page.link/guild");
                        break;
                    case "help":
                        new HelpSystem(api).sendHelp(event);
                        responseString = new StringBuilder("送信しました");
                        break;
                }
                interaction.createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(responseString.toString()).respond();
            } catch (SystemException | DatabaseException ex) {
                ex.printStackTrace();
            }
        });
    }
}

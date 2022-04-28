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
                            dao.deleteMessage("m",messageId);
                        } else if(!messageRecord.getMESSAGEID().equalsIgnoreCase("NULL") && messageRecord.getMESSAGEID().equalsIgnoreCase(messageId) && !messageRecord.getUSERID().equalsIgnoreCase(e.getUser().get().getIdAsString())) {
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
package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag;
import wkwk.Command.Processing;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.record.BotSendMessageRecord;
import wkwk.record.ChannelRecord;
import wkwk.record.ServerDataRecord;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TempChannelSystem {
    DiscordApi api;

    public TempChannelSystem(DiscordApi api) {
        this.api = api;
    }

    public void run() {

        DiscordDAO dao = new DiscordDAO();
        Processing processing = new Processing();

        api.addServerVoiceChannelMemberJoinListener(event -> {
            User joinUser = event.getUser();
            if (!joinUser.isBot()) {
                ChannelCategory joinChannelCategory = null;
                Server server = event.getServer();
                String serverId = server.getIdAsString();
                String joinVoiceId = event.getChannel().getIdAsString();
                if (event.getChannel().getCategory().isPresent()) joinChannelCategory = event.getChannel().getCategory().get();
                try {
                    ServerDataRecord data = dao.TempGetData(serverId);
                    String firstChannel = data.getFstChannel();
                    String vcatId = data.getVoiceCategory();
                    String tcatId = data.getTextCategory();
                    if (joinVoiceId.equals(firstChannel) && data.getTempBy()) {
                        if (server.getChannelCategoryById(tcatId).isPresent() && server.getChannelCategoryById(vcatId).isPresent()) {
                            ChannelCategory tcat = server.getChannelCategoryById(tcatId).get();
                            ChannelCategory vcat = server.getChannelCategoryById(vcatId).get();
                            ChannelRecord list = new ChannelRecord();
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
                        ChannelRecord list = dao.TempGetChannelList(joinVoiceId, "v");
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
        api.addServerVoiceChannelMemberLeaveListener(event -> {
            ServerDataRecord data;
            User user = event.getUser();
            ChannelCategory leaveChannelCategory = null;
            String serverId = event.getServer().getIdAsString();
            if (event.getChannel().getCategory().isPresent()) leaveChannelCategory = event.getChannel().getCategory().get();
            try {
                data = dao.TempGetData(serverId);
                String voiceCategory = data.getVoiceCategory();
                if (api.getChannelCategoryById(voiceCategory).isPresent())
                    for (RegularServerChannel voiceList : api.getChannelCategoryById(voiceCategory).get().getChannels()) {
                        if (voiceList.asServerVoiceChannel().isPresent()) {
                            ServerVoiceChannel voiceChannel = voiceList.asServerVoiceChannel().get();
                            if (voiceChannel.getConnectedUserIds().size() == 0) {
                                ChannelRecord list = dao.TempGetChannelList(voiceChannel.getIdAsString(), "v");
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
                    String leaveVoiceChannel = event.getChannel().getIdAsString();
                    ChannelRecord list = dao.TempGetChannelList(leaveVoiceChannel, "v");
                    if (event.getChannel().getConnectedUserIds().size() > 0) {
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
        api.addButtonClickListener(event -> {
            MessageBuilder messageBuilder = null;
            ButtonInteraction buttonInteraction = event.getButtonInteraction();
            String response = "";
            String id = buttonInteraction.getCustomId();
            if (buttonInteraction.getChannel().isPresent()) {
                String textChannelId = buttonInteraction.getChannel().get().getIdAsString();
                try {
                    ChannelRecord list = dao.TempGetChannelList(textChannelId, "t");
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
                                ServerDataRecord serverList = dao.TempGetData(serverId);
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
                    event.getInteraction().createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(response).respond();
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
                } catch (DatabaseException | SystemException ignored) {
                }
            }
        });
        api.addSelectMenuChooseListener(event -> {
            SelectMenuInteraction menuInteraction = event.getSelectMenuInteraction();
            String cmd = menuInteraction.getCustomId();
            String response = null;
            try {
                if (menuInteraction.getChannel().isPresent()) {
                    ChannelRecord list = dao.TempGetChannelList(menuInteraction.getChannel().get().getIdAsString(), "t");
                    if (list.getServerID() != null) {
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
                        if (response != null) {
                            menuInteraction.getMessage().delete();
                            menuInteraction.createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(response).respond();
                        }
                    }
                }
            } catch (DatabaseException ex) {
                ex.printStackTrace();
            }
        });
    }
}

package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.message.component.SelectMenuBuilder;
import org.javacord.api.entity.message.component.SelectMenuOptionBuilder;
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
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;
import wkwk.parameter.record.ChannelRecord;
import wkwk.parameter.record.ServerDataRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TempChannelSystem extends BotLogin {
    DiscordApi api = getApi();

    public TempChannelSystem() {
        DiscordDAO dao = new DiscordDAO();
        Processing processing = new Processing();

        api.addServerVoiceChannelMemberJoinListener(event -> {
            User joinUser = event.getUser();
            if (!joinUser.isBot()) {
                ChannelCategory joinChannelCategory = null;
                Server server = event.getServer();
                String serverId = server.getIdAsString();
                String joinVoiceId = event.getChannel().getIdAsString();
                if (event.getChannel().getCategory().isPresent())
                    joinChannelCategory = event.getChannel().getCategory().get();
                try {
                    ServerDataRecord data = dao.TempGetData(serverId);
                    String firstChannel = data.getFstChannelId();
                    String vcatId = data.getVoiceCategoryId();
                    String tcatId = data.getTextCategoryId();
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
                                list.setTextId(text.getIdAsString());
                                new MessageBuilder().setContent("通話名前変更 : `/name `\n通話最大人数変更 : `/size `\n募集メッセージ送信 : `/men `").addComponents(
                                        ActionRow.of(Button.success("name", "通話名前変更"),
                                                Button.success("size", "通話人数変更"),
                                                Button.success("send-recruiting", "募集送信"),
                                                Button.success("claim", "通話権限獲得"),
                                                Button.danger("next", "次の項目"))).send(text);
                            } else list.setTextId("NULL");
                            ServerVoiceChannel voice = new ServerVoiceChannelBuilder(server).setName(defaultName).setCategory(vcat).setUserlimit(Integer.parseInt(data.getDefaultSize())).setBitrate(64000).create().get();
                            list.setVoiceId(voice.getIdAsString());
                            list.setServerId(serverId);
                            dao.TempSetChannelList(list);
                            joinUser.move(voice);
                        }
                    } else if (joinChannelCategory != null && joinChannelCategory.getIdAsString().equals(vcatId)) {
                        ChannelRecord list = dao.TempGetChannelList(joinVoiceId, "v");
                        if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                            api.getServerVoiceChannelById(list.getVoiceId()).get().createUpdater().addPermissionOverwrite(joinUser, api.getServerVoiceChannelById(list.getVoiceId()).get().getConnectedUserIds().size() == 1 ? processing.getAdminPermission().build() : processing.getUserPermission().build()).update();
                        }
                        if (api.getServerTextChannelById(list.getTextId()).isPresent()) {
                            api.getServerTextChannelById(list.getTextId()).get().createUpdater().addPermissionOverwrite(joinUser, api.getServerVoiceChannelById(list.getVoiceId()).get().getConnectedUserIds().size() == 1 ? processing.getAdminPermission().build() : processing.getUserPermission().build()).update();
                        }
                    }
                } catch (SystemException | DatabaseException | ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            processing.upDataBotActivity();
        });
        api.addServerVoiceChannelMemberLeaveListener(event -> {
            ServerDataRecord data;
            User user = event.getUser();
            ChannelCategory leaveChannelCategory = null;
            String serverId = event.getServer().getIdAsString();
            if (event.getChannel().getCategory().isPresent())
                leaveChannelCategory = event.getChannel().getCategory().get();
            try {
                data = dao.TempGetData(serverId);
                String voiceCategory = data.getVoiceCategoryId();
                if (api.getChannelCategoryById(voiceCategory).isPresent())
                    for (RegularServerChannel voiceList : api.getChannelCategoryById(voiceCategory).get().getChannels()) {
                        if (voiceList.asServerVoiceChannel().isPresent()) {
                            ServerVoiceChannel voiceChannel = voiceList.asServerVoiceChannel().get();
                            if (voiceChannel.getConnectedUserIds().size() == 0) {
                                ChannelRecord list = dao.TempGetChannelList(voiceChannel.getIdAsString(), "v");
                                if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                                    dao.TempDeleteChannelList(api.getServerVoiceChannelById(list.getVoiceId()).get().getIdAsString(), "v");
                                    api.getServerVoiceChannelById(list.getVoiceId()).get().delete();
                                }
                                if (api.getServerTextChannelById(list.getTextId()).isPresent()) {
                                    dao.TempDeleteChannelList(api.getServerTextChannelById(list.getTextId()).get().getIdAsString(), "t");
                                    api.getServerTextChannelById(list.getTextId()).get().delete();
                                }
                                if (api.getTextChannelById(dao.getMentionChannel(list.getServerId())).isPresent()) {
                                    for (String message : dao.getMentionMessage(list.getTextId()).getMessages()) {
                                        api.getMessageById(message, api.getTextChannelById(dao.getMentionChannel(list.getServerId())).get()).join().delete();
                                    }
                                    dao.deleteMentions(list.getTextId());
                                }
                            }
                        }
                    }
                if (leaveChannelCategory != null && leaveChannelCategory.getIdAsString().equals(voiceCategory)) {
                    String leaveVoiceChannel = event.getChannel().getIdAsString();
                    ChannelRecord list = dao.TempGetChannelList(leaveVoiceChannel, "v");
                    if (event.getChannel().getConnectedUserIds().size() > 0) {
                        if (api.getServerTextChannelById(list.getTextId()).isPresent()) {
                            ServerTextChannel tx = api.getServerTextChannelById(list.getTextId()).get();
                            tx.createUpdater().removePermissionOverwrite(user).update();
                        }
                        if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                            ServerVoiceChannel vx = api.getServerVoiceChannelById(list.getVoiceId()).get();
                            vx.createUpdater().removePermissionOverwrite(user).update();
                        }
                    }
                }
            } catch (SystemException | DatabaseException ignored) {
            }
            processing.upDataBotActivity();
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
                    String requestVoiceId = list.getVoiceId();
                    if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                            requestVoiceId.equals(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                            api.getServerVoiceChannelById(requestVoiceId).isPresent() && api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()) != null &&
                            api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                        if (buttonInteraction.getServer().isPresent())
                            if (id.equals("hide")) {
                                if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                                    PermissionsBuilder permissions = new PermissionsBuilder();
                                    boolean isLock = dao.GetChannelLock(textChannelId);
                                    boolean isHide = dao.GetChannelHide(textChannelId);
                                    if (isHide) {
                                        isHide = false;
                                        permissions.setUnset(PermissionType.READ_MESSAGES);
                                        response += "通話非表示解除完了";
                                    } else {
                                        isHide = true;
                                        permissions.setDenied(PermissionType.READ_MESSAGES);
                                        response += "通話非表示完了";
                                    }
                                    if (isLock) permissions.setDenied(PermissionType.CONNECT);
                                    else permissions.setUnset(PermissionType.CONNECT);
                                    ArrayList<Role> targetRole = new ArrayList<>();
                                    for (Map.Entry<Long, Permissions> permissionMap : api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenRolePermissions().entrySet()) {
                                        if (isHide) {
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
                                    ServerVoiceChannelUpdater updater = api.getServerVoiceChannelById(list.getVoiceId()).get().createUpdater();
                                    targetRole.add(buttonInteraction.getServer().get().getEveryoneRole());
                                    for (Role target : targetRole) {
                                        updater.addPermissionOverwrite(target, permissions.build());
                                    }
                                    updater.update();
                                    dao.UpdateChannelHide(textChannelId, isHide);
                                }
                            } else if (id.equals("lock")) {
                                if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                                    PermissionsBuilder permissions = new PermissionsBuilder();
                                    boolean isLock = dao.GetChannelLock(textChannelId);
                                    boolean isHide = dao.GetChannelHide(textChannelId);
                                    if (isLock) {
                                        isLock = false;
                                        permissions.setUnset(PermissionType.CONNECT);
                                        response += "通話ロック解除完了";
                                    } else {
                                        isLock = true;
                                        permissions.setDenied(PermissionType.CONNECT);
                                        response += "通話ロック完了";
                                    }
                                    if (isHide) permissions.setDenied(PermissionType.READ_MESSAGES);
                                    else permissions.setUnset(PermissionType.READ_MESSAGES);
                                    ArrayList<Role> targetRole = new ArrayList<>();
                                    for (Map.Entry<Long, Permissions> permissionMap : api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenRolePermissions().entrySet()) {
                                        if (isLock) {
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
                                    ServerVoiceChannelUpdater updater = api.getServerVoiceChannelById(list.getVoiceId()).get().createUpdater();
                                    targetRole.add(buttonInteraction.getServer().get().getEveryoneRole());
                                    for (Role target : targetRole) {
                                        updater.addPermissionOverwrite(target, permissions.build());
                                    }
                                    updater.update();
                                    dao.UpdateChannelLock(textChannelId, isLock);
                                }
                            } else if (id.equals("transfer")) {
                                if (api.getServerVoiceChannelById(list.getVoiceId()).get().getConnectedUserIds().size() > 1 && api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                    SelectMenuBuilder selectMenuBuilder = new SelectMenuBuilder().setCustomId("transSelect").setPlaceholder("移譲ユーザーを選択してください").setMaximumValues(1).setMinimumValues(1);
                                    Collection<User> users = api.getServerVoiceChannelById(list.getVoiceId()).get().getConnectedUsers();
                                    users.remove(buttonInteraction.getUser());
                                    for (User user : users) {
                                        selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(user.getName()).setValue(user.getIdAsString()).build());
                                    }
                                    messageBuilder = new MessageBuilder()
                                            .setContent("通話管理権限移譲")
                                            .addComponents(ActionRow.of(selectMenuBuilder.build()));
                                } else if (api.getServerVoiceChannelById(list.getVoiceId()).get().getConnectedUserIds().size() <= 1) {
                                    response = "通話内に一人しか居ません。";
                                } else if (!api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                    response = "あなたは管理権限を持っていません。";
                                }
                            } else if (id.equals("name")) {
                                ArrayList<String> namePreset = dao.GetNamePreset(list.getServerId());
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
                                selectMenuBuilder.addOption(new SelectMenuOptionBuilder().setLabel(Integer.toString(0)).setValue(Integer.toString(0)).build());
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
                                if (api.getServerTextChannelById(serverList.getMentionChannelId()).isPresent()) {
                                    ServerTextChannel mention = api.getServerTextChannelById(serverList.getMentionChannelId()).get();
                                    String mentionMessage = serverList.getStereoTyped();
                                    mentionMessage = processing.RecruitingTextRePress(mentionMessage, sendUser, list, "");
                                    Message message = new MessageBuilder().setContent(mentionMessage).send(mention).join();
                                    processing.botSendMessage(message, buttonInteraction);
                                    dao.addMentionMessage(list.getTextId(), message.getIdAsString(), serverId);
                                }
                            } else if (id.equals("remove-recruiting")) {
                                if (api.getTextChannelById(dao.getMentionChannel(list.getServerId())).isPresent()) {
                                    for (String message : dao.getMentionMessage(list.getTextId()).getMessages()) {
                                        api.getMessageById(message, api.getTextChannelById(dao.getMentionChannel(list.getServerId())).get()).join().delete();
                                    }
                                    dao.deleteMentions(list.getTextId());
                                }
                            }
                    } else if (api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()) == null ||
                            !api.getServerVoiceChannelById(list.getVoiceId()).get().getOverwrittenUserPermissions().get(buttonInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                        response += "あなたは通話管理者ではありません";
                    }
                    if (buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).isPresent() &&
                            requestVoiceId.equals(buttonInteraction.getUser().getConnectedVoiceChannel(buttonInteraction.getServer().get()).get().getIdAsString()) &&
                            api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                        if (id.equals("claim")) {
                            response = "通話管理者が通話にいらっしゃいます";
                            if (api.getServerVoiceChannelById(requestVoiceId).get().getOverwrittenUserPermissions().entrySet().stream().filter(entry -> entry.getValue().getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)).findFirst().map(entry -> api.getServerVoiceChannelById(requestVoiceId).get().getConnectedUserIds().stream().noneMatch(connectId -> Objects.equals(connectId, entry.getKey()))).orElse(true)) {
                                api.getServerVoiceChannelById(requestVoiceId).get().createUpdater().addPermissionOverwrite(buttonInteraction.getUser(), new PermissionsBuilder().setAllowed(PermissionType.MANAGE_CHANNELS).build()).update();
                                if (api.getServerTextChannelById(list.getTextId()).isPresent()) {
                                    api.getServerTextChannelById(list.getTextId()).get().createUpdater()
                                            .addPermissionOverwrite(buttonInteraction.getUser(), processing.getAdminPermission().build())
                                            .update();
                                }
                                response = buttonInteraction.getUser().getName() + "が新しく通話管理者になりました";
                            }
                        }
                    }
                    event.getInteraction().createImmediateResponder().setFlags(InteractionCallbackDataFlag.EPHEMERAL).setContent(response).respond();
                    if (messageBuilder != null) {
                        Message message = messageBuilder.send(buttonInteraction.getChannel().get()).join();
                        processing.botSendMessage(message, buttonInteraction);
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
                    if (list.getServerId() != null) {
                        boolean isManage = api.getServerTextChannelById(list.getTextId()).get().getOverwrittenUserPermissions().get(menuInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS);
                        String requestVoiceId = list.getVoiceId();
                        if (isManage) {
                            switch (cmd) {
                                case "transSelect":
                                    User oldManege = menuInteraction.getUser();
                                    if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                                        User selectUser = api.getUserById(menuInteraction.getChosenOptions().get(0).getValue()).join();
                                        api.getServerVoiceChannelById(list.getVoiceId()).get().createUpdater()
                                                .addPermissionOverwrite(selectUser, processing.getAdminPermission().build())
                                                .removePermissionOverwrite(oldManege)
                                                .addPermissionOverwrite(oldManege, processing.getUserPermission().build())
                                                .update();
                                        if (api.getServerTextChannelById(list.getTextId()).isPresent()) {
                                            api.getServerTextChannelById(list.getTextId()).get().createUpdater()
                                                    .addPermissionOverwrite(selectUser, processing.getAdminPermission().build())
                                                    .removePermissionOverwrite(oldManege)
                                                    .addPermissionOverwrite(oldManege, processing.getUserPermission().build())
                                                    .update();
                                        }
                                        response = selectUser.getName() + "が新しく通話管理者になりました";
                                    }
                                    break;
                                case "name":
                                    if (api.getServerVoiceChannelById(requestVoiceId).isPresent()) {
                                        if (api.getServerTextChannelById(list.getTextId()).isPresent() && api.getServerTextChannelById(list.getTextId()).get().getOverwrittenUserPermissions().get(menuInteraction.getUser().getId()).getAllowedPermission().contains(PermissionType.MANAGE_CHANNELS)) {
                                            String name = menuInteraction.getChosenOptions().get(0).getValue();
                                            if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                                                api.getServerVoiceChannelById(list.getVoiceId()).get().createUpdater().setName(name).update();
                                            }
                                            if (api.getServerTextChannelById(list.getTextId()).isPresent()) {
                                                api.getServerTextChannelById(list.getTextId()).get().createUpdater().setName(name).update();
                                            }
                                            response = "チャンネル名を" + name + "に変更しました";
                                        }
                                    }
                                    break;
                                case "size":
                                    int size = Integer.parseInt(menuInteraction.getChosenOptions().get(0).getValue());
                                    if (api.getServerVoiceChannelById(list.getVoiceId()).isPresent()) {
                                        api.getServerVoiceChannelById(list.getVoiceId()).get().createUpdater().setUserLimit(size).update();
                                        response = "通話人数を" + size + "に変更しました";
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

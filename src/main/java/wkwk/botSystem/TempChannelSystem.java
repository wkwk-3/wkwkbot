package wkwk.botSystem;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.Button;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import wkwk.ChannelList;
import wkwk.ServerDataList;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;

import java.util.concurrent.ExecutionException;

public class TempChannelSystem {
    DiscordApi api;
    public TempChannelSystem(DiscordApi api) {
        this.api = api;
    }
    public void run() {
        DiscordDAO dao = new DiscordDAO();
        api.addServerVoiceChannelMemberJoinListener(e -> {
            User joinUser = e.getUser();
            if (!joinUser.isBot()) {
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
    }
}

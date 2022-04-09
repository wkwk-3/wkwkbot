package wkwk.Command;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelCategoryBuilder;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import wkwk.ServerDataList;
import wkwk.dao.DiscordDAO;
import wkwk.exception.DatabaseException;
import wkwk.exception.SystemException;

public class SetUp {

    public String process(DiscordApi api,DiscordDAO dao,String serverId, Server server, Role everyone) throws DatabaseException {
        dao.TempNewServer(serverId);
        ServerDataList data = new ServerDataList();
        data.setServer(serverId);
        data.setFstChannel(new ServerVoiceChannelBuilder(server).setName("NewTEMP").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(everyone, new PermissionsBuilder().setDenied(PermissionType.SEND_MESSAGES).build()).addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS).build()).create().join().getIdAsString());
        ServerTextChannel mentionChannel = new ServerTextChannelBuilder(server).setName("Mention").setRawPosition(1).create().join();
        mentionChannel.createUpdater().addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MENTION_EVERYONE).build()).update().join();
        data.setMentionChannel(mentionChannel.getIdAsString());
        data.setVoiceCategory(new ChannelCategoryBuilder(server).setName("Voice").setRawPosition(0).create().join().getIdAsString());
        data.setTextCategory(new ChannelCategoryBuilder(server).setName("Text").setRawPosition(0).create().join().getIdAsString());
        dao.TempDataUpData(data);
        return "セットアップ完了";
    }

    public String oldProcess(DiscordApi api,DiscordDAO dao,String serverId, Server server, Role everyone) throws DatabaseException, SystemException {
        ServerDataList old = dao.TempGetData(serverId);
        ServerDataList data = new ServerDataList();
        data.setServer(serverId);
        data.setFstChannel(new ServerVoiceChannelBuilder(server).setName("NewTEMP").setRawPosition(0).setBitrate(64000).addPermissionOverwrite(everyone, new PermissionsBuilder().setDenied(PermissionType.SEND_MESSAGES).build()).addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS).build()).create().join().getIdAsString());
        ServerTextChannel mentionChannel = new ServerTextChannelBuilder(server).setName("Mention").setRawPosition(1).create().join();
        mentionChannel.createUpdater().addPermissionOverwrite(api.getYourself(), new PermissionsBuilder().setAllowed(PermissionType.MENTION_EVERYONE).build()).update().join();
        data.setMentionChannel(mentionChannel.getIdAsString());
        data.setVoiceCategory(new ChannelCategoryBuilder(server).setName("Voice").setRawPosition(0).create().join().getIdAsString());
        data.setTextCategory(new ChannelCategoryBuilder(server).setName("Text").setRawPosition(0).create().join().getIdAsString());
        dao.TempDataUpData(data);
        if (api.getChannelCategoryById(old.getVoiceCategory()).isPresent()) {
            api.getChannelCategoryById(old.getVoiceCategory()).get().delete();
        }
        if (api.getChannelCategoryById(old.getTextCategory()).isPresent()) {
            api.getChannelCategoryById(old.getTextCategory()).get().delete();
        }
        if (api.getServerVoiceChannelById(old.getFstChannel()).isPresent()) {
            api.getServerVoiceChannelById(old.getFstChannel()).get().delete();
        }
        if (api.getServerTextChannelById(old.getMentionChannel()).isPresent()) {
            api.getServerTextChannelById(old.getMentionChannel()).get().delete();
        }
        return "セットアップ完了";
    }
}

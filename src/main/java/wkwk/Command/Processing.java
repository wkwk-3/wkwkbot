package wkwk.Command;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.ButtonInteraction;
import wkwk.core.BotLogin;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.BotSendMessageRecord;
import wkwk.parameter.record.ChannelRecord;
import wkwk.parameter.record.ReactionRoleRecord;

import java.util.ArrayList;

public class Processing extends BotLogin {

    DiscordApi api = getApi();
    DiscordDAO dao = new DiscordDAO();

    public String RecruitingTextRePress(String raw, User sendUser, ChannelRecord list, String text) {
        return raw.replaceAll("&user&", sendUser.getMentionTag())
                .replaceAll("&text&", text.replaceAll(" ", "\n"))
                .replaceAll("/n", "\n")
                .replaceAll("&channel&", "<#" + list.getVoiceId() + "> ")
                .replaceAll("&everyone&", "@everyone ")
                .replaceAll("&here&", "@here ");
    }

    public PermissionsBuilder getAdminPermission() {
        return getUserPermission()
                .setAllowed(
                        PermissionType.MANAGE_CHANNELS,
                        PermissionType.MANAGE_MESSAGES
                );
    }

    public PermissionsBuilder getUserPermission() {
        return new PermissionsBuilder()
                .setAllowed(PermissionType.READ_MESSAGES,
                        PermissionType.READ_MESSAGE_HISTORY,
                        PermissionType.SEND_MESSAGES,
                        PermissionType.ADD_REACTIONS,
                        PermissionType.ATTACH_FILE,
                        PermissionType.USE_APPLICATION_COMMANDS,
                        PermissionType.USE_EXTERNAL_STICKERS,
                        PermissionType.USE_EXTERNAL_EMOJIS);
    }

    public Role getReactionRole(Emoji emoji, ReactionRoleRecord record) {
        ArrayList<String> emojis = record.getEmoji();
        ArrayList<String> roles = record.getRoleId();
        String targetRole = null;
        for (int i = 0; i < record.getEmoji().size(); i++) {
            if (emoji.asUnicodeEmoji().get().equals(emojis.get(i)) && api.getRoleById(roles.get(i)).isPresent()) {
                targetRole = roles.get(i);
                break;
            }
        }
        return targetRole != null && api.getRoleById(targetRole).isPresent() ? api.getRoleById(targetRole).get() : null;
    }


    public void upDataBotActivity() {
        api.updateActivity(ActivityType.PLAYING, dao.GetServerCount() + "servers | " + dao.GetVoiceCount() + "VC");
    }

    public void botSendMessage(Message message, ButtonInteraction buttonInteraction) {
        message.addReaction("❌");
        BotSendMessageRecord record = new BotSendMessageRecord();
        record.setMessageId(message.getIdAsString());
        record.setChannelId(buttonInteraction.getChannel().get().getIdAsString());
        record.setUserId(buttonInteraction.getUser().getIdAsString());
        record.setServetId(buttonInteraction.getServer().get().getIdAsString());
        dao.addBotSendMessage(record);
    }
    public void botSendMessage(Message message, Server server, Channel channel, User user) {
        message.addReaction("❌");
        BotSendMessageRecord record = new BotSendMessageRecord();
        record.setMessageId(message.getIdAsString());
        record.setChannelId(channel.getIdAsString());
        record.setUserId(user.getIdAsString());
        record.setServetId(server.getIdAsString());
        dao.addBotSendMessage(record);
    }

    public String getBotInviteUrl () {
        return "URL : https://wkb.page.link/bot";
    }
}

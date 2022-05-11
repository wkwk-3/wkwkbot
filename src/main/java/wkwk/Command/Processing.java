package wkwk.Command;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import wkwk.dao.DiscordDAO;
import wkwk.parameter.record.ChannelRecord;
import wkwk.parameter.record.ReactionRoleRecord;

import java.util.ArrayList;

public class Processing {

    DiscordApi api;
    DiscordDAO dao = new DiscordDAO();

    public Processing(DiscordApi api) {
        this.api = api;
    }

    public String RecruitingTextRePress(String raw, User sendUser, ChannelRecord list, String text) {
        return raw.replaceAll("&user&", sendUser.getMentionTag())
                .replaceAll("&text&", text.replaceAll(" ", "\n"))
                .replaceAll("/n", "\n")
                .replaceAll("&channel&", "<#" + list.getVoiceID() + "> ")
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
        ArrayList<String> roles = record.getRoleID();
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
}

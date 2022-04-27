package wkwk.Command;

import org.javacord.api.entity.user.User;
import wkwk.ChannelList;

public class Processing {
    public String RecruitingTextRePress(String raw, User sendUser, ChannelList list, String text) {
        return raw.replaceAll("&user&", sendUser.getMentionTag())
                .replaceAll("&text&", text.replaceAll(" ", "\n"))
                .replaceAll("/n", "\n")
                .replaceAll("&channel&", "<#" + list.getVoiceID() + "> ")
                .replaceAll("&everyone&", "@everyone ")
                .replaceAll("&here&", "@here ");
    }
}

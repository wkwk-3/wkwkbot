package wkwk;

import java.util.ArrayList;

public class ReactionRoleRecord {
    private final ArrayList<String> roleid = new ArrayList<>();
    private final ArrayList<String> emoji = new ArrayList<>();
    private String serverid;
    private String textchannelid;
    private String messageid;

    public String getServerid() {
        return serverid;
    }

    public void setServerid(String serverid) {
        this.serverid = serverid;
    }

    public String getTextchannelid() {
        return textchannelid;
    }

    public void setTextchannelid(String textchannelid) {
        this.textchannelid = textchannelid;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public ArrayList<String> getRoleid() {
        return roleid;
    }

    public void addRoleid(String roleid) {
        this.roleid.add(roleid);
    }

    public ArrayList<String> getEmoji() {
        return emoji;
    }

    public void addEmoji(String emoji) {
        this.emoji.add(emoji);
    }
}

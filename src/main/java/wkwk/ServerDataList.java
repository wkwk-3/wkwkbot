package wkwk;

public class ServerDataList {
    private String server;
    private String mentioncal;
    private String fstchannel;
    private String voicecate;
    private String textcate;
    private String prefix;

    public ServerDataList() {
        server = null;
        fstchannel = null;
        mentioncal = null;
        voicecate = null;
        textcate = null;
        prefix = null;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getFstchannel() {
        return fstchannel;
    }

    public void setFstchannel(String fstchannel) {
        this.fstchannel = fstchannel;
    }

    public String getVoicecate() {
        return voicecate;
    }

    public void setVoicecate(String voicecate) {
        this.voicecate = voicecate;
    }

    public String getTextcate() {
        return textcate;
    }

    public void setTextcate(String textcate) {
        this.textcate = textcate;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getMentioncal() {
        return mentioncal;
    }

    public void setMentioncal(String mentioncal) {
        this.mentioncal = mentioncal;
    }
}

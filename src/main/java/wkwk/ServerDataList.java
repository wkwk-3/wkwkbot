package wkwk;

import lombok.Data;

@Data
public class ServerDataList {

    private String server;
    private String mentioncal;
    private String fstchannel;
    private String voicecate;
    private String textcate;
    private String prefix;
    private boolean tempBy;
    private boolean textBy;
    private String stereotyped;
    private String defaultSize;
    private String defaultName;

    public ServerDataList() {
        server = null;
        fstchannel = null;
        mentioncal = null;
        voicecate = null;
        textcate = null;
        prefix = null;
        tempBy = false;
        textBy = false;
        stereotyped = null;
        defaultSize = null;
        defaultName = null;
    }

    public boolean getTempBy() {
        return tempBy;
    }
    public boolean getTextBy() {
        return textBy;
    }
}

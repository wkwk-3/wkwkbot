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
    private String tempBy;
    private String textBy;
    private String stereotyped;
    private String defaultSize;

    public ServerDataList() {
        server = null;
        fstchannel = null;
        mentioncal = null;
        voicecate = null;
        textcate = null;
        prefix = null;
        tempBy = null;
        textBy = null;
        stereotyped = null;
        defaultSize = null;
    }
}

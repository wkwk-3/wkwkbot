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

    public ServerDataList() {
        server = null;
        fstchannel = null;
        mentioncal = null;
        voicecate = null;
        textcate = null;
        prefix = null;
    }
}

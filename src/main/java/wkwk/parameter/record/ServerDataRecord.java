package wkwk.parameter.record;

import lombok.Data;

@Data
public class ServerDataRecord {

    private String server;
    private String mentionChannel;
    private String fstChannel;
    private String voiceCategory;
    private String textCategory;
    private String prefix;
    private boolean tempBy;
    private boolean textBy;
    private String stereotyped;
    private String defaultSize;
    private String defaultName;

    public ServerDataRecord() {
        server = null;
        fstChannel = null;
        mentionChannel = null;
        voiceCategory = null;
        textCategory = null;
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

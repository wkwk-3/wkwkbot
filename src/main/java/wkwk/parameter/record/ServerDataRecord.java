package wkwk.parameter.record;

import lombok.Data;

@Data
public class ServerDataRecord {

    private String serverId;
    private String mentionChannelId;
    private String fstChannelId;
    private String voiceCategoryId;
    private String textCategoryId;
    private boolean tempBy;
    private boolean textBy;
    private String stereoTyped;
    private String defaultSize;
    private String defaultName;

    public ServerDataRecord() {
        tempBy = false;
        textBy = false;
    }

    public boolean getTempBy() {
        return tempBy;
    }

    public boolean getTextBy() {
        return textBy;
    }
}

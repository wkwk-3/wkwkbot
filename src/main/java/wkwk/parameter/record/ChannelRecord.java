package wkwk.parameter.record;

import lombok.Data;

@Data
public class ChannelRecord {

    private String voiceId;
    private String textId;
    private String serverId;

    private boolean hideBy;
    private boolean lockBy;
}

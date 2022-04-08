package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReactRoleParameters {

    SERVER_ID(ReactMessageParameters.SERVER_ID.getParameter()),
    MESSAGE_ID(ReactMessageParameters.MESSAGE_ID.getParameter()),
    ROLE_ID("ROLEID"),
    EMOJI("EMOJI");

    private final String parameter;

}

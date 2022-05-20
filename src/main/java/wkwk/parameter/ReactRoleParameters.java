package wkwk.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReactRoleParameters {

    SERVER_ID(ReactMessageParameters.SERVER_ID.getParameter()),
    MESSAGE_ID(ReactMessageParameters.MESSAGE_ID.getParameter()),
    ROLE_ID("ROLE_ID"),
    EMOJI("EMOJI");

    private final String parameter;

}

package wkwk.paramater;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NamePresetParameters {

    SERVER_ID(ServerPropertyParameters.SERVER_ID.getParameter()),
    NAME("NAME");

    private final String parameter;

}

package com.melluh.mcmitm.protocol;

import com.melluh.mcmitm.util.Utils;

public enum ProtocolState {

    HANDSHAKING, STATUS, LOGIN, PLAY;

    public String getDisplayName() {
        return Utils.capitalize(this.name().toLowerCase());
    }

}

package com.melluh.mcmitm.auth;

import com.melluh.mcauth.MojangAuthenticator.GameProfile;
import com.melluh.mcauth.tokens.MicrosoftToken;
import com.melluh.mcauth.tokens.MojangToken;

public class Account {
        
    private MicrosoftToken microsoftToken;
    private MojangToken mojangToken;
    private GameProfile gameProfile;
        
    public Account(MicrosoftToken microsoftToken, MojangToken mojangToken, GameProfile gameProfile) {
        this.microsoftToken = microsoftToken;
        this.mojangToken = mojangToken;
        this.gameProfile = gameProfile;
    }

    public MicrosoftToken getMicrosoftToken() {
            return microsoftToken;
    }

    public MojangToken getMojangToken() {
            return mojangToken;
    }

    public GameProfile getGameProfile() {
            return gameProfile;
    }

}
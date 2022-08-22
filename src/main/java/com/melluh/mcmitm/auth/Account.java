package com.melluh.mcmitm.auth;

import com.melluh.mcauth.MojangAuthenticator.GameProfile;
import com.melluh.mcauth.tokens.MicrosoftToken;
import com.melluh.mcauth.tokens.MojangToken;
import com.melluh.mcauth.tokens.XboxToken;
import com.melluh.mcmitm.gui.MainGui;
import org.tinylog.Logger;

import java.util.concurrent.ExecutionException;

public class Account {
        
    private MicrosoftToken microsoftToken;
    private MojangToken mojangToken;
    private GameProfile gameProfile;
        
    public Account(MicrosoftToken microsoftToken, MojangToken mojangToken, GameProfile gameProfile) {
        this.microsoftToken = microsoftToken;
        this.mojangToken = mojangToken;
        this.gameProfile = gameProfile;
    }

    public boolean refreshTokens() {
        try {
            boolean modified = false;

            if(microsoftToken.isExpired()) {
                Logger.info("Refreshing Microsoft token...");
                microsoftToken = AuthenticationHandler.MICROSOFT_AUTHENTICATOR.refresh(microsoftToken).get();
                modified = true;
            }

            if(mojangToken.isExpired()) {
                Logger.info("Refreshing Mojang token...");
                this.mojangToken = AuthenticationHandler.XBOX_AUTHENTICATOR.getXblToken(microsoftToken)
                        .thenCompose(AuthenticationHandler.XBOX_AUTHENTICATOR::getXstsToken)
                        .thenCompose(AuthenticationHandler.MOJANG_AUTHENTICATOR::getAccessToken)
                        .get();
                this.gameProfile = AuthenticationHandler.MOJANG_AUTHENTICATOR.getProfile(mojangToken).get();
                modified = true;
            }

            if(modified)
                AuthenticationHandler.getInstance().saveToFile();

            return true;
        } catch (ExecutionException | InterruptedException ex) {
            MainGui.getInstance().displayException(ex);
            Logger.error(ex, "Failed to refresh tokens");
            return false;
        }
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
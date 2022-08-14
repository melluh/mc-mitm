package com.melluh.mcmitm.auth;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;
import com.melluh.mcauth.MicrosoftAuthenticator;
import com.melluh.mcauth.MojangAuthenticator;
import com.melluh.mcauth.MojangAuthenticator.GameProfile;
import com.melluh.mcauth.XboxAuthenticator;
import com.melluh.mcauth.tokens.MicrosoftToken;
import com.melluh.mcauth.tokens.MojangToken;
import com.melluh.mcauth.tokens.XboxToken;
import com.melluh.mcmitm.gui.MainGui;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AuthenticationHandler {

    public static final MicrosoftAuthenticator MICROSOFT_AUTHENTICATOR = MicrosoftAuthenticator.createDefault("714f41fe-d740-421a-9763-3e6990462fc5");
    public static final XboxAuthenticator XBOX_AUTHENTICATOR = XboxAuthenticator.createDefault();
    public static final MojangAuthenticator MOJANG_AUTHENTICATOR = MojangAuthenticator.createDefault();

    private static final AuthenticationHandler INSTANCE = new AuthenticationHandler();

    private final List<Account> accounts = new ArrayList<>();

    public void authenticate(MicrosoftToken microsoftToken) {
        try {
            XboxToken xblToken = XBOX_AUTHENTICATOR.getXblToken(microsoftToken).get();
            XboxToken xstsToken = XBOX_AUTHENTICATOR.getXstsToken(xblToken).get();
            MojangToken mojangToken = MOJANG_AUTHENTICATOR.getAccessToken(xstsToken).get();
            GameProfile gameProfile = MOJANG_AUTHENTICATOR.getProfile(mojangToken).get();

            Account account = new Account(microsoftToken, mojangToken, gameProfile);
            accounts.add(account);
            this.saveToFile();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.error(ex, "Failed to authenticate");
            MainGui.getInstance().displayException(ex);
        }
    }

    public void loadFromFile() {
        File file = this.getAccountsFile();
        if(!file.exists())
            return;

        try {
            String str = Files.readString(file.toPath());
            JsonArray accountsArray = JsonParser.array().from(str);

            for(Object obj : accountsArray) {
                JsonObject json = (JsonObject) obj;

                JsonObject microsoftJson = json.getObject("microsoftToken");
                MicrosoftToken microsoftToken = new MicrosoftToken(microsoftJson.getString("accessToken"), microsoftJson.getLong("expiry"), microsoftJson.getString("refreshToken"));

                JsonObject mojangJson = json.getObject("mojangToken");
                MojangToken mojangToken = new MojangToken(mojangJson.getString("accessToken"), mojangJson.getLong("expiry"));

                JsonObject profileJson = json.getObject("profile");
                GameProfile gameProfile = new GameProfile(UUID.fromString(profileJson.getString("uuid")), profileJson.getString("username"));

                this.accounts.add(new Account(microsoftToken, mojangToken, gameProfile));
            }
        } catch (IOException | JsonParserException ex) {
            Logger.error(ex, "Failed to read accounts file");
            MainGui.getInstance().displayException(ex);
        }
    }

    public void saveToFile() {
        JsonArray accountsArray = new JsonArray();

        for(Account account : accounts) {
            JsonObject json = JsonObject.builder()
                    .object("microsoftToken")
                        .value("accessToken", account.getMicrosoftToken().getValue())
                        .value("refreshToken", account.getMicrosoftToken().refreshToken())
                        .value("expiry", account.getMicrosoftToken().getExpiryTime())
                        .end()
                    .object("mojangToken")
                        .value("accessToken", account.getMojangToken().getValue())
                        .value("expiry", account.getMojangToken().getExpiryTime())
                        .end()
                    .object("profile")
                        .value("uuid", account.getGameProfile().uuid().toString())
                        .value("username", account.getGameProfile().username())
                        .end()
                    .done();
            accountsArray.add(json);
        }

        try {
            Files.writeString(this.getAccountsFile().toPath(), JsonWriter.string(accountsArray));
        } catch (IOException ex) {
            Logger.error(ex, "Failed to write accounts file");
        }
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        this.saveToFile();
    }

    private File getAccountsFile() {
        return new File("accounts.json");
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public static AuthenticationHandler getInstance() {
        return INSTANCE;
    }

}

package com.melluh.mcmitm.auth;

import com.melluh.mcauth.MicrosoftAuthenticator;

public class AuthenticationHandler {

    public static final MicrosoftAuthenticator MICROSOFT_AUTHENTICATOR = MicrosoftAuthenticator.newBuilder("714f41fe-d740-421a-9763-3e6990462fc5")
            .build();

}

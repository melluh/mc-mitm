package com.melluh.mcmitm;

public class Launcher {

    // TODO: exception handling
    public static void main(String[] args) throws Exception {
        MinecraftProxy proxy = new MinecraftProxy(25570, "127.0.0.1", 25565);
        proxy.run();
    }

}

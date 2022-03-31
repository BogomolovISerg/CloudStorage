package com.app.cloudstorage.server;
import com.app.cloudstorage.common.Setting;

public class main {

    public static void main(String[] args) throws Throwable {
        Database.connect();
        new Server().start(Setting.PORT);
    }
}

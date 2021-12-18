package com.hadroncfy.proxywhitelist;

public class Config {
    public String kickMessage = "你不在白名单内!";
    public boolean enabled = false;
    public void assign(Config c){
        kickMessage = c.kickMessage;
        enabled = c.enabled;
    }
}

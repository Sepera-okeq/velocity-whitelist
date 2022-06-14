package com.hadroncfy.proxywhitelist.velocity;

import java.io.File;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.hadroncfy.proxywhitelist.Config;
import com.hadroncfy.proxywhitelist.ICommandSender;
import com.hadroncfy.proxywhitelist.ILogger;
import com.hadroncfy.proxywhitelist.IPlugin;
import com.hadroncfy.proxywhitelist.Util;
import com.hadroncfy.proxywhitelist.Whitelist;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@Plugin(id = "velocity-whitelist", name = "velocity-whitelist", version = "1.0", description = "Whitelist plugin for Velocity", authors = {
        "hadroncfy && sepera_okeq" })
public class WhitelistPlugin implements IPlugin {

    @Inject
    private ProxyServer server;
    @Inject
    private Logger logger;
    @Inject
    @DataDirectory
    private Path dataPath;
    private Whitelist whitelist = new Whitelist(this);
    private ILogger loggerImpl = new ILogger(){
    
        @Override
        public void info(String msg) {
            logger.info(msg);
        }
    
        @Override
        public void error(String msg) {
            logger.error(msg);
        }
    };
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        whitelist.init();
        server.getCommandManager().register("vwhitelist", new WhitelistCommand(whitelist));
    }

    @Subscribe
    public void onProxyReloaded(ProxyReloadEvent e){
        whitelist.init();
    }

    @Subscribe
    public void onLoginEvent(LoginEvent l) {
        whitelist.checkPlayerJoin(new PlayerWrapper(l.getPlayer()));
    }

    @Override
    public boolean isOnlineMode() {
        return server.getConfiguration().isOnlineMode();
    }

    @Override
    public File getDataDirectory() {
        return dataPath.toFile();
    }

    @Override
    public void broadcast(ICommandSender sender, String msg) {
        msg = "[" + sender.getLabel() + ": " + msg + "]";
        var tc = Component.text(msg).color(NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, true);
        for (Player player: server.getAllPlayers()) {
            player.sendMessage(tc);
        }
        logger.info(msg);
    }

    @Override
    public ILogger logger() {
        return loggerImpl;
    }

    private File getConfigFile(){
        return new File(getDataDirectory(), "config.json");
    }

    @Override
    public void loadConfig(Config config) {
        File configFile = getConfigFile();;
        if (!configFile.exists()){
            saveConfig(config);
            return;
        }
        String content = Util.readFileSync(configFile, loggerImpl);
        config.assign(gson.fromJson(content, Config.class));
    }

    @Override
    public void saveConfig(Config config) {
        File configFile = getConfigFile();
        Util.writeFileSync(configFile, gson.toJson(config), loggerImpl);
    }
}

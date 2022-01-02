package com.zavier.github;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        PluginManager pluginManager = new DefaultPluginManager(Paths.get("/Users/zhengwei/code/java/pf4j-demo/app/target/classes/plugins"));
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<Long> list = new ArrayList<>();
        final List<Notice> extensions = pluginManager.getExtensions(Notice.class, "email-plugin");

        extensions.forEach(e -> e.notice(list));

        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();
    }
}

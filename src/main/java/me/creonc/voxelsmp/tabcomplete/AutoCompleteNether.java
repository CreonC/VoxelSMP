package me.creonc.voxelsmp.tabcomplete;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteNether implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("nonether")) {
            return null;
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 1) { //first sub command
            completions.add("enablenether");
            completions.add("disablenether");
            completions.add("enableend");
            completions.add("disableend");
            return completions;
        }
        return null;
    }
}

package me.creonc.voxelsmp.tabcomplete;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoComplete implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("graceperiod")) {
            return null;
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 1) { //first sub command
            completions.add("query");
            completions.add("help");
            completions.add("set");
            completions.add("execute");
            return completions;
        } else if (args.length == 2) {
            if (args[0].equals("set")) {
                completions.add("<time(int)><unit(s/m/h/d)>");
                return completions;
            }else{
                return null;
            }

        }
        return null;
    }
}

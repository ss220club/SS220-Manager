package club.ss220.manager.bot.commands;

import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface Command {
    String getName();
    
    String getDescription();
    
    List<CommandData> getCommandData();
    
    EventListener getListner();
}

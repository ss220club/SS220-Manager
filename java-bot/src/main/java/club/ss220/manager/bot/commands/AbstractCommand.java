package club.ss220.manager.bot.commands;

import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public abstract class AbstractCommand implements Command {

    private final String name;
    private final String description;

    protected AbstractCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public abstract List<CommandData> getCommandData();

    @Override
    public abstract EventListener getListner();
}

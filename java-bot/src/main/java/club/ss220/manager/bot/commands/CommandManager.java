package club.ss220.manager.bot.commands;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CommandManager {

    private final JDA jda;
    private final List<Command> commands;

    @Autowired
    public CommandManager(JDA jda, List<Command> commands) {
        this.jda = jda;
        this.commands = commands;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing CommandManager...");
        registerCommands();
        log.info("CommandManager initialized with {} commands", commands.size());
    }
    
    private void registerCommands() {
        List<CommandData> discordCommands = new ArrayList<>();
        
        for (club.ss220.manager.bot.commands.Command command : commands) {
            List<CommandData> commandDataList = command.getCommandData();
            discordCommands.addAll(commandDataList);
            
            jda.addEventListener(command.getListner());
            
            log.info("Registered command: {} with {} interaction types", 
                    command.getName(), commandDataList.size());
        }
        
        jda.updateCommands()
            .addCommands(discordCommands)
            .queue(
                success -> log.info("Successfully registered {} Discord commands", success.size()),
                error -> log.error("Failed to register Discord commands", error)
            );
    }
}

package dev.alphaserpentis.coffeecore.handler.api.discord.commands;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import dev.alphaserpentis.coffeecore.commands.BotCommand;
import dev.alphaserpentis.coffeecore.commands.ButtonCommand;
import dev.alphaserpentis.coffeecore.commands.ModalCommand;
import dev.alphaserpentis.coffeecore.core.CoffeeCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandsHandler extends ListenerAdapter {
    /**
     * The mapping of commands that have been registered to the bot. This is used to check for commands that are already
     * registered and update them if necessary.
     */
    public static final HashMap<String, BotCommand<?>> mappingOfCommands = new HashMap<>();
    /**
     * The executor service that will be used to run the commands.
     */
    public static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Provided a mapping of commands, this will check for any commands that are already registered and update them if
     * necessary. If the command is not registered, it will register it. If the command is registered, but not in the
     * mapping, it will remove it.
     * @param mappingOfCommands The mapping of commands to check and register
     * @param updateCommands Whether to update the commands if they are already registered
     */
    public static void registerCommands(
            @NonNull HashMap<String, BotCommand<?>> mappingOfCommands,
            boolean updateCommands
    ) {
        JDA api = CoffeeCore.api;
        List<Command> listOfActiveCommands = api.retrieveCommands().complete();
        List<String> detectedCommandNames = new ArrayList<>();

        CommandsHandler.mappingOfCommands.putAll(mappingOfCommands);

        // Checks for the detected commands
        for (Iterator<Command> it = listOfActiveCommands.iterator(); it.hasNext(); ) {
            Command cmd = it.next();
            if(mappingOfCommands.containsKey(cmd.getName())) {
                BotCommand<?> botCmd = mappingOfCommands.get(cmd.getName());
                botCmd.setCommandId(cmd.getIdLong());
                if(updateCommands)
                    botCmd.updateCommand(api);

                detectedCommandNames.add(cmd.getName());

                it.remove();
            }
        }

        // Fills in any gaps or removes any commands
        for(Command cmd: listOfActiveCommands) { // Removes unused commands
            api.deleteCommandById(cmd.getId()).complete();
        }

        if(detectedCommandNames.size() < mappingOfCommands.size()) { // Adds new commands
            List<String> missingCommands = new ArrayList<>(mappingOfCommands.keySet());

            missingCommands.removeAll(detectedCommandNames);

            for(String cmdName: missingCommands) {
                BotCommand<?> cmd = mappingOfCommands.get(cmdName);
                cmd.updateCommand(api);
            }
        }
    }

    /**
     * Gets a command from the mapping of commands.
     * @param name The name of the command to get
     * @return {@link BotCommand} or null if the command is not found
     */
    @Nullable
    public static BotCommand<?> getCommand(@NonNull String name) {
        return mappingOfCommands.get(name);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        executor.submit(() -> {
            BotCommand<?> cmd = mappingOfCommands.get(event.getName());
            BotCommand.handleReply(event, cmd);
        });
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        executor.submit(() -> {
            BotCommand<?> cmd = mappingOfCommands.get(event.getButton().getId().substring(0, event.getButton().getId().indexOf("_")));

            ((ButtonCommand<?>) cmd).runButtonInteraction(event);
        });
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        executor.submit(() -> {
            BotCommand<?> cmd = mappingOfCommands.get(event.getModalId().substring(0, event.getModalId().indexOf("_")));

            ((ModalCommand) cmd).runModalInteraction(event);
        });
    }
}
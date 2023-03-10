package dev.alphaserpentis.coffeecore.data.bot;

public class BotSettings {
    public long botOwnerId;
    public String serverDataPath;
    public boolean updateCommandsAtLaunch;
    public boolean registerDefaultCommands;

    public BotSettings(
            long botOwnerId,
            String serverDataPath,
            boolean updateCommandsAtLaunch,
            boolean registerDefaultCommands
    ) {
        this.botOwnerId = botOwnerId;
        this.serverDataPath = serverDataPath;
        this.updateCommandsAtLaunch = updateCommandsAtLaunch;
        this.registerDefaultCommands = registerDefaultCommands;
    }
}

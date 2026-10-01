package space.qclid.arcanum.skills;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.arcanum.skills.gui.SkillsGui;
import space.qclid.arcanum.skills.gui.SkillsGuiListener;
import space.qclid.arcanum.skills.skill.MiningSkill;
import space.qclid.arcanum.skills.skill.RunningSkill;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static space.qclid.arcanum.util.TextUtil.*;

/** Wires the skills subsystem into the plugin: config, manager, skills, GUI, commands, schedulers. */
public final class SkillsFeature {

    private final JavaPlugin plugin;
    private final SkillManager manager;
    private final ToolSpeedService toolSpeed;
    private final MiningSkill mining;
    private final List<Skill> skills = new ArrayList<>();

    public SkillsFeature(JavaPlugin plugin) {
        this.plugin = plugin;

        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) plugin.saveResource("skills.yml", false);
        SkillsConfig config = SkillsConfig.load(YamlConfiguration.loadConfiguration(file), plugin.getLogger());
        SkillCurve curve = new SkillCurve(config.curveA, config.curveP, config.maxLevel);
        this.manager = new SkillManager(plugin, config, curve);

        PlacedBlockTracker tracker = new PlacedBlockTracker();
        this.toolSpeed = new ToolSpeedService(manager);

        this.mining = new MiningSkill(manager, config, tracker);
        skills.add(new RunningSkill(manager, config));
        skills.add(mining);
        toolSpeed.register(SkillType.MINING, MiningSkill::isPickaxe, config.mining::breakSpeedBonus);

        if (space.qclid.arcanum.compat.Compat.BLOCK_BREAK_SPEED == null) {
            plugin.getLogger().warning("Block break speed attribute not found on this server: Mining Efficiency perk is disabled.");
        }
        plugin.getLogger().info("Skills loaded: " + skills.size() + " skills.");

        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(tracker, plugin);
        pm.registerEvents(toolSpeed, plugin);
        pm.registerEvents(new SkillsGuiListener(), plugin);
        for (Skill skill : skills) pm.registerEvents(skill, plugin);
    }

    public SkillManager manager() { return manager; }

    /**
     * For tools that break blocks themselves (Codex runes): awards Mining XP for the block. Call before it is removed.
     */
    public void rewardBlockBreak(Player player, org.bukkit.block.Block block) {
        mining.awardXp(player, block, player.getInventory().getItemInMainHand());
    }

    /** XP popup text for the action bar, or null. */
    public String popupFor(UUID id) { return manager.popupFor(id); }

    /** Starts the 5-tick sampler and the 5-minute autosave. */
    public void start() {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Skill skill : skills) {
                    if (manager.isEnabled(skill.type())) skill.tick(player);
                }
                toolSpeed.update(player);
            }
        }, 1L, 5L);
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> manager.save(), 6000L, 6000L);
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) toolSpeed.clear(player);
        manager.save();
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    private static boolean isAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("arcanum.admin") || sender.hasPermission("dashboard.admin");
    }

    private static void say(CommandSender sender, String miniMessage) {
        sender.sendMessage(MM.deserialize(miniMessage));
    }

    private static Optional<Player> firstPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        List<Player> targets = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
        return targets.isEmpty() ? Optional.empty() : Optional.of(targets.get(0));
    }

    private static void suggestSkills(com.mojang.brigadier.suggestion.SuggestionsBuilder b) {
        for (SkillType t : SkillType.values()) b.suggest(t.id());
    }

    public void registerCommands(Commands commands) {
        var root = Commands.literal("skills")
            .executes(ctx -> {
                if (!(ctx.getSource().getSender() instanceof Player player)) {
                    ctx.getSource().getSender().sendPlainMessage("ᴘʟᴀʏᴇʀѕ ᴏɴʟʏ.");
                    return 1;
                }
                SkillsGui.open(player, manager, skills);
                return 1;
            })
            .then(Commands.literal("set")
                .requires(src -> isAdmin(src.getSender()))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((c, b) -> { suggestSkills(b); return b.buildFuture(); })
                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 150))
                            .executes(ctx -> {
                                var target = firstPlayer(ctx);
                                var type = SkillType.fromId(StringArgumentType.getString(ctx, "skill"));
                                if (target.isEmpty() || type.isEmpty()) {
                                    say(ctx.getSource().getSender(), C_RED + toSmallCaps("Unknown player or skill."));
                                    return 1;
                                }
                                int level = Math.min(IntegerArgumentType.getInteger(ctx, "level"), manager.curve().maxLevel());
                                manager.setXp(target.get().getUniqueId(), type.get(), manager.curve().totalXpForLevel(level));
                                say(ctx.getSource().getSender(), C_GREEN + toSmallCaps(target.get().getName() + "'s " + type.get().displayName() + " is now level " + level));
                                return 1;
                            })))))
            .then(Commands.literal("addxp")
                .requires(src -> isAdmin(src.getSender()))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((c, b) -> { suggestSkills(b); return b.buildFuture(); })
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(ctx -> {
                                var target = firstPlayer(ctx);
                                var type = SkillType.fromId(StringArgumentType.getString(ctx, "skill"));
                                if (target.isEmpty() || type.isEmpty()) {
                                    say(ctx.getSource().getSender(), C_RED + toSmallCaps("Unknown player or skill."));
                                    return 1;
                                }
                                manager.addXp(target.get(), type.get(), LongArgumentType.getLong(ctx, "amount"));
                                say(ctx.getSource().getSender(), C_GREEN + toSmallCaps("Added XP."));
                                return 1;
                            })))))
            .then(Commands.literal("reset")
                .requires(src -> isAdmin(src.getSender()))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .executes(ctx -> {
                        var target = firstPlayer(ctx);
                        if (target.isEmpty()) {
                            say(ctx.getSource().getSender(), C_RED + toSmallCaps("Player not found."));
                            return 1;
                        }
                        for (SkillType t : SkillType.values()) manager.setXp(target.get().getUniqueId(), t, 0);
                        say(ctx.getSource().getSender(), C_GREEN + toSmallCaps("Reset all skills for " + target.get().getName()));
                        return 1;
                    })
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((c, b) -> { suggestSkills(b); return b.buildFuture(); })
                        .executes(ctx -> {
                            var target = firstPlayer(ctx);
                            var type = SkillType.fromId(StringArgumentType.getString(ctx, "skill"));
                            if (target.isEmpty() || type.isEmpty()) {
                                say(ctx.getSource().getSender(), C_RED + toSmallCaps("Unknown player or skill."));
                                return 1;
                            }
                            manager.setXp(target.get().getUniqueId(), type.get(), 0);
                            say(ctx.getSource().getSender(), C_GREEN + toSmallCaps("Reset " + type.get().displayName()));
                            return 1;
                        }))));
        commands.register(root.build(), "Open your skills", List.of());
    }
}

package space.qclid.arcanum.codex.core;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.arcanum.codex.crafting.CodexCrafting;
import space.qclid.arcanum.codex.items.ArcaneItems;
import space.qclid.arcanum.codex.items.ExplorerItems;
import space.qclid.arcanum.codex.tasks.CodexPassiveTask;
import space.qclid.arcanum.data.DataManager;

import java.util.Map;
import java.util.UUID;

public record CodexContext(
    JavaPlugin plugin,
    DataManager dataManager,
    CodexManager manager,
    CodexRegistry registry,
    ArcaneItems arcaneItems,
    ExplorerItems explorerItems,
    CodexCrafting codexCrafting,
    CodexPassiveTask codexPassiveTask,
    Map<UUID, String> activeMachine,
    Map<UUID, ItemStack[]> lastVoidedItems
) {}

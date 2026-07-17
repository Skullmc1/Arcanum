package space.qclid.dashboard.codex.core;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import space.qclid.dashboard.codex.crafting.CodexCrafting;
import space.qclid.dashboard.codex.items.ArcaneItems;
import space.qclid.dashboard.codex.items.ExplorerItems;
import space.qclid.dashboard.codex.tasks.CodexPassiveTask;
import space.qclid.dashboard.data.DataManager;

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

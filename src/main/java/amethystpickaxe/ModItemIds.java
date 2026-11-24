package amethystpickaxe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItemIds {

    public static ResourceKey<Item> create(String name) {
        return ResourceKey.create(
                Registries.ITEM,
                Main.id(name)
        );
    }

    public static final ResourceKey<Item> AMETHYST_PICKAXE =
            create("amethyst_pickaxe");
    public static final ResourceKey<Item> AMETHYST_AXE =
            create("amethyst_axe");
}
package amethystpickaxe;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import java.util.function.Function;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceKey;

public class ModItems {

    public static final Item AMETHYST_PICKAXE = register(
            ModItemIds.AMETHYST_PICKAXE,
            Item::new,
            new Item.Properties()
                    .pickaxe(ToolMaterial.DIAMOND, 1.0F, -2.8F)
                    .enchantable(10)
    );

    public static final Item AMETHYST_AXE = register(
            ModItemIds.AMETHYST_AXE,
            Item::new,
            new Item.Properties()
                    .axe(ToolMaterial.DIAMOND, 5.0F, -3.0F)
                    .enchantable(10)
    );

    public static Item register(
            ResourceKey<Item> itemKey,
            Function<Item.Properties, Item> itemFactory,
            Item.Properties settings
    ) {
        Item item = itemFactory.apply(settings.setId(itemKey));

        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                item
        );

        return item;
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register((creativeTab) -> {
                    creativeTab.accept(AMETHYST_PICKAXE);
                    creativeTab.accept(AMETHYST_AXE);
                });
    }
}
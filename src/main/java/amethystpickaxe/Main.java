package amethystpickaxe;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

public class Main implements ModInitializer {

	public static final String MOD_ID = "amethysttools";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ModEvents.initialize();
	}
}

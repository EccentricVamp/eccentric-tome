package website.eccentric.tome;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

public final class GetMod {

	private static final Map<String, String> modNames = new HashMap<String, String>();

	public static final String MINECRAFT = "minecraft";

	static {
		for (var mod : ModList.get().getMods()) {
			modNames.put(mod.getModId(), mod.getDisplayName());
        }
	}

    public static final String fromState(BlockState state) {
		return orAlias(state.getBlock().getRegistryName().getNamespace());
	}

	public static final String fromStack(ItemStack stack) {
		return orAlias(stack.isEmpty() ? MINECRAFT : stack.getItem().getCreatorModId(stack));
	}

	public static final String orAlias(String mod) {

		return ConfigurationCache.ALIASES.getOrDefault(mod, mod);
	}

    public static final ItemStack shiftStack(ItemStack stack, String mod) {
		if (!stack.hasTag()) return stack;
        
		if (mod.equals(GetMod.fromStack(stack))) return stack;

		var data = stack.getTag().getCompound(TomeItem.TAG_DATA);
		return TomeItem.makeMorphedStack(stack, mod, data);
    }

	public static String name(String mod) {
		return modNames.getOrDefault(mod, mod);
	}
    
}
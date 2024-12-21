package website.eccentric.tome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class Tome {
    public static ItemStack convert(ItemStack tome, ItemStack book) {
        var modsBooks = getModsBooks(tome);
        var mod = ModName.from(book);
        var books = modsBooks.get(mod);
        var registry = ForgeRegistries.ITEMS.getKey(book.getItem());

        books.removeIf(b -> b.getItem().equals(book.getItem()) &&
                areTagsEqual(b.getTag(), book.getTag()));

        setModsBooks(book, modsBooks);
        Migration.setVersion(book);
        book.getOrCreateTag().putBoolean(Tag.IS_TOME, true);
        setHoverName(book);

        copyEnchantments(tome, book);

        return book;
    }

    private static boolean areTagsEqual(@Nullable CompoundTag tag1, @Nullable CompoundTag tag2) {
        if (tag1 == null && tag2 == null) {
            return true;
        }
        if (tag1 == null || tag2 == null) {
            return false;
        }
        return tag1.equals(tag2);
    }

    public static ItemStack revert(ItemStack book) {
        Migration.apply(book);

        var tome = new ItemStack(EccentricTome.TOME.get());
        copyMods(book, tome);
        Migration.setVersion(tome);

        copyEnchantments(book, tome);

        clear(book);

        return tome;
    }

    public static ItemStack attach(ItemStack tome, ItemStack book) {
        var mod = ModName.from(book);
        var modsBooks = getModsBooks(tome);

        var books = modsBooks.getOrDefault(mod, new ArrayList<>());
        books.add(book.copy());
        modsBooks.put(mod, books);

        setModsBooks(tome, modsBooks);
        return tome;
    }

    public static Map<String, List<ItemStack>> getModsBooks(ItemStack stack) {
        return Tag.deserialize(stack.getOrCreateTagElement(Tag.MODS));
    }

    public static void setModsBooks(ItemStack stack, Map<String, List<ItemStack>> modsBooks) {
        stack.getOrCreateTag().put(Tag.MODS, Tag.serialize(modsBooks));
    }

    public static boolean isTome(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        else if (stack.getItem() instanceof TomeItem)
            return true;
        else {
            var tag = stack.getTag();
            if (tag == null)
                return false;

            return tag.getBoolean(Tag.IS_TOME);
        }
    }

    @Nullable
    public static InteractionHand inHand(Player player) {
        var hand = InteractionHand.MAIN_HAND;
        var stack = player.getItemInHand(hand);
        if (isTome(stack))
            return hand;

        hand = InteractionHand.OFF_HAND;
        stack = player.getItemInHand(hand);
        if (isTome(stack))
            return hand;

        return null;
    }

    private static void copyMods(ItemStack source, ItemStack target) {
        var tag = target.getOrCreateTag();
        tag.put(Tag.MODS, source.getTagElement(Tag.MODS));
    }

    private static void clear(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null)
            return;

        tag.remove(Tag.MODS);
        tag.remove(Tag.IS_TOME);
        tag.remove(Tag.VERSION);
        stack.resetHoverName();
    }

    private static void setHoverName(ItemStack book) {
        var name = book.getHoverName().copy().withStyle(ChatFormatting.GREEN);
        book.setHoverName(Component.translatable("eccentrictome.name", name));
    }

    private static void copyEnchantments(ItemStack source, ItemStack target) {
        source.getEnchantmentTags();
        target.getOrCreateTag().put("Enchantments", source.getEnchantmentTags().copy());
    }
}
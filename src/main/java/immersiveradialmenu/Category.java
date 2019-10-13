package immersiveradialmenu;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.tool.ToolboxHandler;
import blusunrize.immersiveengineering.common.items.ItemToolbox;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public enum Category {
  TOOLS(
    ToolboxHandler::isTool,
    3, 10,
    "desc.immersiveengineering.info.toolbox.tool"
  ),
  FOOD(
    ToolboxHandler::isFood,
    0, 3,
    "desc.immersiveengineering.info.toolbox.food"
  ),
  WIRING(
    itemStack -> ToolboxHandler.isWiring(itemStack, Minecraft.getMinecraft().world),
    10, 16,
    "desc.immersiveengineering.info.toolbox.wire"
  ),
  ANYTHING(
    itemStack -> !(itemStack.getItem() instanceof ItemToolbox),
    16, 23,
    "desc.immersiveengineering.info.toolbox.any"
  );

  private Predicate<ItemStack> categoryMatcher;
  private int minSlotInclusive;
  private int maxSlotExclusive;
  private String unlocalisedName;

  private Category(
    Predicate<ItemStack> categoryMatcher,
    int minSlotInclusive,
    int maxSlotExclusive,
    String unlocalisedName
  ) {
    this.categoryMatcher = categoryMatcher;
    this.minSlotInclusive = minSlotInclusive;
    this.maxSlotExclusive = maxSlotExclusive;
    this.unlocalisedName = unlocalisedName;
  }

  private boolean matches(ItemStack itemStack) {
    return this.categoryMatcher.test(itemStack);
  }

  public int[] slotIndices() {
    return IntStream.range(minSlotInclusive, maxSlotExclusive).toArray();
  }

  public int numberOfIndices() {
    return maxSlotExclusive - minSlotInclusive;
  }

  public String unlocalisedName() {
    return unlocalisedName;
  }

  public boolean validForSlot(ItemStack itemStack, int slot) {
    if (slot >= minSlotInclusive && slot < maxSlotExclusive) {
      return matches(itemStack);
    } else {
      return false;
    }
  }

  public static List<Category> categoriesFor(ItemStack itemStack) {
    if (itemStack == null)
      return Lists.newArrayList();
    List<Category> categories = Lists.newArrayList();
    if (itemStack.isEmpty() || TOOLS.matches(itemStack))
      categories.add(TOOLS);
    if (itemStack.isEmpty() || FOOD.matches(itemStack))
      categories.add(FOOD);
    if (itemStack.isEmpty() || WIRING.matches(itemStack))
      categories.add(WIRING);
    if (itemStack.isEmpty() || ANYTHING.matches(itemStack))
      categories.add(ANYTHING);
    return categories;
  }

  public static boolean itemStackValidInSlot(ItemStack itemStack, int slot) {
    if (itemStack.isEmpty()) {
      return true;
    }
    List<Category> categories = categoriesFor(itemStack);
    for (Category category : categories) {
      if (category.validForSlot(itemStack, slot)) {
        return true;
      }
    }
    return false;
  }
}

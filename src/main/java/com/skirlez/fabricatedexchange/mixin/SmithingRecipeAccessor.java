package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.*;

@SuppressWarnings("removal")
@Mixin(SmithingRecipe.class)
public interface SmithingRecipeAccessor {
	@Accessor
	Ingredient getAddition();
	@Accessor
	Ingredient getBase();
}
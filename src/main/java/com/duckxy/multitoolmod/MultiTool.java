package com.duckxy.multitoolmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiTool implements ModInitializer {
	public static final String MOD_ID = "multitool";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Identifier ATTACK_DAMAGE_MODIFIER_ID = Identifier.of(MOD_ID, "base_attack_damage");
	private static final Identifier ATTACK_SPEED_MODIFIER_ID = Identifier.of(MOD_ID, "base_attack_speed");

	// Create the registry key for the item
	public static final RegistryKey<Item> MULTI_TOOL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "multi_tool"));

	public static final Item MULTI_TOOL = new MultiToolItem(new Item.Settings()
			.registryKey(MULTI_TOOL_KEY)
			.maxDamage(1561)
			.attributeModifiers(AttributeModifiersComponent.builder()
					.add(EntityAttributes.ATTACK_DAMAGE,
							new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, 7.0, EntityAttributeModifier.Operation.ADD_VALUE),
							AttributeModifierSlot.MAINHAND)
					.add(EntityAttributes.ATTACK_SPEED,
							new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, -2.4, EntityAttributeModifier.Operation.ADD_VALUE),
							AttributeModifierSlot.MAINHAND)
					.build())
	);

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, MULTI_TOOL_KEY, MULTI_TOOL);
	}
}
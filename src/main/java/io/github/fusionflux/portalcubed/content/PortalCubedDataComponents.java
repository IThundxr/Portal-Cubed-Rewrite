package io.github.fusionflux.portalcubed.content;

import com.mojang.serialization.Codec;

import io.github.fusionflux.portalcubed.content.portal.gun.PortalGunSettings;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;

public class PortalCubedDataComponents {
	public static final DataComponentType<PortalGunSettings> PORTAL_GUN_SETTINGS = register(
			"portal_gun_settings", PortalGunSettings.CODEC, PortalGunSettings.STREAM_CODEC
	);
	public static final DataComponentType<Unit> LEMONADE_ARMED = register(
			"lemonade_armed", Unit.CODEC, PortalCubedStreamCodecs.UNIT
	);
	public static final DataComponentType<Integer> PROP_VARIANT = register(
			"prop_variant", ExtraCodecs.NON_NEGATIVE_INT, ByteBufCodecs.VAR_INT
	);

	private static <T> DataComponentType<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		DataComponentType.Builder<T> builder = DataComponentType.builder();
		builder.persistent(codec).networkSynchronized(streamCodec);
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, name, builder.build());
	}
}

package io.github.fusionflux.portalcubed.content.decoration.signage;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedCodecs;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record Signage(Optional<ResourceLocation> cleanTexture, Optional<ResourceLocation> agedTexture, Component name) {
	public static final Codec<Signage> DIRECT_CODEC = PortalCubedCodecs.validate(
			RecordCodecBuilder.create(instance -> instance.group(
					ResourceLocation.CODEC.optionalFieldOf("clean_texture").forGetter(Signage::cleanTexture),
					ResourceLocation.CODEC.optionalFieldOf("aged_texture").forGetter(Signage::agedTexture),
					ComponentSerialization.CODEC.fieldOf("name").forGetter(Signage::name)
			).apply(instance, Signage::new)),
			Signage::validate
	);
	public static final ResourceKey<Signage> LARGE_BLANK = ResourceKey.create(PortalCubedRegistries.LARGE_SIGNAGE, PortalCubed.id("blank"));
	public static final Codec<Holder<Signage>> LARGE_CODEC = RegistryFixedCodec.create(PortalCubedRegistries.LARGE_SIGNAGE);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Signage>> LARGE_STREAM_CODEC = ByteBufCodecs.holderRegistry(PortalCubedRegistries.LARGE_SIGNAGE);
	public static final ResourceKey<Signage> SMALL_BLANK = ResourceKey.create(PortalCubedRegistries.SMALL_SIGNAGE, PortalCubed.id("blank"));
	public static final Codec<Holder<Signage>> SMALL_CODEC = RegistryFixedCodec.create(PortalCubedRegistries.SMALL_SIGNAGE);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Signage>> SMALL_STREAM_CODEC = ByteBufCodecs.holderRegistry(PortalCubedRegistries.SMALL_SIGNAGE);

	public Optional<ResourceLocation> selectTexture(boolean aged) {
		return aged ? this.agedTexture : this.cleanTexture;
	}

	private static DataResult<Signage> validate(Signage signage) {
		if (signage.cleanTexture.isEmpty() && signage.agedTexture.isEmpty())
			return DataResult.error(() -> "Signage must have at least one texture present");
		return DataResult.success(signage);
	}
}

package io.github.fusionflux.portalcubed.registration.block;

import io.github.fusionflux.portalcubed.registration.Registrar;
import io.github.fusionflux.portalcubed.registration.RenderTypes;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockHelper {
	private final Registrar registrar;

	Map<Block, RenderTypes> renderTypes = new HashMap<>();

	public BlockHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public BlockBuilder<Block> create(String name) {
		return create(name, Block::new);
	}

	public <T extends Block> BlockBuilder<T> create(String name, BlockFactory<T> factory) {
		return new BlockBuilderImpl<>(registrar, name, factory);
	}

	public BlockBuilder<Block> createFrom(String name, Block copyFrom) {
		return create(name).copyFrom(copyFrom);
	}

	public <T extends Block> BlockBuilder<T> createFrom(String name, BlockFactory<T> factory, Block copyFrom) {
		return create(name, factory).copyFrom(copyFrom);
	}

	public <T extends Block> T simple(String name, BlockFactory<T> factory, Block copyFrom) {
		return create(name, factory).copyFrom(copyFrom).build();
	}
}

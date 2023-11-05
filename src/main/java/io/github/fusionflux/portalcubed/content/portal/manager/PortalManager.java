package io.github.fusionflux.portalcubed.content.portal.manager;

import io.github.fusionflux.portalcubed.content.portal.Portal;
import io.github.fusionflux.portalcubed.content.portal.PortalPickResult;
import io.github.fusionflux.portalcubed.content.portal.storage.PortalStorage;
import io.github.fusionflux.portalcubed.content.portal.storage.SectionPortalStorage;
import net.fabricmc.api.EnvType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;

import java.util.List;
import java.util.Objects;

public abstract class PortalManager {
	protected final PortalStorage storage = new SectionPortalStorage();

	public List<Portal> allPortals() {
		return storage.all();
	}

	public static PortalManager of(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return ServerPortalManager.of(serverLevel);
		} else if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT && level instanceof ClientLevel clientLevel) {
			return ClientPortalManager.of(clientLevel);
		}
		throw new IllegalArgumentException(level + " is not ServerLevel or ClientLevel");
	}

	@Nullable
	public PortalPickResult pickPortal(Vec3 start, Vec3 end) {
		return storage.findPortalsInBox(new AABB(start, end))
                .map(portal -> this.pickPortal(portal, start, end))
                .filter(Objects::nonNull)
				.min(PortalPickResult.CLOSEST_TO_START)
				.orElse(null);
	}

	@Nullable
	private PortalPickResult pickPortal(Portal portal, Vec3 start, Vec3 end) {
		// portals cannot be interacted with from behind
		Vec3 delta = end.subtract(start);
		if (delta.dot(portal.normal) > 0)
			return null;
		return portal.plane.clip(start, end).map(hit -> {
			// TODO: transform these across portals
			Vec3 teleportedEnd = end;
			Vec3 hitOut = hit;
            return new PortalPickResult(start, end, hit, hit, portal);
        }).orElse(null);
	}
}

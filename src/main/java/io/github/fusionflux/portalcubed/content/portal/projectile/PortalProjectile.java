package io.github.fusionflux.portalcubed.content.portal.projectile;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.portal.entity.Portal;
import io.github.fusionflux.portalcubed.framework.UnsavedEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PortalProjectile extends UnsavedEntity {
	public static final double SPEED = (6 * 16) / 20f; // 6 chunks per second

	public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(PortalProjectile.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Direction> SHOOTER_FACING = SynchedEntityData.defineId(PortalProjectile.class, EntityDataSerializers.DIRECTION);

	private int color;
	private Direction shooterFacing;

	public PortalProjectile(EntityType<?> variant, Level world) {
		super(variant, world);
		this.noPhysics = true;
	}

	public static PortalProjectile create(Level level, int color, Direction shooterFacing) {
		PortalProjectile projectile = new PortalProjectile(PortalCubedEntities.PORTAL_PROJECTILE, level);
		projectile.entityData.set(COLOR, color);
		projectile.entityData.set(SHOOTER_FACING, shooterFacing);
		return projectile;
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(COLOR, 0);
		entityData.define(SHOOTER_FACING, Direction.UP);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		if (data == COLOR) {
			this.color = entityData.get(COLOR);
		} else if (data == SHOOTER_FACING) {
			this.shooterFacing = entityData.get(SHOOTER_FACING);
		}
	}

	@Override
	public void tick() {
		if (this.getDeltaMovement().lengthSqr() == 0) {
			super.tick();
			return;
		}

		Vec3 start = this.position();
		super.tick();
		this.move(MoverType.SELF, getDeltaMovement());
		Vec3 end = this.position();

		Level level = this.level();
		BlockHitResult hit = level.clip(new ClipContext(
				start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
		));
		if (hit.getType() == HitResult.Type.BLOCK) {
			this.setDeltaMovement(0, 0, 0);
			if (level instanceof ServerLevel serverLevel) {
				this.discard();
				this.spawnPortal(serverLevel, hit);
			}
		}
	}

	private void spawnPortal(ServerLevel level, BlockHitResult hit) {
		Direction facing = hit.getDirection();
		Direction horizontalFacing = facing.getAxis().isHorizontal() ? facing : this.shooterFacing;
		Direction verticalFacing = facing.getAxis().isVertical() ? facing : null;
		Vec3 pos = hit.getLocation().relative(facing, 0.01);
		Portal portal = Portal.create(level, this.color, pos, horizontalFacing, verticalFacing);
		level.addFreshEntity(portal);
	}

	public int getColor() {
		return color;
	}
}

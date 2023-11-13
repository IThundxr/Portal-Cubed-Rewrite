package io.github.fusionflux.portalcubed.content.portal;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;
import io.github.fusionflux.portalcubed.framework.util.PacketUtils;
import io.github.fusionflux.portalcubed.framework.util.TransformUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.Optional;
import java.util.UUID;

public final class Portal {
	// portal plane is 2 pixels short of full blocks on each axis, 1 on each side
	public static final double SIXTEENTH = 1 / 16f;
	public static final double HEIGHT = 2 - (2 * SIXTEENTH);
	public static final double WIDTH = 1 - (2 * SIXTEENTH);
	public static final double THICKNESS = 0;
	public static final double HOLE_DEPTH = 5;

	public final int netId;
    public final Vec3 origin;
	public final AABB plane; // technically a box, but really thin on 1 axis
	public final AABB holeBox;
	public final VoxelShape hole; // the hole this portal punches in the world to allow walking through
	public final Vec3 normal;
	public final FrontAndTop orientation;
	public final Quaternionf rotation;
	public final Quaternionf rotation180;
    public final PortalShape shape;
    public final PortalType type;
	public final int color;
	public final UUID owner;

	private int linkedNetId;
	@Nullable
	private Portal linked;
	private Optional<Portal> linkedOptional;

    public Portal(int netId, Vec3 origin, FrontAndTop orientation, PortalShape shape, PortalType type, int color, UUID owner) {
		this.netId = netId;
        this.origin = origin;
		this.normal = Vec3.atLowerCornerOf(orientation.front().getNormal());
		this.orientation = orientation;
        this.shape = shape;
        this.type = type;
		this.color = color;
		this.owner = owner;

		this.linkedNetId = -1;
		this.linked = null;
		this.linkedOptional = Optional.empty();

		this.rotation = TransformUtils.quaternionOf(orientation);
		this.rotation180 = TransformUtils.rotateAround(rotation, orientation.top().getAxis(), 180);

		Direction.Axis frontAxis = orientation.front().getAxis();
		Direction.Axis verticalAxis = orientation.top().getAxis();
		double y = frontAxis.isVertical() ? THICKNESS : HEIGHT;
		double x = frontAxis == Direction.Axis.X ? THICKNESS : (verticalAxis == Direction.Axis.X ? HEIGHT : WIDTH);
		double z = frontAxis == Direction.Axis.Z ? THICKNESS : (verticalAxis == Direction.Axis.Z ? HEIGHT : WIDTH);
		this.plane = AABB.ofSize(origin, x, y, z);

		Vec3 holeOffset = this.normal.scale(-HOLE_DEPTH);
		AABB holePlane = this.plane.inflate(-0.01);// make hole slightly smaller than TP plane
		this.holeBox = holePlane.expandTowards(holeOffset);
		this.hole = Shapes.create(this.holeBox);
    }

	@Nullable
	public Portal getLinked() {
		return this.linked;
	}

	public Optional<Portal> maybeGetLinked() {
		return linkedOptional;
	}

	public boolean isActive() {
		return this.linked != null;
	}

	public Vector3d relativize(Vector3d pos) {
		return pos.sub(origin.x, origin.y, origin.z);
	}

	public Vector3d derelativize(Vector3d pos) {
		return pos.add(origin.x, origin.y, origin.z);
	}

	/**
	 * Do not use, use the portal manager
	 */
	@ApiStatus.Internal
	public void setLinked(@Nullable Portal portal) {
		this.linkedNetId = portal == null ? -1 : portal.netId;
		this.linked = portal;
		this.linkedOptional = Optional.ofNullable(portal);
	}

	@ApiStatus.Internal
	public void findLinkedPortal(PortalManager manager) {
		if (this.linkedNetId != -1) {
			this.setLinked(manager.getPortalByNetId(this.linkedNetId));
		}
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeVarInt(netId);
		PacketUtils.writeVec3(buf, origin);
		buf.writeEnum(orientation);
		buf.writeEnum(shape);
		buf.writeEnum(type);
		buf.writeVarInt(color);
		buf.writeVarInt(linkedNetId);
		buf.writeUUID(owner);
	}

	public static Portal fromNetwork(FriendlyByteBuf buf) {
		int netId = buf.readVarInt();
		Vec3 origin = PacketUtils.readVec3(buf);
		FrontAndTop orientation = buf.readEnum(FrontAndTop.class);
		PortalShape shape = buf.readEnum(PortalShape.class);
		PortalType type = buf.readEnum(PortalType.class);
		int color = buf.readVarInt();
		int linkedId = buf.readVarInt();
		UUID owner = buf.readUUID();
		Portal portal = new Portal(netId, origin, orientation, shape, type, color, owner);
		portal.linkedNetId = linkedId; // the portal reference will be resolved later with findLinkedPortal
		return portal;
	}
}

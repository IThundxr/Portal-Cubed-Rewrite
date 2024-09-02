package io.github.fusionflux.portalcubed.content.decoration.signage.screen;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.content.decoration.signage.screen.widget.SignageSlotWidget;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.ScrollbarWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureSignageConfigPacket;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;

public class LargeSignageConfigScreen extends Screen {
	private static final ResourceLocation BACKGROUND = PortalCubed.id("textures/gui/container/signage/large_signage.png");
	private static final int BACKGROUND_WIDTH = 176;
	private static final int BACKGROUND_HEIGHT = 136;
	private final ResourceLocation SCROLLER = PortalCubed.id("signage/scroller");
	private static final int ROWS = 4;
	private static final int COLUMNS = 6;
	private static final int SIZE = COLUMNS * ROWS;

	public static final Component TITLE = Component.translatable("container.portalcubed.large_signage");
	public static final Component AGED_TITLE = Component.translatable("container.portalcubed.aged_large_signage");

	private final LargeSignageBlockEntity largeSignage;

	private int leftPos;
	private int topPos;
	private ScrollbarWidget scrollBar;

    public LargeSignageConfigScreen(LargeSignageBlockEntity largeSignage) {
		super(largeSignage.aged ? AGED_TITLE : TITLE);
		this.largeSignage = largeSignage;

		this.scrollBar = new ScrollbarWidget(SCROLLER, () -> {
			boolean wasFocused = this.getFocused() == this.scrollBar;
			this.rebuildWidgets();
			if (wasFocused)
				this.setFocused(this.scrollBar);
		});
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - BACKGROUND_WIDTH) / 2;
        this.topPos = (this.height - BACKGROUND_HEIGHT) / 2;

		PanelLayout root = new 	PanelLayout();
		root.addChild(8, 6, new TitleWidget(title, font));

		{
			GridLayout slots = root.addChild(15, 16, new GridLayout());

			ArrayList<Signage.Holder> supportedSignage = new ArrayList<>(SignageManager.INSTANCE.allOfSize(Signage.Size.LARGE));
			Collections.sort(supportedSignage);

			int rowCount = Mth.positiveCeilDiv(supportedSignage.size(), COLUMNS) - ROWS;
			int scrollRowPos = Math.max((int) ((this.scrollBar.scrollPos() * rowCount) + .5f), 0);
			int i = -(COLUMNS * scrollRowPos);
			if (rowCount > 0) {
				this.scrollBar.active = true;
				this.scrollBar.scrollRate = 1f / rowCount;
			}

			for (Signage.Holder holder : supportedSignage) {
				if (i >= 0) {
					SignageSlotWidget slot = new SignageSlotWidget(holder.value(), this.largeSignage.aged, () -> {
						slots.visitWidgets(widget -> ((SignageSlotWidget) widget).deselect());
						PortalCubedPackets.sendToServer(new ConfigureSignageConfigPacket.Large(this.largeSignage.getBlockPos(), holder));
					});
					if (this.largeSignage.holder() == holder)
						slot.select();

					slots.addChild(slot, i / COLUMNS, i % COLUMNS);
				}
				if (++i >= SIZE) break;
			}
			root.addChild(149, 16, this.scrollBar);
		}

		// arrange elements
		root.arrangeElements();
		// position in the top left corner of the background
		root.setPosition(this.leftPos, this.topPos);
		root.visitWidgets(this::addRenderableWidget);
		// reverse order of widgets, so they go from top to bottom
		Collections.reverse(this.children());
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.renderBackground(graphics, mouseX, mouseY, delta);
		graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		return null;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// close on E
		if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}

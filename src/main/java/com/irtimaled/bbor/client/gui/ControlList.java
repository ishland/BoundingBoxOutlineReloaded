package com.irtimaled.bbor.client.gui;

import com.irtimaled.bbor.client.renderers.RenderHelper;
import com.irtimaled.bbor.client.renderers.Renderer;
import com.irtimaled.bbor.common.MathHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class ControlList extends DrawableHelper implements IControlSet {
    public static final int CONTROLS_WIDTH = 310;
    protected static final int PADDING = 8;

    protected final int listLeft;
    protected final List<ControlListEntry> entries = new ArrayList<>();
    private final int scrollBarLeft;
    private final int listHeight;
    private final MinecraftClient minecraft;
    private final int width;
    private final int height;
    private final int top;
    private final int bottom;

    protected int contentHeight = PADDING;
    private double amountScrolled;
    private boolean clickedScrollbar;
    private boolean transparentBackground;
    private IControl focused;
    private boolean isDragging;

    ControlList(int width, int height, int top, int bottom) {
        this.minecraft = MinecraftClient.getInstance();
        this.width = width;
        this.scrollBarLeft = width - 6;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.listHeight = bottom - top;
        this.listLeft = width / 2 - CONTROLS_WIDTH / 2;
    }

    void add(ControlListEntry entry) {
        entry.index = entries.size();
        addEntry(entry);
    }

    private void addEntry(ControlListEntry entry) {
        this.entries.add(entry);
        this.contentHeight += entry.getControlHeight();
    }

    public void filter(String lowerValue) {
        int height = 0;

        for (ControlListEntry entry : entries) {
            entry.filter(lowerValue);
            if (entry.isVisible()) {
                height += entry.getControlHeight();
            } else if (entry == focused) {
                focused = null;
            }
        }
        this.contentHeight = height + PADDING;
    }

    void close() {
        this.entries.forEach(ControlListEntry::close);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.clickedScrollbar = button == 0 && mouseX >= (double) this.scrollBarLeft;
        return isMouseOver(mouseX, mouseY) &&
                (IControlSet.super.mouseClicked(mouseX, mouseY, button) ||
                        this.clickedScrollbar);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= (double) this.top && mouseY <= (double) this.bottom;
    }

    @Override
    public boolean changeFocus(boolean moveForward) {
        boolean newControlFocused = IControlSet.super.changeFocus(moveForward);
        if (newControlFocused) {
            this.ensureVisible((ControlListEntry) this.getFocused());
        }

        return newControlFocused;
    }

    private void ensureVisible(ControlListEntry control) {
        int controlTop = control.getControlTop();
        int controlHeight = control.getControlHeight();
        int distanceAboveTop = this.top - controlTop;
        if (distanceAboveTop > 0) {
            this.amountScrolled -= Math.max(controlHeight, distanceAboveTop + PADDING);
            return;
        }

        int distanceBelowBottom = controlTop + controlHeight - this.bottom;
        if (distanceBelowBottom > 0) {
            this.amountScrolled += Math.max(controlHeight, distanceBelowBottom + PADDING);
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        if (IControlSet.super.mouseDragged(mouseX, mouseY, button, p_mouseDragged_6_, p_mouseDragged_8_)) {
            return true;
        }
        if (button == 0 && this.clickedScrollbar) {
            if (mouseY < (double) this.top) {
                this.amountScrolled = 0.0D;
            } else if (mouseY > (double) this.bottom) {
                this.amountScrolled = this.getMaxScroll();
            } else {
                double maxScroll = this.getMaxScroll();
                if (maxScroll < 1.0D) {
                    maxScroll = 1.0D;
                }

                double amountScrolled = maxScroll / (double) (this.listHeight - getScrollBarHeight());
                if (amountScrolled < 1.0D) {
                    amountScrolled = 1.0D;
                }

                this.amountScrolled += p_mouseDragged_8_ * amountScrolled;
            }

            return true;
        }
        return false;
    }

    private int getMaxScroll() {
        return Math.max(0, this.contentHeight - (this.listHeight - 4));
    }

    private int getScrollBarHeight() {
        return MathHelper.clamp((int) ((float) (this.listHeight * this.listHeight) / (float) this.contentHeight),
                32,
                this.listHeight - PADDING);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        this.amountScrolled -= scrollAmount * 10;
        return true;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.amountScrolled = MathHelper.clamp(this.amountScrolled, 0.0D, this.getMaxScroll());

        RenderHelper.disableLighting();
        RenderHelper.disableFog();
        if (!transparentBackground) drawListBackground();

        int listTop = this.top + PADDING - (int) this.amountScrolled;

        drawEntries(matrixStack, mouseX, mouseY, listTop);

        RenderHelper.enableDepthTest();
        RenderHelper.depthFuncAlways();

        this.overlayBackground(0, this.top);
        this.overlayBackground(this.bottom, this.height);
        RenderHelper.depthFuncLessEqual();
        RenderHelper.disableDepthTest();
        RenderHelper.enableBlend();
        RenderHelper.blendFuncGui();
        RenderHelper.disableAlphaTest();
        // RenderHelper.shadeModelSmooth();
        RenderHelper.disableTexture();
        drawOverlayShadows();

        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            drawScrollBar(maxScroll);
        }

        RenderHelper.enableTexture();
        // RenderHelper.shadeModelFlat();
        RenderHelper.enableAlphaTest();
        RenderHelper.disableBlend();
    }

    private void drawListBackground() {
        this.minecraft.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        Renderer.startTextured()
                .setColor(32, 32, 32)
                .setAlpha(255)
                .addPoint(0, this.bottom, 0.0D, (float) 0 / 32.0F, (float) (this.bottom + (int) this.amountScrolled) / 32.0F)
                .addPoint(this.width, this.bottom, 0.0D, (float) this.width / 32.0F, (float) (this.bottom + (int) this.amountScrolled) / 32.0F)
                .addPoint(this.width, this.top, 0.0D, (float) this.width / 32.0F, (float) (this.top + (int) this.amountScrolled) / 32.0F)
                .addPoint(0, this.top, 0.0D, (float) 0 / 32.0F, (float) (this.top + (int) this.amountScrolled) / 32.0F)
                .render();
    }

    private void drawEntries(MatrixStack matrixStack, int mouseX, int mouseY, int top) {
        for (ControlListEntry entry : this.entries) {
            if (!entry.isVisible()) continue;

            entry.setX(this.listLeft);
            entry.setY(top);

            int height = entry.getControlHeight();
            int bottom = top + height;
            if(top <= this.bottom && bottom >= this.top) {
                drawEntry(matrixStack, mouseX, mouseY, top, entry, height);
            }
            top = bottom;
        }
    }

    protected void drawEntry(MatrixStack matrixStack, int mouseX, int mouseY, int top, ControlListEntry entry, int height) {
        entry.render(matrixStack, mouseX, mouseY);
    }

    private void overlayBackground(int top, int bottom) {
        this.minecraft.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        Renderer.startTextured()
                .setColor(64, 64, 64)
                .setAlpha(255)
                .addPoint(0, bottom, -100.0D, 0.0D, (float) bottom / 32.0F)
                .addPoint(this.width, bottom, -100.0D, (float) this.width / 32.0F, (float) bottom / 32.0F)
                .addPoint(this.width, top, -100.0D, (float) this.width / 32.0F, (float) top / 32.0F)
                .addPoint(0, top, -100.0D, 0.0D, (float) top / 32.0F)
                .render();
    }

    private void drawScrollBar(int maxScroll) {
        int scrollBarHeight = this.getScrollBarHeight();
        int scrollBarTop = (int) this.amountScrolled * (this.listHeight - scrollBarHeight) / maxScroll + this.top;
        if (scrollBarTop < this.top) {
            scrollBarTop = this.top;
        }

        Renderer.startTextured()
                .setAlpha(255)
                .addPoint(this.scrollBarLeft, this.bottom, 0.0D, 0.0D, 1.0D)
                .addPoint(this.width, this.bottom, 0.0D, 1.0D, 1.0D)
                .addPoint(this.width, this.top, 0.0D, 1.0D, 0.0D)
                .addPoint(this.scrollBarLeft, this.top, 0.0D, 0.0D, 0.0D)
                .render();

        Renderer.startTextured()
                .setColor(128, 128, 128)
                .setAlpha(255)
                .addPoint(this.scrollBarLeft, scrollBarTop + scrollBarHeight, 0.0D, 0.0D, 1.0D)
                .addPoint(this.width, scrollBarTop + scrollBarHeight, 0.0D, 1.0D, 1.0D)
                .addPoint(this.width, scrollBarTop, 0.0D, 1.0D, 0.0D)
                .addPoint(this.scrollBarLeft, scrollBarTop, 0.0D, 0.0D, 0.0D)
                .render();

        Renderer.startTextured()
                .setColor(192, 192, 192)
                .setAlpha(255)
                .addPoint(this.scrollBarLeft, scrollBarTop + scrollBarHeight - 1, 0.0D, 0.0D, 1.0D)
                .addPoint(this.width - 1, scrollBarTop + scrollBarHeight - 1, 0.0D, 1.0D, 1.0D)
                .addPoint(this.width - 1, scrollBarTop, 0.0D, 1.0D, 0.0D)
                .addPoint(this.scrollBarLeft, scrollBarTop, 0.0D, 0.0D, 0.0D)
                .render();
    }

    private void drawOverlayShadows() {
        Renderer.startTextured()
                .addPoint(0, this.top + 4, 0.0D, 0.0D, 1.0D)
                .addPoint(this.width, this.top + 4, 0.0D, 1.0D, 1.0D)
                .setAlpha(255)
                .addPoint(this.width, this.top, 0.0D, 1.0D, 0.0D)
                .addPoint(0, this.top, 0.0D, 0.0D, 0.0D)
                .render();
        Renderer.startTextured()
                .addPoint(this.width, this.bottom - 4, 0.0D, 1.0D, 0.0D)
                .addPoint(0, this.bottom - 4, 0.0D, 0.0D, 0.0D)
                .setAlpha(255)
                .addPoint(0, this.bottom, 0.0D, 0.0D, 1.0D)
                .addPoint(this.width, this.bottom, 0.0D, 1.0D, 1.0D)
                .render();
    }

    ControlList section(String title, CreateControl... createControls) {
        this.add(new ControlListSection(title, createControls));
        return this;
    }

    void setTransparentBackground() {
        this.transparentBackground = true;
    }

    @Override
    public List<? extends IControl> controls() {
        return entries;
    }

    @Override
    public IControl getFocused() {
        return focused;
    }

    @Override
    public void setFocused(IControl control) {
        this.focused = control;
    }

    @Override
    public boolean isDragging() {
        return isDragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.isDragging = dragging;
    }
}

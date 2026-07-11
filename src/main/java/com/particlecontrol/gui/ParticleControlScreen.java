package com.particlecontrol.gui;

import com.particlecontrol.config.ParticleConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ParticleControlScreen extends Screen {

    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 48;
    private static final int LIST_BOTTOM_MARGIN = 32;

    private final Screen parent;
    private List<Identifier> allIds;
    private List<Identifier> filteredIds;
    private TextFieldWidget searchBox;
    private double scrollOffset = 0;

    public ParticleControlScreen(Screen parent) {
        super(Text.literal("Particle control"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.allIds = new ArrayList<>(Registries.PARTICLE_TYPE.getIds());
        this.allIds.sort(Comparator.comparing(Identifier::toString));
        this.filteredIds = this.allIds;

        this.searchBox = new TextFieldWidget(this.textRenderer, 8, 20, this.width - 16, 20, Text.literal("Search"));
        this.searchBox.setPlaceholder(Text.literal("Search particles..."));
        this.searchBox.setChangedListener(this::applyFilter);
        this.addSelectableChild(this.searchBox);
        this.setInitialFocus(this.searchBox);

        int y = this.height - LIST_BOTTOM_MARGIN + 6;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Enable all"),
                b -> ParticleConfig.enableAll(this.allIds)).dimensions(8, y, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Disable all"),
                b -> ParticleConfig.disableAll()).dimensions(112, y, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
                .dimensions(this.width - 108, y, 100, 20).build());
    }

    private void applyFilter(String query) {
        String q = query.toLowerCase();
        if (q.isBlank()) {
            this.filteredIds = this.allIds;
        } else {
            List<Identifier> result = new ArrayList<>();
            for (Identifier id : this.allIds) {
                if (id.toString().toLowerCase().contains(q)) {
                    result.add(id);
                }
            }
            this.filteredIds = result;
        }
        this.scrollOffset = 0;
    }

    private int listBottom() {
        return this.height - LIST_BOTTOM_MARGIN;
    }

    private double maxScroll() {
        int contentHeight = this.filteredIds.size() * ROW_HEIGHT;
        int viewHeight = listBottom() - LIST_TOP;
        return Math.max(0, contentHeight - viewHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.fill(4, LIST_TOP, this.width - 4, listBottom(), 0x66000000);
        context.enableScissor(4, LIST_TOP, this.width - 4, listBottom());

        int startIndex = Math.max(0, (int) (scrollOffset / ROW_HEIGHT));
        int y = LIST_TOP - (int) (scrollOffset % ROW_HEIGHT);
        for (int i = startIndex; i < this.filteredIds.size() && y < listBottom(); i++) {
            Identifier id = this.filteredIds.get(i);
            boolean on = ParticleConfig.isEnabled(id);

            context.drawText(this.textRenderer, Text.literal(id.toString()), 10, y + 6, 0xFFFFFF, false);
            int badgeColor = on ? 0xFF3CB878 : 0xFF888888;
            context.fill(this.width - 60, y + 3, this.width - 12, y + ROW_HEIGHT - 3, badgeColor);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(on ? "ON" : "OFF"),
                    this.width - 36, y + 6, 0x000000);

            y += ROW_HEIGHT;
        }

        context.disableScissor();

        this.searchBox.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 6, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= LIST_TOP && mouseY <= listBottom() && mouseX >= 4 && mouseX <= this.width - 4) {
            int relativeY = (int) (mouseY - LIST_TOP + scrollOffset);
            int index = relativeY / ROW_HEIGHT;
            if (index >= 0 && index < this.filteredIds.size()) {
                Identifier id = this.filteredIds.get(index);
                ParticleConfig.setEnabled(id, !ParticleConfig.isEnabled(id));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollOffset -= verticalAmount * ROW_HEIGHT * 2;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll()));
        return true;
    }

    @Override
    public void close() {
        ParticleConfig.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

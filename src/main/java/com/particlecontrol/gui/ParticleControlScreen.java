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

    private static final int TOP_PADDING = 48;
    private static final int BOTTOM_PADDING = 32;

    private final Screen parent;
    private List<Identifier> allIds;
    private ParticleListWidget list;
    private TextFieldWidget searchBox;

    public ParticleControlScreen(Screen parent) {
        super(Text.literal("Particle control"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.allIds = new ArrayList<>(Registries.PARTICLE_TYPE.getIds());
        this.allIds.sort(Comparator.comparing(Identifier::toString));

        this.list = new ParticleListWidget(this.client, this.width, this.height - TOP_PADDING - BOTTOM_PADDING, TOP_PADDING, 22);
        this.list.setEntries(this.allIds);
        this.addSelectableChild(this.list);

        this.searchBox = new TextFieldWidget(this.textRenderer, 8, 20, this.width - 16, 20, Text.literal("Search"));
        this.searchBox.setPlaceholder(Text.literal("Search particles..."));
        this.searchBox.setChangedListener(query -> filter(query));
        this.addSelectableChild(this.searchBox);
        this.setInitialFocus(this.searchBox);

        int y = this.height - BOTTOM_PADDING + 6;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Enable all"), b -> {
            ParticleConfig.enableAll(this.allIds);
            this.list.setEntries(currentFiltered());
        }).dimensions(8, y, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Disable all"), b -> {
            ParticleConfig.disableAll();
            this.list.setEntries(currentFiltered());
        }).dimensions(112, y, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
                .dimensions(this.width - 108, y, 100, 20).build());
    }

    private List<Identifier> currentFiltered() {
        String query = this.searchBox == null ? "" : this.searchBox.getText().toLowerCase();
        if (query.isBlank()) {
            return this.allIds;
        }
        return this.allIds.stream().filter(id -> id.toString().toLowerCase().contains(query)).toList();
    }

    private void filter(String query) {
        this.list.setEntries(currentFiltered());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        this.list.render(context, mouseX, mouseY, delta);
        this.searchBox.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 6, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
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

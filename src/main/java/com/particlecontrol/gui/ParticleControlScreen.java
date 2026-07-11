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

    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 48;
    private static final int LIST_BOTTOM_MARGIN = 60;

    private final Screen parent;
    private List<Identifier> allIds;
    private List<Identifier> filteredIds;
    private TextFieldWidget searchBox;
    private final List<ButtonWidget> rowButtons = new ArrayList<>();
    private Text pageLabel = Text.literal("");
    private int currentPage = 0;
    private int pageSize = 10;

    public ParticleControlScreen(Screen parent) {
        super(Text.literal("Particle control"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.allIds = new ArrayList<>(Registries.PARTICLE_TYPE.getIds());
        this.allIds.sort(Comparator.comparing(Identifier::toString));
        this.filteredIds = this.allIds;
        this.currentPage = 0;

        int listBottom = this.height - LIST_BOTTOM_MARGIN;
        this.pageSize = Math.max(1, (listBottom - LIST_TOP) / ROW_HEIGHT);

        this.searchBox = new TextFieldWidget(this.textRenderer, 8, 20, this.width - 16, 20, Text.literal("Search"));
        this.searchBox.setPlaceholder(Text.literal("Search particles..."));
        this.searchBox.setChangedListener(this::applyFilter);
        this.addDrawableChild(this.searchBox);
        this.setInitialFocus(this.searchBox);

        int bottomY = this.height - LIST_BOTTOM_MARGIN + 30;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Enable all"),
                b -> { ParticleConfig.enableAll(this.allIds); rebuildRows(); })
                .dimensions(8, bottomY, 90, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Disable all"),
                b -> { ParticleConfig.disableAll(); rebuildRows(); })
                .dimensions(102, bottomY, 90, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("< Prev"),
                b -> { if (this.currentPage > 0) { this.currentPage--; rebuildRows(); } })
                .dimensions(this.width - 218, bottomY, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Next >"),
                b -> { if ((this.currentPage + 1) * this.pageSize < this.filteredIds.size()) { this.currentPage++; rebuildRows(); } })
                .dimensions(this.width - 154, bottomY, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> this.close())
                .dimensions(this.width - 88, bottomY, 80, 20).build());

        rebuildRows();
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
        this.currentPage = 0;
        rebuildRows();
    }

    private void rebuildRows() {
        for (ButtonWidget button : this.rowButtons) {
            this.remove(button);
        }
        this.rowButtons.clear();

        int maxPage = Math.max(0, (this.filteredIds.size() - 1) / this.pageSize);
        if (this.currentPage > maxPage) {
            this.currentPage = maxPage;
        }

        int start = this.currentPage * this.pageSize;
        int end = Math.min(this.filteredIds.size(), start + this.pageSize);

        int y = LIST_TOP;
        for (int i = start; i < end; i++) {
            Identifier id = this.filteredIds.get(i);
            ButtonWidget button = ButtonWidget.builder(rowLabel(id), b -> {
                ParticleConfig.setEnabled(id, !ParticleConfig.isEnabled(id));
                b.setMessage(rowLabel(id));
            }).dimensions(8, y, this.width - 16, 20).build();
            this.addDrawableChild(button);
            this.rowButtons.add(button);
            y += ROW_HEIGHT;
        }

        int totalPages = Math.max(1, (this.filteredIds.size() + this.pageSize - 1) / this.pageSize);
        this.pageLabel = Text.literal("Page " + (this.currentPage + 1) + " / " + totalPages
                + "  (" + this.filteredIds.size() + " particles)");
    }

    private Text rowLabel(Identifier id) {
        boolean on = ParticleConfig.isEnabled(id);
        return Text.literal((on ? "[ON]  " : "[off] ") + id);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 6, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, this.pageLabel, this.width / 2,
                this.height - LIST_BOTTOM_MARGIN + 8, 0xAAAAAA);
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

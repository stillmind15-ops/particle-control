package com.particlecontrol.gui;

import com.particlecontrol.config.ParticleConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ParticleListWidget extends ElementListWidget<ParticleListWidget.Entry> {

    public ParticleListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.setRenderHeader(false, 0);
    }

    public void setEntries(List<Identifier> ids) {
        this.clearEntries();
        for (Identifier id : ids) {
            this.addEntry(new Entry(id));
        }
    }

    public class Entry extends ElementListWidget.Entry<Entry> {
        private final Identifier id;
        private final ButtonWidget toggleButton;

        public Entry(Identifier id) {
            this.id = id;
            this.toggleButton = ButtonWidget.builder(label(), b -> {
                boolean newValue = !ParticleConfig.isEnabled(id);
                ParticleConfig.setEnabled(id, newValue);
                b.setMessage(label());
            }).dimensions(0, 0, 70, 18).build();
        }

        private Text label() {
            boolean on = ParticleConfig.isEnabled(id);
            return Text.literal(on ? "ON" : "OFF");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                            int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(id.toString()),
                    x + 4, y + entryHeight / 2 - 4, 0xFFFFFF, false);
            this.toggleButton.setX(x + entryWidth - 74);
            this.toggleButton.setY(y + (entryHeight - 18) / 2);
            this.toggleButton.render(context, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends net.minecraft.client.gui.Element> children() {
            return List.of(toggleButton);
        }

        @Override
        public List<? extends net.minecraft.client.gui.Selectable> selectableChildren() {
            return List.of(toggleButton);
        }

        public Identifier getId() {
            return id;
        }
    }
}

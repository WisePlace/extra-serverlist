package extraserverlist.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AddTagScreen extends Screen {

    private static final int[] COLOR_PALETTE = {
            0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF43A047,
            0xFF1E88E5, 0xFF8E24AA, 0xFFD81B60, 0xFF757575
    };
    private static final int SWATCH_SIZE = 18;

    private final Screen parent;
    private final String serverAddress;
    private TextFieldWidget tagNameField;
    private int selectedColor = COLOR_PALETTE[4];
    private String errorMessage = null;

    private final List<int[]> swatchBounds = new ArrayList<>();

    public AddTagScreen(Screen parent, String serverAddress) {
        super(Text.literal("Add Tag"));
        this.parent = parent;
        this.serverAddress = serverAddress;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        tagNameField = new TextFieldWidget(this.textRenderer, centerX - 75, centerY - 40, 150, 20, Text.literal("Tag name"));
        tagNameField.setMaxLength(8);
        this.addDrawableChild(tagNameField);
        this.setInitialFocus(tagNameField);

        swatchBounds.clear();
        int totalWidth = COLOR_PALETTE.length * (SWATCH_SIZE + 4) - 4;
        int startX = centerX - totalWidth / 2;
        int swatchY = centerY - 10;

        for (int i = 0; i < COLOR_PALETTE.length; i++) {
            swatchBounds.add(new int[]{startX + i * (SWATCH_SIZE + 4), swatchY, COLOR_PALETTE[i]});
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
            String tagName = tagNameField.getText().trim();
            if (tagName.isEmpty()) {
                errorMessage = "Enter a tag name";
                return;
            }

            TagManager.AddResult result = TagManager.addTag(serverAddress, tagName, selectedColor);
            switch (result) {
                case SUCCESS -> this.client.setScreen(parent);
                case DUPLICATE -> errorMessage = "Tag already exists";
                case MAX_REACHED -> errorMessage = "Max 5 tags reached";
            }
        }).dimensions(centerX - 75, centerY + 25, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> this.client.setScreen(parent))
                .dimensions(centerX + 5, centerY + 25, 70, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, this.width, this.height, 0xC0101010);
        super.render(context, mouseX, mouseY, deltaTicks);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(errorMessage).formatted(net.minecraft.util.Formatting.RED), this.width / 2, this.height / 2 + 50, 0xFFFFFFFF);
        }
        for (int[] swatch : swatchBounds) {
            int x = swatch[0];
            int y = swatch[1];
            int color = swatch[2];
            context.fill(x, y, x + SWATCH_SIZE, y + SWATCH_SIZE, color);
            if (color == selectedColor) {
                int bx = x - 2;
                int by = y - 2;
                int bw = SWATCH_SIZE + 4;
                int bh = SWATCH_SIZE + 4;
                int borderColor = 0xFFFFFFFF;
                context.fill(bx, by, bx + bw, by + 1, borderColor);       // top edge
                context.fill(bx, by + bh - 1, bx + bw, by + bh, borderColor); // bottom edge
                context.fill(bx, by, bx + 1, by + bh, borderColor);       // left edge
                context.fill(bx + bw - 1, by, bx + bw, by + bh, borderColor); // right edge
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        for (int[] swatch : swatchBounds) {
            int x = swatch[0];
            int y = swatch[1];
            if (mouseX >= x && mouseX <= x + SWATCH_SIZE && mouseY >= y && mouseY <= y + SWATCH_SIZE) {
                selectedColor = swatch[2];
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }
}
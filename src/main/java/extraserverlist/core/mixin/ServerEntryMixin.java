package extraserverlist.core.mixin;

import extraserverlist.core.AddTagScreen;
import extraserverlist.core.ExtraServerList;
import extraserverlist.core.TagManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class ServerEntryMixin extends MultiplayerServerListWidget.Entry {

    @Shadow
    public abstract ServerInfo getServer();

    @Unique
    private static final int PLUS_SIZE = 11;

    @Unique
    private int extraserverlist$plusX;
    @Unique
    private int extraserverlist$plusY;

    // Parallel lists: bounds of each currently-drawn tag pill, and the tag name it belongs to.
    @Unique
    private final List<int[]> extraserverlist$tagPillBounds = new ArrayList<>();
    @Unique
    private final List<String> extraserverlist$tagPillNames = new ArrayList<>();

    @Inject(method = "render", at = @At("TAIL"))
    private void extraserverlist$renderTags(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks, CallbackInfo ci) {
        if (!ExtraServerList.customListEnabled) {
            return;
        }

        ServerInfo server = this.getServer();
        if (server == null || server.address == null) {
            return;
        }

        extraserverlist$tagPillBounds.clear();
        extraserverlist$tagPillNames.clear();

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int x = this.getContentX() + 1; // under the icon now, instead of next to the name
        int y = this.getContentY() + 34;

        int iconSize = 8;
        int iconY = y + 1;
        context.fill(x, iconY, x + iconSize, iconY + iconSize, 0xFFCCCCCC);
        context.fill(x + 1, iconY + 1, x + 3, iconY + 3, 0xFF1A1A1A);
        x += iconSize + 4;

        List<TagManager.TagData> tags = TagManager.getTags(server.address);
        for (TagManager.TagData tag : tags) {
            int pillWidth = textRenderer.getWidth(tag.name) + 6;
            int pillHeight = 10;

            context.fill(x, y, x + pillWidth, y + pillHeight, tag.color);
            context.drawTextWithShadow(textRenderer, tag.name, x + 3, y + 1, 0xFFFFFFFF);

            extraserverlist$tagPillBounds.add(new int[]{x, y, pillWidth, pillHeight});
            extraserverlist$tagPillNames.add(tag.name);

            x += pillWidth + 3;
        }

        if (tags.size() < 5) {
            extraserverlist$plusX = x;
            extraserverlist$plusY = y;
            context.fill(x, y, x + PLUS_SIZE, y + PLUS_SIZE, 0xAA4CAF50);
            context.drawTextWithShadow(textRenderer, "+", x + 3, y + 2, 0xFFFFFFFF);
        } else {
            extraserverlist$plusX = -1000;
            extraserverlist$plusY = -1000;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void extraserverlist$onClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (!ExtraServerList.customListEnabled) {
            return;
        }

        int mouseX = (int) click.x();
        int mouseY = (int) click.y();
        ServerInfo server = this.getServer();
        if (server == null || server.address == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;

        // Check the "+" button first.
        if (mouseX >= extraserverlist$plusX && mouseX <= extraserverlist$plusX + PLUS_SIZE
                && mouseY >= extraserverlist$plusY && mouseY <= extraserverlist$plusY + PLUS_SIZE) {
            client.setScreen(new AddTagScreen(currentScreen, server.address));
            cir.setReturnValue(true);
            return;
        }

        // Then check each tag pill.
        for (int i = 0; i < extraserverlist$tagPillBounds.size(); i++) {
            int[] bounds = extraserverlist$tagPillBounds.get(i);
            String tagName = extraserverlist$tagPillNames.get(i);

            if (mouseX >= bounds[0] && mouseX <= bounds[0] + bounds[2]
                    && mouseY >= bounds[1] && mouseY <= bounds[1] + bounds[3]) {
                client.setScreen(new ConfirmScreen(
                        confirmed -> {
                            if (confirmed) {
                                TagManager.removeTag(server.address, tagName);
                            }
                            client.setScreen(currentScreen);
                        },
                        Text.literal("Remove Tag"),
                        Text.literal("Remove tag \"" + tagName + "\"?"),
                        Text.literal("Remove"),
                        Text.literal("Cancel")
                ));
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
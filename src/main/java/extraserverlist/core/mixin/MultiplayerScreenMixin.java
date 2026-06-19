package extraserverlist.core.mixin;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin {

    @ModifyConstant(method = "init", constant = @Constant(intValue = 36))
    private int extraserverlist$tallerRows(int original) {
        return 54;
    }
}
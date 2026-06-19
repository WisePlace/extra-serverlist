package extraserverlist.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;

public class ExtraServerList implements ClientModInitializer {
    public static final String MOD_ID = "extra-serverlist";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean customListEnabled = true;

    private enum SearchCriteria {
        NAME, ADDRESS, TAG
    }

    private static SearchCriteria currentCriteria = SearchCriteria.NAME;

    private static ButtonWidget toggleButton;
    private static ButtonWidget criteriaButton;
    private static TextFieldWidget searchField;

    @Override
    public void onInitializeClient() {
        TagManager.load();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof MultiplayerScreen) {
                rebuildToggleButton(client, screen, scaledWidth);
                rebuildSearchRow(client, screen, scaledWidth);
                applyFilter(client, screen);
            }
        });
    }

    private static void rebuildToggleButton(MinecraftClient client, Screen screen, int scaledWidth) {
        List<ClickableWidget> widgets = Screens.getButtons(screen);
        widgets.remove(toggleButton);

        toggleButton = ButtonWidget.builder(
                getToggleText(),
                button -> {
                    customListEnabled = !customListEnabled;
                    button.setMessage(getToggleText());
                    LOGGER.info("Custom server list toggled: {}", customListEnabled);
                    rebuildSearchRow(client, screen, scaledWidth);
                    applyFilter(client, screen);
                }
        ).dimensions(scaledWidth - 110, 5, 100, 20).build();

        widgets.add(toggleButton);
    }

    private static void rebuildSearchRow(MinecraftClient client, Screen screen, int scaledWidth) {
        List<ClickableWidget> widgets = Screens.getButtons(screen);
        widgets.remove(criteriaButton);
        widgets.remove(searchField);

        if (!customListEnabled) {
            criteriaButton = null;
            searchField = null;
            return;
        }

        int rightEdge = scaledWidth - 120;

        criteriaButton = ButtonWidget.builder(
                Text.literal(currentCriteria.name()),
                button -> {
                    currentCriteria = SearchCriteria.values()[
                            (currentCriteria.ordinal() + 1) % SearchCriteria.values().length
                    ];
                    button.setMessage(Text.literal(currentCriteria.name()));
                    applyFilter(client, screen);
                }
        ).dimensions(10, 5, 70, 20).build();

        searchField = new TextFieldWidget(
                client.textRenderer, 90, 5, rightEdge - 90, 20, Text.literal("Search")
        );
        searchField.setPlaceholder(Text.literal("Search..."));
        searchField.setChangedListener(text -> applyFilter(client, screen));

        widgets.add(criteriaButton);
        widgets.add(searchField);
    }

    private static MultiplayerServerListWidget findServerListWidget(Screen screen) {
        for (ClickableWidget widget : Screens.getButtons(screen)) {
            if (widget instanceof MultiplayerServerListWidget listWidget) {
                return listWidget;
            }
        }
        return null;
    }

    private static void applyFilter(MinecraftClient client, Screen screen) {
        MultiplayerServerListWidget listWidget = findServerListWidget(screen);
        if (listWidget == null) {
            return;
        }

        ServerList realList = new ServerList(client);
        realList.loadFile();

        if (!customListEnabled || searchField == null || searchField.getText().isEmpty()) {
            listWidget.setServers(realList);
            listWidget.setScrollY(0);
            return;
        }

        String query = searchField.getText().toLowerCase();
        ServerList filtered = new ServerList(client);

        for (int i = 0; i < realList.size(); i++) {
            ServerInfo server = realList.get(i);
            boolean matches = switch (currentCriteria) {
                case NAME -> server.name != null && server.name.toLowerCase().contains(query);
                case ADDRESS -> server.address != null && server.address.toLowerCase().contains(query);
                case TAG -> server.address != null && TagManager.getTags(server.address).stream()
                        .anyMatch(tag -> tag.name.toLowerCase().contains(query));
            };

            if (matches) {
                filtered.add(server, false);
            }
        }

        listWidget.setServers(filtered);
        listWidget.setScrollY(0);
    }

    private static Text getToggleText() {
        return Text.literal("Custom List: " + (customListEnabled ? "ON" : "OFF"));
    }
}
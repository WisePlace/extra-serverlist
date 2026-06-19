# Extra ServerList

A [Fabric](https://fabricmc.net/) client-side mod for Minecraft that enhances the vanilla Multiplayer server list with search, filtering, and custom tags — no more scrolling through a long list to find the server you want.

## Features

- **Toggle button** — switch between the enhanced list and the normal vanilla one at any time, from the Multiplayer screen itself.
- **Search bar** — filter your server list live as you type.
- **Search criteria** — search by server **Name**, **Address**, or **Tag**, switchable with one click.
- **Custom tags** — label your servers however you like (e.g. `PVP`, `SKYBLOCK`, `SMP`), each with its own color, chosen from 8 preset colors.
- **One-click tag management** — click the `+` icon under a server to add a tag, click an existing tag to remove it.

All of this is layered directly on top of the vanilla server list — your real `servers.dat` file is never modified by searching or filtering, and tags are stored completely separately.

## Screenshots

*(add screenshots here)*

## Requirements

- Minecraft **1.21.11**
- [Fabric Loader](https://fabricmc.net/use/) **0.19.3** or newer
- [Fabric API](https://modrinth.com/mod/fabric-api) (required dependency)

## Installation

1. Install the [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.11 if you haven't already.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 and place the `.jar` in your `mods` folder.
3. Download the latest `Extra-ServerList-*.jar` from the [Releases](../../releases) page of this repository.
4. Place it in your `.minecraft/mods` folder.
5. Launch Minecraft using the Fabric profile.

## Building from source

If you'd like to modify or build the mod yourself:

```bash
git clone https://github.com/LukaLaurent/extra-serverlist.git
cd extra-serverlist
./gradlew build
```

The built jar will be in `build/libs/`. To run a test instance directly:

```bash
./gradlew runClient
```

## License

Released under [CC0-1.0](LICENSE) — free to use, modify, and redistribute.

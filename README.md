![boatism_thumbnail](external/promo/boatism_thumb_v1.png)

<div style="text-align: center;">
<h1>
    <a href="https://modrinth.com/mod/boatism">
        <img alt="modrinth" src="https://img.shields.io/badge/-modrinth-gray?style=for-the-badge&labelColor=green&labelWidth=15&logo=appveyor&logoColor=white">
    </a>
    <a href="https://github.com/JR1811/boatism/releases">
        <img alt="github" src="https://img.shields.io/github/v/release/JR1811/boatism?logo=github&style=for-the-badge">
    </a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/boatism">
        <img alt="curseforge" src="https://img.shields.io/badge/-CurseForge-gray?style=for-the-badge&logo=curseforge&labelColor=orange">
    </a>
</h1>
</div>

# Boatism Fabric Mod for Minecraft

Embark on a revolutionary journey across the seas with Boatism,
a Fabric mod designed to improve your boating adventures in Minecraft!
Say goodbye to slow and tedious boat rides and embrace the thrill of high-speed travel
with the new and modular boat engine features.

<div style="text-align: center;">
<br>
<a href="https://github.com/JR1811/boatism"><img
    src="external/promo/base_engine_alt.png"
    alt="Boatism Engine"
    width="300"
></a>
</div>

## Features

1. Add new Boat Engine Entities to your boats. Simply hook them on your favourite Boat, fill up the gas and start the
   engine.
2. Travel faster by sea than ever before. Engine `Power Level 5` already exceeds the vanilla boat speed!
3. Keep an eye and an ear out for your engine. Continuous high stress may cause unforeseen accidents!
4. Upgrade your engine with special custom parts.
    - Your fuel capacity is too small? Strap on an extra canister!
    - Is your engine overheating too fast? How about some better cooling options?
    - Your engine can't take a beating? Put on some plating, and it will tank any fall!
5. The engine's back-end code is built with mod compatibility in mind. Try hooking it up with custom mod boats and let
   us know how it went!

## For Developers

Boatism strives to improve compatibility with other mods!

For now every Entity, which extends from the BoatEntity class, is eligible
to hook up the engine and should need no extra implementations to work as intended.

If your boat has a different size, compared to the default boats, you might want
to adjust the relative engine position or if you implemented custom speed and / or velocity handling you might have to
adjust how you handle them using the data which the engine's handler can provide.
Check out the [API package](https://github.com/JR1811/Boatism/tree/master/src/main/java/net/shirojr/boatism/api)
for that.

## Current state and plans for the future

The mod is, as of now, in a stable state and can be experimented with.
Due to a tight schedule, a lot of balancing and bug testing still has to be done, so make sure to let us know about
possible issues
or feature requests in the [GitHub issues](https://github.com/JR1811/Boatism/issues).

## Contributors

Thank you to...

- [@Globox1997](https://github.com/Globox1997) (dev and ideas)
- [@0xJoeMama](https://github.com/0xJoeMama) (dev and ideas)
- Apfelrunder (ideas)
- the people from ModFest 1.20

... and many more for your help!


<div style="text-align: center;">
<br>
<a href="https://fabricmc.net/"><img
    src="external/promo/fabric_supported.png"
    alt="Supported on Fabric"
    width="200"
></a>
<a href="https://modfest.net/1.20"><img
    src="external/promo/badges/created_for_modfest_1_20_long.png"
    alt="Supported on Fabric"
    width="200"
></a>
<a href="https://github.com/JR1811/Boatism/issues"><img
    src="external/promo/badges/work_in_progress.png"
    alt="Work in Progress"
    width="200"
></a>
</div>

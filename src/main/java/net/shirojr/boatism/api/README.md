# Adding compatibility with Boatism's API

Boatism has compatibility with any Entity which extends from the `BoatEntity` by default!


Here you can find all the utilities to adjust and add your content to Boatism.

| Class                                                                                                                                             | description                                                                                                                            |
|---------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| [BoatEngineComponent](https://github.com/JR1811/Boatism/blob/master/src/main/java/net/shirojr/boatism/api/BoatEngineComponent.java)               | Improve or punish engine performance with your custom Items                                                                            |
| [CustomBoatEngineAttachment](https://github.com/JR1811/Boatism/blob/master/src/main/java/net/shirojr/boatism/api/CustomBoatEngineAttachment.java) | Adjust the relative attachment position of the Engine if your custom Boat has a different size compared to default Boat Entities       |
| [BoatEngineCoupler](https://github.com/JR1811/Boatism/blob/master/src/main/java/net/shirojr/boatism/api/BoatEngineCoupler.java)                   | Get a `BoatEngineEntity` from a `BoatEntity` or set a new hooked boat entry by casting a (custom) `BoatEntity` class to this interface |

---

If you have any questions or requests, use the [GitHub Issues](https://github.com/JR1811/Boatism/issues) page.

## Read engine power level and thrust manually

If you are changing the default boat speed and velocity behaviour, it is likely that you are using the same Mixin as Boatism
does. If you are having trouble with the engine there you can keep your velocity calculation and get the engine
data manually by casting to the `BoatEngineCoupler` interface on your custom Boat Entity.
This will give you access to the linked *(a.k.a. hooked)* `BoatEngineEntity`.

Information about the engine's performance is handled in the `BoatEngineEntity`'s `BoatEngineHandler` which is updated
based on the `BoatEngineEntity`'s ticks. Many values, such as boat passenger count, equipment and other factors are
used to calculate the values, which you can access using the `BoatEngineHandler`'s methods.

Keep in mind, that the power level is an arbitrary number of power that the boat is set to by the player.
This will calculate the final thrust value by looking at things like passenger count, equipped items, etc.

In Boatism's default implementation, the thrust value is applied in the `BoatEntity`'s `updatePaddles()` method (yarn)
like this:

```java
// balancing the 0-9 power level value
float powerLevel = boatEngine.getPowerLevel() * 0.008f;
// applying additional thrust modifiers from the engine
float thrust = baseSpeed + (powerLevel * boatEngine.getEngineHandler().calculateThrustModifier(boatEntity));
```

This method is used for a single float value, but extending it with simple vector math should make this viable even for
custom boat implementations which need their velocity (with e.g. Vec3d) changed instead.

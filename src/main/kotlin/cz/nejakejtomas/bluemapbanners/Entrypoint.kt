package cz.nejakejtomas.bluemapbanners

import de.bluecolored.bluemap.api.BlueMapAPI

private val toInit = listOf(ImageRegistry, MarkerAPI)

@Suppress("unused")
fun init() {
    BlueMapAPI.onEnable { api: BlueMapAPI ->
        toInit.forEach { it.initialize(api) }
    }

    BlueMapAPI.onDisable { api: BlueMapAPI ->
        toInit.asReversed().forEach { it.stop(api) }
    }
}
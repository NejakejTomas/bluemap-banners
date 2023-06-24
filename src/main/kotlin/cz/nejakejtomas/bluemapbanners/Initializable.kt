package cz.nejakejtomas.bluemapbanners

import de.bluecolored.bluemap.api.BlueMapAPI

interface Initializable {
    fun initialize(api: BlueMapAPI)
    val isInitialised: Boolean
    fun stop(api: BlueMapAPI)
}
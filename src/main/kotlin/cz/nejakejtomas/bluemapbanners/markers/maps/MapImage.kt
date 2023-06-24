package cz.nejakejtomas.bluemapbanners.markers.maps

import com.flowpowered.math.GenericMath.clamp
import cz.nejakejtomas.bluemapbanners.markers.MarkerImage
import cz.nejakejtomas.bluemapbanners.utils.asStream
import cz.nejakejtomas.bluemapbanners.utils.scaled
import cz.nejakejtomas.bluemapbanners.utils.sha512Base64
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths


class MapImage(image: BufferedImage, val scaleFactor: Double = 1.0) : MarkerImage {
    private val image = image.scaled(scaleFactor, scaleFactor)

    override val location: Path
        get() =
            Paths.get("Maps").resolve("${(sizeX * scaleFactor).toInt()}_${(sizeY * scaleFactor).toInt()}")
    override val fileName get() = "${stream.use { it.sha512Base64() }}.png"
    override val anchorX get() = ((sizeX / 2) * scaleFactor).toInt()
    override val anchorY get() = ((sizeY / 2) * scaleFactor).toInt()
    override val stream: InputStream get() = image.asStream()

    fun userAnchorX(userX: Float) = ((sizeX * (clamp(userX.toDouble(), 0.0, 1.0)) * scaleFactor)).toInt()
    fun userAnchorY(userY: Float) = ((sizeY * (clamp(userY.toDouble(), 0.0, 1.0)) * scaleFactor)).toInt()

    companion object {
        const val sizeX = 128
        const val sizeY = 128
    }
}
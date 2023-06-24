package cz.nejakejtomas.bluemapbanners.markers.maps

import cz.nejakejtomas.bluemapbanners.ImageCache
import cz.nejakejtomas.bluemapbanners.ImageRegistry
import cz.nejakejtomas.bluemapbanners.markers.Marker
import cz.nejakejtomas.bluemapbanners.markers.MarkerVisitor
import cz.nejakejtomas.bluemapbanners.utils.rawData
import cz.nejakejtomas.bluemapbanners.utils.replaced
import de.bluecolored.bluemap.api.markers.POIMarker
import net.minecraft.block.MapColor
import java.awt.image.BufferedImage


class Map private constructor(
    label: String,
    positionX: Int,
    positionY: Int,
    positionZ: Int,
    dimension: String,
    val anchorX: Float?,
    val anchorY: Float?,
    val transparencyColor: Int?,
    image: BufferedImage
) : Marker(label, positionX, positionY, positionZ, dimension) {
    override val category: String = "Maps"
    val rawImage = cache.cache(image)

    private val image
        get() =
            if (transparencyColor == null) rawImage
            else rawImage.replaced(transparencyColor, 0x00000000, 0x00FFFFFF)
    private val image0 get() = MapImage(image, 0.25)
    private val image1 get() = MapImage(image, 0.5)
    private val image2 get() = MapImage(image)

    override fun addToBuilder(index: Int, builder: POIMarker.Builder) {
        super.addToBuilder(index, builder)

        when (index) {
            0 -> {
                builder
                    .icon(
                        ImageRegistry(image0),
                        if (anchorX != null) image0.userAnchorX(anchorX) else image0.anchorX,
                        if (anchorY != null) image0.userAnchorY(anchorY) else image0.anchorY
                    )
                    .minDistance(1000.0)
            }

            1 -> {
                builder
                    .icon(
                        ImageRegistry(image1),
                        if (anchorX != null) image1.userAnchorX(anchorX) else image1.anchorX,
                        if (anchorY != null) image1.userAnchorY(anchorY) else image1.anchorY
                    )
                    .maxDistance(1000.0)
                    .minDistance(100.0)
            }

            2 -> {
                builder
                    .icon(
                        ImageRegistry(image2),
                        if (anchorX != null) image2.userAnchorX(anchorX) else image2.anchorX,
                        if (anchorY != null) image2.userAnchorY(anchorY) else image2.anchorY
                    )
                    .maxDistance(100.0)
            }
        }
    }

    override val markerCount = 3

    override fun <Out, Context> accept(visitor: MarkerVisitor<Out, Context>, context: Context): Out =
        visitor.visit(this, context)

    companion object {
        private val cache = ImageCache()

        private fun fromMinecraftData(data: ByteArray): BufferedImage {
            val bufferedImage = BufferedImage(MapImage.sizeX, MapImage.sizeY, BufferedImage.TYPE_INT_ARGB)
            val imageData = bufferedImage.rawData

            for (y in 0 until MapImage.sizeY) {
                for (x in 0 until MapImage.sizeX) {
                    val index = x + y * MapImage.sizeX
                    val color = getRenderColorARGB(data[index])

                    imageData[index] = color
                }
            }

            return bufferedImage
        }

        private fun getRenderColorARGB(minecraftColor: Byte): Int {
            val color = MapColor.getRenderColor(minecraftColor.toInt())
            // Color is ABGR
            val alpha = color and 0xFF000000u.toInt()
            val red = color and 0x000000FFu.toInt() shl 16
            val green = color and 0x0000FF00u.toInt()
            val blue = color and 0x00FF0000u.toInt() shr 16

            return alpha or red or green or blue
        }

        fun fromBufferedImage(
            label: String,
            positionX: Int, positionY: Int, positionZ:
            Int, dimension:
            String, anchorX: Float?, anchorY: Float?,
            transparencyColor: Int?,
            image: BufferedImage
        ): Map {

            return Map(
                label,
                positionX, positionY, positionZ,
                dimension,
                anchorX, anchorY,
                transparencyColor,
                image
            )
        }

        fun fromMinecraftData(
            label: String,
            positionX: Int, positionY: Int, positionZ:
            Int, dimension:
            String, anchorX: Float?, anchorY: Float?,
            transparencyColor: Int?,
            minecraftData: ByteArray
        ): Map {

            return Map(
                label,
                positionX, positionY, positionZ,
                dimension,
                anchorX, anchorY,
                transparencyColor,
                fromMinecraftData(minecraftData)
            )
        }
    }
}
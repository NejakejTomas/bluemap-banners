package cz.nejakejtomas.bluemapbanners.markers.banners

import cz.nejakejtomas.bluemapbanners.ImageRegistry
import cz.nejakejtomas.bluemapbanners.markers.Marker
import cz.nejakejtomas.bluemapbanners.markers.MarkerVisitor
import cz.nejakejtomas.bluemapbanners.utils.MinecraftColor
import de.bluecolored.bluemap.api.markers.POIMarker

class Banner(
    label: String,
    positionX: Int,
    positionY: Int,
    positionZ: Int,
    dimension: String,
    val color: MinecraftColor,
    val patterns: List<Pattern>
) : Marker(label, positionX, positionY, positionZ, dimension) {
    override fun addToBuilder(index: Int, builder: POIMarker.Builder) {
        super.addToBuilder(index, builder)

        when (index) {
            0 -> {
                val image = BannerImage.byColor[color]!!
                builder
                    .icon(ImageRegistry(image), image.anchorX, image.anchorY)
                if (patterns.isNotEmpty()) builder.minDistance(100.0)
            }

            1 -> {
                val image = PatternedBannerImage(color, patterns)
                builder
                    .maxDistance(200.0)
                    .icon(ImageRegistry(image), image.anchorX, image.anchorY)
            }
        }
    }

    override val markerCount = if (patterns.isEmpty()) 1 else 2
    override val category: String = "Banners"
    override fun <Out, Context> accept(visitor: MarkerVisitor<Out, Context>, context: Context): Out =
        visitor.visit(this, context)
}
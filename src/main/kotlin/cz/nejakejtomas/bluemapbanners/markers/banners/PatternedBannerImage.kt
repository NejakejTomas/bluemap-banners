package cz.nejakejtomas.bluemapbanners.markers.banners

import cz.nejakejtomas.bluemapbanners.markers.MarkerImage
import cz.nejakejtomas.bluemapbanners.utils.MinecraftColor
import cz.nejakejtomas.bluemapbanners.utils.asStream
import cz.nejakejtomas.bluemapbanners.utils.scaled
import cz.nejakejtomas.bluemapbanners.utils.shaded
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

class PatternedBannerImage(val color: MinecraftColor, val patterns: List<Pattern>) : MarkerImage {
    override val location: Path get() = Paths.get("Banners").resolve("Close")
    override val fileName: String
        get() {
            val builder = StringBuilder()

            builder.append(color)

            patterns.forEach {
                builder.append("_${it.type}-${it.color}")
            }

            builder.append(".png")

            return builder.toString()
        }
    override val anchorX get() = (sizeX / 2) * scaleFactor
    override val anchorY get() = sizeY * scaleFactor
    override val stream: InputStream get() = image.asStream()
    private val image: BufferedImage
        get() {
            val bufferedImage = BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB)

            // Base "pattern"
            bufferedImage.paint(Pattern(PatternType.Base, color))

            for (pattern: Pattern in patterns) {
                bufferedImage.paint(pattern)
            }

            return bufferedImage.scaled(scaleFactor, scaleFactor)
        }

    companion object {
        private const val scaleFactor = 1
        private const val sizeX = 20
        private const val sizeY = 40
    }
}

private fun BufferedImage.paint(pattern: Pattern) {
    val patternImage = pattern.type.image.shaded(pattern.color)
    this.graphics.drawImage(patternImage, 0, 0, null)
}
package cz.nejakejtomas.bluemapbanners.markers.banners

import cz.nejakejtomas.bluemapbanners.markers.MarkerImage
import cz.nejakejtomas.bluemapbanners.utils.MinecraftColor
import cz.nejakejtomas.bluemapbanners.utils.asStream
import cz.nejakejtomas.bluemapbanners.utils.scaled
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths


enum class BannerImage(private val color: MinecraftColor) : MarkerImage {
    White(MinecraftColor.White),
    Orange(MinecraftColor.Orange),
    Magenta(MinecraftColor.Magenta),
    LightBlue(MinecraftColor.LightBlue),
    Yellow(MinecraftColor.Yellow),
    Lime(MinecraftColor.Lime),
    Pink(MinecraftColor.Pink),
    Gray(MinecraftColor.Gray),
    LightGray(MinecraftColor.LightGray),
    Cyan(MinecraftColor.Cyan),
    Purple(MinecraftColor.Purple),
    Blue(MinecraftColor.Blue),
    Brown(MinecraftColor.Brown),
    Green(MinecraftColor.Green),
    Red(MinecraftColor.Red),
    Black(MinecraftColor.Black),
    ;

    override val location: Path get() = Paths.get("Banners").resolve("Far")
    override val anchorX get() = (sizeX / 2) * scaleFactor
    override val anchorY get() = sizeY * scaleFactor
    private val image: BufferedImage
        get() {
            val bufferedImage = BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB)
            bufferedImage.paint(color)

            return bufferedImage.scaled(scaleFactor, scaleFactor)
        }
    override val stream: InputStream get() = image.asStream()
    override val fileName get() = "$name.png"

    companion object {
        private const val scaleFactor = 3
        private const val sizeX = 6
        private const val sizeY = 8

        val values by lazy { listOf(*BannerImage.values()) }
        val byColor by lazy {
            values.map {
                it.color to it
            }.toMap()
        }
    }
}

private fun BufferedImage.paint(color: MinecraftColor) {
    val black = 0xFF000000u.toInt()
    val transparent = 0x00000000u.toInt()
    for (x in 0 until 6) setRGB(x, 0, black)
    for (y in 1 until 6) {
        setRGB(0, y, transparent)
        setRGB(1, y, black)
        setRGB(2, y, color.color)
        setRGB(3, y, color.color)
        setRGB(4, y, black)
        setRGB(0, y, transparent)
    }
    for (y in 6 until 8) {
        setRGB(0, y, transparent)
        setRGB(1, y, transparent)
        setRGB(2, y, black)
        setRGB(3, y, black)
        setRGB(4, y, transparent)
        setRGB(0, y, transparent)
    }
}
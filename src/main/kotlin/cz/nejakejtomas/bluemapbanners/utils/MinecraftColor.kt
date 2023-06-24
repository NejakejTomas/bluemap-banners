package cz.nejakejtomas.bluemapbanners.utils

import net.minecraft.util.DyeColor

enum class MinecraftColor(val minecraftDyeColor: DyeColor, val color: Int) {
    White(DyeColor.WHITE, 0xFFEEEEEEu.toInt()),
    Orange(DyeColor.ORANGE, 0xFFF9801Du.toInt()),
    Magenta(DyeColor.MAGENTA, 0xFFC74EBDu.toInt()),
    LightBlue(DyeColor.LIGHT_BLUE, 0xFF3AB3DAu.toInt()),
    Yellow(DyeColor.YELLOW, 0xFFFED83Du.toInt()),
    Lime(DyeColor.LIME, 0xFF80C71Fu.toInt()),
    Pink(DyeColor.PINK, 0xFFF38BAAu.toInt()),
    Gray(DyeColor.GRAY, 0xFF474F52u.toInt()),
    LightGray(DyeColor.LIGHT_GRAY, 0xFF9D9D97u.toInt()),
    Cyan(DyeColor.CYAN, 0xFF169C9Cu.toInt()),
    Purple(DyeColor.PURPLE, 0xFF8932B8u.toInt()),
    Blue(DyeColor.BLUE, 0xFF3C44AAu.toInt()),
    Brown(DyeColor.BROWN, 0xFF835432u.toInt()),
    Green(DyeColor.GREEN, 0xFF5E7C16u.toInt()),
    Red(DyeColor.RED, 0xFFB02E26u.toInt()),
    Black(DyeColor.BLACK, 0xFF1D1D21u.toInt()),
    ;

    val r: UByte
        get() = (color shr 16 and 0xFF).toUByte()

    val g: UByte
        get() = (color shr 8 and 0xFF).toUByte()

    val b: UByte
        get() = (color and 0xFF).toUByte()

    val a: UByte
        get() = (color shr 24 and 0xFF).toUByte()

    companion object {
        val values by lazy { listOf(*values()) }
        val byMinecraftDyeColor by lazy {
            values.map {
                it.minecraftDyeColor to it
            }.toMap()
        }
    }
}
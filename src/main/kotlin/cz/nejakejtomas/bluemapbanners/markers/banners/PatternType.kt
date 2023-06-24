package cz.nejakejtomas.bluemapbanners.markers.banners

import cz.nejakejtomas.bluemapbanners.utils.toArgb
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

enum class PatternType(val id: String, private val resource: String) {
    Base("b", "base"),
    SquareBottomLeft("bl", "square_bottom_left"),
    SquareBottomRight("br", "square_bottom_right"),
    SquareTopLeft("tl", "square_top_left"),
    SquareTopRight("tr", "square_top_right"),
    StripeBottom("bs", "stripe_bottom"),
    StripeTop("ts", "stripe_top"),
    StripeLeft("ls", "stripe_left"),
    StripeRight("rs", "stripe_right"),
    StripeCenter("cs", "stripe_center"),
    StripeMiddle("ms", "stripe_middle"),
    StripeBottomDownRight("drs", "stripe_downright"),
    StripeDownLeft("dls", "stripe_downleft"),
    SmallStripes("ss", "small_stripes"),
    Cross("cr", "cross"),
    StraightCross("sc", "straight_cross"),
    TriangleBottom("bt", "triangle_bottom"),
    TriangleTop("tt", "triangle_top"),
    TrianglesBottom("bts", "triangles_bottom"),
    TrianglesTop("tts", "triangles_top"),
    DiagonalLeft("ld", "diagonal_left"),
    DiagonalUpRight("rd", "diagonal_up_right"),
    DiagonalUpLeft("lud", "diagonal_up_left"),
    DiagonalRight("rud", "diagonal_right"),
    Circle("mc", "circle"),
    Rhombus("mr", "rhombus"),
    HalfVertical("vh", "half_vertical"),
    HalfHorizontal("hh", "half_horizontal"),
    HalfVerticalRight("vhr", "half_vertical_right"),
    HalfHorizontalBottom("hhb", "half_horizontal_bottom"),
    Border("bo", "border"),
    CurlyBorder("cbo", "curly_border"),
    Gradient("gra", "gradient"),
    GradientUp("gru", "gradient_up"),
    Bricks("bri", "bricks"),
    Globe("glb", "globe"),
    Creeper("cre", "creeper"),
    Skull("sku", "skull"),
    Flower("flo", "flower"),
    Mojang("moj", "mojang"),
    Piglin("pig", "piglin"),
    ;

    val image: BufferedImage
        get() = ImageIO.read(PatternType::class.java.getResourceAsStream("/patterns/$resource.png"))
            .getSubimage(1, 1, 20, 40).toArgb()

    companion object {
        val values by lazy { listOf(*values()) }
        val byId by lazy {
            values.map {
                it.id to it
            }.toMap()
        }
    }
}
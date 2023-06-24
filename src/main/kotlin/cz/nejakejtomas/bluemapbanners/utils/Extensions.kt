package cz.nejakejtomas.bluemapbanners.utils

import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.RescaleOp
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.*
import javax.imageio.ImageIO

fun BufferedImage.scaled(scaleX: Double, scaleY: Double): BufferedImage {
    val output = BufferedImage((width * scaleX).toInt(), (height * scaleY).toInt(), type)

    val scaleInstance = AffineTransform.getScaleInstance(scaleX, scaleY)
    val scaleOp = AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
    scaleOp.filter(this, output)

    return output
}

fun BufferedImage.scaled(scaleX: Int, scaleY: Int): BufferedImage = scaled(scaleX.toDouble(), scaleY.toDouble())

fun BufferedImage.shaded(color: MinecraftColor): BufferedImage {
    val output = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    val scales = floatArrayOf(
        color.r.toFloat() / 255f,
        color.g.toFloat() / 255f,
        color.b.toFloat() / 255f,
        color.a.toFloat() / 255f
    )
    val offsets = FloatArray(4)

    val op = RescaleOp(scales, offsets, null)
    return op.filter(this, output)
}

val BufferedImage.rawData: IntArray
    get() {
        assert(type == BufferedImage.TYPE_INT_ARGB)
        return (raster.dataBuffer as DataBufferInt).data
    }

fun BufferedImage.replaced(color: Int, with: Int, colorMask: Int): BufferedImage {
    assert(type == BufferedImage.TYPE_INT_ARGB)
    val output = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val thisData = this.rawData
    val outData = output.rawData

    for (i in thisData.indices) {
        if ((thisData[i] and colorMask) == color) outData[i] = with
        else outData[i] = thisData[i]
    }

    return output
}

fun BufferedImage.toArgb(): BufferedImage {
    val output = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    output.graphics.drawImage(this, 0, 0, null)

    return output
}

fun BufferedImage.asStream(formatName: String = "png"): InputStream {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(this, formatName, outputStream)

    return ByteArrayInputStream(outputStream.toByteArray())
}

fun InputStream.sha512Base64(): String {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val digest = messageDigest.digest(readAllBytes())

    return Base64.getEncoder().encodeToString(digest)
        .replace('/', '-')
        .replace("=", "")
}

fun InputStream.sha256Base64(): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val digest = messageDigest.digest(readAllBytes())

    return Base64.getEncoder().encodeToString(digest)
        .replace('/', '-')
        .replace("=", "")
}

fun ByteArray.sha512Base64(): String {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val digest = messageDigest.digest(this)

    return Base64.getEncoder().encodeToString(digest)
        .replace('/', '-')
        .replace("=", "")
}

fun ByteArray.sha256Base64(): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val digest = messageDigest.digest(this)

    return Base64.getEncoder().encodeToString(digest)
        .replace('/', '-')
        .replace("=", "")
}
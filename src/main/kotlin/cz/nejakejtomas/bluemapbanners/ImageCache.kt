package cz.nejakejtomas.bluemapbanners

import cz.nejakejtomas.bluemapbanners.utils.asStream
import cz.nejakejtomas.bluemapbanners.utils.sha512Base64
import java.awt.image.BufferedImage
import java.lang.ref.WeakReference

class ImageCache {
    private val imageCache: HashMap<String, WeakReference<BufferedImage>> = HashMap()

    fun cache(dataImage: BufferedImage): BufferedImage {
        val hash = dataImage.asStream().use { it.sha512Base64() }
        val possiblyImage = imageCache[hash]
        val image = possiblyImage?.get()

        if ((possiblyImage == null) or (image == null)) {
            imageCache[hash] = WeakReference(dataImage)
        }

        return dataImage
    }
}
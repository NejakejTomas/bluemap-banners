package cz.nejakejtomas.bluemapbanners

import cz.nejakejtomas.bluemapbanners.markers.MarkerImage
import de.bluecolored.bluemap.api.BlueMapAPI
import de.bluecolored.bluemap.api.WebApp
import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

object ImageRegistry : Initializable {
    private val logger = LoggerFactory.getLogger(MarkerAPI::class.java)

    private var initialised = false
    override val isInitialised
        get() = initialised

    private lateinit var webApp: WebApp
    private lateinit var imageRoot: Path
    private lateinit var webRoot: Path
    private val registered: MutableMap<Path, String> = hashMapOf()

    override fun initialize(api: BlueMapAPI) {
        this.webApp = api.webApp
        imageRoot = webApp.webRoot.resolve(Paths.get("data", "images"))
        webRoot = Paths.get("data", "images")
        imageRoot.toFile().deleteRecursively()
        // TODO: do not clear and do not write if exists?
        initialised = true
    }

    override fun stop(api: BlueMapAPI) {}

    operator fun invoke(image: MarkerImage): String {
        val path: String? = registered[image.path]
        if (path != null) return path

        val filePath = image.path

        try {
            image.stream.use {
                val newPath = createImage(image.stream, filePath)
                registered[filePath] = newPath

                return newPath
            }
        } catch (e: Exception) {
            logger.error("Cannot create image", e)

            return ""
        }
    }

    private fun createImage(stream: InputStream, path: Path): String {
        val webPath = webRoot.resolve(path)
        val file = imageRoot.resolve(path).toFile()
        file.parentFile.mkdirs()

        FileOutputStream(file).use {
            stream.use { inputStream ->
                inputStream.transferTo(it)
            }
        }

        return webPath.toString()
    }
}

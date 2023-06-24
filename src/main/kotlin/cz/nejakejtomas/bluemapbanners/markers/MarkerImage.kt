package cz.nejakejtomas.bluemapbanners.markers

import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths

interface MarkerImage {
    val location: Path
    val fileName: String
    val anchorX: Int
    val anchorY: Int
    val stream: InputStream
    val path: Path get() = markersPath.resolve(location).resolve(fileName)
}

private val markersPath = Paths.get("markers")
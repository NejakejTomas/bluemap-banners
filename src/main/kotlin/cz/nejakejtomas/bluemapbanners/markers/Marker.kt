package cz.nejakejtomas.bluemapbanners.markers

import de.bluecolored.bluemap.api.markers.MarkerSet
import de.bluecolored.bluemap.api.markers.POIMarker

open class Marker(
    val label: String,
    val positionX: Int,
    val positionY: Int,
    val positionZ: Int,
    val dimension: String
) {
    fun addToSet(set: MarkerSet) {
        for (i in 0 until markerCount) {
            val builder = POIMarker.Builder()
            addToBuilder(i, builder)
            set.put("${id}_${i}", builder.build())
        }

        // For possibility of cleanup
        val builder = POIMarker.Builder()
        addToBuilder(-1, builder)
    }

    fun removeFromSet(set: MarkerSet) {
        for (i in 0 until markerCount) {
            set.remove("${id}_${i}")
        }
    }

    protected open fun addToBuilder(index: Int, builder: POIMarker.Builder): Unit = builder.run {
        label(label)
        position(positionX + 0.5, positionY + 0.0, positionZ + 0.5)
        maxDistance(Double.MAX_VALUE)
        listed(index == 0)
    }

    open val markerCount = 1
    open val category = "Markers"
    protected open val id = "${positionX}_${positionY}_${positionZ}_${label}"
    open fun <Out, Context> accept(visitor: MarkerVisitor<Out, Context>, context: Context): Out =
        visitor.visit(this, context)
}
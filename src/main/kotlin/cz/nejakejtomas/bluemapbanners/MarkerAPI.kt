package cz.nejakejtomas.bluemapbanners

import cz.nejakejtomas.bluemapbanners.dbs.Dbs
import cz.nejakejtomas.bluemapbanners.dbs.MarkerDao
import cz.nejakejtomas.bluemapbanners.markers.Marker
import cz.nejakejtomas.bluemapbanners.markers.MarkerVisitor
import cz.nejakejtomas.bluemapbanners.markers.banners.Banner
import cz.nejakejtomas.bluemapbanners.markers.maps.Map
import cz.nejakejtomas.bluemapbanners.utils.asStream
import de.bluecolored.bluemap.api.BlueMapAPI
import de.bluecolored.bluemap.api.markers.MarkerSet
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory


object MarkerAPI : Initializable {
    private val logger = LoggerFactory.getLogger(MarkerAPI::class.java)

    private var initialised = false
    override val isInitialised
        get() = initialised

    private lateinit var api: BlueMapAPI
    private val markerSets_: MutableMap<WorldCategory, MarkerSet> = mutableMapOf()
    private val markerSets = object : MutableMap<WorldCategory, MarkerSet> by markerSets_ {
        override fun get(key: WorldCategory): MarkerSet? {
            val set = markerSets_[key]
            if (set != null) return set

            val newSet = MarkerSet.builder()
                .label(key.category)
                .toggleable(true)
                .defaultHidden(false)
                .build()

            markerSets_[key] = newSet

            val world = api.getWorld(key.world)
            if (world.isEmpty) return null

            for (map in world.get().maps) {
                map.markerSets[key.category] = newSet
            }

            return newSet
        }
    }

    private val databaseFolder get() = api.webApp.webRoot.parent.resolve("markers")
    private val databasePath get() = databaseFolder.resolve("MarkerDatabase.sqlite")
    private lateinit var database: Database

    override fun initialize(api: BlueMapAPI) {
        this.api = api
        databaseFolder.toFile().mkdirs()
        database = Dbs.setup(databasePath)

        initialised = true

        loadMarkers()
//        printMarkers()
    }

    private fun printMarkers() {
        // For debug purposes
        // TODO: escape somehow?
        val visitor = object : MarkerVisitor<String, Unit> {
            override fun visit(marker: Marker, context: Unit): String {
                return "Marker(\"${marker.label}\", ${marker.positionX}, ${marker.positionY}, ${marker.positionZ}, \"${marker.dimension}\")"
            }

            override fun visit(banner: Banner, context: Unit): String {
                val patterns = banner.patterns.fold(StringBuilder()) { builder, pattern ->
                    builder.append("Pattern(PatternType.${pattern.type.name}, MinecraftColor.${pattern.color.name})")
                    builder
                }

                return "Banner(\"${banner.label}\", ${banner.positionX}, ${banner.positionY}, ${banner.positionZ}, \"${banner.dimension}\", MinecraftColor.${banner.color}, listOf($patterns))"
            }

            override fun visit(map: Map, context: Unit): String {
                val image = map.rawImage.asStream().use {
                    it.readAllBytes().fold(StringBuilder()) { builder, byte ->
                        builder.append("$byte, ")
                        builder
                    }
                }

                return "Map.fromBufferedImage(\"${map.label}\", ${map.positionX}, ${map.positionY}, ${map.positionZ}, \"${map.dimension}\", ${map.anchorX}, ${map.anchorY}, ${map.transparencyColor}, byteArrayOf($image))"
            }
        }

        MarkerDao.getAll(database) { marker ->
            println("addMarker(${marker.accept(visitor, Unit)})")
        }
    }

    override fun stop(api: BlueMapAPI) {}

    fun addMarker(marker: Marker): Boolean {
        try {
            MarkerDao.insert(database, marker)


            val set = markerSets[WorldCategory(marker.dimension, marker.category)] ?: return false
            marker.addToSet(set)

            return true
        } catch (e: Exception) {
            logger.error("Cannot add marker", e)

            return false
        }
    }

    fun removeMarker(marker: Marker): Boolean {
        try {
            MarkerDao.remove(database, marker)

            val set = markerSets[WorldCategory(marker.dimension, marker.category)] ?: return false
            marker.removeFromSet(set)

            return true
        } catch (e: Exception) {
            logger.error("Cannot remove marker", e)

            return false
        }
    }

    private fun loadMarkers() {
        MarkerDao.getAll(database) { marker ->
            val set = markerSets[WorldCategory(marker.dimension, marker.category)] ?: return@getAll
            marker.addToSet(set)
        }
    }

    private data class WorldCategory(val world: String, val category: String)
}
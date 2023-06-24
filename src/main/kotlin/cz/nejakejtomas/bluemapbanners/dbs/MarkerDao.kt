package cz.nejakejtomas.bluemapbanners.dbs

import cz.nejakejtomas.bluemapbanners.dbs.tables.DimensionTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.MarkerTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.banners.BannerPatternTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.banners.BannerTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.maps.MapDataTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.maps.MapTable
import cz.nejakejtomas.bluemapbanners.markers.Marker
import cz.nejakejtomas.bluemapbanners.markers.MarkerVisitor
import cz.nejakejtomas.bluemapbanners.markers.banners.Banner
import cz.nejakejtomas.bluemapbanners.markers.banners.Pattern
import cz.nejakejtomas.bluemapbanners.markers.maps.Map
import cz.nejakejtomas.bluemapbanners.utils.asStream
import cz.nejakejtomas.bluemapbanners.utils.sha512Base64
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.sequences.Sequence

object MarkerDao {
    private fun getDimensionId(dimension: String): EntityID<Int> {
        return DimensionTable.select { DimensionTable.name eq dimension }.limit(1).singleOrNull()?.let {
            it[DimensionTable.id]
        } ?: DimensionTable.insertAndGetId {
            it[DimensionTable.name] = dimension
        }
    }

    private fun insertPatterns(patterns: List<Pattern>, bannerId: EntityID<Int>) {
        var position = 0
        BannerPatternTable.batchInsert(patterns) {
            this[BannerPatternTable.banner] = bannerId
            this[BannerPatternTable.position] = position++
            this[BannerPatternTable.pattern] = it.type
            this[BannerPatternTable.color] = it.color
        }
    }

    private fun markerSelect(marker: Marker): Op<Boolean> {
        return (MarkerTable.x eq marker.positionX) and
                (MarkerTable.y eq marker.positionY) and
                (MarkerTable.z eq marker.positionZ) and
                (MarkerTable.dimension eq getDimensionId(marker.dimension) and
                        (MarkerTable.label eq marker.label))
    }

    private fun getMarker(marker: Marker): ResultRow? {
        return MarkerTable.select {
            markerSelect(marker)
        }.limit(1).singleOrNull()
    }

    @JvmInline
    value class Context(val database: Database)

    private val Insert = object : MarkerVisitor<Unit, Context> {

        private fun insertMarker(marker: Marker): EntityID<Int> {
            val dimensionId = getDimensionId(marker.dimension)

            return MarkerTable.insertAndGetId {
                it[MarkerTable.x] = marker.positionX
                it[MarkerTable.y] = marker.positionY
                it[MarkerTable.z] = marker.positionZ
                it[MarkerTable.label] = marker.label
                it[MarkerTable.dimension] = dimensionId
            }
        }

        override fun visit(marker: Marker, context: Context) {
            transaction(context.database) {
                insertMarker(marker)
            }
        }

        override fun visit(banner: Banner, context: Context) {
            transaction(context.database) {
                val markerId = insertMarker(banner)

                BannerTable.insert {
                    it[BannerTable.color] = banner.color
                    it[BannerTable.marker] = markerId
                }

                insertPatterns(banner.patterns, markerId)
            }
        }

        override fun visit(map: Map, context: Context) {
            transaction(context.database) {
                val markerId = insertMarker(map)

                val dataHash = MapDataTable.insertIgnore {
                    it[MapDataTable.data] = map.rawImage.asStream().use { stream -> stream.readAllBytes() }
                    it[MapDataTable.hash] = map.rawImage.asStream().use { stream -> stream.sha512Base64() }
                } get MapDataTable.hash

                MapTable.insert {
                    it[MapTable.marker] = markerId
                    it[MapTable.anchorX] = map.anchorX
                    it[MapTable.anchorY] = map.anchorY
                    it[MapTable.transparencyColor] = map.transparencyColor
                    it[MapTable.data] = dataHash
                }
            }
        }
    }

    private val Remove = object : MarkerVisitor<Unit, Context> {

        override fun visit(marker: Marker, context: Context) {
            transaction(context.database) {
                MarkerTable.deleteWhere { markerSelect(marker) }
            }
        }

        override fun visit(banner: Banner, context: Context) {
            transaction(context.database) {
                val markerId = getMarker(banner)?.let { it[MarkerTable.id] }
                if (markerId == null) {
                    rollback()
                    return@transaction
                }

                BannerPatternTable.deleteWhere { BannerPatternTable.banner eq markerId }
                BannerTable.deleteWhere { BannerTable.marker eq markerId }
                MarkerTable.deleteWhere { MarkerTable.id eq markerId }
            }
        }

        override fun visit(map: Map, context: Context) {
            transaction(context.database) {
                val markerId = getMarker(map)?.let { it[MarkerTable.id] }
                if (markerId == null) {
                    rollback()
                    return@transaction
                }

                MapDataTable.deleteWhere {
                    exists(MapTable
                        .join(MapDataTable, JoinType.INNER, MapTable.data, MapDataTable.hash)
                        .select { MapTable.marker eq markerId })
                }
                MapTable.deleteWhere { MapTable.marker eq markerId }
                MarkerTable.deleteWhere { MarkerTable.id eq markerId }
            }
        }
    }

    fun insert(database: Database, marker: Marker) {
        marker.accept(Insert, Context(database))
    }

    fun remove(database: Database, marker: Marker) {
        marker.accept(Remove, Context(database))
    }

    fun getAll(database: Database, each: (Marker) -> Unit) {
        transaction(database) {
            val all =
                getAllBanners() +
                        getAllMarkers() +
                        getAllMaps()

            all.forEach(each)
        }
    }

    private fun getAllMarkers(): Sequence<Marker> {
        return MarkerTable
            .join(DimensionTable, JoinType.INNER, MarkerTable.dimension, DimensionTable.id)
            .join(BannerTable, JoinType.LEFT, MarkerTable.id, BannerTable.marker)
            .join(MapTable, JoinType.LEFT, MarkerTable.id, MapTable.marker)
            // Add tables here (left join)
            .select {
                (BannerTable.marker eq null) and
                        (MapTable.marker eq null)
                // Add tables here (not null)
            }
            .asSequence()
            .map { markerDimensionRow ->
                Marker(
                    markerDimensionRow[MarkerTable.label],
                    markerDimensionRow[MarkerTable.x],
                    markerDimensionRow[MarkerTable.y],
                    markerDimensionRow[MarkerTable.z],
                    markerDimensionRow[DimensionTable.name]
                )
            }
    }

    private fun getAllBanners(): Sequence<Banner> {
        return MarkerTable
            .join(DimensionTable, JoinType.INNER, MarkerTable.dimension, DimensionTable.id)
            .join(BannerTable, JoinType.INNER, MarkerTable.id, BannerTable.marker)
            .selectAll()
            .asSequence()
            .map { bannerMarkerDimensionRow ->
                val patterns =
                    BannerPatternTable.select { BannerPatternTable.banner eq bannerMarkerDimensionRow[MarkerTable.id] }
                        .sortedBy { it[BannerPatternTable.position] }
                        .map { Pattern(it[BannerPatternTable.pattern], it[BannerPatternTable.color]) }

                Banner(
                    bannerMarkerDimensionRow[MarkerTable.label],
                    bannerMarkerDimensionRow[MarkerTable.x],
                    bannerMarkerDimensionRow[MarkerTable.y],
                    bannerMarkerDimensionRow[MarkerTable.z],
                    bannerMarkerDimensionRow[DimensionTable.name],
                    bannerMarkerDimensionRow[BannerTable.color],
                    patterns
                )
            }
    }

    private fun convertImageData(data: ByteArray): BufferedImage {
        ByteArrayInputStream(data).use {
            val image = ImageIO.read(it)

            if (image.type == BufferedImage.TYPE_INT_ARGB) return image

            val output = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
            output.graphics.drawImage(image, 0, 0, null);

            return output
        }
    }

    private fun getAllMaps(): Sequence<Map> {
        return MarkerTable
            .join(DimensionTable, JoinType.INNER, MarkerTable.dimension, DimensionTable.id)
            .join(MapTable, JoinType.INNER, MarkerTable.id, MapTable.marker)
            .join(MapDataTable, JoinType.INNER, MapTable.data, MapDataTable.hash)
            .selectAll()
            .asSequence()
            .map { mapMarkerDimensionRow ->
                Map.fromBufferedImage(
                    mapMarkerDimensionRow[MarkerTable.label],
                    mapMarkerDimensionRow[MarkerTable.x],
                    mapMarkerDimensionRow[MarkerTable.y],
                    mapMarkerDimensionRow[MarkerTable.z],
                    mapMarkerDimensionRow[DimensionTable.name],
                    mapMarkerDimensionRow[MapTable.anchorX],
                    mapMarkerDimensionRow[MapTable.anchorY],
                    mapMarkerDimensionRow[MapTable.transparencyColor],
                    convertImageData(mapMarkerDimensionRow[MapDataTable.data])
                )
            }
    }
}
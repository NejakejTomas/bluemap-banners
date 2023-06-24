package cz.nejakejtomas.bluemapbanners.dbs.tables.markers.maps

import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.MarkerTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.banners.BannerTable
import org.jetbrains.exposed.sql.Table

object MapTable : Table() {
    val marker = reference("marker", MarkerTable)
    val anchorX = float("anchorX").nullable()
    val anchorY = float("anchorY").nullable()
    val transparencyColor = integer("transparencyColor").nullable()
    val data = reference("data", MapDataTable.hash)

    override val primaryKey: PrimaryKey = PrimaryKey(BannerTable.marker)
}
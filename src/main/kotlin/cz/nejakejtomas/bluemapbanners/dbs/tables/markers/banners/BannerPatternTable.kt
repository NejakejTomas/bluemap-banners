package cz.nejakejtomas.bluemapbanners.dbs.tables.markers.banners

import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.MarkerTable
import cz.nejakejtomas.bluemapbanners.markers.banners.PatternType
import cz.nejakejtomas.bluemapbanners.utils.MinecraftColor
import org.jetbrains.exposed.sql.Table

object BannerPatternTable : Table() {
    val banner = reference("banner", MarkerTable)
    val position = integer("position")
    val pattern = enumeration<PatternType>("pattern")
    val color = enumeration<MinecraftColor>("color")

    override val primaryKey: PrimaryKey = PrimaryKey(banner, position)

    init {
        foreignKey(banner to MarkerTable.id)
    }
}
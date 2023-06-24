package cz.nejakejtomas.bluemapbanners.dbs.tables.markers.banners

import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.MarkerTable
import cz.nejakejtomas.bluemapbanners.utils.MinecraftColor
import org.jetbrains.exposed.sql.Table

object BannerTable : Table() {
    val marker = reference("marker", MarkerTable)
    val color = enumeration<MinecraftColor>("color")

    override val primaryKey: PrimaryKey = PrimaryKey(marker)
}
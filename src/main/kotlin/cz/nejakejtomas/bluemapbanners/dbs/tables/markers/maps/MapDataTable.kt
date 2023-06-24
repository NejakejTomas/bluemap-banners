package cz.nejakejtomas.bluemapbanners.dbs.tables.markers.maps

import org.jetbrains.exposed.sql.Table

object MapDataTable : Table() {
    // Base64 encoded 512 bit value without leading padding
    val hash = varchar("hash", 86)
    val data = binary("data")

    override val primaryKey: PrimaryKey = PrimaryKey(hash)
}
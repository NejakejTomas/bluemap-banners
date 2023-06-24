package cz.nejakejtomas.bluemapbanners.dbs.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object DimensionTable : IntIdTable() {
    val name = DimensionTable.text("name").index()
}
package cz.nejakejtomas.bluemapbanners.dbs.tables.markers

import cz.nejakejtomas.bluemapbanners.dbs.tables.DimensionTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption


object MarkerTable : IntIdTable() {
    val x = integer("x").index()
    val y = integer("y").index()
    val z = integer("z").index()
    val dimension = reference("dimension", DimensionTable, ReferenceOption.NO_ACTION).index()

    val label = text("name").index()
}
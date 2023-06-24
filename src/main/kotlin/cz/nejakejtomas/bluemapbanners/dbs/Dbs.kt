package cz.nejakejtomas.bluemapbanners.dbs

import cz.nejakejtomas.bluemapbanners.dbs.tables.DimensionTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.MarkerTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.banners.BannerPatternTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.banners.BannerTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.maps.MapDataTable
import cz.nejakejtomas.bluemapbanners.dbs.tables.markers.maps.MapTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.nio.file.Path

object Dbs {
    private val logger = LoggerFactory.getLogger(Dbs::class.java)

    fun setup(path: Path): Database {
        val database = Database.connect(
            "jdbc:sqlite:file:${path.toString().replace('\\', '/')}",
            driver = "org.sqlite.JDBC"
        )

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                BannerPatternTable,
                BannerTable,
                DimensionTable,
                MarkerTable,
                MapTable,
                MapDataTable
            )
        }

        logger.info("Database initialized")

        return database
    }
}
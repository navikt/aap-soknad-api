package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.SingleConnectionDataSource

class V3__ : BaseJavaMigration() {

    @Override
    override fun migrate(context: Context) {
        JdbcTemplate(SingleConnectionDataSource(context.connection, true))
                .execute("")
        }
}
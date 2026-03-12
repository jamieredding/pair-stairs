package dev.coldhands.pair.stairs.backend.infrastructure.persistance.backup

import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService
import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService.Companion.BackupAlreadyExists
import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService.Companion.BackupError
import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService.Companion.HadException
import dev.coldhands.pair.stairs.backend.domain.DatabaseBackupService.Companion.UnsupportedDatabase
import dev.forkhandles.result4k.*
import org.h2.engine.Constants
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.sql.DriverManager
import kotlin.io.path.exists

class H2DatabaseBackupService(
    private val jdbcUrl: String,
    private val username: String,
    private val password: String
) :
    DatabaseBackupService {
    private val logger = LoggerFactory.getLogger(H2DatabaseBackupService::class.java)
    override fun backup(path: Path): Result<Unit, BackupError> {
        if (!jdbcUrl.startsWith(Constants.START_URL))
            return UnsupportedDatabase(jdbcUrl).asFailure()

        if (path.exists())
            return BackupAlreadyExists(path).asFailure()

        return resultFrom {
            DriverManager.getConnection(jdbcUrl, username, password)
                .use {
                    it.prepareStatement("BACKUP TO '$path'").execute()
                }
        }
            .map { }
            .mapFailure {
                logger.error("Unable to backup to '$path'", it)
                HadException(it)
            }
    }
}
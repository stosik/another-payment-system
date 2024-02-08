package pl.stosik.billing.data.adapter.driven

import org.jetbrains.exposed.sql.*
import pl.stosik.billing.data.asyncQuery
import pl.stosik.billing.data.port.driven.JobLockAcquirer
import pl.stosik.billing.data.singleOrNull
import pl.stosik.billing.models.infrastracture.JobLock

object JobLockTable : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 128).uniqueIndex()
    val locked = bool("locked")
    override val primaryKey = PrimaryKey(columns = arrayOf(id), name = "pk_job_lock_id")
}

class JobLockRepository(private val db: Database) : JobLockAcquirer {

    override suspend fun acquire(name: String): Boolean {
        return db.asyncQuery {
            val jobLock = findLockByName(name) ?: createLock(name)

            if (jobLock?.locked == true) {
                return@asyncQuery false
            } else {
                JobLockTable
                    .update({ JobLockTable.name eq name }) {
                        it[locked] = true
                    }.let {
                        it > 0
                    }
            }
        }
    }

    override suspend fun release(name: String): Boolean = db.asyncQuery {
        JobLockTable
            .update({ JobLockTable.name eq name }) {
                it[locked] = false
            }.let {
                it > 0
            }
    }

    override suspend fun releaseAllInstanceLocks(instanceId: String): Boolean = db.asyncQuery {
        JobLockTable
            .update({ JobLockTable.name like "%$instanceId%" }) {
                it[locked] = false
            }.let {
                it > 0
            }
    }

    private suspend fun createLock(name: String): JobLock? = db.asyncQuery {
        findLockByName(name) ?: saveJobLock(name)
    }

    private suspend fun findLockByName(name: String) = db.asyncQuery {
        JobLockTable
            .select { JobLockTable.name eq name }
            .firstOrNull()
            ?.toJobLock()
    }

    private suspend fun saveJobLock(jobLockName: String) = db.asyncQuery {
        JobLockTable
            .insert {
                it[name] = jobLockName
                it[locked] = false
            }
            .singleOrNull()
            ?.toJobLock()
    }
}

private fun ResultRow.toJobLock(): JobLock = JobLock(
    id = this[JobLockTable.id],
    name = this[JobLockTable.name],
    locked = this[JobLockTable.locked]
)
package pl.stosik.billing.data

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

fun <T> Database.query(block: Transaction.() -> T): T = transaction(db = this) { block() }

suspend fun <T> Database.asyncQuery(block: suspend Transaction.() -> T): T =
    newSuspendedTransaction(db = this, context = Dispatchers.IO) { block() }

fun InsertStatement<*>.singleOrNull() = this.resultedValues?.singleOrNull()

package pl.stosik.paygrind.data.configuration

import io.r2dbc.pool.ConnectionPool

interface ConnectionPoolBuilder {
    fun createPool(): ConnectionPool
}
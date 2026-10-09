package com.ntvelop.goldengoosepda.network

object DatabaseConfig {
    const val DEFAULT_DB_NAME = "goldengoose_pos"
    const val DEFAULT_PORT = 443

    /**
     * Builds a PostgreSQL JDBC connection URL ensuring port 443 and ?sslmode=disable
     * to avoid SSL negotiation failures over reverse-proxied TCP streams.
     * Example output: jdbc:postgresql://100.103.214.109:443/goldengoose_pos?sslmode=disable
     */
    fun buildJdbcUrl(serverIp: String, dbName: String = DEFAULT_DB_NAME, port: Int = DEFAULT_PORT): String {
        var host = serverIp.trim()
            .replace("https://", "", ignoreCase = true)
            .replace("http://", "", ignoreCase = true)

        if (host.contains("/")) {
            host = host.substringBefore("/")
        }
        if (host.contains(":")) {
            host = host.substringBefore(":")
        }

        val baseUrl = "jdbc:postgresql://$host:$port/$dbName"
        return if (baseUrl.contains("?")) {
            if (baseUrl.contains("sslmode=")) baseUrl else "$baseUrl&sslmode=disable"
        } else {
            "$baseUrl?sslmode=disable"
        }
    }
}

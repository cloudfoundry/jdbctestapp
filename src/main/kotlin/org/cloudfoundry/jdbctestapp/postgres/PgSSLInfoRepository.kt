package org.cloudfoundry.jdbctestapp.postgres

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PgSSLInfoRepository: CrudRepository<PgSSLInfo, Int> {
    @Query("select * from pg_stat_ssl where pid=pg_backend_pid()", nativeQuery = true)
    fun sslInfo(): PgSSLInfo
}
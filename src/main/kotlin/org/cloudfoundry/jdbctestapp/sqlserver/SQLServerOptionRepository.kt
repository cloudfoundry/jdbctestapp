package org.cloudfoundry.jdbctestapp.sqlserver

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface SQLServerOptionRepository: CrudRepository<SQLServerOption, String> {
// This query can be run by administrators (VIEW SERVER STATE is the required privilege).
// Bindings won't be able to execute it
    @Query("SELECT encrypt_option FROM sys.dm_exec_connections WHERE session_id = @@SPID", nativeQuery = true)
    fun sslInfo(): SQLServerOption
}
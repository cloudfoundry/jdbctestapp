package org.cloudfoundry.jdbctestapp.mysql

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface MysqlOptionRepository: CrudRepository<MysqlOption, String> {
    @Query("SHOW STATUS LIKE 'Ssl_cipher'", nativeQuery = true)
    fun sslCipher(): MysqlOption
}
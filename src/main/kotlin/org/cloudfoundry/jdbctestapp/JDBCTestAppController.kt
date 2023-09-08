package org.cloudfoundry.jdbctestapp

import org.cloudfoundry.jdbctestapp.mysql.MysqlOption
import org.cloudfoundry.jdbctestapp.mysql.MysqlOptionRepository
import org.cloudfoundry.jdbctestapp.postgres.PgSSLInfo
import org.cloudfoundry.jdbctestapp.postgres.PgSSLInfoRepository
import org.cloudfoundry.jdbctestapp.sqlserver.SQLServerOption
import org.cloudfoundry.jdbctestapp.sqlserver.SQLServerOptionRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.Optional

@RestController
class JDBCTestAppController(
    val userRepository: UserRepository,
    val mysqlOptionRepository: MysqlOptionRepository,
    val pgSSLInfoRepository: PgSSLInfoRepository,
    val sqlServerSSLInfoRepository: SQLServerOptionRepository
) {

    @GetMapping(path = ["/"])
    fun readData(): Iterable<User> {
        return userRepository.findAll()
    }

    @GetMapping(path = ["/{userId}"])
    fun getUser(@PathVariable userId: Int): Optional<User> {
        return userRepository.findById(userId)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = ["/{userId}"])
    fun deleteUser(@PathVariable userId: Int) {
        userRepository.deleteById(userId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = ["/"])
    fun writeData(@RequestParam(name = "name") userName: String): User {
        val newUser = User(name = userName)
        return userRepository.save(newUser)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = ["/"])
    fun deleteAll() {
        userRepository.deleteAll()
    }

    @GetMapping(path = ["/mysql-ssl"])
    fun mysqlSSL(): MysqlOption {
        return mysqlOptionRepository.sslCipher()
    }

    @GetMapping(path = ["/postgres-ssl"])
    fun pgSSL(): PgSSLInfo {
        return pgSSLInfoRepository.sslInfo()
    }
    @GetMapping(path = ["/sqlserver-ssl"])
    fun sqlServerSSL(): SQLServerOption {
        return sqlServerSSLInfoRepository.sslInfo()
    }
}
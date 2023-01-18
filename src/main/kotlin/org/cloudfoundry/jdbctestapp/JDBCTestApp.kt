package org.cloudfoundry.jdbctestapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JDBCTestApp

fun main(args: Array<String>) {
    runApplication<JDBCTestApp>(*args)
}

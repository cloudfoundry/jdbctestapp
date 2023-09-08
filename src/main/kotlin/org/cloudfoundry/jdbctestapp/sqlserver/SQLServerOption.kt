package org.cloudfoundry.jdbctestapp.sqlserver

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Column

@Entity
data class SQLServerOption (
    @Id
    @Column(name = "encrypt_option")
    var encryptOption: String
)
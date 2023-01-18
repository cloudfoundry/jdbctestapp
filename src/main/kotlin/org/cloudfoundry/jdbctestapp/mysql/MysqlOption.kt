package org.cloudfoundry.jdbctestapp.mysql

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Column

@Entity
data class MysqlOption (
    @Id
    @Column(name = "Variable_name")
    var variableName: String,
    @Column(name = "Value")
    var value: String
)
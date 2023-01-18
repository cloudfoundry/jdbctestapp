package org.cloudfoundry.jdbctestapp.postgres

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "pg_catalog.pg_stat_ssl")
data class PgSSLInfo(
    @Id
    val pid: Int,
    val ssl: Boolean,
    val version: String? = null,
    val cipher: String? = null,
    val bits: Int? = null,
    @Column(name = "client_dn")
    val clientDN: String? = null,
    @Column(name = "client_serial")
    val clientSerial: Number? = null,
    @Column(name = "issuer_dn")
    val issuerDN: String? = null
    )

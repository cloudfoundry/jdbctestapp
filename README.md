# JDBC test application

This application can be used to test database connections set up with a JDBC URL,
and report SSL/TLS information. Currently, the application only supports the following
database engines:

- PostgreSQL
- MySQL

It has been verified to run on the following IaaS platforms:

- AWS
- GCP

## Generate JAR for deployment

This application is supposed to be deployed to [Cloud Foundry](https://cloudfoundry.org), which requires a fat JAR for
deployment. However, before the JAR is generated, the project needs some DB-specific adjustments. These can be made by
running one of the configuration gradle tasks provided: either `configureForMysql`, or `configureForPostgres` depending
on the required database.

After that, running `gradlew bootJar` will produce a JAR pre-configured for the indicated database engine
in `build/libs`.

### Automatic TLS Certificate extraction

By default, the generated JAR will contain a shell script that will automatically
run on CloudFoundry and attempt to extract TLS certificates from the service binding.
Currently supported services are:

- CloudSQL for MySQL on GCP via [Cloud Services Broker](https://github.com/cloudfoundry/cloud-service-broker).

In order to disable the script, build the JAR with a project flag `disableBindingTLSDetection` set to `false`, e.g.:
```
gradlew bootJar -P disableBindingTLSDetection=true
```

## Generating a sample manifest

Once the database engine is configured, a sample Cloud Foundry application manifest can be generated.
Running `gradlew deploymentManifest` task will generate a sample manifest in the root of the `build` directory.

### GCP CloudSQL

On GCP, each CloudSQL instance gets its own CA and certificate generated, so the manifest will have to include 
additional environment variables in order to support this:

- `-P iaas=gcp` (Required for GCP) This flag is need in this exact form to set up the manifest to use instance-specific
  certificates, keys and CAs.
- `-P keystorePassword=super-secret-password` It's recommended to override the default password used in the keystore
  generated for the GCP deployment.

Run the `deploymentManifest` gradle task to generate a sample manifest in the root of the `build` directory:

```shell
# For CloudSQL-specific certificate handling on GCP
$ ./gradlew deploymentManifest -P iaas=gcp -P keystorePassword=super-secure-password

# For AWS no options are needed
$ ./gradlew deploymentManifest 
```

## Deploying from this repository

It's also possible to use gradle in order to deploy this application to Cloud Foundry. As the deployment task depends
on the `bootJar` and the `deploymentManifest` tasks, it requires the same configuration, namely, running the database
engine configuration tasks, and requiring the `-P iaas=gcp` flag when deploying to GCP. The deployment tasks rely on
the CF CLI, and expect it to be logged in.

There are two gradle tasks: `initialDeploy` and `deploy`. The first is intended to deploy the app before binding it to
a service, passing a `--no-start` flag to the CF CLI.

## Test endpoints

The application provides a set of Create (`POST /?name=<new-user-name>`), Get (`GET /<user-id>`), List (`GET /`), and
Delete (`DELETE /<user-id>`) operations on a
`User` entity, mounted at the application root. The `User` is an extremely simple entity that has only two attributes:
`id` and `name`.

## SSL information endpoints

### PostgreSQL

`GET /postgres-ssl` provides the full `pg_stat_ssl` report on the current connection encoded as JSON, e.g.:

```json
{
  "pid": 8546,
  "ssl": true,
  "version": "TLSv1.2",
  "cipher": "ECDHE-RSA-AES256-GCM-SHA384",
  "bits": 256,
  "clientDN": "/CN=DaH08q7h0487hF8u/O=Google\\, Inc/C=US",
  "clientSerial": "948546794",
  "issuerDN": "/dnQualifier=58a5c5f4-55b0-4d22-aba7-87880d1ad0ab/CN=Google Clo"
}
```

Please Note:

- The `clientDN`, `clientSerial` and `issuerDN` will be filled in only if a client certificate is used.
- The `version`, `cipher` and `bits` fields will only be filled in if the current database connection is secure.

### MySQL

`GET /mysql-ssl` reports the ciphers used for the current connection, e.g.:

```json
{
  "variableName": "Ssl_cipher",
  "value": "ECDHE-RSA-AES128-GCM-SHA256"
}
```

The value will only be blank if the database connection is not encrypted.

## Troubleshooting

The application won't start with the following error message

```
Found non-empty schema(s) "XXX" but no schema history table. Use baseline() or set baselineOnMigrate to true to initialize the schema history table.
```

This problem can be resolved by one of the following options:

- create a new schema or database to use with the application
- set application property `spring.flyway.baseline-on-migrate` to `true` and property `spring.flyway.baseline-version`
  to `0`
- delete all objects from the current schema before running the application for the first time
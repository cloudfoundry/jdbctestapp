if [[ $(echo "$VCAP_SERVICES" | jq '0 + (.["csb-aws-postgresql"] | length) + (.["csb-aws-aurora-postgresql"] | length)') -gt 0 ]]; then
  cat /app/BOOT-INF/classes/banners/aws-postgres.txt
  cat /app/BOOT-INF/classes/banners/aws-postgres-end.txt
fi

if [[ $(echo "$VCAP_SERVICES" | jq '.["csb-google-mysql"] | length') -gt 0 ]]; then
  cat /app/BOOT-INF/classes/banners/gcp-mysql.txt
  export PATH=$PATH:$(dirname "$(which /app/.java-buildpack/*/bin/keytool)")
  mkdir /tmp/certs

  echo "==> Extracting Google SQL CA certificate"
  echo "$VCAP_SERVICES" | jq -r '.["csb-google-mysql"][0].credentials.sslrootcert' > /tmp/certs/ca.pem
  keytool -import -trustcacerts -cacerts -storepass changeit -noprompt -alias CloudSQLCA -file /tmp/certs/ca.pem
  echo "Done"

  echo "==> Setting up the keystore"
  echo "$VCAP_SERVICES" | jq -r '.["csb-google-mysql"][0].credentials.sslcert' > /tmp/certs/client-cert.pem
  touch /tmp/certs/client-key.pem /tmp/certs/certificate.p12
  chmod 0600 /tmp/certs/client-key.pem /tmp/certs/certificate.p12

  echo "$VCAP_SERVICES" | jq -r '.["csb-google-mysql"][0].credentials.sslkey' > /tmp/certs/client-key.pem
  echo "Done"

  echo "==> Populating the keystore"
  openssl pkcs12 -export -in /tmp/certs/client-cert.pem -inkey /tmp/certs/client-key.pem -out /tmp/certs/certificate.p12 -name "certificate" -passout "pass:${JAVA_KEYSTORE_PASSWORD}"
  keytool -importkeystore -srckeystore /tmp/certs/certificate.p12 -srcstoretype pkcs12 -destkeystore /app/META-INF/keystore.jks -srcstorepass "$JAVA_KEYSTORE_PASSWORD" -deststorepass "$JAVA_KEYSTORE_PASSWORD"
  rm /tmp/certs/*
  echo "==> Pre-start complete"
  cat /app/BOOT-INF/classes/banners/gcp-mysql-end.txt
fi

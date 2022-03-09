# kafka-tls-support-with-akka

## Enable TLS for Kafka
All these steps are summarization of the official Kafka documentation that you can find [here](https://kafka.apache.org/30/documentation.html#security_ssl).

- **Create keypair for our CA**
  ```bash
  openssl req -new -x509 -keyout ca-key -out ca-cert -days 9999
  ```
  This will create `ca-cert` and `ca-key` files which we will use at further steps.
  <br/><br/>

- **Generate SSL key and certificate for each Kafka broker**
  First we need to create a truststore using `java keytool` and we need to import our `ca-cert` into it.
    ```bash
    keytool -keystore kafka.broker0.truststore.jks -alias ca-cert -import -file ca-cert
    ```
  Then we need to create a keystore.
    ```bash
    keytool -keystore kafka.broker0.keystore.jks -alias broker0 -validity 9999 -genkey -keyalg RSA -ext SAN=dns:localhost
    ```
  Here, at this point it is important to set `Subject Alternative Name (SAN)` as your host DNS to prevent some `Host Name Verification` error.
  Also, when we run this command, it will ask us `What is your first and last name?` which represent the `Common Name (CN)`. We need to set this property as we set in `SAN` value.
  The other approach to prevent `Host Name Verification` error is setting `ssl.endpoint.identification.algorithm` field as an empty string inside `server.properties` file.

    ```
    ssl.endpoint.identification.algorithm=
    ```

  Now, let's create our certificate signing request and sign it.
  ```bash
  keytool -keystore kafka.broker0.keystore.jks -alias broker0 -certreq -file ca-request-broker0
  openssl x509 -req -CA ca-cert -CAkey ca-key -in ca-request-broker0 -out ca-signed-broker0 -days 9999 -CAcreateserial
  ```
  For the last step, import the signed certificate and CA into keystore.
    ```bash
    keytool -keystore kafka.consumer.keystore.jks -alias ca-cert -import -file ca-cert
    keytool -keystore kafka.consumer.keystore.jks -alias consumer -import -file ca-signed-consumer
    ```
  <br></br>

- **Configuring Kafka Brokers**
    - Inside the `server.properties` file we need to add following configurations.
      ```
      ssl.client.auth=required
      ssl.keystore.location=<PATH_TO_kafka.broker0.keystore.jks> //for the example application it is under resources folder.
      ssl.keystore.password=<keystore_password> //for the example application it is 123456
      ssl.key.password=<key_password> //for the example application it is 123456
      ssl.truststore.location=<PATH_TO_kafka.broker0.truststore.jks> //for the example application it is under resources folder.
      ssl.truststore.password=<truststore_password> //for the example application it is 123456
      ssl.protocol=TLSv1.2
      ```
    - Also we need to change `listeners` and `listener.security.protocol.map` properties as follow:
      ```
      listeners=PLAINTEXT://localhost:9092,SSL://localhost:9093 // to complitely disable not secured way you can delete `PLAINTEXT://localhost:9092` part.
      listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
      ```

      And run the broker with the `server.properties` configuration.
      <br></br>

- **Configuring Kafka Clients for This Application**
    - Create a new `truststore` and `keystore` files for each producer and consumer client as described in previous steps.
    - Move created `truststore` and `keystore` files under resources folder.
    - Open `application.base.conf` file and change the following keys:
    - ```
      secureConnection
      bootstrapServersSecure
      truststoreLocation
      truststorePassword
      keystoreLocation
      keystorePassword
      ```
/*
 * Copyright (c) 2020 Contributors to the Quarkus StartStop project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.quarkus.ts.startstop.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Michal Karm Babacek <karm@redhat.com>
 */
public enum CodeQuarkusExtensions {

    QUARKUS_RESTEASY("quarkus-resteasy", "RESTEasy JAX-RS", "98e", true),
    QUARKUS_RESTEASY_JACKSON("quarkus-resteasy-jackson", "RESTEasy Jackson", "pV1", true),
    QUARKUS_RESTEASY_JSONB("quarkus-resteasy-jsonb", "RESTEasy JSON-B", "49J", true),
    QUARKUS_VERTX_GRAPHQL("quarkus-vertx-graphql", "Eclipse Vert.x GraphQL", "F9R", false),
    QUARKUS_HIBERNATE_VALIDATOR("quarkus-hibernate-validator", "Hibernate Validator", "YjV", true),
    QUARKUS_REST_CLIENT_MUTINY("quarkus-rest-client-mutiny", "Mutiny support for REST Client", "Ph0", false),
    QUARKUS_REST_CLIENT("quarkus-rest-client", "REST Client", "ekA", true),
    QUARKUS_REST_CLIENT_JAXB("quarkus-rest-client-jaxb", "REST Client JAXB", "8iY", true),
    QUARKUS_REST_CLIENT_JSONB("quarkus-rest-client-jsonb", "REST Client JSON-B", "Q3z", true),
    QUARKUS_REST_CLIENT_JACKSON("quarkus-rest-client-jackson", "REST Client Jackson", "pJg", true),
    // TODO clarify vvvv
    QUARKUS_HIBERNATE_ORM_REST_DATA_PANACHE("quarkus-hibernate-orm-rest-data-panache", "REST resources for Hibernate ORM with Panache", "CPa", false),
    QUARKUS_MONGODB_REST_DATA_PANACHE("quarkus-mongodb-rest-data-panache", "REST resources for MongoDB with Panache", "2nT", false),
    QUARKUS_RESTEASY_JAXB("quarkus-resteasy-jaxb", "RESTEasy JAXB", "d7W", true),
    QUARKUS_RESTEASY_MULTIPART("quarkus-resteasy-multipart", "RESTEasy Multipart", "kV0", true),
    QUARKUS_RESTEASY_MUTINY("quarkus-resteasy-mutiny", "RESTEasy Mutiny", "8Tx", false),
    QUARKUS_RESTEASY_QUTE("quarkus-resteasy-qute", "RESTEasy Qute", "ju", false),
    // TODO Introduce buckets with reactive jax-rs extensions once they move from experimental / unsupported level
    // To avoid java.lang.IllegalStateException: The 'quarkus-resteasy-reactive' and 'quarkus-resteasy' extensions cannot be used at the same time.
//    QUARKUS_RESTEASY_REACTIVE("quarkus-resteasy-reactive", "RESTEasy Reactive", "WJu", false),
//    QUARKUS_RESTEASY_REACTIVE_JSONB("quarkus-resteasy-reactive-jsonb", "RESTEasy Reactive JSON-B", "sTz", false),
//    QUARKUS_RESTEASY_REACTIVE_JACKSON("quarkus-resteasy-reactive-jackson", "RESTEasy Reactive Jackson", "tEq", false),
//    QUARKUS_RESTEASY_REACTIVE_QUTE("quarkus-resteasy-reactive-qute", "RESTEasy Reactive Qute", "X8W", false),
    QUARKUS_REACTIVE_MESSAGING_HTTP("quarkus-reactive-messaging-http", "Reactive HTTP and WebSocket Connector", "5Gh", false),
    QUARKUS_VERTX_WEB("quarkus-vertx-web", "Reactive Routes", "JsG", true),
    QUARKUS_SMALLRYE_GRAPHQL("quarkus-smallrye-graphql", "SmallRye GraphQL", "jjB", false),
    QUARKUS_SMALLRYE_JWT("quarkus-smallrye-jwt", "SmallRye JWT", "D9x", true),
    QUARKUS_SMALLRYE_OPENAPI("quarkus-smallrye-openapi", "SmallRye OpenAPI", "ARC", true),
    QUARKUS_UNDERTOW("quarkus-undertow", "Undertow Servlet", "LMC", true),
    QUARKUS_WEBSOCKETS("websockets", "WebSockets", "JPE", true),
    QUARKUS_WEBJARS_LOCATOR("quarkus-webjars-locator", "WebJar Locator", "XSP", false),
    QUARKUS_GRPC("quarkus-grpc", "gRPC", "iDg", true),
    QUARKUS_HIBERNATE_ORM("quarkus-hibernate-orm", "Hibernate ORM", "vH0", true),
    QUARKUS_HIBERNATE_ORM_PANACHE("quarkus-hibernate-orm-panache", "Hibernate ORM with Panache", "XZs", true),
    QUARKUS_JDBC_POSTGRESQL("quarkus-jdbc-postgresql", "JDBC Driver - PostgreSQL", "8IG", true),
    QUARKUS_JDBC_MARIADB("quarkus-jdbc-mariadb", "JDBC Driver - MariaDB", "78I", true),
    QUARKUS_JDBC_MYSQL("quarkus-jdbc-mysql", "JDBC Driver - MySQL", "9Be", true),
    QUARKUS_JDBC_MSSQL("quarkus-jdbc-mssql", "JDBC Driver - Microsoft SQL Server", "R6e", true),
    QUARKUS_JDBC_DB2("quarkus-jdbc-db2", "JDBC Driver - DB2", "bMO", false),
    QUARKUS_JDBC_H2("quarkus-jdbc-h2", "JDBC Driver - H2", "FBQ", false),
    QUARKUS_JDBC_DERBY("quarkus-jdbc-derby", "JDBC Driver - Derby", "UCw", false),
    QUARKUS_AGROAL("quarkus-agroal", "Agroal - Database connection pool", "qiu", true),
    QUARKUS_AMAZON_DYNAMODB("quarkus-amazon-dynamodb", "Amazon DynamoDB", "fUt", false),
    QUARKUS_AMAZON_IAM("quarkus-amazon-iam", "Amazon IAM", "xlQ", false),
    QUARKUS_AMAZON_KMS("quarkus-amazon-kms", "Amazon KMS", "xWW", false),
    QUARKUS_AMAZON_S3("quarkus-amazon-s3", "Amazon S3", "q1T", false),
    QUARKUS_AMAZON_SES("quarkus-amazon-ses", "Amazon SES", "zSW", false),
    QUARKUS_AMAZON_SNS("quarkus-amazon-sns", "Amazon SNS", "zXr", false),
    QUARKUS_AMAZON_SQS("quarkus-amazon-sqs", "Amazon SQS", "zYW", false),
    // https://github.com/quarkusio/quarkus/issues/14445
    // BLAZE_PERSISTENCE_INTEGRATION_QUARKUS("blaze-persistence-integration-quarkus", "Blaze-Persistence", "weW", false),
    QUARKUS_CACHE("quarkus-cache", "Cache", "W1i", true),
    // https://github.com/datastax/cassandra-quarkus/issues/144
    // CASSANDRA_QUARKUS_CLIENT("cassandra-quarkus-client", "DataStax Apache Cassandra client", "mj6", false),
    QUARKUS_ELASTICSEARCH_REST_HIGH_LEVEL_CLIENT("quarkus-elasticsearch-rest-high-level-client", "Elasticsearch REST High Level Client", "OJe", false),
    QUARKUS_ELASTICSEARCH_REST_CLIENT("quarkus-elasticsearch-rest-client", "Elasticsearch REST client", "NhW", false),
    QUARKUS_FLYWAY("quarkus-flyway", "Flyway", "wTM", false),
    QUARKUS_GOOGLE_CLOUD_BIGQUERY("quarkus-google-cloud-bigquery", "Google Cloud Bigquery", "BZL", false),
    QUARKUS_HAZELCAST_CLIENT("quarkus-hazelcast-client", "Hazelcast Client", "yii", false),
    QUARKUS_HIBERNATE_ENVERS("quarkus-hibernate-envers", "Hibernate Envers", "8j9", false),
    QUARKUS_HIBERNATE_ORM_PANACHE_KOTLIN("quarkus-hibernate-orm-panache-kotlin", "Hibernate ORM with Panache and Kotlin", "O3q", false),
    QUARKUS_HIBERNATE_REACTIVE("quarkus-hibernate-reactive", "Hibernate Reactive", "r1s", false),
    QUARKUS_HIBERNATE_REACTIVE_PANACHE("quarkus-hibernate-reactive-panache", "Hibernate Reactive with Panache", "20r", false),
    QUARKUS_HIBERNATE_SEARCH_ORM_ELASTICSEARCH("quarkus-hibernate-search-orm-elasticsearch", "Hibernate Search + Elasticsearch", "oHk", false),
    QUARKUS_INFINISPAN_CLIENT("quarkus-infinispan-client", "Infinispan Client", "sOv", true),
    QUARKUS_LIQUIBASE("quarkus-liquibase", "Liquibase", "Xkd", false),
    MINIO_CLIENT("minio-client", "Minio Client extension", "LPo", false),
    QUARKUS_MONGODB_CLIENT("quarkus-mongodb-client", "MongoDB client", "ThS", false),
    QUARKUS_MONGODB_PANACHE("quarkus-mongodb-panache", "MongoDB with Panache", "y3v", false),
    QUARKUS_MONGODB_PANACHE_KOTLIN("quarkus-mongodb-panache-kotlin", "MongoDB with Panache for Kotlin", "gT", false),
    QUARKUS_NARAYANA_JTA("quarkus-narayana-jta", "Narayana JTA - Transaction manager", "PBP", true),
    QUARKUS_NARAYANA_STM("quarkus-narayana-stm", "Narayana STM - Software Transactional Memory", "Nl9", false),
    QUARKUS_NEO4J("quarkus-neo4j", "Neo4j client", "pDS", false),
    QUARKUS_REACTIVE_DB2_CLIENT("quarkus-reactive-db2-client", "Reactive DB2 client", "8XW", false),
    QUARKUS_REACTIVE_MYSQL_CLIENT("quarkus-reactive-mysql-client", "Reactive MySQL client", "fmW", false),
    QUARKUS_REACTIVE_PG_CLIENT("quarkus-reactive-pg-client", "Reactive PostgreSQL client", "ih0", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING("quarkus-smallrye-reactive-messaging", "SmallRye Reactive Messaging", "Nts", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING_AMQP("quarkus-smallrye-reactive-messaging-amqp", "SmallRye Reactive Messaging - AMQP Connector", "ur3", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING_KAFKA("quarkus-smallrye-reactive-messaging-kafka", "SmallRye Reactive Messaging - Kafka Connector", "9qf", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING_MQTT("quarkus-smallrye-reactive-messaging-mqtt", "SmallRye Reactive Messaging - MQTT Connector", "ZsB", false),
    // SRCFG00014: Property quarkus.qpid-jms.url is required but the value was not found or is empty
    // https://github.com/quarkusio/quarkus/issues/8506
    // QUARKUS_QPID_JMS("quarkus-qpid-jms", "AMQP 1.0 JMS client - Apache Qpid JMS", "Zdm", false),
    QUARKUS_KAFKA_CLIENT("quarkus-kafka-client", "Apache Kafka Client", "FKK", true),
    // SRCFG00014: Property quarkus.kafka-streams.topics is required but the value was not found or is empty
    // https://github.com/quarkusio/quarkus/issues/8506
    // QUARKUS_KAFKA_STREAMS("quarkus-kafka-streams", "Apache Kafka Streams", "ShK", false),
    // SRCFG00014: Property quarkus.artemis.url is required but the value was not found or is empty
    // https://github.com/quarkusio/quarkus/issues/8506
    // QUARKUS_ARTEMIS_JMS("quarkus-artemis-jms", "Artemis JMS", "DWo", false),
    QUARKUS_GOOGLE_CLOUD_PUBSUB("quarkus-google-cloud-pubsub", "Google Cloud Pubsub", "Rbo", false),
    QUARKUS_CONFIG_YAML("quarkus-config-yaml", "YAML Configuration", "UAO", true),
    QUARKUS_LOGGING_JSON("quarkus-logging-json", "Logging JSON", "7RG", true),
    QUARKUS_LOGGING_GELF("quarkus-logging-gelf", "Logging GELF", "wCO", false),
    QUARKUS_LOGGING_SENTRY("quarkus-logging-sentry", "Logging Sentry", "eoJ", false),
    QUARKUS_VERTX("quarkus-vertx", "Eclipse Vert.x", "WqB", true),
    QUARKUS_MUTINY("quarkus-mutiny", "Mutiny", "zqM", true),
    QUARKUS_REDIS_CLIENT("quarkus-redis-client", "Redis Client", "jlX", false),
    QUARKUS_SMALLRYE_CONTEXT_PROPAGATION("quarkus-smallrye-context-propagation", "SmallRye Context Propagation", "7pM", true),
    QUARKUS_KUBERNETES("quarkus-kubernetes", "Kubernetes", "dZK", false),
    QUARKUS_OPENSHIFT("quarkus-openshift", "OpenShift", "Bqi", false),
    QUARKUS_SMALLRYE_HEALTH("quarkus-smallrye-health", "SmallRye Health", "tqK", true),
    QUARKUS_SMALLRYE_FAULT_TOLERANCE("quarkus-smallrye-fault-tolerance", "SmallRye Fault Tolerance", "vgg", true),
    QUARKUS_AMAZON_LAMBDA("quarkus-amazon-lambda", "AWS Lambda", "ia1", false),
    QUARKUS_AMAZON_LAMBDA_HTTP("quarkus-amazon-lambda-http", "AWS Lambda HTTP", "L0j", false),
    QUARKUS_AMAZON_LAMBDA_XRAY("quarkus-amazon-lambda-xray", "AWS Lambda X-Ray", "JsX", false),
    QUARKUS_AMAZON_ALEXA("quarkus-amazon-alexa", "Amazon Alexa", "5zI", false),
    QUARKUS_AZURE_FUNCTIONS_HTTP("quarkus-azure-functions-http", "Azure Functions HTTP", "3YS", false),
    QUARKUS_CONTAINER_IMAGE_DOCKER("quarkus-container-image-docker", "Container Image Docker", "V9i", false),
    QUARKUS_CONTAINER_IMAGE_JIB("quarkus-container-image-jib", "Container Image Jib", "qZz", false),
    QUARKUS_CONTAINER_IMAGE_OPENSHIFT("quarkus-container-image-openshift", "Container Image OpenShift", "fPM", true),
    QUARKUS_CONTAINER_IMAGE_S2I("quarkus-container-image-s2i", "Container Image S2I", "pbs", false),
    QUARKUS_FUNQY_AMAZON_LAMBDA("quarkus-funqy-amazon-lambda", "Funqy AWS Lambda Binding", "Pqi", false),
    QUARKUS_FUNQY_GOOGLE_CLOUD_FUNCTIONS("quarkus-funqy-google-cloud-functions", "Funqy Google Cloud Functions", "K2H", false),
    QUARKUS_FUNQY_HTTP("quarkus-funqy-http", "Funqy HTTP Binding", "oru", false),
    QUARKUS_FUNQY_KNATIVE_EVENTS("quarkus-funqy-knative-events", "Funqy Knative Events Binding", "yZc", false),
    QUARKUS_GOOGLE_CLOUD_FUNCTIONS("quarkus-google-cloud-functions", "Google Cloud Functions", "Njx", false),
    QUARKUS_GOOGLE_CLOUD_FUNCTIONS_HTTP("quarkus-google-cloud-functions-http", "Google Cloud Functions HTTP", "Ej9", false),
    QUARKUS_KUBERNETES_CLIENT("quarkus-kubernetes-client", "Kubernetes Client", "Spa", false),
    QUARKUS_KUBERNETES_CONFIG("quarkus-kubernetes-config", "Kubernetes Config", "VUX", false),
    QUARKUS_KUBERNETES_SERVICE_BINDING("quarkus-kubernetes-service-binding", "Kubernetes Service Binding", "8iE", false),
    QUARKUS_MINIKUBE("quarkus-minikube", "Minikube", "VUy", false),
    QUARKUS_OPENSHIFT_CLIENT("quarkus-openshift-client", "OpenShift Client", "CxU", false),
    QUARKUS_MICROMETER("quarkus-micrometer", "Micrometer metrics", "Svz", true),
    QUARKUS_SMALLRYE_METRICS("quarkus-smallrye-metrics", "SmallRye Metrics", "Ll4", false), //intentionally because of micrometer
    QUARKUS_SMALLRYE_OPENTRACING("quarkus-smallrye-opentracing", "SmallRye OpenTracing", "f7", true),
//  Commented out as it would require touching application.properties with confAppPropsForSkeleton(String appDir)
//  https://github.com/quarkusio/quarkus/issues/8506
//  QUARKUS_OIDC("quarkus-oidc", "OpenID Connect", "fgL", true),
    QUARKUS_ELYTRON_SECURITY_JDBC("quarkus-elytron-security-jdbc", "Elytron Security JDBC", "Pxs", false),
    QUARKUS_ELYTRON_SECURITY_LDAP("quarkus-elytron-security-ldap", "Elytron Security LDAP Realm", "z3K", false),
    QUARKUS_ELYTRON_SECURITY_OAUTH2("quarkus-elytron-security-oauth2", "Elytron Security OAuth 2.0", "9Ie", false),
    QUARKUS_ELYTRON_SECURITY_PROPERTIES_FILE("quarkus-elytron-security-properties-file", "Elytron Security Properties File", "Dk8", false),
    QUARKUS_KEYCLOAK_ADMIN_CLIENT("quarkus-keycloak-admin-client", "Keycloak Admin Client", "d1H", false),
//  Commented out as it would require touching application.properties with confAppPropsForSkeleton(String appDir)
//  https://github.com/quarkusio/quarkus/issues/8506
//  QUARKUS_KEYCLOAK_AUTHORIZATION("quarkus-keycloak-authorization", "Keycloak Authorization", "2Bx", false),
    QUARKUS_OIDC_CLIENT("quarkus-oidc-client", "OpenID Connect Client", "wfZ", false),
    QUARKUS_OIDC_CLIENT_FILTER("quarkus-oidc-client-filter", "OpenID Connect Client Filter", "T0U", false),
    QUARKUS_OIDC_TOKEN_PROPAGATION("quarkus-oidc-token-propagation", "OpenID Connect Token Propagation", "Bg9", false),
    QUARKUS_SECURITY_JPA("quarkus-security-jpa", "Security JPA", "W8w", false),
    QUARKUS_SMALLRYE_JWT_BUILD("quarkus-smallrye-jwt-build", "SmallRye JWT Build", "Phi", true),
    QUARKUS_VAULT("quarkus-vault", "Vault", "r73", false),
    CAMEL_QUARKUS_CORE("camel-quarkus-core", "Camel Core", "8bB", false),
    CAMEL_QUARKUS_AMQP("camel-quarkus-amqp", "Camel AMQP", "maV", false),
    CAMEL_QUARKUS_AS2("camel-quarkus-as2", "Camel AS2", "tqO", false),
    CAMEL_QUARKUS_AWS2_ATHENA("camel-quarkus-aws2-athena", "Camel AWS 2 Athena", "hqL", false),
    CAMEL_QUARKUS_AWS2_CW("camel-quarkus-aws2-cw", "Camel AWS 2 CloudWatch", "xc", false),
    CAMEL_QUARKUS_AWS2_DDB("camel-quarkus-aws2-ddb", "Camel AWS 2 DynamoDB", "JWu", false),
    CAMEL_QUARKUS_AWS2_EC2("camel-quarkus-aws2-ec2", "Camel AWS 2 Elastic Compute Cloud (EC2)", "KaI", false),
    CAMEL_QUARKUS_AWS2_ECS("camel-quarkus-aws2-ecs", "Camel AWS 2 Elastic Container Service (ECS)", "KbL", false),
    CAMEL_QUARKUS_AWS2_EKS("camel-quarkus-aws2-eks", "Camel AWS 2 Elastic Kubernetes Service (EKS)", "KfL", false),
    CAMEL_QUARKUS_AWS2_IAM("camel-quarkus-aws2-iam", "Camel AWS 2 Identity and Access Management (IAM)", "LaF", false),
    CAMEL_QUARKUS_AWS2_KMS("camel-quarkus-aws2-kms", "Camel AWS 2 Key Management Service (KMS)", "LLL", false),
    CAMEL_QUARKUS_AWS2_LAMBDA("camel-quarkus-aws2-lambda", "Camel AWS 2 Lambda", "v3L", false),
    CAMEL_QUARKUS_AWS2_MQ("camel-quarkus-aws2-mq", "Camel AWS 2 MQ", "B7", false),
    CAMEL_QUARKUS_AWS2_MSK("camel-quarkus-aws2-msk", "Camel AWS 2 Managed Streaming for Apache Kafka (MSK)", "MjD", false),
    CAMEL_QUARKUS_AWS2_S3("camel-quarkus-aws2-s3", "Camel AWS 2 S3 Storage Service", "D7", false),
    CAMEL_QUARKUS_AWS2_STS("camel-quarkus-aws2-sts", "Camel AWS 2 Security Token Service (STS)", "NPg", false),
    CAMEL_QUARKUS_AWS2_SES("camel-quarkus-aws2-ses", "Camel AWS 2 Simple Email Service (SES)", "NHL", false),
    CAMEL_QUARKUS_AWS2_SNS("camel-quarkus-aws2-sns", "Camel AWS 2 Simple Notification System (SNS)", "NMg", false),
    CAMEL_QUARKUS_AWS2_SQS("camel-quarkus-aws2-sqs", "Camel AWS 2 Simple Queue Service (SQS)", "NNL", false),
    CAMEL_QUARKUS_AWS2_TRANSLATE("camel-quarkus-aws2-translate", "Camel AWS 2 Translate", "67i", false),
    CAMEL_QUARKUS_AWS_EC2("camel-quarkus-aws-ec2", "Camel AWS Elastic Compute Cloud (EC2)", "MMA", false),
    CAMEL_QUARKUS_AWS_ECS("camel-quarkus-aws-ecs", "Camel AWS Elastic Container Service (ECS)", "MND", false),
    CAMEL_QUARKUS_AWS_EKS("camel-quarkus-aws-eks", "Camel AWS Elastic Kubernetes Service (EKS)", "MRD", false),
    CAMEL_QUARKUS_AWS_IAM("camel-quarkus-aws-iam", "Camel AWS Identity and Access Management (IAM)", "NMx", false),
    CAMEL_QUARKUS_AWS_KMS("camel-quarkus-aws-kms", "Camel AWS Key Management Service (KMS)", "OnD", false),
    CAMEL_QUARKUS_AWS_KINESIS("camel-quarkus-aws-kinesis", "Camel AWS Kinesis", "3SM", false),
    CAMEL_QUARKUS_AWS_LAMBDA("camel-quarkus-aws-lambda", "Camel AWS Lambda", "7q0", false),
    CAMEL_QUARKUS_AWS_S3("camel-quarkus-aws-s3", "Camel AWS S3 Storage Service", "V7W", false),
    CAMEL_QUARKUS_AWS_SNS("camel-quarkus-aws-sns", "Camel AWS Simple Notification System (SNS)", "Qn9", false),
    CAMEL_QUARKUS_AWS_SQS("camel-quarkus-aws-sqs", "Camel AWS Simple Queue Service (SQS)", "QpD", false),
    CAMEL_QUARKUS_AWS_SWF("camel-quarkus-aws-swf", "Camel AWS Simple Workflow (SWF)", "Qsq", false),
    CAMEL_QUARKUS_AWS_SDB("camel-quarkus-aws-sdb", "Camel AWS SimpleDB (SDB)", "QiR", false),
    CAMEL_QUARKUS_AWS_TRANSLATE("camel-quarkus-aws-translate", "Camel AWS Translate", "xTI", false),
    CAMEL_QUARKUS_ACTIVEMQ("camel-quarkus-activemq", "Camel ActiveMQ", "EPu", false),
    CAMEL_QUARKUS_ARANGODB("camel-quarkus-arangodb", "Camel ArangoDb", "NDy", false),
    CAMEL_QUARKUS_AHC("camel-quarkus-ahc", "Camel Async HTTP Client (AHC)", "tl7", false),
    CAMEL_QUARKUS_AHC_WS("camel-quarkus-ahc-ws", "Camel Async HTTP Client (AHC) Websocket", "Mmt", false),
    CAMEL_QUARKUS_ATOM("camel-quarkus-atom", "Camel Atom", "krt", false),
    CAMEL_QUARKUS_ATTACHMENTS("camel-quarkus-attachments", "Camel Attachments", "xl5", false),
    CAMEL_QUARKUS_AVRO("camel-quarkus-avro", "Camel Avro", "jUW", false),
    CAMEL_QUARKUS_AVRO_RPC("camel-quarkus-avro-rpc", "Camel Avro RPC", "k21", false),
    CAMEL_QUARKUS_AZURE("camel-quarkus-azure", "Camel Azure", "dW2", false),
    CAMEL_QUARKUS_AZURE_STORAGE_BLOB("camel-quarkus-azure-storage-blob", "Camel Azure Storage Blob Service", "SjJ", false),
    CAMEL_QUARKUS_BASE64("camel-quarkus-base64", "Camel Base64", "siX", false),
    CAMEL_QUARKUS_BEAN("camel-quarkus-bean", "Camel Bean", "gys", false),
    CAMEL_QUARKUS_BEAN_VALIDATOR("camel-quarkus-bean-validator", "Camel Bean Validator", "bMp", false),
    CAMEL_QUARKUS_BINDY("camel-quarkus-bindy", "Camel Bindy", "xlc", false),
    CAMEL_QUARKUS_BOX("camel-quarkus-box", "Camel Box", "tFr", false),
    CAMEL_QUARKUS_BRAINTREE("camel-quarkus-braintree", "Camel Braintree", "MXc", false),
    CAMEL_QUARKUS_BROWSE("camel-quarkus-browse", "Camel Browse", "RwK", false),
    CAMEL_QUARKUS_CSV("camel-quarkus-csv", "Camel CSV", "tWU", false),
    CAMEL_QUARKUS_CSIMPLE("camel-quarkus-csimple", "Camel CSimple", "v1Z", false),
    CAMEL_QUARKUS_CAFFEINE("camel-quarkus-caffeine", "Camel Caffeine Cache", "ku2", false),
    CAMEL_QUARKUS_CAFFEINE_LRUCACHE("camel-quarkus-caffeine-lrucache", "Camel Caffeine LRUCache", "IdP", false),
    CAMEL_QUARKUS_CORE_CLOUD("camel-quarkus-core-cloud", "Camel Cloud", "AvJ", false),
    CAMEL_QUARKUS_COMPONENTDSL("camel-quarkus-componentdsl", "Camel Component DSL", "WxS", false),
    CAMEL_QUARKUS_CONSUL("camel-quarkus-consul", "Camel Consul", "s5m", false),
    CAMEL_QUARKUS_CONTROLBUS("camel-quarkus-controlbus", "Camel Control Bus", "Lwz", false),
    CAMEL_QUARKUS_COUCHDB("camel-quarkus-couchdb", "Camel CouchDB", "cSm", false),
    CAMEL_QUARKUS_CRON("camel-quarkus-cron", "Camel Cron", "7ss", false),
    CAMEL_QUARKUS_CRYPTO("camel-quarkus-crypto", "Camel Crypto (JCE)", "e7p", false),
    CAMEL_QUARKUS_DATAFORMAT("camel-quarkus-dataformat", "Camel Data Format", "oRh", false),
    CAMEL_QUARKUS_DEBEZIUM_MONGODB("camel-quarkus-debezium-mongodb", "Camel Debezium MongoDB Connector", "367", false),
    CAMEL_QUARKUS_DEBEZIUM_MYSQL("camel-quarkus-debezium-mysql", "Camel Debezium MySQL Connector", "vQa", false),
    CAMEL_QUARKUS_DEBEZIUM_POSTGRES("camel-quarkus-debezium-postgres", "Camel Debezium PostgresSQL Connector", "M6D", false),
    CAMEL_QUARKUS_DEBEZIUM_SQLSERVER("camel-quarkus-debezium-sqlserver", "Camel Debezium SQL Server Connector", "ycD", false),
    CAMEL_QUARKUS_DIRECT("camel-quarkus-direct", "Camel Direct", "c8Z", false),
    CAMEL_QUARKUS_DISRUPTOR("camel-quarkus-disruptor", "Camel Disruptor", "nnM", false),
    CAMEL_QUARKUS_DOZER("camel-quarkus-dozer", "Camel Dozer", "9tO", false),
    CAMEL_QUARKUS_DROPBOX("camel-quarkus-dropbox", "Camel Dropbox", "GNG", false),
    CAMEL_QUARKUS_ELASTICSEARCH_REST("camel-quarkus-elasticsearch-rest", "Camel Elasticsearch Rest", "4Tk", false),
    CAMEL_QUARKUS_ENDPOINTDSL("camel-quarkus-endpointdsl", "Camel Endpoint DSL", "qoo", false),
    CAMEL_QUARKUS_EXEC("camel-quarkus-exec", "Camel Exec", "PxD", false),
    CAMEL_QUARKUS_FHIR("camel-quarkus-fhir", "Camel FHIR", "LKT", false),
    CAMEL_QUARKUS_FOP("camel-quarkus-fop", "Camel FOP", "uFj", false),
    CAMEL_QUARKUS_FTP("camel-quarkus-ftp", "Camel FTP", "uHO", false),
    CAMEL_QUARKUS_FILE("camel-quarkus-file", "Camel File", "Lt7", false),
    CAMEL_QUARKUS_FILE_WATCH("camel-quarkus-file-watch", "Camel File Watch", "evi", false),
    CAMEL_QUARKUS_FLATPACK("camel-quarkus-flatpack", "Camel Flatpack", "xN9", false),
    CAMEL_QUARKUS_GEOCODER("camel-quarkus-geocoder", "Camel Geocoder", "evQ", false),
    CAMEL_QUARKUS_GIT("camel-quarkus-git", "Camel Git", "uRS", false),
    CAMEL_QUARKUS_GITHUB("camel-quarkus-github", "Camel GitHub", "34H", false),
    CAMEL_QUARKUS_GOOGLE_BIGQUERY("camel-quarkus-google-bigquery", "Camel Google BigQuery", "Fgy", false),
    CAMEL_QUARKUS_GOOGLE_CALENDAR("camel-quarkus-google-calendar", "Camel Google Calendar", "p8U", false),
    CAMEL_QUARKUS_GOOGLE_DRIVE("camel-quarkus-google-drive", "Camel Google Drive", "kiA", false),
    CAMEL_QUARKUS_GOOGLE_MAIL("camel-quarkus-google-mail", "Camel Google Mail", "Z0L", false),
    CAMEL_QUARKUS_GOOGLE_PUBSUB("camel-quarkus-google-pubsub", "Camel Google Pubsub", "UlJ", false),
    CAMEL_QUARKUS_GOOGLE_SHEETS("camel-quarkus-google-sheets", "Camel Google Sheets", "Aws", false),
    CAMEL_QUARKUS_GRAPHQL("camel-quarkus-graphql", "Camel GraphQL", "MSX", false),
    CAMEL_QUARKUS_GROK("camel-quarkus-grok", "Camel Grok", "Bsv", false),
    CAMEL_QUARKUS_GSON("camel-quarkus-gson", "Camel Gson", "BcX", false),
    CAMEL_QUARKUS_HTTP("camel-quarkus-http", "Camel HTTP", "taq", false),
    CAMEL_QUARKUS_HAZELCAST("camel-quarkus-hazelcast", "Camel Hazelcast Atomic Number", "X86", false),
    CAMEL_QUARKUS_HEADERSMAP("camel-quarkus-headersmap", "Camel Headersmap", "FNE", false),
    CAMEL_QUARKUS_HYSTRIX("camel-quarkus-hystrix", "Camel Hystrix", "UNf", false),
    CAMEL_QUARKUS_INFINISPAN("camel-quarkus-infinispan", "Camel Infinispan", "QXx", false),
    CAMEL_QUARKUS_INFLUXDB("camel-quarkus-influxdb", "Camel InfluxDB", "P75", false),
    CAMEL_QUARKUS_JAXB("camel-quarkus-jaxb", "Camel JAXB", "in0", false),
    CAMEL_QUARKUS_JDBC("camel-quarkus-jdbc", "Camel JDBC", "hOD", false),
    CAMEL_QUARKUS_JMS("camel-quarkus-jms", "Camel JMS", "vEm", false),
    CAMEL_QUARKUS_JOLT("camel-quarkus-jolt", "Camel JOLT", "eYR", false),
    CAMEL_QUARKUS_JPA("camel-quarkus-jpa", "Camel JPA", "vFz", false),
    CAMEL_QUARKUS_JSLT("camel-quarkus-jslt", "Camel JSLT", "dYR", false),
    CAMEL_QUARKUS_JSONPATH("camel-quarkus-jsonpath", "Camel JSON Path", "kWp", false),
    CAMEL_QUARKUS_JSON_VALIDATOR("camel-quarkus-json-validator", "Camel JSON Schema Validator", "YZZ", false),
    CAMEL_QUARKUS_JSONB("camel-quarkus-jsonb", "Camel JSON-B", "N25", false),
    CAMEL_QUARKUS_JSONATA("camel-quarkus-jsonata", "Camel JSONATA", "qWC", false),
    CAMEL_QUARKUS_JTA("camel-quarkus-jta", "Camel JTA", "vHz", false),
    CAMEL_QUARKUS_JACKSON("camel-quarkus-jackson", "Camel Jackson", "Sbp", false),
    CAMEL_QUARKUS_JACKSONXML("camel-quarkus-jacksonxml", "Camel JacksonXML", "RQA", false),
    CAMEL_QUARKUS_WEBSOCKET_JSR356("camel-quarkus-websocket-jsr356", "Camel Javax Websocket (JSR 356)", "RxD", false),
    CAMEL_QUARKUS_JING("camel-quarkus-jing", "Camel Jing", "gs5", false),
    CAMEL_QUARKUS_JIRA("camel-quarkus-jira", "Camel Jira", "gra", false),
    CAMEL_QUARKUS_JOHNZON("camel-quarkus-johnzon", "Camel Johnzon", "MqU", false),
    CAMEL_QUARKUS_KAFKA("camel-quarkus-kafka", "Camel Kafka", "ezA", false),
    CAMEL_QUARKUS_KOTLIN("camel-quarkus-kotlin", "Camel Kotlin", "GsT", false),
    CAMEL_QUARKUS_KUBERNETES("camel-quarkus-kubernetes", "Camel Kubernetes", "9b3", false),
    CAMEL_QUARKUS_KUDU("camel-quarkus-kudu", "Camel Kudu", "7Ol", false),
    CAMEL_QUARKUS_LEVELDB("camel-quarkus-leveldb", "Camel LevelDB", "x7e", false),
    CAMEL_QUARKUS_LOG("camel-quarkus-log", "Camel Log", "waa", false),
    CAMEL_QUARKUS_LUMBERJACK("camel-quarkus-lumberjack", "Camel Lumberjack", "1TQ", false),
    CAMEL_QUARKUS_MSV("camel-quarkus-msv", "Camel MSV", "wrU", false),
    CAMEL_QUARKUS_MAIL("camel-quarkus-mail", "Camel Mail", "WgZ", false),
    CAMEL_QUARKUS_MAIN("camel-quarkus-main", "Camel Main", "WgX", false),
    CAMEL_QUARKUS_MASTER("camel-quarkus-master", "Camel Master", "AJs", false),
    CAMEL_QUARKUS_MICROPROFILE_HEALTH("camel-quarkus-microprofile-health", "Camel MicroProfile Health", "OJ5", false),
    CAMEL_QUARKUS_MICROPROFILE_METRICS("camel-quarkus-microprofile-metrics", "Camel MicroProfile Metrics", "cvj", false),
    CAMEL_QUARKUS_MICROMETER("camel-quarkus-micrometer", "Camel Micrometer", "RE8", false),
    CAMEL_QUARKUS_MICROPROFILE_FAULT_TOLERANCE("camel-quarkus-microprofile-fault-tolerance", "Camel Microprofile Fault Tolerance", "885", false),
    CAMEL_QUARKUS_MINIO("camel-quarkus-minio", "Camel Minio", "Bem", false),
    CAMEL_QUARKUS_MOCK("camel-quarkus-mock", "Camel Mock", "SO1", false),
    CAMEL_QUARKUS_MONGODB("camel-quarkus-mongodb", "Camel MongoDB", "Y9a", false),
    CAMEL_QUARKUS_MONGODB_GRIDFS("camel-quarkus-mongodb-gridfs", "Camel MongoDB GridFS", "rNa", false),
    CAMEL_QUARKUS_MUSTACHE("camel-quarkus-mustache", "Camel Mustache", "Edg", false),
    CAMEL_QUARKUS_NSQ("camel-quarkus-nsq", "Camel NSQ", "wHk", false),
    CAMEL_QUARKUS_NAGIOS("camel-quarkus-nagios", "Camel Nagios", "fVt", false),
    CAMEL_QUARKUS_NATS("camel-quarkus-nats", "Camel Nats", "OqS", false),
    CAMEL_QUARKUS_NETTY("camel-quarkus-netty", "Camel Netty", "frc", false),
    CAMEL_QUARKUS_NETTY_HTTP("camel-quarkus-netty-http", "Camel Netty HTTP", "Bl8", false),
    CAMEL_QUARKUS_OLINGO4("camel-quarkus-olingo4", "Camel Olingo4", "8YU", false),
    CAMEL_QUARKUS_OPENAPI_JAVA("camel-quarkus-openapi-java", "Camel OpenAPI Java", "Hbh", false),
    CAMEL_QUARKUS_OPENTRACING("camel-quarkus-opentracing", "Camel OpenTracing", "0Q1", false),
    CAMEL_QUARKUS_OPTAPLANNER("camel-quarkus-optaplanner", "Camel OptaPlanner", "Xai", false),
    CAMEL_QUARKUS_PDF("camel-quarkus-pdf", "Camel PDF", "w5E", false),
    CAMEL_QUARKUS_PAHO("camel-quarkus-paho", "Camel Paho", "y2W", false),
    CAMEL_QUARKUS_PLATFORM_HTTP("camel-quarkus-platform-http", "Camel Platform HTTP", "d0M", false),
    CAMEL_QUARKUS_PGEVENT("camel-quarkus-pgevent", "Camel PostgresSQL Event", "P3X", false),
    CAMEL_QUARKUS_PG_REPLICATION_SLOT("camel-quarkus-pg-replication-slot", "Camel PostgresSQL Replication Slot", "Oux", false),
    CAMEL_QUARKUS_PROTOBUF("camel-quarkus-protobuf", "Camel Protobuf", "odV", false),
    CAMEL_QUARKUS_LZF("camel-quarkus-lzf", "Camel Quarkus LZF", "wfE", false),
    CAMEL_QUARKUS_ZIP_DEFLATER("camel-quarkus-zip-deflater", "Camel Quarkus Zip Deflate", "Zw2", false),
    CAMEL_QUARKUS_QUARTZ("camel-quarkus-quartz", "Camel Quartz", "m26", false),
    CAMEL_QUARKUS_QUTE("camel-quarkus-qute", "Camel Qute", "mbB", false),
    CAMEL_QUARKUS_REST_OPENAPI("camel-quarkus-rest-openapi", "Camel REST OpenApi", "XJ8", false),
    CAMEL_QUARKUS_RSS("camel-quarkus-rss", "Camel RSS", "xHm", false),
    CAMEL_QUARKUS_RABBITMQ("camel-quarkus-rabbitmq", "Camel RabbitMQ", "YFS", false),
    CAMEL_QUARKUS_REACTIVE_EXECUTOR("camel-quarkus-reactive-executor", "Camel Reactive Executor", "VH6", false),
    CAMEL_QUARKUS_REACTIVE_STREAMS("camel-quarkus-reactive-streams", "Camel Reactive Streams", "PEr", false),
    CAMEL_QUARKUS_REF("camel-quarkus-ref", "Camel Ref", "xz0", false),
    CAMEL_QUARKUS_REST("camel-quarkus-rest", "Camel Rest", "irm", false),
    CAMEL_QUARKUS_SAP_NETWEAVER("camel-quarkus-sap-netweaver", "Camel SAP NetWeaver", "EcI", false),
    CAMEL_QUARKUS_JSCH("camel-quarkus-jsch", "Camel SCP", "d4y", false),
    CAMEL_QUARKUS_SEDA("camel-quarkus-seda", "Camel SEDA", "OF", false),
    CAMEL_QUARKUS_SOAP("camel-quarkus-soap", "Camel SOAP dataformat", "0lV", false),
    CAMEL_QUARKUS_SQL("camel-quarkus-sql", "Camel SQL", "xVK", false),
    CAMEL_QUARKUS_SSH("camel-quarkus-ssh", "Camel SSH", "xWG", false),
    CAMEL_QUARKUS_SAGA("camel-quarkus-saga", "Camel Saga", "bNa", false),
    CAMEL_QUARKUS_SALESFORCE("camel-quarkus-salesforce", "Camel Salesforce", "Jip", false),
    CAMEL_QUARKUS_SCHEDULER("camel-quarkus-scheduler", "Camel Scheduler", "hqJ", false),
    CAMEL_QUARKUS_SERVICENOW("camel-quarkus-servicenow", "Camel ServiceNow", "DqL", false),
    CAMEL_QUARKUS_SERVLET("camel-quarkus-servlet", "Camel Servlet", "sBr", false),
    CAMEL_QUARKUS_SHIRO("camel-quarkus-shiro", "Camel Shiro", "R2R", false),
    CAMEL_QUARKUS_SJMS("camel-quarkus-sjms", "Camel Simple JMS", "bavn", false),
    CAMEL_QUARKUS_SJMS2("camel-quarkus-sjms2", "Camel Simple JMS2", "Bxl", false),
    CAMEL_QUARKUS_SLACK("camel-quarkus-slack", "Camel Slack", "o0q", false),
    CAMEL_QUARKUS_SMALLRYE_REACTIVE_MESSAGING("camel-quarkus-smallrye-reactive-messaging", "Camel SmallRye Reactive Messaging", "Aow", false),
    CAMEL_QUARKUS_SNAKEYAML("camel-quarkus-snakeyaml", "Camel SnakeYAML", "4bb", false),
    CAMEL_QUARKUS_SOLR("camel-quarkus-solr", "Camel Solr", "0go", false),
    CAMEL_QUARKUS_STREAM("camel-quarkus-stream", "Camel Stream", "YQS", false),
    CAMEL_QUARKUS_STRINGTEMPLATE("camel-quarkus-stringtemplate", "Camel String Template", "oh2", false),
    CAMEL_QUARKUS_TAGSOUP("camel-quarkus-tagsoup", "Camel TagSoup (a.k.a. TidyMarkup)", "Ggv", false),
    CAMEL_QUARKUS_TARFILE("camel-quarkus-tarfile", "Camel Tar File", "mub", false),
    CAMEL_QUARKUS_TELEGRAM("camel-quarkus-telegram", "Camel Telegram", "y44", false),
    CAMEL_QUARKUS_THREADPOOLFACTORY_VERTX("camel-quarkus-threadpoolfactory-vertx", "Camel ThreadPoolFactory Vert.x", "Yem", false),
    CAMEL_QUARKUS_TIKA("camel-quarkus-tika", "Camel Tika", "32F", false),
    CAMEL_QUARKUS_TIMER("camel-quarkus-timer", "Camel Timer", "Vcj", false),
    CAMEL_QUARKUS_TWILIO("camel-quarkus-twilio", "Camel Twilio", "wkU", false),
    CAMEL_QUARKUS_TWITTER("camel-quarkus-twitter", "Camel Twitter", "ySR", false),
    CAMEL_QUARKUS_VM("camel-quarkus-vm", "Camel VM", "4hL", false),
    CAMEL_QUARKUS_VALIDATOR("camel-quarkus-validator", "Camel Validator", "42M", false),
    CAMEL_QUARKUS_VELOCITY("camel-quarkus-velocity", "Camel Velocity", "ZJp", false),
    CAMEL_QUARKUS_VERTX("camel-quarkus-vertx", "Camel Vert.x", "Lrd", false),
    CAMEL_QUARKUS_VERTX_HTTP("camel-quarkus-vertx-http", "Camel Vert.x HTTP Client", "M9a", false),
    CAMEL_QUARKUS_VERTX_KAFKA("camel-quarkus-vertx-kafka", "Camel Vert.x Kafka", "2WM", false),
    CAMEL_QUARKUS_VERTX_WEBSOCKET("camel-quarkus-vertx-websocket", "Camel Vert.x WebSocket", "SHn", false),
    CAMEL_QUARKUS_WEATHER("camel-quarkus-weather", "Camel Weather", "ST3", false),
    CAMEL_QUARKUS_XML_IO("camel-quarkus-xml-io", "Camel XML IO", "gjU", false),
    CAMEL_QUARKUS_XML_JAXB("camel-quarkus-xml-jaxb", "Camel XML JAXB", "18L", false),
    CAMEL_QUARKUS_XML_JAXP("camel-quarkus-xml-jaxp", "Camel XML JAXP", "18x", false),
    CAMEL_QUARKUS_XPATH("camel-quarkus-xpath", "Camel XPath", "Q6t", false),
    CAMEL_QUARKUS_XSLT("camel-quarkus-xslt", "Camel XSLT", "vvR", false),
    CAMEL_QUARKUS_XSTREAM("camel-quarkus-xstream", "Camel XStream", "eCC", false),
    CAMEL_QUARKUS_ZENDESK("camel-quarkus-zendesk", "Camel Zendesk", "wKG", false),
    CAMEL_QUARKUS_ZIPFILE("camel-quarkus-zipfile", "Camel Zip File", "baW6", false),
    CAMEL_QUARKUS_GRPC("camel-quarkus-grpc", "Camel gRPC", "Br9", false),
    CAMEL_QUARKUS_ICAL("camel-quarkus-ical", "Camel iCal", "pOZ", false),
    CAMEL_QUARKUS_UNIVOCITY_PARSERS("camel-quarkus-univocity-parsers", "Camel uniVocity CSV", "AQL", false),
    DEBEZIUM_QUARKUS_OUTBOX("debezium-quarkus-outbox", "Debezium Quarkus Outbox", "Zv2", false),
    KOGITO_QUARKUS("kogito-quarkus", "Kogito", "fYM", false),
    OPTAPLANNER_QUARKUS("optaplanner-quarkus", "OptaPlanner AI constraint solver", "PHF", false),
    OPTAPLANNER_QUARKUS_JSONB("optaplanner-quarkus-jsonb", "OptaPlanner JSON-B", "CWK", false),
    OPTAPLANNER_QUARKUS_JACKSON("optaplanner-quarkus-jackson", "OptaPlanner Jackson", "HNL", false),
    QUARKUS_AVRO("quarkus-avro", "Apache Avro", "Aas", false),
    QUARKUS_JAXB("quarkus-jaxb", "JAXB", "BHf", true),
    QUARKUS_JSONB("quarkus-jsonb", "JSON-B", "TPK", true),
    QUARKUS_JSONP("quarkus-jsonp", "JSON-P", "TPY", true),
    QUARKUS_JACKSON("quarkus-jackson", "Jackson", "bapn", true),
    QUARKUS_TIKA("quarkus-tika", "Apache Tika", "R5J", false),
    QUARKUS_JGIT("quarkus-jgit", "JGit", "C53", false),
    QUARKUS_MAILER("quarkus-mailer", "Mailer", "baLo", false),
    QUARKUS_PICOCLI("quarkus-picocli", "Picocli", "UX2", false),
    QUARKUS_QUARTZ("quarkus-quartz", "Quartz", "8YL", true),
    QUARKUS_QUTE("quarkus-qute", "Qute Templating", "xTN", false),
    QUARKUS_SCHEDULER("quarkus-scheduler", "Scheduler - tasks", "CBv", true),
    QUARKUS_SPRING_BOOT_PROPERTIES("quarkus-spring-boot-properties", "Quarkus Extension for Spring Boot properties", "XkM", true),
    QUARKUS_SPRING_CACHE("quarkus-spring-cache", "Quarkus Extension for Spring Cache API", "DG", true),
    QUARKUS_SPRING_CLOUD_CONFIG_CLIENT("quarkus-spring-cloud-config-client", "Quarkus Extension for Spring Cloud Config Client", "egg", true),
    QUARKUS_SPRING_DI("quarkus-spring-di", "Quarkus Extension for Spring DI API", "VOH", true),
    QUARKUS_SPRING_DATA_JPA("quarkus-spring-data-jpa", "Quarkus Extension for Spring Data JPA API", "2ec", true),
    QUARKUS_SPRING_DATA_REST("quarkus-spring-data-rest", "Quarkus Extension for Spring Data REST", "kdX", true),
    QUARKUS_SPRING_SCHEDULED("quarkus-spring-scheduled", "Quarkus Extension for Spring Scheduled ", "RUR", true),
    QUARKUS_SPRING_SECURITY("quarkus-spring-security", "Quarkus Extension for Spring Security API", "mek", true),
    QUARKUS_SPRING_WEB("quarkus-spring-web", "Quarkus Extension for Spring Web API", "0D7", true),
    QUARKUS_KOTLIN("quarkus-kotlin", "Kotlin", "OxX", false),
    QUARKUS_SCALA("quarkus-scala", "Scala", "3e", false);

    public final String id;
    public final String name;
    public final String shortId;
    public final boolean supported;

    CodeQuarkusExtensions(String id, String name, String shortId, boolean supported) {
        this.id = id;
        this.name = name;
        this.shortId = shortId;
        this.supported = supported;
    }

    public enum Flag {
        SUPPORTED,
        NOT_SUPPORTED,
        MIXED
    }

    public static List<List<CodeQuarkusExtensions>> partition(int buckets, Flag flag) {
        if (buckets < 1) {
            throw new IllegalArgumentException("Nope, buckets must be bigger than 0.");
        }
        List<CodeQuarkusExtensions> extensions = Stream.of(CodeQuarkusExtensions.values()).filter(x ->
                flag == Flag.SUPPORTED && x.supported ||
                        flag == Flag.NOT_SUPPORTED && !x.supported ||
                        flag == Flag.MIXED).collect(Collectors.toUnmodifiableList());
        if (buckets > extensions.size()) {
            buckets = extensions.size();
        }
        int chunk = extensions.size() / buckets;
        if (chunk < 1) {
            throw new IllegalArgumentException("Chunk must be bigger than 0. No supported extensions?");
        }
        List<List<CodeQuarkusExtensions>> result = new ArrayList<>();
        int i = 0;
        for (int j = 0; j < buckets; j++) {
            if (j + 1 == buckets) {
                // The last bucket is bigger if necessary
                result.add(new ArrayList<>(extensions.subList(i, extensions.size())));
            } else {
                result.add(new ArrayList<>(extensions.subList(i, Math.min(i + chunk, extensions.size()))));
            }
            i = i + chunk;
        }
        return result;
    }
}

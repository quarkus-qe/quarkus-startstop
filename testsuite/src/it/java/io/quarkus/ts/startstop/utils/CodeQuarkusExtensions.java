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
    QUARKUS_RESTEASY_JSONB("quarkus-resteasy-jsonb", "RESTEasy JSON-B", "49J", true),
    QUARKUS_RESTEASY_JACKSON("quarkus-resteasy-jackson", "RESTEasy Jackson", "pV1", true),
    QUARKUS_VERTX_WEB("quarkus-vertx-web", "Eclipse Vert.x - Web", "JsG", true),
    QUARKUS_HIBERNATE_VALIDATOR("quarkus-hibernate-validator", "Hibernate Validator", "YjV", true),
    QUARKUS_REST_CLIENT("quarkus-rest-client", "REST Client", "ekA", true),
    QUARKUS_RESTEASY_JAXB("quarkus-resteasy-jaxb", "RESTEasy JAXB", "d7W", true),
    QUARKUS_RESTEASY_MUTINY("quarkus-resteasy-mutiny", "RESTEasy Mutiny", "8Tx", false),
    QUARKUS_RESTEASY_QUTE("quarkus-resteasy-qute", "RESTEasy Qute", "ju", false),
//  Commented out as it would require touching application.properties with confAppPropsForSkeleton(String appDir)
//  https://github.com/quarkusio/quarkus/issues/8506
//  QUARKUS_SMALLRYE_JWT("quarkus-smallrye-jwt", "SmallRye JWT", "D9x", false),
    QUARKUS_SMALLRYE_OPENAPI("quarkus-smallrye-openapi", "SmallRye OpenAPI", "ARC", true),
    QUARKUS_UNDERTOW("quarkus-undertow", "Undertow Servlet", "LMC", true),
    QUARKUS_UNDERTOW_WEBSOCKETS("quarkus-undertow-websockets", "Undertow WebSockets", "barD", true),
    QUARKUS_HIBERNATE_ORM("quarkus-hibernate-orm", "Hibernate ORM", "vH0", true),
    QUARKUS_HIBERNATE_ORM_PANACHE("quarkus-hibernate-orm-panache", "Hibernate ORM with Panache", "XZs", true),
    QUARKUS_JDBC_POSTGRESQL("quarkus-jdbc-postgresql", "JDBC Driver - PostgreSQL", "8IG", true),
    QUARKUS_JDBC_MARIADB("quarkus-jdbc-mariadb", "JDBC Driver - MariaDB", "78I", true),
    QUARKUS_JDBC_MYSQL("quarkus-jdbc-mysql", "JDBC Driver - MySQL", "9Be", true),
    QUARKUS_JDBC_MSSQL("quarkus-jdbc-mssql", "JDBC Driver - Microsoft SQL Server", "R6e", true),
    QUARKUS_JDBC_H2("quarkus-jdbc-h2", "JDBC Driver - H2", "FBQ", false),
    QUARKUS_JDBC_DERBY("quarkus-jdbc-derby", "JDBC Driver - Derby", "UCw", false),
    QUARKUS_AGROAL("quarkus-agroal", "Agroal - Database connection pool", "qiu", true),
    QUARKUS_AMAZON_DYNAMODB("quarkus-amazon-dynamodb", "Amazon DynamoDB client", "fUt", false),
    QUARKUS_CACHE("quarkus-cache", "Cache", "W1i", false),
    QUARKUS_FLYWAY("quarkus-flyway", "Flyway", "wTM", false),
    QUARKUS_HIBERNATE_SEARCH_ELASTICSEARCH("quarkus-hibernate-search-elasticsearch", "Hibernate Search + Elasticsearch", "opZ", false),
    QUARKUS_INFINISPAN_CLIENT("quarkus-infinispan-client", "Infinispan Client", "sOv", false),
    QUARKUS_LIQUIBASE("quarkus-liquibase", "Liquibase", "Xkd", false),
    QUARKUS_MONGODB_CLIENT("quarkus-mongodb-client", "MongoDB client", "ThS", false),
    QUARKUS_MONGODB_PANACHE("quarkus-mongodb-panache", "MongoDB with Panache", "y3v", false),
    QUARKUS_NARAYANA_JTA("quarkus-narayana-jta", "Narayana JTA - Transaction manager", "PBP", true),
    QUARKUS_NARAYANA_STM("quarkus-narayana-stm", "Narayana STM - Software Transactional Memory", "Nl9", false),
    QUARKUS_NEO4J("quarkus-neo4j", "Neo4j client", "pDS", false),
    QUARKUS_REACTIVE_MYSQL_CLIENT("quarkus-reactive-mysql-client", "Reactive MySQL client", "fmW", false),
    QUARKUS_REACTIVE_PG_CLIENT("quarkus-reactive-pg-client", "Reactive PostgreSQL client", "ih0", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING("quarkus-smallrye-reactive-messaging", "SmallRye Reactive Messaging", "Nts", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING_AMQP("quarkus-smallrye-reactive-messaging-amqp", "SmallRye Reactive Messaging - AMQP Connector", "ur3", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING_KAFKA("quarkus-smallrye-reactive-messaging-kafka", "SmallRye Reactive Messaging - Kafka Connector", "9qf", true),
    QUARKUS_SMALLRYE_REACTIVE_MESSAGING_MQTT("quarkus-smallrye-reactive-messaging-mqtt", "SmallRye Reactive Messaging - MQTT Connector", "ZsB", false),
    QUARKUS_QPID_JMS("quarkus-qpid-jms", "AMQP 1.0 JMS client - Apache Qpid JMS", "Zdm", false),
    QUARKUS_KAFKA_CLIENT("quarkus-kafka-client", "Apache Kafka Client", "FKK", true),
    QUARKUS_KAFKA_STREAMS("quarkus-kafka-streams", "Apache Kafka Streams", "ShK", false),
    QUARKUS_ARTEMIS_JMS("quarkus-artemis-jms", "Artemis JMS", "DWo", false),
    QUARKUS_CONFIG_YAML("quarkus-config-yaml", "YAML Configuration", "UAO", true),
    QUARKUS_LOGGING_JSON("quarkus-logging-json", "Logging JSON", "7RG", false),
    QUARKUS_LOGGING_GELF("quarkus-logging-gelf", "Logging GELF", "wCO", false),
    QUARKUS_LOGGING_SENTRY("quarkus-logging-sentry", "Logging Sentry", "eoJ", false),
    QUARKUS_VERTX("quarkus-vertx", "Eclipse Vert.x", "WqB", true),
    QUARKUS_MUTINY("quarkus-mutiny", "Mutiny", "zqM", false),
    QUARKUS_SMALLRYE_CONTEXT_PROPAGATION("quarkus-smallrye-context-propagation", "SmallRye Context Propagation", "7pM", true),
    QUARKUS_SMALLRYE_REACTIVE_STREAMS_OPERATORS("quarkus-smallrye-reactive-streams-operators", "SmallRye Reactive Streams Operators", "QYr", true),
    QUARKUS_KUBERNETES("quarkus-kubernetes", "Kubernetes", "dZK", false),
    QUARKUS_OPENSHIFT("quarkus-openshift", "OpenShift", "Bqi", false),
    QUARKUS_SMALLRYE_HEALTH("quarkus-smallrye-health", "SmallRye Health", "tqK", true),
    QUARKUS_SMALLRYE_FAULT_TOLERANCE("quarkus-smallrye-fault-tolerance", "SmallRye Fault Tolerance", "vgg", true),
    QUARKUS_AMAZON_LAMBDA("quarkus-amazon-lambda", "AWS Lambda", "ia1", false),
    QUARKUS_AMAZON_LAMBDA_HTTP("quarkus-amazon-lambda-http", "AWS Lambda HTTP", "L0j", false),
    QUARKUS_AMAZON_LAMBDA_XRAY("quarkus-amazon-lambda-xray", "AWS Lambda X-Ray", "JsX", false),
    QUARKUS_AZURE_FUNCTIONS_HTTP("quarkus-azure-functions-http", "Azure Functions HTTP", "3YS", false),
    QUARKUS_CONTAINER_IMAGE_DOCKER("quarkus-container-image-docker", "Container Image Docker", "V9i", false),
    QUARKUS_CONTAINER_IMAGE_JIB("quarkus-container-image-jib", "Container Image Jib", "qZz", false),
    QUARKUS_CONTAINER_IMAGE_S2I("quarkus-container-image-s2i", "Container Image S2I", "pbs", false),
    QUARKUS_KUBERNETES_CLIENT("quarkus-kubernetes-client", "Kubernetes Client", "Spa", false),
    QUARKUS_SMALLRYE_METRICS("quarkus-smallrye-metrics", "SmallRye Metrics", "Ll4", true),
    QUARKUS_SMALLRYE_OPENTRACING("quarkus-smallrye-opentracing", "SmallRye OpenTracing", "f7", true),
//  Commented out as it would require touching application.properties with confAppPropsForSkeleton(String appDir)
//  https://github.com/quarkusio/quarkus/issues/8506
//  QUARKUS_OIDC("quarkus-oidc", "OpenID Connect", "fgL", true),
    QUARKUS_ELYTRON_SECURITY_JDBC("quarkus-elytron-security-jdbc", "Elytron Security JDBC", "Pxs", false),
    QUARKUS_ELYTRON_SECURITY_OAUTH2("quarkus-elytron-security-oauth2", "Elytron Security OAuth 2.0", "9Ie", false),
    QUARKUS_ELYTRON_SECURITY_PROPERTIES_FILE("quarkus-elytron-security-properties-file", "Elytron Security Properties File", "Dk8", false),
//  Commented out as it would require touching application.properties with confAppPropsForSkeleton(String appDir)
//  https://github.com/quarkusio/quarkus/issues/8506
//  QUARKUS_KEYCLOAK_AUTHORIZATION("quarkus-keycloak-authorization", "Keycloak Authorization", "2Bx", false),
    QUARKUS_SECURITY_JPA("quarkus-security-jpa", "Security JPA", "W8w", false),
    QUARKUS_VAULT("quarkus-vault", "Vault", "r73", false),
    CAMEL_QUARKUS_CORE("camel-quarkus-core", "Camel Quarkus Core", "8bB", false),
    CAMEL_QUARKUS_AWS_EC2("camel-quarkus-aws-ec2", "Camel Quarkus AWS EC2", "MMA", false),
    CAMEL_QUARKUS_AWS_ECS("camel-quarkus-aws-ecs", "Camel Quarkus AWS ECS", "MND", false),
    CAMEL_QUARKUS_AWS_EKS("camel-quarkus-aws-eks", "Camel Quarkus AWS EKS", "MRD", false),
    CAMEL_QUARKUS_AWS_IAM("camel-quarkus-aws-iam", "Camel Quarkus AWS IAM", "NMx", false),
    CAMEL_QUARKUS_AWS_KMS("camel-quarkus-aws-kms", "Camel Quarkus AWS KMS", "OnD", false),
    CAMEL_QUARKUS_AWS_KINESIS("camel-quarkus-aws-kinesis", "Camel Quarkus AWS Kinesis", "3SM", false),
    CAMEL_QUARKUS_AWS_LAMBDA("camel-quarkus-aws-lambda", "Camel Quarkus AWS Lambda", "7q0", false),
    CAMEL_QUARKUS_AWS_S3("camel-quarkus-aws-s3", "Camel Quarkus AWS S3", "V7W", false),
    CAMEL_QUARKUS_AWS_SNS("camel-quarkus-aws-sns", "Camel Quarkus AWS SNS", "Qn9", false),
    CAMEL_QUARKUS_AWS_SQS("camel-quarkus-aws-sqs", "Camel Quarkus AWS SQS", "QpD", false),
    CAMEL_QUARKUS_AWS_TRANSLATE("camel-quarkus-aws-translate", "Camel Quarkus AWS Translate", "xTI", false),
    CAMEL_QUARKUS_ACTIVEMQ("camel-quarkus-activemq", "Camel Quarkus ActiveMQ", "EPu", false),
    CAMEL_QUARKUS_AHC("camel-quarkus-ahc", "Camel Quarkus Async HTTP Client (AHC)", "tl7", false),
    CAMEL_QUARKUS_AHC_WS("camel-quarkus-ahc-ws", "Camel Quarkus Async HTTP Client (AHC) Web Socket", "Mmt", false),
    CAMEL_QUARKUS_ATTACHMENTS("camel-quarkus-attachments", "Camel Quarkus Attachments", "xl5", false),
    CAMEL_QUARKUS_AZURE("camel-quarkus-azure", "Camel Quarkus Azure", "dW2", false),
    CAMEL_QUARKUS_BASE64("camel-quarkus-base64", "Camel Quarkus Base64", "siX", false),
    CAMEL_QUARKUS_BEAN("camel-quarkus-bean", "Camel Quarkus Bean", "gys", false),
    CAMEL_QUARKUS_BEAN_VALIDATOR("camel-quarkus-bean-validator", "Camel Quarkus Bean Validator", "bMp", false),
    CAMEL_QUARKUS_BINDY("camel-quarkus-bindy", "Camel Quarkus Bindy", "xlc", false),
    CAMEL_QUARKUS_BOX("camel-quarkus-box", "Camel Quarkus Box", "tFr", false),
    CAMEL_QUARKUS_BRAINTREE("camel-quarkus-braintree", "Camel Quarkus Braintree", "MXc", false),
    CAMEL_QUARKUS_CSV("camel-quarkus-csv", "Camel Quarkus CSV", "tWU", false),
    CAMEL_QUARKUS_CONSUL("camel-quarkus-consul", "Camel Quarkus Consul", "s5m", false),
    CAMEL_QUARKUS_CONTROLBUS("camel-quarkus-controlbus", "Camel Quarkus ControlBus", "Lwz", false),
    CAMEL_QUARKUS_CORE_CLOUD("camel-quarkus-core-cloud", "Camel Quarkus Core Cloud", "AvJ", false),
    CAMEL_QUARKUS_DATAFORMAT("camel-quarkus-dataformat", "Camel Quarkus Data Format", "oRh", false),
    CAMEL_QUARKUS_DIRECT("camel-quarkus-direct", "Camel Quarkus Direct", "c8Z", false),
    CAMEL_QUARKUS_DOZER("camel-quarkus-dozer", "Camel Quarkus Dozer", "9tO", false),
    CAMEL_QUARKUS_ENDPOINTDSL("camel-quarkus-endpointdsl", "Camel Quarkus Endpoint DSL", "qoo", false),
    CAMEL_QUARKUS_EXEC("camel-quarkus-exec", "Camel Quarkus Exec", "PxD", false),
    CAMEL_QUARKUS_FHIR("camel-quarkus-fhir", "Camel Quarkus FHIR", "LKT", false),
    CAMEL_QUARKUS_FTP("camel-quarkus-ftp", "Camel Quarkus FTP", "uHO", false),
    CAMEL_QUARKUS_FILE("camel-quarkus-file", "Camel Quarkus File", "Lt7", false),
    CAMEL_QUARKUS_FILE_WATCH("camel-quarkus-file-watch", "Camel Quarkus File Watch", "evi", false),
    CAMEL_QUARKUS_GRAPHQL("camel-quarkus-graphql", "Camel Quarkus GraphQL", "MSX", false),
    CAMEL_QUARKUS_GSON("camel-quarkus-gson", "Camel Quarkus Gson", "BcX", false),
    CAMEL_QUARKUS_HTTP("camel-quarkus-http", "Camel Quarkus HTTP", "taq", false),
    CAMEL_QUARKUS_HYSTRIX("camel-quarkus-hystrix", "Camel Quarkus Hystrix", "UNf", false),
    CAMEL_QUARKUS_INFINISPAN("camel-quarkus-infinispan", "Camel Quarkus Infinispan", "QXx", false),
    CAMEL_QUARKUS_JAXB("camel-quarkus-jaxb", "Camel Quarkus JAXB dataformat", "in0", false),
    CAMEL_QUARKUS_JDBC("camel-quarkus-jdbc", "Camel Quarkus JDBC", "hOD", false),
    CAMEL_QUARKUS_JMS("camel-quarkus-jms", "Camel Quarkus JMS", "vEm", false),
    CAMEL_QUARKUS_JSONPATH("camel-quarkus-jsonpath", "Camel Quarkus JSON Path", "kWp", false),
    CAMEL_QUARKUS_JACKSON("camel-quarkus-jackson", "Camel Quarkus Jackson", "Sbp", false),
    CAMEL_QUARKUS_JACKSONXML("camel-quarkus-jacksonxml", "Camel Quarkus Jackson XML", "RQA", false),
    CAMEL_QUARKUS_JIRA("camel-quarkus-jira", "Camel Quarkus Jira", "gra", false),
    CAMEL_QUARKUS_JOHNZON("camel-quarkus-johnzon", "Camel Quarkus Johnzon", "MqU", false),
    CAMEL_QUARKUS_KAFKA("camel-quarkus-kafka", "Camel Quarkus Kafka", "ezA", false),
    CAMEL_QUARKUS_KOTLIN("camel-quarkus-kotlin", "Camel Quarkus Kotlin", "GsT", false),
    CAMEL_QUARKUS_LZF("camel-quarkus-lzf", "Camel Quarkus LZF", "wfE", false),
    CAMEL_QUARKUS_LOG("camel-quarkus-log", "Camel Quarkus Log", "waa", false),
    CAMEL_QUARKUS_MAIL("camel-quarkus-mail", "Camel Quarkus Mail", "WgZ", false),
    CAMEL_QUARKUS_MICROPROFILE_HEALTH("camel-quarkus-microprofile-health", "Camel Quarkus MicroProfile Health", "OJ5", false),
    CAMEL_QUARKUS_MICROPROFILE_METRICS("camel-quarkus-microprofile-metrics", "Camel Quarkus MicroProfile Metrics", "cvj", false),
    CAMEL_QUARKUS_MONGODB("camel-quarkus-mongodb", "Camel Quarkus MongoDB", "Y9a", false),
    CAMEL_QUARKUS_MUSTACHE("camel-quarkus-mustache", "Camel Quarkus Mustache", "Edg", false),
    CAMEL_QUARKUS_NETTY("camel-quarkus-netty", "Camel Quarkus Netty", "frc", false),
    CAMEL_QUARKUS_NETTY_HTTP("camel-quarkus-netty-http", "Camel Quarkus Netty HTTP", "Bl8", false),
    CAMEL_QUARKUS_OLINGO4("camel-quarkus-olingo4", "Camel Quarkus Olingo4", "8YU", false),
    CAMEL_QUARKUS_OPENTRACING("camel-quarkus-opentracing", "Camel Quarkus OpenTracing", "0Q1", false),
    CAMEL_QUARKUS_PDF("camel-quarkus-pdf", "Camel Quarkus PDF", "w5E", false),
    CAMEL_QUARKUS_PAHO("camel-quarkus-paho", "Camel Quarkus Paho", "y2W", false),
    CAMEL_QUARKUS_PLATFORM_HTTP("camel-quarkus-platform-http", "Camel Quarkus Platform HTTP", "d0M", false),
    CAMEL_QUARKUS_REACTIVE_EXECUTOR("camel-quarkus-reactive-executor", "Camel Quarkus Reactive Executor", "VH6", false),
    CAMEL_QUARKUS_REACTIVE_STREAMS("camel-quarkus-reactive-streams", "Camel Quarkus Reactive Streams", "PEr", false),
    CAMEL_QUARKUS_REF("camel-quarkus-ref", "Camel Quarkus Ref", "xz0", false),
    CAMEL_QUARKUS_REST("camel-quarkus-rest", "Camel Quarkus Rest", "irm", false),
    CAMEL_QUARKUS_SEDA("camel-quarkus-seda", "Camel Quarkus SEDA", "OF", false),
    CAMEL_QUARKUS_SJMS("camel-quarkus-sjms", "Camel Quarkus SJMS", "bavn", false),
    CAMEL_QUARKUS_SJMS2("camel-quarkus-sjms2", "Camel Quarkus SJMS2", "Bxl", false),
    CAMEL_QUARKUS_SQL("camel-quarkus-sql", "Camel Quarkus SQL", "xVK", false),
    CAMEL_QUARKUS_SALESFORCE("camel-quarkus-salesforce", "Camel Quarkus Salesforce", "Jip", false),
    CAMEL_QUARKUS_SCHEDULER("camel-quarkus-scheduler", "Camel Quarkus Scheduler", "hqJ", false),
    CAMEL_QUARKUS_SERVLET("camel-quarkus-servlet", "Camel Quarkus Servlet", "sBr", false),
    CAMEL_QUARKUS_SLACK("camel-quarkus-slack", "Camel Quarkus Slack", "o0q", false),
    CAMEL_QUARKUS_SNAKEYAML("camel-quarkus-snakeyaml", "Camel Quarkus SnakeYAML", "4bb", false),
    CAMEL_QUARKUS_STREAM("camel-quarkus-stream", "Camel Quarkus Stream", "YQS", false),
    CAMEL_QUARKUS_TAGSOUP("camel-quarkus-tagsoup", "Camel Quarkus TagSoup", "Ggv", false),
    CAMEL_QUARKUS_TARFILE("camel-quarkus-tarfile", "Camel Quarkus Tarfile", "mub", false),
    CAMEL_QUARKUS_TELEGRAM("camel-quarkus-telegram", "Camel Quarkus Telegram", "y44", false),
    CAMEL_QUARKUS_TIMER("camel-quarkus-timer", "Camel Quarkus Timer", "Vcj", false),
    CAMEL_QUARKUS_TWITTER("camel-quarkus-twitter", "Camel Quarkus Twitter", "ySR", false),
    CAMEL_QUARKUS_VM("camel-quarkus-vm", "Camel Quarkus VM", "4hL", false),
    CAMEL_QUARKUS_VALIDATOR("camel-quarkus-validator", "Camel Quarkus Validator", "42M", false),
    CAMEL_QUARKUS_WEBSOCKET_JSR356("camel-quarkus-websocket-jsr356", "Camel Quarkus WebSocket JSR 356", "RxD", false),
    CAMEL_QUARKUS_XML_IO("camel-quarkus-xml-io", "Camel Quarkus XML IO", "gjU", false),
    CAMEL_QUARKUS_XML_JAXB("camel-quarkus-xml-jaxb", "Camel Quarkus XML JAXB", "18L", false),
    CAMEL_QUARKUS_XML_JAXP("camel-quarkus-xml-jaxp", "Camel Quarkus XML JAXP", "18x", false),
    CAMEL_QUARKUS_XPATH("camel-quarkus-xpath", "Camel Quarkus XPath", "Q6t", false),
    CAMEL_QUARKUS_XSLT("camel-quarkus-xslt", "Camel Quarkus XSLT", "vvR", false),
    CAMEL_QUARKUS_XSTREAM("camel-quarkus-xstream", "Camel Quarkus XStream", "eCC", false),
    CAMEL_QUARKUS_ZIP_DEFLATER("camel-quarkus-zip-deflater", "Camel Quarkus Zip Deflate", "Zw2", false),
    CAMEL_QUARKUS_ZIPFILE("camel-quarkus-zipfile", "Camel Quarkus Zipfile", "baW6", false),
    CAMEL_QUARKUS_ICAL("camel-quarkus-ical", "Camel Quarkus iCal", "pOZ", false),
    QUARKUS_KOGITO("quarkus-kogito", "Kogito", "pVr", false),
    QUARKUS_OPTAPLANNER_JACKSON("quarkus-optaplanner-jackson", "OptaPlanner Jackson", "ya9", false),
    QUARKUS_OPTAPLANNER("quarkus-optaplanner", "OptaPlanner constraint solver AI", "v8G", false),
    QUARKUS_JAXB("quarkus-jaxb", "JAXB", "BHf", true),
    QUARKUS_JSONB("quarkus-jsonb", "JSON-B", "TPK", true),
    QUARKUS_JSONP("quarkus-jsonp", "JSON-P", "TPY", true),
    QUARKUS_JACKSON("quarkus-jackson", "Jackson", "bapn", true),
    QUARKUS_TIKA("quarkus-tika", "Apache Tika", "R5J", false),
    QUARKUS_JGIT("quarkus-jgit", "JGit", "C53", false),
    QUARKUS_MAILER("quarkus-mailer", "Mailer", "baLo", false),
    QUARKUS_QUARTZ("quarkus-quartz", "Quartz", "8YL", true),
    QUARKUS_QUTE("quarkus-qute", "Qute Templating", "xTN", false),
    QUARKUS_SCHEDULER("quarkus-scheduler", "Scheduler - tasks", "CBv", true),
    QUARKUS_SPRING_BOOT_PROPERTIES("quarkus-spring-boot-properties", "Quarkus Extension for Spring Boot properties", "XkM", true),
    QUARKUS_SPRING_CLOUD_CONFIG_CLIENT("quarkus-spring-cloud-config-client", "Quarkus Extension for Spring Cloud Config Client", "egg", false),
    QUARKUS_SPRING_DI("quarkus-spring-di", "Quarkus Extension for Spring DI API", "VOH", true),
    QUARKUS_SPRING_DATA_JPA("quarkus-spring-data-jpa", "Quarkus Extension for Spring Data JPA API", "2ec", true),
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

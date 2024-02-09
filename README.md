# GigaChat support for LangChain4j

## Introduction

langchain4j-gigachat is an unofficial implementation of the [GigaChat](https://developers.sber.ru/docs/ru/gigachat/overview) LLM provider for [Langchain4j](https://github.com/langchain4j/langchain4j).

## Compatibility

- Java: 8 or higher

## Getting started

1. Add LangChain4j Gigachat dependency to your project:
    - Maven:
      ```
      <dependency>
          <groupId>ru.vzotov</groupId>
          <artifactId>langchain4j-gigachat</artifactId>
          <version>0.1.0</version>
      </dependency>
      ```
    - Gradle:
      ```
      implementation 'ru.vzotov:langchain4j-gigachat:0.1.0'
      ```

## Specifics of SSL support

You need to install Russian trusted certificates.
Please follow [the instructions here](https://developers.sber.ru/docs/ru/gigachat/certificates) to get them.

### Installation for Java 8
Run from `JAVA_HOME/jre/bin`
```
keytool -importcert -keystore ../lib/security/cacerts -storepass changeit -noprompt -alias rus_root_ca -trustcacerts -file russian_trusted/russian_trusted_root_ca_pem.crt
keytool -importcert -keystore ../lib/security/cacerts -storepass changeit -noprompt -alias rus_sub_ca -trustcacerts -file russian_trusted/russian_trusted_root_ca_pem.crt
```

### Installation for Java 9+
Run from `JAVA_HOME/bin`
```
keytool -importcert -storepass changeit -noprompt -alias rus_root_ca -cacerts -trustcacerts -file russian_trusted/russian_trusted_root_ca_pem.crt
keytool -importcert -storepass changeit -noprompt -alias rus_sub_ca -cacerts -trustcacerts -file russian_trusted/russian_trusted_root_ca_pem.crt
```

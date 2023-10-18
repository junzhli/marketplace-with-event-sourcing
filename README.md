# Marketplace with event sourcing and CQRS

### It is comprised of three microservices
* Account (Crud)
* Order (Event sourcing)
* Order View (Query side of order service)

### Prerequisite
Run with Java 11 and onwards  
It also needs the following components
* Postgres
* MongoDB
* Redis

Set up for running locally
  * `docker-compose up -d`

### How to run

#### Account

```shell
./gradlew :account:bootRun
```

#### Order

```shell
./gradlew :order:bootRun
```

#### Order View

```shell
./gradlew :order:bootRun
```

### TODO
* Swagger
* GraphQL as user facing api on `order-view`
* Authentication
* Test cases
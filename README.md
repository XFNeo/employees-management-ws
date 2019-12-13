# employees-management-ws  
Simple Spring Boot REST web service for employees management.  
This service depends on [departments-managment service](https://github.com/XFNeo/departments-management-ws).  
Swagger user interface available on "/swagger-ui.html"  
The application automated builds on [Docker hub](https://hub.docker.com/r/xfneo/employees-management-ws).

## Prerequisites
 - PostgreSQL 11 with database "employees_service"
 - JDK 8
 - Launched [departments-managment service](https://github.com/XFNeo/departments-management-ws)
 - Docker and docker-compose for container deploy
 
### Environment variables:  
- DB_USERNAME - username for database. Default: postgres
- DB_PASSWORD - password for database. Default: postgres
- DB_HOST - database host. Default: localhost
- DB_PORT - database port. Default: 5432
- APP_PORT - application port for api. Default: 8080
- DEPARTMENTS_SERVICE_URL - URL and port to [departments-managment service](https://github.com/XFNeo/departments-management-ws). Default:  http://localhost:8080

## Deploy application:
### Linux:
```sh
chmod +x mvnw
./mvnw package
cp target/employees-management*.jar ./app.jar
java -jar app.jar
```
### Windows:
```cmd
mvnw.cmd package
copy /B target\employees-management*.jar app.jar
java -jar app.jar
```

## Deploy application with docker:
Run the command (don't forget to change necessary environment variables and prepare database):
```sh
docker run --name empl_app -p 80:8080 -e DB_USERNAME=postgres -e DB_PASSWORD=postgres -e DB_HOST=localhost -e DB_PORT=5432 -e DEPARTMENTS_SERVICE_URL=http://localhost:9090 xfneo/employees-management-ws:latest
```

## Deploy applications (employees-management-ws and departments-management-ws) and databases with docker-compose:
Go to directory with docker-compose.yml file and run the command:
```sh
docker-compose up
```

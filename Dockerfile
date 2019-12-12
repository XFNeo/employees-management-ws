FROM openjdk:8-jdk-alpine
WORKDIR /root/
RUN apk add git \
		&& git clone https://github.com/XFNeo/employees-management-ws.git \
		&& cd /root/employees-management-ws \
		&& chmod +x mvnw \
		&& ./mvnw package \
		&& cp target/employees-management*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT  [ "java", "-jar", "/root/employees-management-ws/app.jar" ]

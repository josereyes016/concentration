FROM eclipse-temurin:23
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN mkdir -p /src/main/resources/static/
COPY target/classes/static/concentration.timeseries.nc /src/main/resources/static/concentration.timeseries.nc
ENTRYPOINT ["java","-jar","/app.jar"]

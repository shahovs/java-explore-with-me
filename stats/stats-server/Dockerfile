FROM bellsoft/liberica-openjdk-alpine-musl:11
ENV TZ=Europe/Podgorica
#VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
#COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
#ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]
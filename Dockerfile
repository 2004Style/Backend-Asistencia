# Etapa de Compilación (Build Stage)
# Usa una imagen de Maven + JDK para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia primero el pom.xml para aprovechar el cache de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia el resto del código fuente
COPY src ./src

# Compila y empaqueta en un JAR (sin correr tests)
RUN mvn clean package -DskipTests


# Etapa de Ejecución (Run Stage)
# Usa solo el JRE para una imagen más liviana
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
EXPOSE 8080

# Copia el JAR desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

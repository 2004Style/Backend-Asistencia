# Etapa de Compilación (Build Stage)
# Usa una imagen de Maven con una versión de JDK compatible para compilar el proyecto.
# Eclipse Temurin es una excelente opción de OpenJDK.
FROM eclipse-temurin:17-jdk-jammy as builder

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia primero el pom.xml para aprovechar el cache de capas de Docker.
# Las dependencias de Maven solo se descargarán de nuevo si pom.xml cambia.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia el resto del código fuente de la aplicación
COPY src ./src

# Compila la aplicación y empaquétala en un JAR, omitiendo los tests.
# -DskipTests acelera la compilación en entornos de CI/CD.
RUN mvn clean package -DskipTests


# Etapa de Ejecución (Run Stage)
# Usa una imagen JRE (Java Runtime Environment) más pequeña para la ejecución.
FROM eclipse-temurin:17-jre-jammy

# Establece el directorio de trabajo
WORKDIR /app

# Expone el puerto en el que se ejecuta la aplicación Spring Boot (el predeterminado es 8080)
EXPOSE 8080

# Copia el JAR compilado desde la etapa de 'builder' a la etapa actual
COPY --from=builder /app/target/*.jar app.jar

# Comando para ejecutar la aplicación cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]

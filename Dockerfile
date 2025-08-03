# Usar una imagen base de Java 24 (OpenJDK)
FROM openjdk:24-jdk-slim

# Información del mantenedor
LABEL description="Spring Boot 3.5.3 con Java 24"

# Establecer el directorio de trabajo
WORKDIR /app

# Instalar Maven y curl
RUN apt-get update && \
    apt-get install -y maven curl && \
    rm -rf /var/lib/apt/lists/*

# Crear un usuario no-root para seguridad
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copiar archivo pom.xml para descargar dependencias
COPY pom.xml ./

# Descargar dependencias (aprovecha el cache de Docker)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src/ ./src/
COPY .env .

# Construir la aplicación
RUN mvn clean package -DskipTests

# Crear directorio para logs
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Cambiar al usuario no-root
USER appuser

# Exponer el puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=production

# Comando de salud para Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Punto de entrada de la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/*.jar"]
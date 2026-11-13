# Validación — 13/11/2026

Verificado con Eclipse Temurin JDK 25 en Windows:

```powershell
.\mvnw.cmd -B clean verify
java -jar target/student-management-desktop-1.0.0-SNAPSHOT.jar --check
```

Resultado: compilación y empaquetado correctos; siete pruebas aprobadas (tres de dominio y cuatro de persistencia JDBC/H2), sin errores ni pruebas omitidas. La comprobación del JAR inicializó correctamente una base local.

La captura `demo.png` se generó con tres registros ficticios en una base H2 en memoria, sin utilizar datos del archivo local de alumnos.

GitHub Actions ejecuta el build y la comprobación del JAR con Java 25.

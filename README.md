# Hello World con RabbitMQ y Spring Boot

Actividad 2.1.2 de Cloud Native: crear una cola, un productor y un consumidor básicos. Permite enviar mensajes desde un menú de consola o una API REST y ver cómo el consumidor los recibe.

## Cómo funciona

```text
Menú de consola ─┐
                ├─> Sender ─> RabbitMQ: cola hello ─> Receiver ─> Consola
API REST ────────┘
```

RabbitMQ se ejecuta en Docker. La aplicación Java se ejecuta directamente en tu equipo e incluye tanto el productor como el consumidor.

1. Al iniciar la aplicación, `RabbitMQConfig` declara la cola `hello`.
2. El menú o `MessageController` llama al productor `Sender`.
3. `Sender` agrega la hora al texto y lo envía con `RabbitTemplate`, usando el exchange predeterminado y la clave de enrutamiento `hello`.
4. RabbitMQ entrega el mensaje al consumidor `Receiver`.
5. El método anotado con `@RabbitListener` imprime el mensaje recibido. Spring gestiona la confirmación cuando el procesamiento termina correctamente.

La recepción es asíncrona: el mensaje recibido puede aparecer mientras el menú ya está esperando otra opción. La respuesta HTTP indica que terminó la llamada de envío, no que el consumidor haya terminado de procesar el mensaje.

## Requisitos

- Git, para clonar el repositorio.
- JDK 21.
- Docker Desktop funcionando con contenedores Linux; en Windows, WSL 2 configurado.
- Conexión a Internet para descargar las imágenes y dependencias la primera vez.
- Puertos `5672`, `15672` y `8080` disponibles.

El proyecto usa Spring Boot **4.1.1** y la imagen `rabbitmq:4.2-management`. Incluye Maven Wrapper, por lo que no necesitas instalar Maven por separado.

Los comandos de esta guía están preparados para **PowerShell en Windows**.

```powershell
java --version
javac --version
docker --version
docker compose version
```

## Estructura del repositorio

```text
actividad-2.1.2-cloud-native-1/
├── docker-compose.yml
├── README.md
└── rabbitmq-tutorials/
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    ├── .mvn/wrapper/
    └── src/
        ├── main/
        │   ├── java/com/example/
        │   │   ├── RabbitmqTutorialsApplication.java
        │   │   ├── RabbitMQConfig.java
        │   │   ├── Sender.java
        │   │   ├── Receiver.java
        │   │   └── MessageController.java
        │   └── resources/application.yml
        └── test/java/com/example/RabbitmqTutorialsApplicationTests.java
```

## 1. Obtener el proyecto

```powershell
git clone https://github.com/francog14/actividad-2.1.2-cloud-native-1.git
cd actividad-2.1.2-cloud-native-1
```

Si ya tienes el repositorio, abre esa carpeta. La raíz es la carpeta que contiene `docker-compose.yml`; el proyecto Java está dentro de `rabbitmq-tutorials`.

## 2. Iniciar RabbitMQ

Abre Docker Desktop. Desde la **raíz del repositorio**, ejecuta:

```powershell
docker compose up -d
docker compose ps
```

Espera a que el servicio `rabbitmq` aparezca como `healthy`. Si muestra `health: starting`, espera unos segundos y repite `docker compose ps`.

Puedes comprobarlo también con:

```powershell
docker exec rabbitmq rabbitmq-diagnostics ping
```

Para revisar el arranque:

```powershell
docker logs rabbitmq
```

Abre el [panel de RabbitMQ](http://localhost:15672) e ingresa con usuario **guest** y contraseña **guest**.

| Puerto | Uso |
|---|---|
| 5672 | Conexión AMQP entre Spring Boot y RabbitMQ |
| 15672 | Panel web de RabbitMQ |
| 8080 | API REST de la aplicación Spring Boot |

## 3. Compilar la aplicación

Desde la raíz del repositorio, entra en la carpeta Java:

```powershell
cd rabbitmq-tutorials
cmd /c mvnw.cmd clean package
```

La primera ejecución descarga Maven y las dependencias. Al finalizar debe aparecer `BUILD SUCCESS` y se genera:

```text
target/rabbitmq-tutorials-0.0.1-SNAPSHOT.jar
```

Este comando compila, ejecuta la prueba existente y empaqueta la aplicación. La prueba comprueba la carga del contexto de Spring; la verificación del envío y recepción se realiza con los pasos siguientes.

## 4. Ejecutar la aplicación

Desde la carpeta `rabbitmq-tutorials`:

```powershell
java -jar target/rabbitmq-tutorials-0.0.1-SNAPSHOT.jar
```

Mantén esta terminal abierta. Aparecerá:

```text
--- Menú ---
1. Enviar mensaje
2. Enviar múltiples mensajes
3. Salir
Selecciona una opción:
```

La conexión se configura en `src/main/resources/application.yml`: servidor `localhost`, puerto `5672`, usuario y contraseña `guest`, y virtual host `/`.

## 5. Enviar mensajes desde el menú

### Un mensaje

Selecciona `1`, pulsa Enter y escribe `Hola RabbitMQ!`.

Salida de ejemplo:

```text
Mensaje enviado: [14:12:47.562] Hola RabbitMQ!
Mensaje recibido: [14:12:47.562] Hola RabbitMQ!
```

### Varios mensajes

Selecciona `2` e indica `5`. Deberían aparecer cinco envíos y cinco recepciones. Las líneas pueden intercalarse con el menú porque el consumidor trabaja de forma asíncrona.

## 6. Probar la API REST

Deja la aplicación Java abierta. Usa **otra terminal PowerShell** para las llamadas HTTP.

### POST: enviar un mensaje JSON

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/messages" -Method Post -ContentType "application/json" -Body '{"message":"Hola desde POST!"}'
```

Respuesta esperada:

```text
Mensaje enviado: Hola desde POST!
```

En la terminal de Java deben aparecer tanto el envío como la recepción del mensaje.

### GET: prueba sencilla desde el navegador

Abre [enviar Hola desde REST](http://localhost:8080/api/messages/send?message=Hola%20desde%20REST).

También puedes usar PowerShell:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/messages/send?message=Hola%20desde%20REST"
```

El endpoint GET se incluye como demostración de la actividad. Para enviar mensajes desde un cliente, utiliza POST.

## 7. Revisar la cola en RabbitMQ

Con la aplicación abierta:

1. Entra al [panel de RabbitMQ](http://localhost:15672).
2. Abre **Queues and Streams**.
3. Selecciona la cola **hello**.
4. Busca **Consumers**: con una sola instancia de la aplicación debe aparecer un consumidor activo.

| Indicador | Significado |
|---|---|
| Ready | Mensajes esperando ser entregados |
| Unacked | Mensajes entregados pendientes de confirmación |
| Total | Suma de Ready y Unacked |

Después de procesar los mensajes, es normal que los tres contadores estén en **0**: no representan el total histórico de mensajes enviados. En **Connections** y **Channels** puedes revisar las conexiones y canales de la aplicación.

La cola se declara como **no durable** (`new Queue("hello", false)`): no se conserva al reiniciar RabbitMQ. Aunque Docker Compose usa el volumen `rabbitmq_data`, eso no vuelve durable a esta cola. Al volver a iniciar la aplicación, Spring la declara de nuevo.

## 8. Detener y volver a iniciar

Para detener la aplicación Java, selecciona `3` en el menú. Esto cierra también la API REST y el consumidor.

Para detener RabbitMQ, desde la carpeta que contiene `docker-compose.yml`:

```powershell
docker compose stop
```

Si estás en la subcarpeta Java, primero vuelve a la raíz con `cd ..`.

Para usar el proyecto otro día:

1. Abre Docker Desktop.
2. Desde la raíz del repositorio, inicia RabbitMQ y comprueba su estado:

```powershell
docker compose up -d
docker compose ps
```

3. Cuando esté listo, inicia la aplicación:

```powershell
cd rabbitmq-tutorials
java -jar target/rabbitmq-tutorials-0.0.1-SNAPSHOT.jar
```

Si modificaste el código o todavía no existe el JAR, ejecuta antes `cmd /c mvnw.cmd clean package`.

## Problemas frecuentes

| Problema | Qué revisar |
|---|---|
| No se reconoce `mvnw.cmd` | Ejecuta `dir`: debes estar en la carpeta que contiene `pom.xml` y `mvnw.cmd`. El nombre es `mvnw.cmd`, sin una barra antes de `.cmd`. |
| Docker no puede conectar con el motor | Abre Docker Desktop y espera a que termine de arrancar. |
| `Connection refused` en el puerto 5672 | Inicia RabbitMQ y comprueba que esté `healthy` antes de iniciar Java. |
| No abre `localhost:8080` | La aplicación Java debe seguir ejecutándose; revisa sus mensajes de arranque. |
| El puerto 8080 está ocupado | Detén la otra instancia o cambia `server.port` en `application.yml`, recompila y adapta las URL. |
| La cola `hello` no aparece | Inicia la aplicación Java: es quien declara la cola. |
| Consumers muestra 0 | Comprueba que la aplicación siga abierta y conectada a RabbitMQ. |
| Error de sintaxis YAML | Usa espacios, respeta la sangría y evita caracteres de formato copiados desde un PDF. |
| `Unable to access jarfile` | Comprueba que la compilación terminó con `BUILD SUCCESS` y ejecuta el comando desde la carpeta Java. |

Las credenciales `guest/guest` y la configuración de este repositorio corresponden a la práctica local.

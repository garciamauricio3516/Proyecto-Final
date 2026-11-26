# Cards4Play - API REST

Proyecto final de Desarrollo de Software
Institución Universitaria EAM
Ingeniería de Software

## 1. Integrantes

- Mauricio Garcia
- Juan Jose Valbuena
- Paul Noreña

## 2. Descripción del proyecto

Cards4Play es una aplicación backend desarrollada en Java
para la gestión de una tienda de cartas coleccionables,
accesorios, productos sellados, boosters y torneos.

El sistema proporciona una API REST que permite gestionar
usuarios, administrar productos, realizar compras, abrir
boosters e inscribir clientes en torneos.

La aplicación implementa autenticación, autorización por
roles, persistencia mediante archivos JSON, conversión
de precios de USD a COP y despliegue mediante Docker
y Railway.

## 3. Tecnologías utilizadas

- Java 17
- Apache Maven
- Spark Java 2.9.4
- Gson 2.13.2
- SLF4J
- JSON
- HTTP Basic Authentication
- Postman
- Docker
- Git
- GitHub
- Railway

## 4. Arquitectura del proyecto

El proyecto organiza sus responsabilidades en las
siguientes capas:

- Modelo: entidades y lógica de negocio.
- Controladores: coordinación de operaciones del sistema.
- Persistencia: almacenamiento y recuperación de datos JSON.
- API REST: exposición de funcionalidades mediante HTTP.
- Main: inicialización de la aplicación.

### Estructura

    Cards4Play/
    |-- src/main/java/
    |   |-- Modelo/
    |   |-- Controladores/
    |   |-- Persistencia/
    |   |-- com/cards4play/
    |-- postman/
    |-- datos/
    |-- Dockerfile
    |-- pom.xml
    |-- .gitignore
    |-- .dockerignore
    |-- README.md

## 5. Funcionalidades

### Administrador

- Autenticarse en el sistema.
- Registrar clientes.
- Registrar cartas coleccionables.
- Registrar accesorios.
- Registrar productos sellados.
- Crear boosters.
- Crear torneos.
- Consultar productos y torneos.

### Cliente

- Autenticarse en el sistema.
- Consultar el inventario de la tienda.
- Consultar productos individuales.
- Comprar productos.
- Consultar su historial de compras.
- Abrir boosters adquiridos.
- Consultar torneos disponibles.
- Inscribirse en torneos.

## 6. Reglas de negocio

### Boosters

Cada booster contiene exactamente 10 cartas.

Las probabilidades base de selección por rareza son:

| Rareza | Probabilidad |
|--------|-------------:|
| N      | 50%          |
| UC     | 25%          |
| R      | 15%          |
| SR     | 7%           |
| UR     | 3%           |

Cuando no existen cartas de la rareza seleccionada,
se busca una rareza inferior disponible y posteriormente
una superior si es necesario.

Las cartas utilizadas para generar boosters no se
eliminan del inventario original.

El sistema procura evitar cartas repetidas cuando
existen suficientes cartas diferentes disponibles.

Se garantiza al menos una carta de rareza R o superior
cuando existe disponibilidad de dichas rarezas.

El contenido del booster permanece oculto hasta que
su propietario lo abre.

Un booster no puede venderse nuevamente después de
su compra ni abrirse más de una vez.

### Torneos

- Cada torneo tiene una capacidad máxima de participantes.
- Un cliente no puede inscribirse dos veces.
- No se permiten inscripciones cuando el torneo está lleno.

### Compras

- Cada compra queda registrada en el historial del cliente.
- Los clientes solamente pueden consultar su propio historial.
- - El historial de compras muestra la rareza de las cartas adquiridas.
- Los boosters comprados muestran su estado de apertura.
- Los boosters cerrados mantienen oculto su contenido.
- Los boosters abiertos muestran las 10 cartas obtenidas, incluyendo su identificador, nombre, tipo, rareza y precio en USD.
- Los productos vendidos se retiran del inventario de la tienda.
- Se registra el valor de la compra en USD y COP.

## 7. Autenticación y seguridad

La API utiliza HTTP Basic Authentication.

Existen dos roles:

- ADMIN
- CLIENTE

Las operaciones protegidas requieren credenciales válidas.
Cada operación verifica los permisos correspondientes
al rol del usuario.

### Credenciales locales predeterminadas

Para una tienda nueva en un entorno local:

- Usuario: admin
- Contraseña: admin123

La contraseña del administrador puede configurarse
mediante la variable de entorno ADMIN_PASSWORD.

En Railway se utiliza una contraseña personalizada,
configurada mediante las variables del servicio.

La contraseña de Railway no se almacena en el repositorio
ni en la colección Postman distribuida.

La modificación de ADMIN_PASSWORD se aplica al crear
una tienda nueva y no modifica automáticamente los
usuarios almacenados en un JSON existente.

Las respuestas de la API no exponen las contraseñas
de los usuarios ni el contenido de boosters sin abrir.

La aplicación tiene fines académicos y no debe utilizarse
para almacenar credenciales o información personal real.

## 8. Ejecución local con Maven

### Requisitos

- JDK 17 o superior.
- Apache Maven.

Desde la carpeta que contiene pom.xml, ejecutar:

```bash
mvn clean package
```

Posteriormente:

```bash
java -jar target/cards4play-1.0-SNAPSHOT-jar-with-dependencies.jar
```

La aplicación estará disponible en:

http://localhost:4567

Verificación del estado del servidor:

http://localhost:4567/health

## 9. Ejecución mediante Docker

### Requisitos

- Docker Desktop o Docker Engine en ejecución.

### Construir la imagen

Desde la carpeta que contiene el Dockerfile:

```bash
docker build -t cards4play:1.1 .
```

### Crear el volumen

```bash
docker volume create cards4play-datos
```

### Ejecutar el contenedor

```bash
docker run -d --name cards4play-api -p 4567:4567 -v cards4play-datos:/app/datos -e TASA_DEFECTO=4000 cards4play:1.1
```

### Consultar los contenedores

```bash
docker ps
```

### Consultar los registros

```bash
docker logs cards4play-api
```

### Detener el contenedor

```bash
docker stop cards4play-api
```

### Eliminar el contenedor

```bash
docker rm cards4play-api
```

### Recrear el contenedor conservando los datos

```bash
docker run -d --name cards4play-api -p 4567:4567 -v cards4play-datos:/app/datos -e TASA_DEFECTO=4000 cards4play:1.1
```

El volumen permite conservar los datos aunque el
contenedor sea eliminado y creado nuevamente.

## 10. Persistencia de datos

El sistema almacena la información mediante Gson
en el archivo:

    datos/tienda.json

En Docker, el archivo se almacena dentro de:

    /app/datos/tienda.json

La persistencia incluye:

- Usuarios.
- Inventario.
- Compras.
- Boosters.
- Torneos.
- Inscripciones.

La escritura utiliza archivos temporales y reemplazo
del archivo de almacenamiento para reducir el riesgo
de corrupción durante el guardado.

Se verificó la recuperación de los datos después de
eliminar y recrear el contenedor Docker utilizando
el mismo volumen.

El despliegue de Railway no utiliza un volumen
persistente, por lo que los datos pueden perderse
al realizar un nuevo despliegue.

## 11. Conversión de USD a COP

Los precios de los productos se establecen en dólares
estadounidenses (USD).

El sistema consulta la siguiente API externa para
obtener la tasa de cambio:

https://open.er-api.com/v6/latest/USD

La conversión se realiza mediante:

    Precio COP = Precio USD * Tasa de cambio

Cuando la API externa no responde o devuelve una
respuesta inválida, el sistema utiliza una tasa
de cambio predeterminada.

Valor predeterminado:

    1 USD = 4000 COP

La tasa puede modificarse mediante la variable:

    TASA_DEFECTO

Ejemplo:

```bash
docker run -d --name cards4play-alternativo -p 4568:4567 -e TASA_DEFECTO=4200 cards4play:1.1
```

Cuando la API externa no responde, se registra
un mensaje como:

    La API no respondió, por lo que se toma un valor por defecto de 4200.0

Las respuestas correspondientes indican el origen
de la tasa utilizada:

- API: tasa obtenida del servicio externo.
- DEFECTO: tasa predeterminada.

## 12. Endpoints de la API REST

### Estado del servidor

| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | /health | Público |

### Usuarios y autenticación

| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | /usuarios/me | Autenticado |
| POST | /usuarios | ADMIN |

### Productos

| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | /productos | Autenticado |
| GET | /productos/:id | Autenticado |
| POST | /cartas | ADMIN |
| POST | /accesorios | ADMIN |
| POST | /productos-sellados | ADMIN |
| POST | /boosters | ADMIN |

### Compras

| Método | Endpoint | Acceso |
|--------|----------|--------|
| POST | /compras | CLIENTE |
| GET | /compras | CLIENTE |

### Boosters

| Método | Endpoint | Acceso |
|--------|----------|--------|
| POST | /boosters/:id/abrir | CLIENTE |

### Torneos

| Método | Endpoint | Acceso |
|--------|----------|--------|
| POST | /torneos | ADMIN |
| GET | /torneos | Autenticado |
| GET | /torneos/:id | Autenticado |
| POST | /torneos/:id/inscripciones | CLIENTE |

## 13. Ejemplos de peticiones JSON

### Registrar cliente

POST /usuarios

```json
{
  "nombreUsuario": "cliente01",
  "contrasena": "cliente123"
}
```

### Registrar carta

POST /cartas

```json
{
  "nombre": "Charizard",
  "rareza": "UR",
  "precioUsd": 25
}
```

### Registrar accesorio

POST /accesorios

```json
{
  "nombre": "Protector de cartas",
  "precioUsd": 5
}
```

### Registrar producto sellado

POST /productos-sellados

```json
{
  "nombre": "Caja coleccionista",
  "precioUsd": 30
}
```

### Crear booster

POST /boosters

```json
{
  "precioUsd": 15
}
```

### Comprar producto

POST /compras

```json
{
  "idProducto": "ID_DEL_PRODUCTO"
}
```

### Crear torneo

POST /torneos

```json
{
  "nombre": "Torneo Cards4Play",
  "fecha": "2026-12-20",
  "maxParticipantes": 16
}
```

## 14. Códigos de respuesta HTTP

| Código | Significado |
|--------|-------------|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Server Error |

Las respuestas de error controladas utilizan
una estructura JSON como la siguiente:

```json
{
  "error": "Descripción del error"
}
```

## 15. Pruebas automatizadas con Postman

La colección de Postman se encuentra en:

    postman/Cards4Play.postman_collection.json

### Instrucciones

1. Abrir Postman.
2. Seleccionar Import.
3. Importar el archivo JSON de la colección.
4. Configurar la variable baseUrl.
5. Configurar las credenciales correspondientes.
6. Ejecutar la colección mediante Collection Runner.

### Variables principales

| Variable | Valor local |
|----------|-------------|
| baseUrl | http://localhost:4567 |
| adminUser | admin |
| adminPassword | admin123 |
| clientPassword | cliente123 |

Las demás variables se generan o actualizan
automáticamente durante la ejecución.

Para probar Railway, modificar baseUrl y utilizar
la contraseña del administrador configurada en
el servicio.

### Organización de las pruebas

- Estado del servidor.
- Usuarios y autenticación.
- Productos.
- Compras.
- Boosters.
- Torneos.
- Errores y seguridad.

### Resultados

- 23 verificaciones funcionales del backend.
- 32 peticiones automatizadas de la API REST.
- 169 pruebas automatizadas con Postman.
- Verificación de persistencia en Docker.
- Verificación de tasa de cambio predeterminada.
- Verificación de autenticación y autorización.
- Verificación del despliegue en Railway.

La colección fue ejecutada satisfactoriamente
en el entorno local y en Railway.

## 16. Despliegue en Railway

La aplicación está desplegada mediante Railway
utilizando el repositorio de GitHub y el Dockerfile.

URL pública:

https://proyecto-final-production-40d11b60.up.railway.app

Endpoint de verificación:

https://proyecto-final-production-40d11b60.up.railway.app/health

### Variables de entorno

| Variable | Descripción |
|----------|-------------|
| PORT | Puerto asignado al servicio |
| TASA_DEFECTO | Tasa de cambio predeterminada |
| ADMIN_PASSWORD | Contraseña del administrador |

Railway proporciona el puerto mediante la variable PORT.

La aplicación escucha en la dirección 0.0.0.0
para permitir el acceso desde el exterior.

El servicio utiliza /health como endpoint de
verificación de disponibilidad.

## 17. Control de versiones

El proyecto utiliza Git y GitHub para gestionar
el código fuente.

Rama principal:

- main

Ramas de desarrollo:

- feature/configuracion-proyecto
- feature/correcciones-backend
- feature/api-rest
- feature/pruebas-postman
- feature/docker
- feature/seguridad-despliegue
- feature/documentacion

Las funcionalidades se desarrollaron en ramas
independientes y se integraron mediante merges
a la rama principal.

Se utilizan archivos .gitignore y .dockerignore
para excluir archivos compilados, datos locales
y configuraciones innecesarias.

## 18. Repositorio

https://github.com/garciamauricio3516/Proyecto-Final

## 19. Conclusión

Cards4Play integra programación orientada a objetos,
servicios REST, persistencia JSON, autenticación,
consumo de servicios externos, pruebas automatizadas,
control de versiones, contenedores Docker y despliegue
en la nube.

El proyecto fue desarrollado como ejercicio académico
de la asignatura Desarrollo de Software de la
Institución Universitaria EAM.

## Colecciones de Postman por entorno

El proyecto Cards4Play dispone de dos colecciones independientes de Postman para facilitar las pruebas locales y remotas de la API REST.

| Característica | Docker Local | Railway |
|---|---|---|
| Carpetas | 7 | 7 |
| Peticiones totales | 41 | 41 |
| Peticiones automatizadas | 32 | 32 |
| Peticiones manuales | 9 | 9 |
| Pruebas automatizadas | 169 | 169 |

### Docker Local

- Archivo: `postman/Cards4Play-Docker-Local.postman_collection.json`
- URL: `http://localhost:4567`
- Usuario administrador: `admin`
- Contraseña de desarrollo: `admin123`
- Persistencia mediante el volumen Docker `cards4play-datos`.

### Railway

- Archivo: `postman/Cards4Play-Railway.postman_collection.json`
- URL: `https://proyecto-final-production-40d11b60.up.railway.app`
- Usuario administrador: `admin`
- Contraseña configurada mediante la variable de entorno `ADMIN_PASSWORD`.

Por seguridad, la contraseña del administrador de Railway no se incluye en la colección exportada. Debe configurarse en la variable `adminPassword` de Postman antes de ejecutar las peticiones.

### Ejecución de las pruebas

1. Importar la colección correspondiente en Postman.
2. Verificar las variables de conexión y autenticación.
3. Seleccionar las 32 peticiones automatizadas en Collection Runner.
4. Excluir las 9 peticiones identificadas como Manual.
5. Ejecutar una iteración.

Las peticiones manuales están destinadas a demostraciones individuales y requieren configurar las credenciales y los identificadores correspondientes.

Durante el desarrollo se obtuvieron 169 pruebas aprobadas en Docker local y 169 en Railway. Las colecciones exportadas posteriormente fueron revisadas estructuralmente y se ajustaron sus credenciales para evitar publicar contraseñas privadas.

### Independencia de los entornos

Docker local y Railway ejecutan la misma aplicación, pero mantienen datos independientes. Las operaciones realizadas en un entorno no modifican los datos del otro.

Docker local conserva los datos mediante un volumen persistente. Railway utiliza el almacenamiento del contenedor, por lo que sus datos pueden perderse durante un nuevo despliegue.
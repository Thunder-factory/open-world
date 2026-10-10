# Ciudad interactiva con OpenGL

La aplicación representa una ciudad 3D interactiva desarrollada en Java con LWJGL, OpenGL, Maven y JOML. El proyecto incluye conducción, tráfico autónomo, entregas dinámicas, iluminación diurna y nocturna, decoración urbana, semáforos y minimapa.

## Tecnologías

- Java 21
- Maven
- LWJGL 3.3.3
- OpenGL 3.3 Core Profile
- GLFW
- JOML 1.10.9
- GLSL

## Requisitos

El proyecto requiere:

- JDK 21
- Maven
- Soporte para OpenGL 3.3 o superior

## Compilación y ejecución

Desde el directorio raíz, Maven compila y ejecuta la aplicación con:

```bash
mvn clean compile
mvn exec:exec
```

## Controles

| Tecla | Acción |
|---|---|
| `W` / `↑` | Acelerar |
| `S` / `↓` | Retroceder |
| `A` / `←` | Girar a la izquierda |
| `D` / `→` | Girar a la derecha |
| `SPACE` | Frenar |
| `C` | Alternar cámara FOLLOW / AERIAL |
| `Q` | Mostrar temporalmente la cámara frontal |
| `N` | Alternar iluminación DAY / NIGHT |
| `F` | Encender o apagar los faros |
| `M` | Mostrar u ocultar el minimapa |
| `H` | Mostrar u ocultar la ayuda |
| `R` | Reiniciar la ejecución y generar una nueva ruta |
| `T` | Mostrar diagnóstico en la consola |
| `ENTER` | Continuar después de completar el recorrido |
| `ESC` | Cerrar la aplicación |

## Ciudad

`City` administra una cuadrícula de 11 × 11 celdas con calles conectadas,
edificios, parques, pasos peatonales, ventanas, árboles, bancos, fuentes,
farolas y semáforos. La red vial permite acceder a las zonas utilizadas por
la generación de entregas.

Las celdas principales son:

```text
ROAD
BUILDING
PARK
```

Cada parque utiliza cuatro farolas ubicadas en sus esquinas. Las farolas de
los parques emplean la misma fuente puntual y la misma emisión nocturna que
las farolas de las calles.

## Arquitectura

```text
src/main/java/com/openworld
├── Main.java                         punto de entrada
├── Game.java                         orquestación del ciclo
├── core
│   ├── Input.java                    estados de teclado
│   └── Window.java                   ventana y framebuffer
├── gameplay
│   └── DeliveryManager.java          ruta y progreso de entregas
├── lighting
│   └── LightingSystem.java           día, noche, farolas y faros
├── render
│   ├── Camera.java                   cámara principal
│   ├── Minimap.java                  segundo pase de cámara
│   ├── Renderer.java                 comandos de dibujo
│   └── mesh                       mallas reutilizables
├── traffic
│   ├── AutonomousTrafficSystem.java tráfico autónomo
│   └── TrafficVehicle.java           vehículo con waypoint
├── validation
│   └── LogicValidator.java           comprobaciones de estado
├── vehicle
│   ├── Car.java                      movimiento y colisión
│   └── VehicleGeometry.java          medidas compartidas
├── visual
│   ├── DeliveryVisual.java           marcador de entrega
│   ├── TreeVisual.java               árboles
│   └── VehicleVisual.java            piezas visuales del automóvil
└── world
    ├── City.java                     cuadrícula y objetos urbanos
    └── TrafficLightSystem.java      ciclo visual de semáforos
```

`Game` crea los sistemas, ejecuta las validaciones, coordina la actualización
por cuadro y libera los recursos OpenGL al finalizar.

El ciclo principal sigue este orden:

```text
Window.create()
    ↓
Input, Renderer, City y sistemas de juego
    ↓
LogicValidator.run()
    ↓
loop de actualización y renderizado
    ↓
cleanup de recursos OpenGL
```

El límite de tiempo por cuadro se establece en `0.05` segundos mediante
`MAX_DELTA_TIME`. El título de la ventana se actualiza cada `0.20` segundos.

## Geometría, low-poly y vértices

La escena mantiene una estética `low-poly`: las formas principales conservan
planos visibles, siluetas simples y una cantidad moderada de polígonos. Las
mallas se generan en código, sin modelos externos ni texturas externas.

`MeshFactory` almacena seis valores por vértice:

```text
position.x, position.y, position.z,
normal.x,   normal.y,   normal.z
```

`Mesh` utiliza un VAO, un VBO de vértices y un EBO de índices. El atributo cero
contiene la posición y el atributo uno contiene la normal. Las mallas se
reutilizan con distintas posiciones, escalas, rotaciones y materiales.

`MeshLibrary` conserva una instancia de cada malla reutilizable:

```text
cube, quad, cylinder, sphere, cone
windowFrame, circle, ring
vehicleBody, vehicleGlassTrim, vehicleGlass
vehicleFrontLights, vehicleGrille
vehicleRearLights, vehicleRearPlate
```

Las formas curvas reciben más vértices cuando una silueta circular resulta
importante para la lectura visual. La densidad actual es:

| Malla | Densidad | Uso principal |
|---|---:|---|
| Cilindro | 12 segmentos | ruedas, postes y elementos urbanos |
| Cono | 12 segmentos | marcadores y piezas cónicas |
| Esfera | 8 latitudes × 12 longitudes | copas, espejos y núcleos |
| Círculo | 24 segmentos | áreas planas y luces circulares |
| Aro | 48 segmentos | marcadores y bordes redondos |

El aumento de vértices se concentra en curvas y contornos. Las cajas, quads
y piezas planas conservan una topología pequeña porque no necesitan
subdivisión. `createCircle()` genera un centro y una corona de triángulos;
`createRing()` genera vértices interiores y exteriores y divide cada sector en
dos triángulos.

`createWindowFrame()` construye cuatro barras independientes. El borde utiliza
`WINDOW_FRAME_BORDER_RATIO = 0.12`, lo que permite representar superficies
interiores hundidas en ventanas y elementos luminosos.

## Vehículos

El vehículo controlado incorpora aceleración, reversa, frenado, dirección
dependiente de la velocidad, resistencia al movimiento, límites de circulación
y colisiones. `VehicleVisual` compone la carrocería mediante piezas separadas:

```text
carrocería, marco de cristales y cristales
luces delanteras, parrilla y luces traseras
placa trasera, cuatro ruedas y cubos centrales
dos espejos y sus cristales
```

`VehicleGeometry` centraliza las medidas compartidas:

```text
PLAYER_VISUAL_SCALE       = 0.90
TRAFFIC_VISUAL_SCALE      = 0.88
PLAYER_COLLISION_RADIUS   = 1.92
TRAFFIC_COLLISION_RADIUS  = 1.88
FRONT_FACE_Z              = -1.95
REAR_FACE_Z               =  1.48
```

La diferencia entre `FRONT_FACE_Z` y `REAR_FACE_Z` permite distinguir las
proporciones del frente y la cola. Las luces delanteras, la parrilla, las
luces traseras y la placa se colocan sobre caras diferentes.

El estado del vehículo se describe mediante posición X-Z, ángulo y velocidad.
Con ángulo cero, el frente apunta hacia `-Z`:

```text
forwardX = -sin(angle)
forwardZ = -cos(angle)
```

El desplazamiento se integra mediante:

```text
posición nueva = posición actual + dirección × velocidad × deltaTime
```

El vehículo del jugador también consulta `AutonomousTrafficSystem.canDrive()`
para evitar atravesar los vehículos autónomos.

## Tráfico autónomo

`AutonomousTrafficSystem` crea vehículos con rutas de waypoints sobre la red
vial. `TrafficVehicle` conserva posición, ángulo, velocidad, waypoint actual
y estado de movimiento.

Durante cada actualización, el sistema mueve el vehículo, avanza el waypoint
cuando corresponde, valida los segmentos de la ruta y expone posición y radio
para las colisiones del jugador. El reinicio restaura posiciones, ángulos,
velocidades y waypoints iniciales.

## Cámaras

La cámara `FOLLOW` sigue al vehículo desde atrás y por encima, utilizando su
vector frontal. La cámara `AERIAL` calcula una vista elevada a partir del
tamaño de la ciudad. La tecla `C` alterna ambos modos.

La tecla `Q` activa una vista frontal temporal mientras permanece presionada.
Al liberarla se restaura el modo de cámara activo.

## Iluminación

El sistema distingue los modos `DAY` y `NIGHT`. El fragment shader combina:

```text
color base × (ambiente + luz direccional + luces puntuales + faros)
+ emisión
```

Las normales se transforman mediante la inversa transpuesta de la matriz de
modelo. `LightingSystem` controla la luz ambiental, la luz direccional, las
farolas, las ventanas emisivas y los focos direccionales del vehículo.

Cada foco del vehículo utiliza posición, dirección, alcance, atenuación, cono
interior y cono exterior. La tecla `F` cambia el estado de los focos sin
alterar el modo día o noche.

## Semáforos

Cada intersección utiliza cuatro semáforos, uno por aproximación. Cada unidad
contiene una carcasa, un marco circular profundo y tres lentes circulares
ligeramente hundidas:

```text
lente roja
lente amarilla
lente verde
```

El ciclo visual utiliza las fases:

```text
NS_GREEN  5.0 s
NS_YELLOW 1.5 s
EW_GREEN  5.0 s
EW_YELLOW 1.5 s
```

Los semáforos no modifican la velocidad ni el movimiento de los vehículos.

## Entregas

`Game` controla la cantidad de destinos mediante `DELIVERY_POINT_COUNT`, cuyo
valor actual es tres. `DeliveryManager` recibe la posición inicial del
vehículo y genera cada destino aleatoriamente dentro de los límites del mundo.

Una coordenada se rechaza cuando no pertenece a una zona transitable, no deja
espacio para el radio de llegada o queda a menos de 30 unidades del punto
anterior. El algoritmo utiliza hasta 5000 intentos por destino.

La entrega se completa cuando:

```text
distancia al destino <= 3.0
abs(velocidad) <= 1.0
```

El progreso inicia en `0/3`. La tecla `R` vuelve a generar la ruta. Después
del último destino, `ENTER` permite cerrar el panel de finalización y continuar
conduciendo.

`DeliveryVisual` muestra el destino activo mediante una zona circular, un aro
de 48 segmentos, un aro pulsante, una marca central y un rombo flotante
compuesto por dos conos y un núcleo esférico. La emisión mantiene la
legibilidad durante la noche.

## Minimap

El minimapa utiliza una cámara ortográfica, un viewport propio y un segundo
pase de renderizado. El vehículo permanece centrado y orientado hacia arriba;
el mapa gira según su dirección. La vista muestra el área cercana, limitada a
3.20 celdas alrededor del vehículo.

El minimapa se dibuja en un área circular situada en la parte superior derecha.
El marco utiliza 72 segmentos. El destino se representa sobre su posición
cuando está dentro del área y con una flecha en el borde cuando queda fuera.
La letra `N` se coloca según la orientación actual del mapa.

`glScissor` limita la limpieza al área del minimapa y el viewport principal se
restaura al terminar el segundo pase.

## Orden de actualización y renderizado

Cada iteración de `Game.loop()` ejecuta:

```text
calculateDeltaTime()
Input.update()
processInput()
AutonomousTrafficSystem.update()
Car.update()
LightingSystem.update()
DeliveryManager.update()
Hud.update()
TrafficLightSystem.update()
Camera.update()
render()
Window.update()
```

La vista principal se dibuja en este orden:

```text
Renderer.beginFrame()
City.render()
LightingSystem.render()
TrafficLightSystem.render()
DeliveryVisual.render()
AutonomousTrafficSystem.render()
Car.render()
Minimap.render()
Hud.render()
Renderer.endFrame()
```

El minimapa se dibuja después de la escena 3D y el HUD se dibuja al final para
permanecer visible sobre las capas anteriores.

## Reinicio

La tecla `R` restaura los sistemas sin reconstruir las mallas OpenGL:

```text
Car                    posición, orientación y velocidad iniciales
DeliveryManager        nueva ruta, tiempo 0 y progreso 0/3
TrafficLightSystem     NS_GREEN y tiempo de fase 0
AutonomousTrafficSystem posiciones, velocidades y waypoints iniciales
LightingSystem         DAY y faros apagados
Camera                 FOLLOW
Minimap                visible y HEADING_UP
Hud                    estado inicial de ayuda y recorrido
```

## Ventana y validaciones

`Window` diferencia el tamaño lógico de la ventana del tamaño real del
framebuffer. El callback de redimensionamiento actualiza ambos valores y evita
configurar viewports con dimensiones cero. El tamaño del minimapa se calcula a
partir del framebuffer para conservar su escala después de un redimensionado.

`LogicValidator.run()` comprueba dimensiones y tipos de celda, conectividad
vial, posición inicial, movimiento, reinicio, colisiones, tráfico autónomo,
entregas dinámicas, iluminación, faros, semáforos, cámara y minimapa. Una
condición incumplida genera una excepción descriptiva durante el inicio.

## Diagnóstico

Mientras `T` permanece presionada, el sistema informa `deltaTime`, velocidad,
posición, ángulo, cámara, iluminación, faros, minimapa, fase de semáforos,
progreso y tiempo de entregas, estado final del recorrido y validez de la
posición.

## Recursos y limitaciones

La geometría principal se genera mediante primitivas y datos definidos en el
código. No se utilizan modelos 3D ni texturas externas. Las colisiones
utilizan aproximaciones circulares sobre el plano X-Z y los semáforos funcionan
como elementos visuales sin aplicar reglas de tráfico.

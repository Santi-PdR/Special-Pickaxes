# Validación de entrega — 2026-10-06

## Entorno y alcance

La compilación y las pruebas de Minecraft se ejecutaron en **GitHub Actions,
Ubuntu, Java 17, Forge 47.3.0, Minecraft 1.20.1**, no en el sandbox de edición.
Aquí se intentaron `./gradlew build`, `./gradlew test` y
`./gradlew runGameTestServer`: los tres quedaron bloqueados por ausencia de Java.
Las descargas locales de JDK/Maven/Forge fallaron por TLS. La verificación remota
no se sustituyó por una comprobación estática.

La descarga normal de artefactos/logs de Actions también falló por acceso al CDN.
Se recuperaron mediante Checks API con `tools/fetch_ci_evidence.py`, verificando
SHA-256 del ZIP y de cada archivo exportado. El transporte está acotado y solo
exporta productos/logs de build explícitos, nunca credenciales ni datos de mundos.

## Resultado final

Código validado: **`d65452c`** (los commits posteriores de documentación no cambian
fuentes ni recursos del JAR).

[GitHub Actions — ejecución 37520582115](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/37520582115): **success**.

| Comprobación | Resultado |
|---|---|
| `./gradlew build test` | BUILD SUCCESSFUL; JAR reobfuscado; 6 JUnit, 0 fallos/errores |
| `./gradlew runGameTestServer` | All 10 required tests passed |
| Instalación del JAR + servidor dedicado | Done (22.955s); guardado y salida limpia, código 0 |
| Escaneo de logs finales | Sin ERROR/Exception/Crash/Stacktrace/Failed/Invalid |
| Validación de recursos | 4 comprobaciones, OK |
| `git diff --check` | OK |

**Archivo:** `special-pickaxes-1.20.1-1.0.0.jar`  
**Tamaño:** 70,177 bytes (68,53 KiB)  
**Ubicación en el workspace:** `artifacts/special-pickaxes-1.20.1-1.0.0.jar`  
**Salida original del build:** `build/libs/special-pickaxes-1.20.1-1.0.0.jar`  
**SHA-256:** `7e89e5fd5a43082e92300e752338c061a988aa0cb193f91a0fb139203d23104d`

La copia descargable en `artifacts/` está excluida de Git; se conserva en el
workspace y en el artefacto de Actions. Contiene 24 class files Java 17 y los
recursos del mod; no incluye clases de los JAR de referencia ni fixtures de test.

Los logs de referencia se conservan en `docs/validation/` (sin espacios finales de línea; los originales están en la evidencia de Actions). El log de instalación
completo, informes y JAR también están en el artefacto `forge-validation` del run.

## Cobertura automática real

| Sistema | Comprobación realizada |
|---|---|
| Registro | Diez RegistryObjects resueltos a SpecialPickaxeItem |
| Recetas | RecipeManager contiene los diez IDs |
| Recursos | JSON válido, modelo→textura existente, PNG 16×16, claves EN/ES, ingredientes del patrón y registro de loot modifier |
| Overdrive | Activación, efecto, BreakSpeed 8→80, coste exacto y rechazo de segundo uso por cooldown |
| Excavador | Destrucción real de plano 3×3 por gameMode; nueve puntos de durabilidad |
| Vein Miner | Conectividad de mineral, parada ante tipo distinto, cuatro bloques/cuatro puntos de desgaste; BFS con ciclos/límites/barreras en JUnit |
| Inferno | Loot real de hierro con Fortune convertido en lingotes; Silk Touch conserva mineral; diamante sin receta permanece diamante |
| Magnetic | Velocidad dirigida al jugador; rechazo de objetos con pickup delay |
| Scanner | Detección de la posición de mineral de la fixture, límite de resultados y activación en servidor |
| Storm | Encuentra monstruo visible y reduce realmente su salud |
| Void | Guarda ancla, vuelve a posición exacta y elimina el ancla tras usarla |
| Ender | Movimiento por camino libre; barrido deniega muro y destino sólido |
| Explosive | Activación sobre bloque observado, desgaste limitado, bloque protegido y bloque fuera de radio sobreviven |
| Claims/herramienta | Cancelación de BreakEvent conserva bloque y durabilidad; bedrock rechazado |
| Dedicated server | Instalación Forge limpia, JAR reobfuscado en mods/, generación de mundo, Done, save-all, stop limpio |
| Empaquetado | Clases Java 17; sin clases de los mods de referencia; fixtures/GameTests excluidos del JAR final |

Los tests de juego usan FakePlayer para acciones y un manejador real
ServerGamePacketListenerImpl sin socket para la ruta de teleportación. No se
presenta eso como prueba de un cliente remoto conectado. El servidor limpio
recibe los diez comandos `give @a specialpickaxes:<id>`: los IDs son parseados,
pero al no haber jugadores conectados responde “No player was found”. La
obtención/equipamiento de herramientas se verifica en los GameTests, no se
atribuye falsamente a esos comandos del servidor vacío.

## Revisión de errores y regresiones

Se revisaron `ERROR`, `Exception`, `Crash`, `Stacktrace`, `Failed` e `Invalid`.
El workflow falla si los logs de ejecución finales contienen esas palabras.
Durante el desarrollo se corrigieron:

- Error de servidor de tests por `server.properties` ausente.
- Apuntado de Explosive usando interpolación anterior en vez de posición actual.
- Fixture de movimiento: la conexión ficticia de Forge hacía teleport no-op;
  se sustituyó por el manejador real, sin debilitar las aserciones de posición.
- Configuración demasiado temprana de una tarea generada por ForgeGradle.
- Firma incorrecta del constructor de BreakSpeed en un test.
- Descarga del instalador mediante urllib: se sustituyó por curl con reintentos
  y se conserva el diagnóstico de instalación completo.

Las advertencias de primera creación de configuración de Forge y los avisos de
bibliotecas internas Forge sin mods.toml no son excepciones del mod. No se han
silenciado ni eliminado de los logs. Los avisos de deprecación de Gradle/Forge
permanecen visibles y no impidieron la compilación con el toolchain fijado.

## Lo que NO se afirma haber validado

- No se realizó una sesión gráfica de cliente: apariencia en mano/inventario,
  percepción de sonidos/partículas, overlay y traducciones requieren revisión
  visual. Sí se validaron archivos y referencias de los recursos.
- No hubo conexión de un cliente Minecraft real a ese servidor: sincronización
  de red end-to-end pendiente, aunque se usan protocolos/efectos vanilla y los
  cambios de mundo se prueban del lado servidor.
- No se instaló un mod externo de claims. Se comprobó cancelación Forge real;
  las integraciones específicas de protección de escaneo/movimiento deben usar
  AbilityUseEvent y probarse en el modpack de destino.
- Sin benchmark multijugador ni certificación de todas las recetas/loot tables
  de terceros. La implementación usa los sistemas Forge/Minecraft apropiados,
  pero eso no demuestra compatibilidad universal.

## Reproducción

```sh
./gradlew build test
./gradlew runGameTestServer
python3 tools/dedicated_smoke.py
python3 tools/validate_resources.py
python3 tools/validate_runtime_logs.py run/logs/latest.log build/packaged-smoke/console.log
git diff --check
sha256sum build/libs/special-pickaxes-1.20.1-1.0.0.jar
```

El smoke test usa un servidor **aislado de prueba**, puerto 25575, online-mode
false y aceptación de EULA en `build/packaged-smoke/`; no modifica una instalación
externa ni recomienda usar online-mode false en producción.

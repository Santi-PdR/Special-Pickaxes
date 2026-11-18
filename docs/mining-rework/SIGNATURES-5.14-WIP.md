# 5.14 — identidad visual, trabajo en curso

Esta entrega NO completa la petición de unas 50 mejoras notorias. 5.14.1 pasó la validación completa; no se ha aprobado visualmente cada animación.

## Implementado, pendiente de prueba gráfica de ejecución

Las activaciones ahora seleccionan trece partículas registradas, una por pico, en lugar de reutilizar siete sprites. Cada firma tiene cuatro imágenes propias de 32×32, transparencia, emisión, entrada/salida gradual, giro y trayectoria limitada. Son dibujos de **partículas**, no texturas de objetos: no se ha dibujado ni sustituido ningún icono de pico.

| Pico | Símbolo | Movimiento |
|---|---|---|
| Alba Eterna | amanecer y estrella | expansión ascendente |
| Coro Sísmico | tres ondas | vibración longitudinal |
| Ocaso Gravitatorio | órbitas incompletas | espiral expansiva |
| Crisol Alquímico | sello triangular doble | hélice ascendente |
| Corona del Tiempo | reloj de arena | suspensión escalonada |
| Telar del Mundo | rama con hojas | crecimiento vertical |
| Ícaro | alas simétricas | apertura ascendente |
| Axioma del Vacío | anillo fracturado y rombo | implosión |
| Rompemundos | fractura central | onda balística |
| Exodio Estelar | estrella y estela | caída meteórica |
| Iridio Rompevetas | cristal facetado | ascenso orbital |
| Flor Infernal | flor de cinco puntas | floración parabólica |
| Desgarraestratos | tres cortes | deslizamiento lateral |

También se separaron las siluetas de emisión antes compartidas: Flor Infernal/Crisol y Ocaso/Corona/Rompemundos. Se conservan 16 partículas por activación, 256 globales por tick, envío sólo al lanzador, ausencia de colisiones y ninguna emisión ambiental. Los antiguos registros siguen existiendo por compatibilidad.

## Arte externo: investigación, no integración

`artwork/candidates/research.json` conserva repositorio, ruta y SHA del blob de tres originales descargados de Malum y MineFantasy. Se inspeccionaron spellweaving_pickaxe y pickDragon. No se han aprobado como arte definitivo ni establecido permisos específicos de assets. Permanecen fuera de `src/main/resources`: no se empaquetan en el mod. No se presenta Mythic Metals como un descubrimiento nuevo ni se confunde licencia del código con la del arte.

## Validación

5.14.1 pasó CI 44367188726: 166 JUnit, 400 GameTests, cliente gráfico y servidor empaquetado. Los tests de recursos y Python locales no sustituyen esa evidencia. No se ha comprobado visualmente cada firma en movimiento dentro del juego.

## 5.14.1 — primaria y secundaria distinguibles

- 13 secuencias secundarias adicionales, cuatro imágenes por secuencia: 104 imágenes en total, 26 firmas.
- Símbolos secundarios que representan la acción: chevrones ascendentes de Ícaro, embudo magnético de Ocaso, cristales atraídos de Iridio, plano cortado de Desgarraestratos y columnas ardientes de Flor Infernal.
- 13 trayectorias y 13 siluetas de lanzamiento secundarias diferentes de las primarias.
- 13 timbres secundarios distintos del sonido primario, usando sonidos existentes de Minecraft. No son grabaciones originales ni archivos externos.
- Presupuesto compartido: alternar primaria y secundaria no evita la deduplicación del mismo tick.
- Corrección del test de 5.14.0: `ArtifactKind` inicializa sonidos y necesita los registros Minecraft. La prueba unitaria compara ahora los enums geométricos puros; GameTest comprueba la lista real, la exclusividad de los 26 registros y los sonidos.

El run 44355060736 compiló Java y creó el JAR, pero falló en la prueba de bootstrap. Por tanto, NO validó el runtime de 5.14.0. El run 44366567461 detectó que RESPAWN_ANCHOR_DEPLETE es un Holder y requiere `.value()`; corregido antes de volver a validar.

La validación completa de 5.14.1 pasó en el runtime `8722f0c4b59a419da6637cc1c444be06a83d52f0`. No se han integrado texturas externas nuevas ni se ha completado la petición de unas 50 mejoras notorias.

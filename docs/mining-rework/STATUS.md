# Rework minero — validación en curso, release bloqueado

Se continúa el mismo diseño de nueve artefactos. No hay release 5.0 aprobado.
Las pruebas y el JAR de 4.0 no certifican este trabajo.

## Correcciones y migración realizadas

- Build Java 17 trasladado a GitHub Actions. Versión de trabajo: 5.0.0.
- Retirados dieciséis tests de habilidades eliminadas; migrados catálogo, Crisol,
  Worldloom, Eventide, Axiom, modos, tooltips y ciclo de selección al contrato actual.
- Pruebas nuevas: fluidos source/flowing de agua/lava y fluido Forge externo real,
  todas las fronteras destructivas, seis programas direccionales, tres Worldloom
  en cuatro orientaciones, geometría, reentrada, repetición física y duplicación de
  paquetes, propiedad de selección, herramienta exacta y Crisol radial.
- Pulsaciones ligadas a UUID de herramienta; secuencia sobrevive respawn y se
  limpia en logout. Callbacks no pueden reentrar activación ni scheduler.
- Palimpsest recupera la excavación cercana cuando el rayo, tras minar, alcanza
  una pared lejana sin memoria. Fixture direccional limpia sus barreras entre casos.
- Fluido de prueba inicializado durante RegisterEvent, no con registro congelado.
- Se exige resumen de GameTests completos: un exit 0 de Gradle no basta.
- Servidor empaquetado valida los nueve IDs; escáner detecta también items desconocidos.
- Cliente migrado a 23 modos, capturas normales/SHIFT, C real y activación real,
  inventario de nueve items y escena de WORLD SHATTER con comprobación de cuña/barrera.
  Esta migración todavía requiere resultados completos y revisión visual.
- Estasis no aumenta velocidad al liberar entidades; dominios limitados por capacidad.
- Documentación del jugador y compatibilidad añadida; ocho assets archivados intactos.

## Evidencia nueva disponible (no extrapolar entre commits)

| Commit | Run | Resultado comprobado |
|---|---|---|
| 2c9db17 | 37675048174 | `./gradlew build` Java 17 aprobado; solo diagnóstico, no release. |
| c85eff8 | 37675376388 | Build y 17 JUnit aprobados; integración detectó inicialización prematura del fluido de test. |
| 0237600 | 37675999825 | 50 GameTests ejecutados: 48 pasaron, 2 fallaron (fixture CORE DRILL y reconstrucción). No release. |
| fa0309a | 37676198338 / 37676198271 | Build con 20 JUnit aprobado; 50 GameTests ejecutados, solo reconstrucción falló. Corrección posterior en 5eedfa8. |

El conjunto actual añade casos posteriores; debe validarse entero en su propio
commit. Recursos: 8 pruebas Python aprobadas. Diff-check limpio en los checkpoints.
Ni los tests Java nuevos ni las capturas pendientes se dan por aprobados aquí.

## Publicación

El workflow conserva diagnósticos sin JAR. Se descubrió que el exportador histórico
por Checks también incluía binarios: se retiraron esos exports heredados de los
primeros runs mediante `withdraw_unvalidated.py`, ejecutado por la app de Actions.
El exportador actual no incluye JAR sin autorización explícita de publicación.
No se debe anunciar ni entregar un binario hasta tener todos los checks verdes,
revisar capturas, auditar el commit final y habilitar la publicación final.

## Pendientes para cerrar

1. Esperar los runs del código actual, recuperar evidencia y corregir fallos.
2. Completar cliente/servidor y revisar visualmente TODAS las capturas requeridas.
3. Auditar cobertura, regeneración de recursos, referencias y secretos del JAR final.
4. Actualizar README, validación y guía con resultados definitivos.
5. Registrar commit, run, tamaño y SHA-256 y publicar exclusivamente el JAR aprobado.

No se usó/modificó el repositorio siege ni se accedió a test-1. No se probó el
modpack completo, shaders, LOD, configuraciones reales de claims ni TPS multijugador.

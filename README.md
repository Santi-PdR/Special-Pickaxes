# Special Pickaxes — Relic Workshop 3.0.0

**Minecraft 1.20.1 · Forge 47.3.0+ · Java 17**

Evolución de los diez artefactos de Siege: **14 picos+hachas+palas**, selección por
esquinas, análisis previo, transformaciones regionales, memoria entre sesiones,
trabajos grandes acotados, modos nombrados y nueva colección de pixel-art.
Sin recetas, minerales, worldgen ni dependencias de animación externas.

## Para jugar

[Guía completa de los 14 artefactos, controles y ejemplos](docs/expansion/PLAYER-GUIDE.md).
Nuevos: **Worldbreaker, Chronicle, Keystone y Tessellator**.

Instala `special-pickaxes-1.20.1-3.0.0.jar` en cliente y servidor, reemplazando el JAR
anterior (no instales dos versiones). Los IDs existentes no se modificaron.
Obtención: creativo, `/give` o `/specialpickaxes grant Jugador ID` (permiso 2).
La instancia solicitada para probar es **`test-1`**, nunca `siege` sin autorización.
No se afirma haber accedido a una instancia externa no disponible en el workspace.

- Usar arma una selección regional; clic izquierdo elige esquinas.
- Usar analiza sin cambios; usar de nuevo en **LISTO** confirma ejecución.
- Agachado + izquierdo cambia transformación. Agachado + usar cancela.
- Sin selección/trabajo, agachado + usar cambia el modo explícito.
- Durante el trabajo, usar pausa/reanuda; cambiar de herramienta cancela.

Consulta la guía: economía, capacidad de memoria, filtros de materia, dimensiones
compatibles y curva de Fortune extrema son decisiones explícitas, no promesas de
copiar cualquier bloque o producir cantidades ilimitadas de loot.

## Ingeniería y evidencias

- [Auditoría del punto de partida y decisiones](docs/expansion/AUDIT.md).
- [Dirección artística reproducible, sin imágenes aleatorias](docs/expansion/ART-DIRECTION.md).
- [Auditoría conservada de los cuatro JARs de referencia](docs/siege/AUDIT.md).
- [Alcance de validación](docs/VALIDACION.md).

```sh
./gradlew build test
./gradlew runGameTestServer
python3 tools/validate_resources.py
python3 tools/verify_release.py
python3 tools/dedicated_smoke.py
git diff --check
```

CI instala Java 17 y prueba además el cliente real con Xvfb: galería, catorce
capturas de tooltips/previews/modos con Efficiency 1000 y tercera persona.
Estas fixtures están excluidas del JAR. El artefacto `forge-validation` contiene
JAR, RELEASE.json, SHA256SUMS, informes y capturas. El manifiesto **Build-Commit**
identifica el commit exacto; un run fallido no constituye una entrega validada.
No hay package.json: npm run check no corresponde.

Las cuatro referencias se conservan, no se redistribuyen dentro del mod ni se
cargan como dependencias. La documentación 2.0 en `docs/siege/DESIGNS.md` y los
informes 1.0 son históricos, no instrucciones de uso de 3.0.

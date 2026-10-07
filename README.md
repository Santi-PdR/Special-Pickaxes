# Special Pickaxes — mining rework

Forge **1.20.1 / Java 17**. Nueve artefactos de minería extrema, también efectivos
como hacha y pala. Sin recetas obligatorias, minerales nuevos, worldgen, energía,
almacenamiento ni X-Ray.

**5.0.0 está en validación; todavía no es una release aprobada.**
No se usa la evidencia de 4.0 para certificar el rework. Los binarios permanecen
bloqueados hasta pasar build, tests, GameTests, servidor, cliente y revisión visual.

## Catálogo

Palimpsest · Choir · Eventide · Crucible · Interregnum · Worldloom · Icarus ·
Hollow Axiom · Worldbreaker.

- Worldbreaker: CARVE, FRACTURE, CLEAVE, CORE DRILL y WORLD SHATTER.
- Worldloom: REFUGIO, PUENTE y PARED; construcción con bloques reales.
- Crucible: regional con dos esquinas y confirmación, o radial directo.
- Axiom: matriz conectada a recursos existentes, preservando las menas.
- Icarus: excava antes de avanzar, sin saltar barreras.

Solo Crisol regional utiliza selección. Todos los trabajos respetan bedrock,
fluidos, bloques protegidos, block entities, borde y chunks cargados. Las rutas
direccionales se detienen, no continúan tras una barrera. Los drops proceden del
harvesting nativo y la congestión pausa el trabajo.

## Jugar y administrar

R activa; C cambia de modo; B selecciona donde corresponde; Enter confirma;
V cancela; K pausa/reanuda. Controles reasignables. SHIFT sobre el item muestra
la ayuda del modo actual. No se usa Shift + clic derecho.

Adquisición administrativa:

```text
/specialpickaxes grant <jugador> <id>
```

IDs: `palimpsest`, `fault_choir`, `eventide`, `paradox_crucible`, `interregnum`,
`worldloom`, `icarus`, `hollow_axiom`, `worldbreaker`.

- [Guía del jugador](docs/mining-rework/PLAYER-GUIDE.md)
- [Estado y evidencia de validación](docs/mining-rework/STATUS.md)
- [Compatibilidad y límites reales](docs/mining-rework/COMPATIBILITY.md)
- [Procedencia de los nueve sprites](docs/mining-rework/ASSET-PROVENANCE.json)
- [Biblioteca de ocho assets retirados](asset-library/README.md)

Los cuatro JAR de referencia se conservan intactos. No se empaquetan en el mod.
La documentación histórica 3.1/4.0 no describe el catálogo actual.

## Verificar

```sh
python3 tools/validate_resources.py
python3 tools/scan_secrets.py
./gradlew build
./gradlew runGameTestServer
python3 tools/verify_gametests.py run/logs/latest.log
python3 tools/dedicated_smoke.py
python3 tools/client_smoke.py
git diff --check
```

El cliente desechable de CI utiliza Xvfb. Conserva PNG originales y copias de
revisión de la misma captura; comprueba tooltips, cambios de modo y activaciones
con teclas reales. El JAR excluye fixtures, clases de test y fluido de test Forge.

La publicación requiere una revisión visual de un run exitoso con código idéntico.
No se afirma acceso a test-1 ni haber probado el modpack completo Eternal Craft —
Siege, shaders o todas las configuraciones de otros mods. No se utiliza su repositorio.

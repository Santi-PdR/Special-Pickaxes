# Special Pickaxes 4.0

**Forge 1.20.1 · Java 17 · veinte artefactos · ninguna energía**

Una revisión de los controles, presentación e identidad de 3.1, sin añadir
objetos por cantidad. Hover: nombre, una frase y tecla principal. SHIFT: pequeño manual de juego.
Sin HUD permanente, sin Shift + clic derecho para poderes.

## Jugar

[Guía breve](docs/4.0/PLAYER-GUIDE.md). Configura las teclas en
**Opciones → Controles → Special Pickaxes**:
R activar, G secundaria, C modo, B selección, Enter confirmar, V cancelar, K pausa.
Son valores iniciales reasignables; pueden coincidir con otros mods.

Directos: apuntar y activar. Regionales: seleccionar esquinas; el análisis comienza
solo y Confirmar ejecuta cuando está listo. Solo Atlas, Crucible, Chronicle,
Tessellator y los programas regionales de Worldbreaker usan esquinas.

**Worldbreaker es la pieza suprema.** Convergencia fractura un anillo conservando
el núcleo y construye un santuario pagado; no es solamente aumentar el radio.
Los otros diecinueve tienen especialidades, no copias de este programa.

Texturas reales seleccionadas de los cuatro JAR autorizados, con modelos propios,
firmas espaciales y audio por fase. No texturas de picos vanilla ni el generador
experimental de 3.0. [Procedencia de cada asset](docs/4.0/ASSET-PROVENANCE.json).
No se añade dependencia de los mods de referencia ni se modifican sus archivos.

Instala `special-pickaxes-1.20.1-4.0.0.jar` en **cliente y servidor**, reemplazando
el anterior. Obtención administrativa/creativo/recompensas: sin recetas, ores ni
worldgen. Se conservan IDs, herramientas triples, ToolActions, encantamientos altos,
materiales reales, checks de mundo y scheduler.

## Ingeniería

[Auditoría individual y arquitectura](docs/4.0/AUDIT.md).
[Validación y alcance](docs/VALIDACION.md).

```sh
./gradlew build test
./gradlew runGameTestServer
python3 tools/validate_resources.py
python3 tools/scan_secrets.py
python3 tools/verify_release.py
git diff --check
```

Actions compila, prueba y empaqueta; su artefacto `forge-validation` incluye el
JAR, manifiesto de procedencia, resultados y capturas de cliente real. Un run
fallido no constituye una entrega validada. No hay package.json.

No se usa/modifica siege, ni se inventa acceso a test-1. Los mundos CI son
desechables y no certifican el modpack externo completo. La documentación 3.0/3.1
se conserva como historia, no como controles actuales.

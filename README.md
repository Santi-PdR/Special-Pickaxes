# Special Pickaxes — 3.1.0

**Minecraft 1.20.1 · Forge 47.3.0+ · Java 17**

**20 artefactos legendarios, cada uno pico+hacha+pala.** La versión 3.1 conserva
la infraestructura de 3.0, pero abandona la interacción regional universal:
restauración local, partitura minera, dominios cinéticos, anclas, construcción,
perforación física, retorno por pasos y acompañantes reactivos tienen controles propios.

- **Sin energía:** no existe recurso que cargar, regenerar o consumir. Quedan
  durabilidad, cooldown, materiales reales y condiciones propias de cada habilidad.
- **Tooltip compacto:** identidad, modo y aviso de SHIFT. Mantén **SHIFT** para
  mecánica, controles, costes, límites, encantamientos, ejemplos y advertencias.
- **Bases visuales existentes:** modelos handheld y texturas originales de los
  picos de Minecraft. Glint, aura animada, partículas de operación y sonidos
  distinguen las reliquias; se retiraron los 14 sprites experimentales de 3.0.
- Sin recetas, minerales nuevos, worldgen ni dependencias de animación.

## Instalar y jugar

Instala `special-pickaxes-1.20.1-3.1.0.jar` en cliente y servidor, reemplazando el
JAR anterior. No instales dos versiones. Los 14 IDs existentes se conservan.
Obtención: creativo, `/give` o `/specialpickaxes grant Jugador ID` (permiso 2).

**Nuevos:** `aegis`, `lodestar`, `seam_ripper`, `causeway`, `counterseal`, `covenant`.
Consulta la [guía 3.1](docs/3.1/PLAYER-GUIDE.md) y la [auditoría de diseño](docs/3.1/DESIGN.md).

### ¿Quién usa regiones?

Solo **Atlas, Crucible, Chronicle y Tessellator**, más Worldbreaker en
**BREAK, REPHASE, TRANSPOSE y RECORD**. Worldbreaker **CARVE y RESTORE son directos**.
Meridian usa dos anclas puntuales, no esquinas de una región.

En operaciones regionales: usar activa selección; izquierdo marca esquinas;
agachado + izquierdo cambia modo/transformación; usar analiza sin modificar;
usando otra vez en LISTO confirmas. Durante trabajos, usar pausa/reanuda y
agachado + usar cancela. Cambiar herramienta cancela el trabajo en ejecución.
Los controles directos específicos están en cada tooltip SHIFT y en la guía.

## Ingeniería y verificación

```sh
./gradlew build test
./gradlew runGameTestServer
python3 tools/validate_resources.py
python3 tools/scan_secrets.py
python3 tools/verify_release.py
python3 tools/dedicated_smoke.py
git diff --check
```

Actions ejecuta Java 17, JUnit, GameTests, servidor con el JAR reobfuscado y cliente
real bajo Xvfb: galería de 20, **40 capturas normales/SHIFT**, Efficiency 1000 y
tercera persona. Las fixtures no se incluyen en el JAR. El artefacto
`forge-validation` contiene JAR, `RELEASE.json`, `SHA256SUMS`, informes y capturas.
**Build-Commit** identifica el código exacto: un run fallido no certifica una entrega.
No hay `package.json`; `npm run check` no corresponde.

[Alcance y límites de validación](docs/VALIDACION.md). No se ha inventado acceso a
**test-1** ni se usa/modifica **siege**. CI crea mundos desechables; no equivale a
certificar el modpack externo completo. Las cuatro referencias originales
permanecen intactas y fuera del JAR; su auditoría histórica está en
[docs/siege/AUDIT.md](docs/siege/AUDIT.md). Los documentos de expansión 3.0 son
históricos y no describen los controles, tooltips ni arte de 3.1.

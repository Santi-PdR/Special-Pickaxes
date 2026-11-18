# 5.15 — impactos durante la excavación y arte externo

## Cambios implementados y validación automatizada completa

La petición sigue siendo una reforma de unas 50 mejoras notorias. No se presenta esta tanda como 50 mejoras completas, ni se cuentan pruebas/fotogramas como mejoras jugables.

Se reemplaza el antiguo feedback de trabajo retirado (que no emitía nada) por impactos **custom en el frente de excavación**, sólo mientras la cola ejecuta pasos exitosos. Los trece estilos tienen un patrón de trabajo y un remate al completar, utilizados cuando la habilidad ejecuta una cola (los campos sin cola no reproducen estos impactos); los remates cancelados no se celebran. El efecto usa el tipo real del trabajo, también al ejecutar una habilidad copiada con Rompemundos.

| Pico | Impacto de trabajo | Remate |
|---|---|---|
| Alba Eterna | corona de glifos dorados | corona ascendente amplia |
| Coro Sísmico | onda transversal | frente de onda expandido |
| Ocaso Gravitatorio | espiral decreciente | vórtice abierto |
| Crisol Alquímico | matriz de transmutación | sello cuadriculado amplio |
| Corona del Tiempo | anillo vertical segmentado | esfera ecuatorial ampliada |
| Telar del Mundo | brotes alternados | ramas abiertas |
| Ícaro | plumas laterales | alas desplegadas |
| Axioma del Vacío | arco irregular | fractura orbital |
| Rompemundos | estrella dentada | estallido radial |
| Exodio Estelar | cono descendente | cono amplio de caída |
| Iridio Rompevetas | arco de cristales | cúpula facetada |
| Flor Infernal | pétalos ascendentes | floración grande |
| Desgarraestratos | incisión dentada | corte transversal amplio |

Los impactos usan las secuencias de partículas propias ya existentes: **no se afirma que haya 26 sprites nuevos**. Cambian la ubicación, la geometría y el momento de emisión. Hasta ocho partículas cada diez ticks por trabajo activo; dieciséis al finalizar; comparten el mismo presupuesto global de 256 con los casts. Sólo recibe el minero, sin partículas vanilla, sin forzar visibilidad, sin cargar chunks y sin efectos ambientales de equipamiento/Curios. Alcance visual máximo 48 bloques. Una partícula vive menos de dos segundos. Pausar/reanudar no crea temporizadores de efectos.

## Texturas externas integradas sin modificaciones

- Alba Eterna: Malum `spellweaving_pickaxe.png`.
- Axioma del Vacío: Malum `soul_stained_steel_pickaxe.png`.
- Once picos conservan sus originales de Mythic Metals; no se venden como nuevos.
- Originales de Forbidden Arcanus descargados e inspeccionados, pero **no integrados**. No se añade un sprite sólo por aumentar el recuento.
- SHA256, commit fijo y ruta fuente en `ASSET-PROVENANCE.json`; nota de derechos en `MALUM_ASSET_NOTICE.md`. No se localizó licencia explícita para esos assets en el árbol fijado: destinados a la build privada solicitada, sin permiso público de redistribución supuesto.

## Validación y límites

Runtime `a527ffbfd83931c57af9266e0865ef3eca3e60d1`, [CI 44375291707](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44375291707): SUCCESS. 168 JUnit, cero fallos/errores/omisiones; 400 GameTests obligatorios; cliente gráfico (32 escenas/64 tooltips), servidor empaquetado y logs estrictos aprobados. Local: 130 pruebas Python sin fallos, una omitida por falta de Java local; 25 pruebas de recursos correctas.

Capturas reales sin retoques inspeccionadas: `review/portrait-palimpsest-5.15.0.jpg` y `review/portrait-hollow_axiom-5.15.0.jpg`, originales del export de evidencia del mismo run en `build/client-smoke/portrait-*.jpg`. Ambas muestran el sprite correcto en mano; tamaño 640×360. No prueban todas las escalas, ni la calidad de las 26 animaciones en movimiento, ni aprobación estética del usuario.

JAR `special-pickaxes-1.20.1-5.15.0.jar`, 782740 bytes, SHA256 `ce337b3b683d135c4860d8a8b18f8f27db0552b0781b9384cbc83068e1eda33e`. Artefacto personal autenticado `14462437214` (7 días), evidencia completa `14462112977`. Sin release pública.

**Pendiente de la petición global:** reforma de unas 50 mejoras notorias en todos los aspectos; esta tanda no la completa. No se han añadido nuevas mecánicas de excavación en 5.15.0. Los 26 patrones de trabajo/remate son variaciones audiovisuales, no 26 mecánicas distintas.

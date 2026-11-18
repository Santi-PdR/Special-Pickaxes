# 5.15 — impactos durante la excavación y arte externo

## Cambios implementados, validación pendiente

La petición sigue siendo una reforma de unas 50 mejoras notorias. No se presenta esta tanda como 50 mejoras completas, ni se cuentan pruebas/fotogramas como mejoras jugables.

Se reemplaza el antiguo feedback de trabajo retirado (que no emitía nada) por impactos **custom en el frente de excavación**, sólo mientras la cola ejecuta pasos exitosos. Los trece picos tienen un patrón de trabajo y un remate al completar; los remates cancelados no se celebran. El efecto usa el tipo real del trabajo, también al ejecutar una habilidad copiada con Rompemundos.

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

## Pendiente

Compilación Java, tests de geometría, GameTests, cliente real, servidor empaquetado y revisión gráfica. No se declara aprobación estética del usuario ni revisión de todas las animaciones. La última build completamente verificada al comenzar esta tanda es 5.14.1.

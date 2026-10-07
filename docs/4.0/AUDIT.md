# Auditoría individual 4.0

Base preservada: e88a08e (3.1). Se mantienen veinte objetos: añadir más diluiría
la revisión. Las texturas de los cuatro archivos autorizados fueron inspeccionadas
visualmente antes de elegirlas. ASSET-PROVENANCE.json registra origen y hash.
No se modifican esos archivos ni se añade una dependencia de sus mods.

## Decisiones por artefacto

Cada fila evalúa identidad, comprensión, control, correspondencia visual,
feedback, solapamiento, utilidad excepcional y simplificación.

| Artefacto | Identidad / utilidad y distinción | Control y simplificación | Lenguaje visual / firma |
|---|---|---|---|
| Palimpsest | Reparar tus propias huellas, no copiar estructuras ajenas | Apuntar y activar; fuera los contadores permanentes | Vitalite: crecimiento; fragmentos convergentes |
| Choir | Reutilizar tu partitura de excavación; no otra cantera | Activar y rotar con Modo | Echo: resonancia; hélice de pulsos sculk |
| Eventide | Excavar controlando trayectorias; no congelar actores | Activación directa; polaridad con Modo | Anillos schizo; implosión de partículas |
| Meridian | Dos frentes sincronizados, no retorno personal | Activar A/B; Secundaria desconecta | Ender; dos extremos enlazados |
| Crucible | Cambiar materia sin generar loot | Seleccionar, analizar automáticamente, Confirmar | Volcanite; fuego contenido |
| Interregnum | Suspender amenazas y sostener una regla minera local | Activar; Secundaria libera | Lightning; esfera de agujas detenidas |
| Worldloom | Refugio/puente predefinido, no editor arquitectónico | Material secundario y activar | Natural; crecimiento y arco |
| Icarus | Viajar perforando, a diferencia del corte estacionario | Mirar y activar; dirección con Modo | Elytra; alas y espiral axial |
| Axiom | Separar matriz y mineral, no interfaz A/B | Activar; nervaduras opcionales | Shadow; contracción oscura |
| Atlas | Permutar conservando materia, no copiar | Cuatro esquinas; transformación separada de Confirmar | Adamantium; dos estados alternos |
| Worldbreaker | Única pieza suprema: fractura anular + santuario central pagado | Convergencia directa por defecto; conserva programas regionales | Inferno Reaver; corona triple, mayor firma breve |
| Chronicle | Checkpoint deliberado, no memoria automática de minería | Registrar/restaurar; análisis automático | Stormstone; reloj de ecos |
| Keystone | Una bóveda orientada, no puente dinámico ni copia | Apuntar base y activar | Hammer; arco geométrico |
| Tessellator | Reproducir plano pagando, sin mover el original | Cuatro esquinas y Confirmar | Titanium; correspondencias geométricas |
| Aegis | Desviar disparos sin detener mobs | Activar; Modo elige trayectoria; Secundaria libera | Filo luminoso; escudo eléctrico |
| Lodestar | Regresar por pasos reales, sin excavación | Marcar / regresar; Secundaria olvida | Seismic; brújula de náutilos |
| Seam Ripper | Separar contacto A/B; rechaza A=B | Material B secundario; activar | Filo serrado rojizo; corte axial |
| Causeway | Apoyos reactivos al caminar, no puente fijo | Activar; Secundaria detiene | Longpickaxe; trazo de apoyo |
| Counterseal | Proteger terreno, no inmunidad de entidades | Activar; Secundaria detiene | Sculk; sello oscuro implosivo |
| Covenant | Sellar paso reciente con material elegido, no restaurar un estado histórico | Activar y avanzar; Secundaria detiene | Bone; fragmentos de cierre |

## Jerarquía

Especialistas: la mayoría de las reliquias. Dominio extremo: Interregnum, Atlas,
Chronicle y Tessellator. Pieza suprema única: Worldbreaker. Convergencia combina
sustracción anular que preserva el núcleo y construcción pagada, no solo una
cantera con más radio. Se respetan permisos, estados, materiales y el scheduler.

Las seis transformaciones de Atlas/Tessellator son ajustes geométricos, no seis
poderes distintos. Se conserva esta capacidad útil sin mostrar listas en el
hover compacto. Los modos persisten por jugador; nuevas partidas Worldbreaker
comienzan en Convergencia, partidas anteriores conservan su modo.

## Arquitectura y restricciones

RelicKeys solo envía intenciones; RelicNetwork exige dirección cliente→servidor,
versión compatible y enums acotados. RelicControl valida jugador/herramienta,
limita frecuencia y separa activar, secundaria, modo, selección, confirmar,
cancelar y pausar. Cancelar no queda bloqueado por cooldown de activación.

No HUD permanente ni progreso numérico recurrente. El hover normal tiene nombre
más una frase y la acción principal. SHIFT contiene secciones de juego y nombres de teclas reasignadas.
El análisis se inicia al marcar la última esquina; no hace falta otro clic ambiguo.

Efectos de firma: máximo 12 partículas por fase, 24 para la pieza suprema y
presupuesto agregado de 256 emisiones por tick. No entidades persistentes ni
shader/dependencia obligatoria. Se evita sacudir la cámara o fingir distorsión
mediante shaders no probados. Las firmas usan sonidos existentes distintos por
artefacto, con pitch y volumen por fase; no se afirma haber grabado audio nuevo.

Las referencias autorizadas aportan los sprites reales: esta versión no afirma
que hayan sido dibujados íntegramente por el proyecto. Modelos, asociaciones,
firmas y mecánicas son propios; los sprites se preservan para no degradar su pixel art.

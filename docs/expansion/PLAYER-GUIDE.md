> **Documento histórico de 3.0.** Los controles, tooltips y arte actuales están en [la guía 3.1](../3.1/PLAYER-GUIDE.md). El generador de sprites experimentales se retiró; su versión histórica permanece en Git.

# Guía de campo — Special Pickaxes 3.0.0

Minecraft 1.20.1 · Forge 47.3.0+ · Java 17. **14 herramientas combinadas: pico + hacha + pala.**
No recetas, minerales ni worldgen. Obtención administrativa: `/specialpickaxes grant Jugador ID`.

## Controles sin sorpresas

- **Herramientas regionales:** Usar arma la selección. Clic izquierdo elige las esquinas; no mina mientras seleccionas. Dos esquinas para un volumen; cuatro para Atlas/Teselador/TRANSPOSE. Usar inicia el **análisis sin modificar el mundo**. Cuando termina y aparece LISTO, usar confirma la ejecución.
- **Transformación:** agachado + clic izquierdo durante una selección doble alterna directa, espejo X, espejo Z, 90°, 180°, 270°. El cliente dibuja aristas continuas sin tapar los bloques. La línea conecta una esquina del origen con su correspondencia en destino. Para 90/270 se intercambian ancho y profundidad.
- **Modo:** sin selección ni trabajo, agachado + usar alterna el modo con su nombre. Durante una selección simple, agachado + clic izquierdo cambia modo y reinicia sus esquinas.
- **Cancelar:** agachado + usar cancela selección/trabajo. No borra automáticamente la memoria. Cambiar de herramienta, morir, salir o cambiar de dimensión cancela el trabajo.
- **Pausa:** durante un trabajo, usar pausa/reanuda. Si se descarga el próximo chunk, se pausa sin avanzar el cursor; vuelve a cargarlo y usa para continuar. No se fuerzan cargas.
- **Otros artefactos:** usar muestra preview; repetir sobre el mismo objetivo confirma. Meridian utiliza directamente sus dos anclas. Interregno se libera con agachado + usar cuando no existe selección pendiente.

## Recursos, escala y límites compartidos

- Construcción/restauración/copia: un bloque simple del inventario por celda colocada. No consume stacks nombrados/con NBT. No sobrescribe celdas ocupadas. En creativo se aplica la economía normal de creativo.
- Rephasing y permutación no producen drops ni XP propios. Solo se admiten estados por defecto de una paleta explícita de bloques estructurales vanilla: rocas, ladrillos, tablones, cristal, cuarzo y terracota. No inventarios, máquinas, fluidos, minerales ni bloques de seguridad.
- La minería usa el jugador real y Forge: herramientas, claims, Fortune, Silk Touch y desgaste. Las operaciones dejan intactos los bloques con entidades de bloque, fluidos y dureza negativa.
- **262.144 celdas por región**, 32 trabajos concurrentes, 192 candidatos globales/tick, base 24 por jugador y máximo encantado 128. Valores configurables. Alcance operativo 512 bloques, siempre cargados; timeout 30 minutos. La selección expira a los diez minutos sin trabajo activo.
- Análisis y ejecución recorren el volumen por ticks. Los estados analizados se comprueban otra vez: cambios posteriores se omiten. Al terminar se informa éxito y omisiones. Un intercambio es atómico por pareja; cancelar no deshace parejas ya terminadas.
- Las selecciones no pueden solaparse. Dimensiones incompatibles no se ejecutan. El volumen es inclusivo: ambos bloques de esquina pertenecen a él.
- La densidad de drops aplica backpressure: no se destruyen más bloques cuando hay 256 entidades de ítem a ocho bloques. Recoge los drops y repite los bloques omitidos.
- Los trabajos pausados solo se reanudan en la sesión actual, con la misma herramienta y antes del timeout. No se serializan ni reejecutan tras reinicio.

## Encantamientos altos

Efficiency, Fortune, Silk Touch, Unbreaking y Mending continúan funcionando. Las tags de pico, hacha y pala y ToolActions se comparten; no se inventan encantamientos nuevos.

Efficiency aumenta el presupuesto del scheduler mediante una curva logarítmica, sin superar el techo global. Se leen niveles NBT numéricos sin truncarlos a 255. Efficiency 100/255/1000 funciona; por encima del último cuadrado entero seguro (46.340), la velocidad se calcula en double y se satura a un valor finito. Unbreaking protege la suma `nivel + 1` ante el máximo entero.

Fortune es exacto hasta 4096 inclusive. Por encima utiliza crecimiento logarítmico documentado para evitar la expansión ilimitada de loot vanilla: `4096 + floor(128 × ln(nivel / 4096))`. **No se promete loot lineal ilimitado**. El nivel original no se borra del NBT. Mending repara con XP normalmente y reduce el coste de activación en Palimpsesto, Crónica y Teselador. La activación regional cuesta cuatro puntos por defecto y Unbreaking sigue aplicándose.

Para niveles que no caben en un short, utiliza NBT de tipo entero; un plugin que ya truncó un número antes de entregarlo al mod no se puede reconstruir. Ejemplo: `/give @s specialpickaxes:worldbreaker{Enchantments:[{id:"minecraft:efficiency",lvl:1000}]}`.

## Historia que sobrevive sesiones

Palimpsesto conserva hasta **4096 registros durante 24 horas reales**, ambos configurables (hasta siete días). Cada registro guarda posición, dimensión, material y fecha. Sobrevive guardado, reconexión, muerte y reinicio; no mezcla dimensiones al restaurar. El tooltip muestra cantidad, antigüedad y última huella. Se conservan memorias antiguas de 2.0 compatibles.

Crónica y WORLD RECORD toman un checkpoint explícito: confirmar un registro nuevo reemplaza el anterior. La captura está acotada por la misma capacidad; las celdas fuera de capacidad figuran como omitidas. WORLD RESTORE restaura ese checkpoint, no una copia ilimitada del mundo. Una cancelación durante captura conserva únicamente las celdas ya registradas.

## Palimpsesto del Último Alba — `palimpsest`

**Identidad:** Reparar tus huellas de minería, no crear bloques gratis.

**Modo principal:** Restauración de huecos recordados dentro de la selección.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Arma, selecciona, analiza y confirma LISTO.

**Cómo seleccionar:** Dos esquinas delimitan el volumen.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Esquinas y aristas de color, destino dorado, línea de correspondencia y progreso.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Repara las paredes de una cantera de ayer usando piedra real.

## Coro de las Fallas — `fault_choir`

**Identidad:** Convertir una excavación personal en una plantilla espacial reutilizable.

**Modo principal:** Repetición de la plantilla en el bloque apuntado; exige estado coincidente.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa para preview y repite para confirmar.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Repite una escalera tallada en otra pared con la misma roca.

## La Noche que Pesa — `eventide`

**Identidad:** Controlar geología y trayectorias hostiles dentro del mismo dominio.

**Modo principal:** Atracción y excavación geológica; modo alternativo de repulsión.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa para preview y repite para confirmar.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Excava un espacio de combate mientras desvías proyectiles.

## Meridiano de Dos Mundos — `meridian`

**Identidad:** Sincronizar trabajo entre un origen y un destino visibles.

**Modo principal:** Usar apunta A; usar después apunta B. Minar cerca de A repercute en B.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa sobre A y luego B; agachado + usar desconecta.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Trabaja dos frentes de cantera manteniendo su disposición relativa.

La orientación relativa al marcar A/B determina rotaciones de 90°. Los desplazamientos alrededor de A se rotan y trasladan a B; se exige el mismo estado de bloque. Distancia de influencia: ocho bloques alrededor de A. No transporta al jugador.

## Crisol de la Paradoja — `paradox_crucible`

**Identidad:** Cambiar familias estructurales sin procesamiento de minerales.

**Modo principal:** Piedra, pizarra, basalto u obsidiana.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa para preview y repite para confirmar.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Convierte una pared estructural en obsidiana sin generar ítems.

## Corona del Interregno — `interregnum`

**Identidad:** Imponer una regla local de estasis y minería multiplicada por ocho.

**Modo principal:** Estasis local; minar dentro prolonga el dominio.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa para preview y repite para confirmar.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Mantén enemigos inmóviles mientras extraes recursos dentro del dominio.

## Bastidor de la Primera Muralla — `worldloom`

**Identidad:** Levantar un refugio o puente de inmediato con tus materiales.

**Modo principal:** Refugio hueco con puerta o puente de tres bloques de ancho.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa para preview y repite para confirmar.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Cruza una grieta usando roca que ya llevas.

## Lágrima de Ícaro — `icarus`

**Identidad:** Viajar dentro de un corredor que se excava antes de atravesarlo.

**Modo principal:** Perforación frontal o inversa; movimiento condicionado a colisión libre.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa para preview y repite para confirmar.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Abre una ruta transitable sin atravesar una pared protegida.

## El Axioma Hueco — `hollow_axiom`

**Identidad:** Sustraer la matriz dejando los recursos y estructuras que interesan.

**Modo principal:** Conservar nervaduras o sustraer la matriz completa elegible.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Usa para preview y repite para confirmar.

**Cómo seleccionar:** Apunta al bloque objetivo; la preview no lo oculta.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Preview, partículas de identidad, modo nombrado y ActionBar; Meridian mantiene visible A ↔ B.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Deja una veta visible al retirar la roca circundante.

## Atlas de las Dos Orillas — `bifold_atlas`

**Identidad:** Permutar regiones completas compatibles, no cubitos de radio fijo.

**Modo principal:** Correspondencia directa, espejo X/Z, rotación 90/180/270.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Arma, selecciona, analiza y confirma LISTO.

**Cómo seleccionar:** Cuatro esquinas para origen y destino; deben ser compatibles.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Esquinas y aristas de color, destino dorado, línea de correspondencia y progreso.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Intercambia dos paredes 9×3×1, o una 9×3×1 por otra 1×3×9 rotada.

## El Rompemundos — `worldbreaker`

**Identidad:** Analizar y programar grandes volúmenes: el artefacto regional supremo.

**Modo principal:** BREAK, CARVE, REPHASE, TRANSPOSE, RESTORE y RECORD.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Arma, selecciona, analiza y confirma LISTO.

**Cómo seleccionar:** Dos esquinas; TRANSPOSE requiere cuatro.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Esquinas y aristas de color, destino dorado, línea de correspondencia y progreso.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Selecciona 50.000 celdas; analiza, revisa y confirma el programa.

CARVE conserva las esquinas del volumen y abre un túnel elíptico siguiendo Z. REPHASE toma la roca de la mano secundaria (piedra por defecto). RECORD captura la paleta segura; RESTORE solo rellena huecos registrados con inventario.

## Crónica de la Piedra Silente — `chronicle`

**Identidad:** Guardar un estado deliberado del terreno, aunque no lo hayas excavado tú.

**Modo principal:** Registrar estado / restaurar los huecos de ese estado.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Arma, selecciona, analiza y confirma LISTO.

**Cómo seleccionar:** Dos esquinas delimitan el volumen.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Esquinas y aristas de color, destino dorado, línea de correspondencia y progreso.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Guarda una fachada antes de que otros la desmonten; restaura sus huecos.

## Clave del Arco Imposible — `keystone`

**Identidad:** Construir arquitectura curva a partir de un volumen elegido, no un cubo sólido.

**Modo principal:** Bóveda parabólica / bóveda con nervios laterales de apoyo.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Arma, selecciona, analiza y confirma LISTO.

**Cómo seleccionar:** Dos esquinas delimitan el volumen.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Esquinas y aristas de color, destino dorado, línea de correspondencia y progreso.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Elige la luz y altura de un arco para construir una bóveda transitable.

## El Teselador — `tessellator`

**Identidad:** Reproducir un plano existente pagando su material; el original permanece.

**Modo principal:** Correspondencia directa, espejos y rotaciones para colocar una copia pagada.

**Modo secundario:** Alterna los modos indicados; los controles distinguen modo, selección y cancelación.

**Cómo activar:** Arma, selecciona, analiza y confirma LISTO.

**Cómo seleccionar:** Cuatro esquinas para origen y destino; deben ser compatibles.

**Cómo cancelar:** Agachado + usar; durante ejecución usar pausa/reanuda. Cambiar herramienta cancela.

**Feedback visual:** Esquinas y aristas de color, destino dorado, línea de correspondencia y progreso.

**Feedback sonoro:** Firma del artefacto al seleccionar/confirmar; señal grave de error, extinción al cancelar y cierre al terminar.

**Recursos:** Aplica la economía descrita arriba: materiales reales para colocar, conservación para intercambiar y loot Forge para minar.

**Encantamientos:** Efficiency acelera el trabajo acotado; Unbreaking reduce desgaste; Fortune/Silk Touch actúan donde se mina; Mending repara con XP.

**Límites:** Respeta límites y filtros compartidos. No fuerza chunks ni salta protecciones.

**Ejemplo:** Repite una fachada de piedra/ladrillo en una zona vacía usando materiales reales.

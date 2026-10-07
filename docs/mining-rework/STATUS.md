# Rework minero — trabajo en curso, no publicable

Este documento NO certifica una versión. La entrega 4.0 y sus resultados no
verifican los cambios actuales. No se ha compilado, ejecutado Minecraft ni
publicado un JAR de este rework.

## Cambios introducidos

- Registro y comando administrativo limitados a nueve artefactos jugables.
  Los enum antiguos siguen como compatibilidad interna pendiente de limpieza;
  no registran items. Modelos, texturas runtime y tags contienen solo nueve.
- Ocho sprites retirados preservados en `asset-library/`, con procedencia.
  No se reutilizan los de Chronicle, Causeway ni Tessellator. Ningún JAR de
  referencia modificado.
- Scheduler compartido con programas direccionales por secciones. CARVE,
  FRACTURE, CLEAVE, CORE DRILL, WORLD SHATTER e Icarus están conectados.
  La prevalidación y la extracción consumen presupuesto; una barrera aborta
  la ruta, no la salta. La congestión de drops pausa antes de consumir el paso.
- Bedrock y FluidState de nivel/estado bloquean minería, colocación e intercambio.
- Axiom busca matriz conectada a minerales existentes; no comunica sus posiciones.
  Activación por flanco de tecla y rechazo de secuencias de paquetes repetidas.
- Solo Crisol regional utiliza dos esquinas, distintas y con propietario exacto.
  Cambiar de modo cancela selección/trabajo incompatible. Confirmación separada.
- Crisol radial es esférico; Worldloom ofrece refugio, puente y pared; Coro tiene
  rotación y reflexión. Eventide ordena una cantera elipsoidal, sin campo de combate.
- Textos normal/SHIFT elegidos por modo actual en español e inglés.
- Ocho nuevos GameTests escritos (NO ejecutados): cinco barreras CARVE,
  esquina duplicada/cambio de modo, paquete Axiom repetido y catálogo.

## Comprobaciones realizadas en este estado

- `python3 tools/validate_resources.py`: 8 pruebas aprobadas.
- `git diff --check`: limpio.
- `python3 tools/scan_secrets.py`: 101 entradas rastreadas revisadas; no sustituye
  al análisis del JAR final ni de archivos nuevos una vez incluidos en Git.
- Java no disponible localmente. Fallaron la instalación APT (repositorios
  inaccesibles) y la consulta de descarga de Adoptium (error SSL).

## Trabajo obligatorio pendiente

1. Compilar Java 17/Forge y corregir lo que revele. No hay certificación de compilación.
2. Migrar los tests históricos: todavía esperan veinte items, modos viejos,
   esquinas idénticas y Convergence. Actualmente NO representan el contrato nuevo.
3. Añadir y ejecutar cobertura por cada geometría, fluidos modded/API, claims,
   drops/pausa/reanudación, Icarus y ciclo completo de selección/red/multijugador.
4. Revisar geología/UX, calidad de Eventide e Interregnum y efectos específicos.
   No se implementó evolución: no introducir contadores o desbloqueos nominales.
5. Migrar fixtures cliente, galería completa de los nueve y sus modos, textos de
   ayuda restantes, auditoría de compatibilidad y documentación del jugador.
6. Ajustar versión y herramientas de release (aún configuradas para 4.0), ejecutar
   Actions completa, servidor empaquetado, cliente real, auditoría final y JAR.
7. Revisar identidad de herramienta en intentos de red del mismo tipo y eliminar
   rutas/infraestructura obsoleta que ya no tengan utilidad.

No se accedió a test-1 ni se usó/modificó siege. Ninguna validación del modpack
completo se da por realizada. No hay commit/publicación nuevos de este trabajo.

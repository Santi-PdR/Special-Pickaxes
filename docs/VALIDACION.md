# Validación 3.1.0

La autoridad es el run de Forge verification del **Build-Commit del JAR**, no un
resultado histórico. `RELEASE.json`, manifiesto y `SHA256SUMS` deben coincidir.

## Gates automatizados

- Se mantienen los 36 GameTests de 3.0 y se añaden contratos de selección por modo,
  tooltip, migración sin energía, los seis poderes nuevos, permiso propio de
  Lodestar y herramientas triples/niveles 1000 en los veinte artefactos.
- Incluyen pagos reales, loot, Mending con XP, Fortune 1000, Efficiency extremo,
  claims/eventos, colisiones, máquinas, fluids, chunks ausentes y cancelación.
- Dos regiones de 50000 celdas verifican el presupuesto global y conservación de
  cursores; no constituye un benchmark completo de TPS del modpack.
- JUnit mantiene geometría, traversal, biyecciones y escalado numérico.
- Recursos verifican modelos vanilla existentes, idiomas, manuales, ausencia de
  energía activa, tags triples, ausencia de recetas/worldgen/sprites experimentales.
- Se compila/reobfusca y se arranca un servidor limpio con el JAR empaquetado.
- Cliente Minecraft/Forge real bajo Xvfb: galería de 20, cuarenta capturas de
  tooltips normales y SHIFT con comprobación de líneas, Efficiency 1000, modos,
  selección regional, efectos cosméticos y vista en tercera persona. SHIFT se
  pulsa físicamente mediante AWT; la fixture conduce el resto del estado de
  servidor y no pretende simular todas las pulsaciones de cada poder.
- Fixtures de prueba excluidas del JAR. Logs, whitespace, contenido/versión/commit
  del paquete y firmas comunes de credenciales se comprueban en CI. El escáner
  de firmas no es una auditoría exhaustiva de seguridad.

## Alcance honesto

No hay Java local confirmado ni `package.json`: la ejecución Java se realiza en
Actions con Java 17; `npm run check` no corresponde. `test-1` no está disponible
por una ruta/acceso confirmado: no se inventa ni se clona. No se usa/modifica siege.

Los mundos gráficos y de servidor son desechables. No certifican multiplayer
manual, todos los mods del pack, shaders, cada integración de claims, TPS sostenido
con muchas megaselecciones ni la estética bajo cualquier escala de GUI. Las
protecciones operan mediante las APIs comunes documentadas; un mod de claims
externo debe observar los eventos correspondientes.

Las cifras de pruebas y enlaces de la entrega deben tomarse del run final exitoso,
no inferirse de esta lista de cobertura. Las evidencias históricas 3.0 no validan 3.1.

# 5.18.0 — prueba jugable personal

Minecraft Java **1.20.1**, **Forge 47.4.10**, **Java 17**. No es Fabric ni NeoForge.

## Instalar y probar

1. Descarga el artefacto autenticado `special-pickaxes-5.18.0-personal` [de esta ejecución CI aprobada](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44416777028/artifacts/14480089792) (requiere iniciar sesión en GitHub; retención de 7 días). Extrae el ZIP.
2. Cierra Minecraft. Haz copia de tu mundo. Quita únicamente versiones anteriores `special-pickaxes-*.jar` de `mods` y coloca `special-pickaxes-1.20.1-5.18.0.jar`. No copies el ZIP a `mods`.
3. En SKLauncher, la carpeta indicada para esta instancia es `D:\Minecraft\SKLauncher\sklauncher-data-v4\instances\test-1\mods`.
4. Abre primero un mundo de prueba en creativo con trucos. Ejecuta `/specialpickaxes kit @s` con **13 huecos libres** en el inventario principal. En servidor, requiere operador nivel 2; también puede usarse desde consola con el nombre del jugador. Si falta espacio, no entrega ni tira ningún pico.
5. Consulta Opciones → Controles → Special Pickaxes para ver y resolver conflictos de teclas. Las habilidades pueden modificar grandes volúmenes: no pruebes junto a construcciones valiosas.
6. Usa el manual integrado para las funciones de cada pico. No hay recetas: el comando de concesión individual y la pestaña creativa siguen disponibles.

## Cambios de esta versión

- Conjunto completo de las 13 identidades con concesión atómica por espacio disponible; sin crear herramientas duplicadas en el suelo cuando falta inventario.
- HUD de trabajo indica pausa manual, zona sin cargar o saturación de botín; muestra la tecla configurada para intentar continuar. Reanudar sigue validando la causa: no salta protecciones ni carga chunks.
- HUD se coloca por encima de la barra rápida al aumentar la escala de GUI; la guía de mena usa lectura compacta en ventanas estrechas. En ventanas extremadamente pequeñas se oculta para evitar dibujar fuera de pantalla.
- Lectura de cooldown valida el intervalo original frente a NBT inválido, rollback y desbordamiento, en vez de mostrar esperas imposibles.

Se mantienen IDs, datos previos y las 13 habilidades. No añade partículas ambientales permanentes ni cambia el permiso de redistribución del arte externo.

## Verificación

[CI 44416777028](https://github.com/Santi-PdR/Special-Pickaxes/actions/runs/44416777028), fuente `5c10ccb452afbeb667cd4c2e4ef8e4c65eed5282`: **SUCCESS**. 178 JUnit sin fallos/errores/omisiones; 420 GameTests obligatorios; cliente gráfico 32 escenas/64 tooltips; servidor con JAR empaquetado y validación estricta de logs aprobados. Local: 132 Python (una omisión sin Java), 25 recursos y escaneo de secretos correctos.

JAR: **804691 bytes**, SHA256 `c47921400e6865fcdfa86c96b958bd8e07b6d14bb78a5eec191570554e5ee54c`. Artefacto personal `14480089792`; evidencia `14480139804`. El CDN de descarga no es accesible desde este entorno, por lo que se entrega el enlace autenticado, no una supuesta copia local del JAR.

El primer run `44415426565` falló en la prueba existente `icarianLiftStopsAfterDepartureBetweenBudgetedTurns`. Se añadieron precondiciones y diagnóstico sin relajar sus aserciones ni cambiar el ascensor. La ejecución completa siguiente pasó; la causa del fallo intermitente no está confirmada.

 Las pruebas de disposición verifican límites geométricos, no aprobación visual humana en todas las escalas. Windows/SKLauncher y compatibilidad con otros mods no se han ejecutado aquí.

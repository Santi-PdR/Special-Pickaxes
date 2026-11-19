# Alba / Dawnbreaker — alpha.3 validada

Se conserva todo el arte existente. No se cambian las habilidades dedicadas de Iridium ni Strata.

## Identidad implementada

- Primaria **Ruptura solar**: ocho brazos de roca natural, 24 bloques de longitud por eje y 5 de alto, 2765 celdas únicas. Se ejecutan desde el centro hacia afuera; no es una búsqueda de minerales ni una veta conectada. Los brazos diagonales llegan hasta aproximadamente 34 bloques del centro.
- Secundaria **Nova solar**: esfera de radio 12 alrededor del bloque natural apuntado, 7153 celdas candidatas. La secundaria repetida cancela la cola.
- Se conservan drops nativos y experiencia mediante la ruta segura de minería. Los drops producidos por Alba brillan; también los de su copia en mano de Worldbreaker.
- Variante Curios: brilla el botín recién extraído con otra herramienta; no revela minerales, no atrae objetos y no ilumina objetos anteriores sin cambios. Se confirma la cicatriz al final del tick y se exige la misma instancia en el slot original. Conserva curación y velocidad; la defensa cada ocho segundos ahora bloquea el 65% de un golpe hostil, seguida de cuatro pulsos de reserva alimentaria.
- La copia en mano de Worldbreaker utiliza ambos programas solares. La copia global de variantes Curios sigue pendiente en la reforma general.
- Ondas de partículas solares propias ya existentes, sobre el frente de excavación, sólo durante la ejecución; sin tocar PNG/modelos ni agregar partículas ambientales por equipar.
- Guía inglesa, española y uruguaya actualizada. Modo único `solar_rupture`; los índices de modo guardados se conservan.

## Seguridad y límites

Preserva procedencia de bloques colocados, inventarios, fluidos, chunks descargados, permisos/claims y requisitos de herramienta. Revalida la activación después de callbacks y bloquea la reentrada desde esos callbacks. Cada inspección consume presupuesto del scheduler compartido; el límite configurado de trabajo puede truncar las geometrías. 2765/7153 son candidatos máximos por geometría, no promesa de drops ni de bloques rotos.

El Glowing de minería ordinaria usa snapshots acotados (256 entidades inspeccionadas); ante saturación se omite conservadoramente. No se cambia `capturesIridiumDrops` ni se asigna propiedad Iridium a drops Alba.

## Sistema anterior retirado

Eliminados SurveyGuide, SurveyBearing y SurveyVolume, sus búsquedas y decodificación. Los cinco campos `surveyGuide*` de herramientas antiguas se borran sin leer coordenadas ni consultar minerales. Se sustituyen las pruebas de la mecánica retirada por pruebas solares; la reducción del número de pruebas antiguas no debe presentarse como cobertura adicional.

## Estado de comprobación

Python: 138 pruebas, una omitida sin Java local; recursos: 25 correctas. Runtime final `d48cc5364253a6daee0a74678a92fe9a326c7996`, CI `44450388632` SUCCESS: **173 JUnit** sin fallos/errores/omisiones, **437 GameTests**, servidor empaquetado y cliente real **32 escenas / 64 tooltips** aprobados. El cliente activó `palimpsest-solar_rupture` mediante el control real. Los 58 hashes congelados coinciden. No se inspeccionó una nueva captura de acción solar: no afirmar aprobación estética completa ni compatibilidad con todos los modpacks. No publicar esta alpha como reforma completa.

JAR alpha.3: 821992 bytes, SHA256 `847869b2576a5819ae9686c450da8d227709d4356e4d2efaf83a244008e70d02`. Evidencia descargada en `/tmp/sp603final`. No release 6.0 pública. El recuento JUnit pasa de 177 a 173 porque se retiraron siete pruebas exclusivas del radar y se agregaron tres de geometría solar. Se conserva la cobertura general y se sustituyen GameTests retirados; no ocultar la variación del recuento.

Primera CI `44447488250`: compilación/JUnit aprobados, tres fallos GameTest. Dos expectativas no destructivas del sondeo retirado se sustituyeron por excavación de nova conservando las aserciones de criaturas/proyectiles/cooldown. Segunda CI `44448372209`: queda un fallo de fixture de rayos; diagnóstico: centro roto, 265 éxitos y pausa `drops` antes de alcanzar la mena. Roca residual de fixtures elevados fuera del volumen de limpieza de templates saturaba legítimamente el límite de 256 entidades. Se aísla el núcleo del fixture; **no se modifica ni relaja la contrapresión de drops**. Revalidación pendiente.

CI `44449149211`: **173 JUnit sin fallos/errores/omisiones, 437 GameTests y servidor empaquetado aprobados**. El cliente encontró un desfase del catálogo Python (`vein`) frente al modo Java nuevo (`solar_rupture`) y abortó correctamente ante escena desconocida. Se actualiza el catálogo canónico; suite gráfica completa pendiente de la nueva CI.

La sincronización inicial del catálogo omitió la etiqueta del modo; los tests locales la detectaron y se añadió en los tres idiomas antes del runtime final aprobado.

# Traductor de C# a Python/JavaScript — Backend

## ⚠️ Aviso importante
Este entorno donde generé el código **no tiene JDK completo instalado** (solo el
runtime de Java, sin `javac`) **ni JavaCC**, y no tengo acceso a internet para
instalarlos. Por eso:
- El archivo `Traductor.jj` **no pude compilarlo ni probarlo con javacc.jar aquí**.
- Sí pude probar la lógica de los generadores (`GeneradorPython.java`,
  `GeneradorJavaScript.java`, `Nodo.java`) de forma manual, construyendo un AST
  de ejemplo a mano en `PruebaGeneradores.java` y revisando línea por línea que
  los índices de hijos del árbol coincidan con el orden en que la gramática
  los agrega (`n.agregar(...)`).
- **Tú sí tienes JavaCC funcionando** (ya lo usaste para `Analizador.jj`), así
  que el primer paso es compilar `Traductor.jj` en tu máquina y corregir lo
  que truene. Es muy probable que haya algún detalle de sintaxis JavaCC que
  necesite ajuste — avísame el error exacto y te ayudo a corregirlo.

## Qué se hizo respecto a tu `Analizador.jj` original
- Mismos tokens, mismas reglas gramaticales, mismo `mostrarToken()` /
  `regla()` (la salida de consola con el análisis léxico se conserva igual).
- Cada regla (`declaracionVariable()`, `condicion()`, `expresion()`, etc.)
  pasó de ser `void` a regresar un `Nodo` (clase nueva, ver `Nodo.java`), y
  arma el árbol con `n.agregar(hijo)` / `n.atributo(clave, valor)`.
- Ese árbol completo (`programa`) se le pasa a `GeneradorPython` o
  `GeneradorJavaScript`, que lo recorren y arman el código de salida.

## Archivos reconstruidos (no me los pasaste)
`Diccionario.java`, `PalabraInfo.java` y `Semantico.java` **no estaban en lo
que subiste** — Analizador.jj los usa pero nunca vi su contenido real. Los
reconstruí a partir de cómo se llaman en el `.jj` (mismos nombres de método:
`cargarCSV`, `mapa`, `declararVariable`, `validarOperacionAritmetica`,
`hayErrores`, `imprimirErrores`, `limpiar`, `error`). **Si ya tienes tus
versiones originales, reemplaza estos tres archivos por los tuyos** — es más
seguro que usar mi reconstrucción, sobre todo si tu `Semantico.java` original
tiene más validaciones de las que yo adiviné.

## Cómo compilar (con JavaCC ya instalado)
```bash
# 1. Generar el parser Java a partir de la gramática
javacc Traductor.jj

# 2. Compilar todo (el .jj genera varios .java: Traductor.java, TraductorTokenManager.java, etc.)
javac *.java

# 3. Ejecutar
java Traductor
# te va a pedir: nombre del archivo .cs, y el lenguaje destino (python/javascript)
```

Prueba con el archivo `ejemplo.cs` incluido.

## Qué SÍ cubre ahora mismo el generador
Declaración de variables, asignación, `if/else`, `while`, `for` (estilo C,
convertido a `while` en Python), `do-while`, `foreach`, `switch/case/default`,
`print`/`println`, `break`/`continue`/`return`, bloques, expresiones
aritméticas y lógicas de UN solo operador (ej. `a + b`, `a < b`) — igual que
tu gramática original, que tampoco encadena varios operadores seguidos
(`a + b + c` no es válido todavía, ni en el analizador original ni en este).

## Limitaciones conocidas / próximos pasos (para que corrijas o yo te ayude)
1. **Expresiones con más de un operador** (`a + b * c`, `a < b && c > d`):
   la gramática actual de Analizador.jj tampoco las soporta — habría que
   extender `expresion()` y `expresionLogica()` a algo recursivo/con
   precedencia antes de que el traductor las pueda generar.
2. **`print` vs `println`**: ahora mismo generan lo mismo (`print(...)` /
   `console.log(...)`). Si quieres diferenciarlos de verdad en la salida
   (por ejemplo, sin salto de línea), hay que usar el atributo `variante`
   que ya guarda el nodo `escritura`.
3. **Clases, métodos y arreglos**: tu gramática original no los reconoce
   todavía (no hay tokens `class`, `[]` con contenido, etc. en las reglas de
   instrucción), así que tampoco los traduce este generador. Es el siguiente
   bloque grande a agregar si tu proyecto lo requiere.
4. Revisa que `Diccionario.java`/`PalabraInfo.java`/`Semantico.java`
   reconstruidos coincidan con lo que espera tu equipo (o sustitúyelos).

## Estructura de archivos
```
backend/src/
├── Traductor.jj              (gramática + AST — NUEVO, revisar/compilar)
├── Nodo.java                 (clase de nodo del AST — NUEVO)
├── GeneradorPython.java      (AST -> Python — NUEVO)
├── GeneradorJavaScript.java  (AST -> JavaScript — NUEVO)
├── PruebaGeneradores.java    (prueba manual sin JavaCC — NUEVO)
├── Diccionario.java          (RECONSTRUIDO — reemplaza por tu original si lo tienes)
├── PalabraInfo.java          (RECONSTRUIDO — reemplaza por tu original si lo tienes)
├── Semantico.java            (RECONSTRUIDO — reemplaza por tu original si lo tienes)
├── palabras.csv              (tu archivo, copiado tal cual)
└── ejemplo.cs                (archivo de prueba)
```

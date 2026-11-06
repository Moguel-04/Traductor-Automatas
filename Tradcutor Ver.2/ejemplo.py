# Funciones auxiliares para conservar el comportamiento de C#.

def _cs_parse_integer(valor, minimo, maximo):
    texto = str(valor).strip()
    digitos = texto[1:] if texto[:1] in ("+", "-") else texto

    if not digitos or not digitos.isascii() or not digitos.isdigit():
        raise ValueError("Formato de entero no valido")

    resultado = int(texto, 10)

    if resultado < minimo or resultado > maximo:
        raise OverflowError("Valor fuera del rango permitido")

    return resultado


def _cs_parse_float(valor):
    return float(str(valor).strip())


def _cs_parse_bool(valor):
    texto = str(valor).strip().lower()

    if texto == "true":
        return True

    if texto == "false":
        return False

    raise ValueError("La cadena no representa un valor booleano valido")


def _cs_parse_char(valor):
    texto = str(valor)

    if len(texto) != 1:
        raise ValueError("Se esperaba exactamente un caracter")

    return texto


def _cs_convert_integer(valor, minimo, maximo):
    if valor is None:
        resultado = 0
    elif isinstance(valor, bool):
        resultado = int(valor)
    elif isinstance(valor, str):
        return _cs_parse_integer(valor, minimo, maximo)
    else:
        resultado = int(round(valor))

    if resultado < minimo or resultado > maximo:
        raise OverflowError("Valor fuera del rango permitido")

    return resultado


def _cs_convert_char_to_integer(valor, minimo, maximo):
    if not isinstance(valor, str) or len(valor) != 1:
        raise TypeError("Tipo no valido para Convert.ToInt32")

    resultado = ord(valor)

    if resultado < minimo or resultado > maximo:
        raise OverflowError("Valor fuera del rango permitido")

    return resultado


def _cs_convert_float(valor):
    if valor is None:
        return 0.0

    return float(valor)


def _cs_convert_single(valor):
    import struct

    resultado = _cs_convert_float(valor)

    try:
        return struct.unpack("f", struct.pack("f", resultado))[0]
    except OverflowError:
        raise OverflowError("Valor fuera del rango de Single")


def _cs_convert_bool(valor):
    if valor is None:
        return False

    if isinstance(valor, str):
        return _cs_parse_bool(valor)

    return bool(valor)


def _cs_convert_char(valor):
    if valor is None:
        return "\0"

    if isinstance(valor, str):
        if len(valor) != 1:
            raise ValueError("Se esperaba exactamente un caracter")

        return valor

    if isinstance(valor, bool) or not isinstance(valor, int):
        raise TypeError("Tipo no valido para Convert.ToChar")

    if valor < 0 or valor > 65535:
        raise OverflowError("Valor fuera del rango de Char")

    return chr(valor)


def _cs_convert_string(valor):
    if valor is None:
        return ""

    return str(valor)


# Division entera compatible con C#.

def _cs_integer_division(a, b):
    if b == 0:
        raise ZeroDivisionError("Division entre cero")

    resultado = abs(a) // abs(b)

    if (a < 0) != (b < 0):
        return -resultado

    return resultado


# Modulo entero compatible con C#.

def _cs_integer_modulo(a, b):
    if b == 0:
        raise ZeroDivisionError("Modulo entre cero")

    cociente = abs(a) // abs(b)

    if (a < 0) != (b < 0):
        cociente = -cociente

    return a - cociente * b


# Variables
edad = 25
matematica = 85
ciencia = 90
historia = 78
x = 5
y = 10
z = 15
nombre = "Carlos"
apellido = "López"
ciudad = "Madrid"
pais = "España"
activo = True
puntuacion = 100
# Arreglos
edades = [25, 30, 35, 40]
frutas = ["manzana", "naranja", "plátano"]
flags = [True, False, True]
numeros = [1, 2, 3, 4, 5]
# Funciones
def Despedir():
    print("Adiós, mundo")

def Sumar(a, b):
    resultado = a + b
    return resultado

respuesta = Sumar(10, 5)
print(respuesta)
# WHILE principal
contador = 0
while contador < 10:
    if _cs_integer_modulo(contador, 2) == 0:
        print("Número par: " + str(contador))
        if contador == 0:
            print("Iniciando conteo")
        elif contador == 2:
            print("Número par medio: " + str(contador))
            if activo == True:
                print("Sistema activo en caso 2")
                procesados = 0
                while procesados < 3:
                    print("Procesando: " + str(procesados))
                    procesados += 1
        elif contador == 4:
            print("Número par medio: " + str(contador))
            try:
                calculo = _cs_integer_division(contador, (contador - 2))
                print("Cálculo exitoso: " + str(calculo))
            except ArithmeticError:
                print("Error en cálculo para: " + str(contador))
        elif contador == 6:
            print("Número par medio: " + str(contador))
            for i in range(0, 2, 1):
                print("For interno en caso 6")
        elif contador == 8:
            print("Número par medio: " + str(contador))
            # Longitud del texto
            print(len(("texto prueba")))
            # Ordenar arreglo
            numeros.sort()
        else:
            print("Otro número par: " + str(contador))
    else:
        if contador == 1:
            print("Número impar: " + str(contador))
            if edad >= 18:
                print("Usuario adulto verificando...")
                if puntuacion == 100:
                    print("¡Puntuación perfecta!")
                    intentos = 2
                    while intentos > 0:
                        print("Intentos: " + str(intentos))
                        if intentos == 1:
                            print("¡Último intento!")
                        intentos -= 1
                elif puntuacion == 50 or puntuacion == 75:
                    print("Puntuación media")
                    for j in range(0, 2, 1):
                        print("Optimizando...")
                else:
                    print("Puntuación: " + str(puntuacion))
                    try:
                        datos = ["a", "b"]
                        for item in datos:
                            print("Dato: " + str(item))
                    except Exception as error:
                        print("Error datos: " + str((error.args[0] if error.args else "")))
            else:
                print("Usuario menor de edad")
                if not (edad >= 16):
                    print("Acceso restringido")
        elif contador == 3:
            print("Número impar: " + str(contador))
            for num in numeros:
                if num > 2:
                    print("Número > 2: " + str(num))
                    if num == 3:
                        calculoo = (num * 10) + (_cs_integer_division(edad, 5))
                        print("Cálculo 3: " + str(calculoo))
                    elif num == 4:
                        print("Cuadrado perfecto")
                    elif num == 5:
                        multi = 1
                        while multi <= 2:
                            print(str(num) + " x " + str(multi) + " = " + str((num * multi)))
                            multi += 1
        elif contador == 5:
            print("Número impar: " + str(contador))
            repeticiones = 0
            while True:
                print("Do-while ejecución: " + str(repeticiones) + " en contador: " + str(contador))
                repeticiones += 1
                if not (repeticiones < 3):
                    break
        elif contador == 7:
            print("Número impar: " + str(contador))
        elif contador == 9:
            print("Número impar: " + str(contador))
            try:
                print("Proceso crítico final...")
                if activo:
                    try:
                        division = _cs_integer_division(100, (edad - 25))
                        print("División: " + str(division))
                    except ArithmeticError:
                        print("División fallida")
                if puntuacion < 0:
                    raise InvalidOperationException("Puntuación inválida")
            except Exception as errorCritico:
                print("Error: " + str((errorCritico.args[0] if errorCritico.args else "")))
                if (errorCritico.args[0] if errorCritico.args else "") == "Puntuación inválida":
                    puntuacion = 0
                    print("Puntuación reiniciada")
                else:
                    print("Error no manejado")
            finally:
                print("Proceso finalizado")
    contador += 1
# Finalización del programa
print("Finalizando programa...")
if puntuacion > 50:
    print("Resultado satisfactorio")
    for k in range(0, 2, 1):
        try:
            finalScore = puntuacion * 2
            print("Puntuación final: " + str(finalScore))
            if not (edad >= 18 and activo):
                print("Acceso denegado final")
        except Exception:
            print("Error cálculo final")
print("=== RESUMEN FINAL ===")
if puntuacion == 0:
    print("Puntuación reiniciada: " + str(puntuacion))
elif puntuacion == 50:
    numero = _cs_convert_integer(input(), -2147483648, 2147483647)
    print(numero + 1)
    decimal = _cs_parse_float(input())
    print(decimal)
    entero = _cs_convert_integer(input(), -2147483648, 2147483647)
    precio = _cs_convert_float(input())
    print(entero)
    print(precio)
    dato = "25"
    num = _cs_convert_integer(dato, -2147483648, 2147483647)
    print(num + 5)
    prueba = _cs_convert_integer(input(), -2147483648, 2147483647)
elif puntuacion == 75:
    print("Puntuación media final: " + str(puntuacion))
elif puntuacion == 100:
    print("¡Puntuación perfecta final!")
else:
    print("Nombre: " + nombre + " " + apellido)
    print("Edad: " + str(edad))
    print("Puntuación: " + str(puntuacion))
    print("Estado: " + str(activo))
    print("Ciudad: " + ciudad)
print("Programa ejecutado exitosamente")
Despedir()

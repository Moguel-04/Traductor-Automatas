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


# Pruebas
numeros = [5, 10, 15, 20]
def sumarElementos(datos):
    suma = 0
    for numero in datos:
        suma += numero
    return suma

def calcularPromedio(datos):
    suma = sumarElementos(datos)
    promedio = _cs_convert_float(suma) / len(datos)
    return promedio

mensaje = None
nombre = "Kain"
limite = _cs_convert_integer("10", -2147483648, 2147483647)
promedio = calcularPromedio(numeros)
activo = _cs_parse_bool("true")
inicial = _cs_convert_char(65)
codigo = _cs_convert_char_to_integer(inicial, -2147483648, 2147483647)
sumaTotal = sumarElementos(numeros)
acumulado = 0
residuo = _cs_integer_modulo(17, 5)
for i in range(0, len(numeros), 1):
    if numeros[i] == 10:
        continue
    acumulado += numeros[i]
while acumulado < 45:
    acumulado += 1
    if acumulado == 42:
        continue
    if acumulado == 44:
        break
while True:
    acumulado -= 1
    if not (acumulado > 42):
        break
opcion = 2
if opcion == 1:
    acumulado += 1
elif opcion == 2:
    acumulado += 3
else:
    acumulado = 0
if mensaje == None:
    mensaje = "Resumen"
try:
    if promedio > limite and activo:
        print("El promedio supera el limite")
    else:
        print("El promedio no supera el limite")
    print(mensaje)
    print(nombre)
    print(promedio)
    print(sumaTotal)
    print(acumulado)
    print(residuo)
    print(inicial)
    print(codigo)
    divisor = 0
    resultado = _cs_integer_division(sumaTotal, divisor)
    print(resultado)
except Exception:
    print("Se capturo una excepcion")
finally:
    print("Proceso finalizado")
print(activo)

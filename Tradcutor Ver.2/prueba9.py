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


numeros = [10, 20, 30, 40]
def calcularSuma(valores):
    suma = 0
    for valor in valores:
        suma += valor
    return suma

def calcularPromedio(valores):
    suma = calcularSuma(valores)
    promedio = _cs_convert_float(suma) / len(valores)
    return promedio

total = 0
promedio = 0.0
try:
    for i in range(0, 4, 1):
        if numeros[i] == 30:
            continue
        total += numeros[i]
    promedio = calcularPromedio(numeros)
    print("Promedio calculado:")
    print(promedio)
    divisor = 0
    resultado = _cs_integer_division(total, divisor)
    print("Division realizada:")
    print(resultado)
except Exception:
    print("Se produjo un error durante el calculo.")
    total = -1
finally:
    print("Proceso finalizado.")
print("Total final:")
print(total)

# Division entera compatible con C#.

def _cs_integer_division(a, b):
    if b == 0:
        raise ZeroDivisionError("Division entre cero")

    resultado = abs(a) // abs(b)

    if (a < 0) != (b < 0):
        return -resultado

    return resultado


resultado = 0
try:
    numero = 10
    divisor = 0
    resultado = _cs_integer_division(numero, divisor)
    print("Division realizada")
except Exception:
    print("Se produjo un error")
    resultado = -1
finally:
    print("Finalizo el bloque")
print(resultado)

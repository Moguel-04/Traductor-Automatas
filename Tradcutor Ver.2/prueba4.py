def sumar(a, b):
    return a + b

def calcularTotal(cantidad, precio):
    total = 0
    for i in range(0, cantidad, 1):
        total += precio
    return total

resultado1 = sumar(10, 20)
resultado2 = calcularTotal(4, 5)
if resultado1 > resultado2:
    print("La primera suma es mayor")
else:
    print("El total calculado es mayor o igual")
print(resultado1)
print(resultado2)

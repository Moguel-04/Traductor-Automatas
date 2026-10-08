contador = 0
limite = 5
mensaje = "Iniciando"
activo = True
promedio = 10.5
print(mensaje)
while contador < limite:
    print(contador)
    if contador == 2:
        print("Llegamos al punto medio")
    else:
        print("Continuamos")
    contador = contador + 1
while True:
    print(contador)
    contador = contador - 1
    if not (contador > 0):
        break
i = 0
while i < 3:
    print(i)
    i = i + 1
if contador == 0:
    print("El contador termino en cero")
    break
elif contador == 1:
    print("El contador termino en uno")
    break
else:
    print("El contador tiene otro valor")
    break
for numero in numeros:
    print(numero)
return

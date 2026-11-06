numeros = [3, 6, 9, 12]
suma = 0
encontrados = 0
for i in range(0, len(numeros), 1):
    if numeros[i] < 6:
        continue
    if numeros[i] > 10:
        break
    suma += numeros[i]
    encontrados += 1
for numero in numeros:
    if numero == 9:
        print("Encontrado")
print(suma)
print(encontrados)

opcion = 2
resultado = 0
if opcion == 1:
    resultado = 10
    print("Opcion uno")
elif opcion == 2:
    resultado = 20
    print("Opcion dos")
elif opcion == 3:
    resultado = 30
    print("Opcion tres")
else:
    resultado = -1
    print("Opcion no valida")
resultado += 5
if resultado > 20:
    print("Resultado mayor que veinte")
print(resultado)
otraOpcion = 8
if otraOpcion == 1:
    print("Primera opcion")
elif otraOpcion == 2:
    print("Segunda opcion")
else:
    print("Se ejecuto default")

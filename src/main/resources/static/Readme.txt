1. Tener docker instalado.
2. Ejecutar en el directorio principal lo siguiente (el punto incluido):
    docker build -t umatravel .
3. Esperar a que se cree la imagen.
4. Ejecutar para comprobar que se ha creado:
    docker images 
5. Crear el contenedor con:
    docker run -p 5500:80 -d --name execumatravel umatravel

6. Ir a: http://localhost:5500/
Nota: Podeis cambiar el puerto 5500 por otro siempre y cuando no entre en conflicto con el puerto del backend.

Adicional:
-Se puede parar el contenedor con:
    docker stop execumatravel
-Se puede ver la lista de contenedores con
    docker ps -a
-Se puede eliminar el contenedor con:
    docker rm execumatravel
-Se puede eliminar la imagen con
    docker rmi umatravel

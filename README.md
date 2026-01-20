# TP Docker – RentalService
## Kevin CHAILLOT

---

## 1. Installation
La version Java JDK 21 était déjà installée sur ma machine.

---

## 2. Tester le programme sans Docker

### Builder le projet

```bash
./gradlew build
```

### Tester le projet

```bash
java -jar build/libs/RentalService-0.0.1-SNAPSHOT.jar
```

---

## 3. Création du fichier Dockerfile

Créer le fichier `Dockerfile` dans le dossier `RentalService`.

```dockerfile
FROM eclipse-temurin:21

VOLUME /tmp

EXPOSE 8080

ADD ./build/libs/RentalService-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

---

## 4. Création de l’image Docker

Lancer la création de l’image Docker :

```bash
docker build -t rentalservice .
```

---

## 5. Tester le programme avec Docker

```bash
docker run -p 8080:8080 rentalservice
```

Vérifier dans votre navigateur à l’adresse :

```
http://localhost:8080/bonjour
```

---

## 6. Publier l’image dans Docker Hub

Se connecter au Docker Hub :

```bash
docker login
```

Faire un tag de l’image :

```bash
docker tag rentalservice kevmdf/rentalservice:latest
```

Publier l’image dans Docker Hub :

```bash
docker push kevmdf/rentalservice:latest
```

---

## 7. TP2 – Microservices avec Docker Compose

### Architecture du projet

- `RentalService` : microservice Java (Spring Boot)
- `MicroservicePHP` : microservice PHP
- Communication HTTP entre les deux services
- Orchestration via Docker Compose

---

### Lancement des microservices

À la racine du projet :

```bash
docker-compose up
```

---

### Test de fonctionnement

Ouvrir un navigateur à l’adresse suivante :

```
http://localhost:8080/customer/Jean%20Dupont
```

---

### Résultat attendu

```
Kevin Jean Dupont
```

---

### Commandes utiles

```bash
cd RentalService
./gradlew clean build
docker build -t kevmdf/rentalservice .
cd ..
docker-compose down
docker-compose up
```

---

## Conclusion

Ce projet met en œuvre une architecture microservices composée d’un service Java et d’un service PHP communiquant via HTTP.  
Les services sont conteneurisés avec Docker et orchestrés à l’aide de Docker Compose.

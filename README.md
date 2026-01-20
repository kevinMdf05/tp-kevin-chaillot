# TP Docker, Docker Compose & Kubernetes – Microservices

## Kevin CHAILLOT

---

## Sommaire

1. [Présentation du projet](#présentation-du-projet)
2. [TP1 – Service Java (RentalService)](#tp1--service-java-rentalservice)
3. [TP2 – Service PHP (microservice-php-kevin)](#tp2--service-php-microservice-php-kevin)
4. [TP2 bis – Docker Compose](#tp2-bis--docker-compose)
5. [TP3 – Kubernetes avec Minikube](#tp3--kubernetes-avec-minikube)
6. [Liens Docker Hub](#liens-docker-hub)

---

## Présentation du projet

Ce projet implémente une architecture microservices avec deux services qui communiquent via HTTP :

| Service | Technologie | Port | Rôle |
|---------|-------------|------|------|
| **RentalService** | Java 21 / Spring Boot 3.2.1 | 8080 | Service principal appelant le service PHP |
| **microservice-php-kevin** | PHP 8.2 / Apache | 80 | Retourne le prénom "Kevin" |

L'objectif est de déployer ces services de trois manières différentes :
- **Docker** : conteneurisation individuelle
- **Docker Compose** : orchestration locale
- **Kubernetes** : déploiement sur cluster Minikube

---

## TP1 – Service Java (RentalService)

### Prérequis

Java JDK 21 doit être installé. Vérification :

```powershell
java -version
```

Installation si nécessaire :
```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

### Exécution sans Docker

Se placer dans le dossier du service Java :

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
```

Compiler le projet :

```powershell
./gradlew build
```

Lancer l'application :

```powershell
java -jar build/libs/RentalService-0.0.1-SNAPSHOT.jar
```

Test dans le navigateur : http://localhost:8080/bonjour

Pour arrêter : `Ctrl + C`

### Exécution avec Docker

Contenu du fichier `Dockerfile` dans le dossier `RentalService` :

```dockerfile
FROM eclipse-temurin:21
VOLUME /tmp
EXPOSE 8080
ADD ./build/libs/RentalService-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

Construction de l'image :

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
docker build -t rentalservice .
```

Lancement du conteneur (arrêter le serveur Java local avant) :

```powershell
docker run -p 8080:8080 rentalservice
```

Test dans le navigateur : http://localhost:8080/bonjour

### Publication sur Docker Hub

```powershell
docker login
docker tag rentalservice kevmdf/rentalservice:latest
docker push kevmdf/rentalservice:latest
```

---

## TP2 – Service PHP (microservice-php-kevin)

### Création du service

Création du dossier :

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot
mkdir microservice-php-kevin
cd microservice-php-kevin
```

Contenu du fichier `index.php` :

```php
<?php
header("Content-Type: text/plain");
echo "Kevin";
```

### Dockerfile

Contenu du fichier `Dockerfile` dans `microservice-php-kevin` :

```dockerfile
FROM php:8.2-apache

COPY index.php /var/www/html/index.php

EXPOSE 80
```

### Build et exécution

Construction de l'image :

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\microservice-php-kevin
docker build -t microservice-php-kevin .
```

Lancement du conteneur :

```powershell
docker run -p 8081:80 microservice-php-kevin
```

Test dans le navigateur : http://localhost:8081

Résultat : `Kevin`

### Publication sur Docker Hub

```powershell
docker login
docker tag microservice-php-kevin kevmdf/microservice-php-kevin:latest
docker push kevmdf/microservice-php-kevin:latest
```

---

## TP2 bis – Docker Compose

### Architecture

Les deux microservices communiquent via un réseau Docker interne :

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Compose                       │
│                                                         │
│  ┌─────────────────────┐    HTTP    ┌────────────────┐ │
│  │   RentalService     │ ────────── │ microservicephp│ │
│  │   (Java/Spring)     │            │ (PHP/Apache)   │ │
│  │   Port: 8080        │            │ Port: 80       │ │
│  └─────────────────────┘            └────────────────┘ │
│                                                         │
│              Réseau: mynetwork (bridge)                │
└─────────────────────────────────────────────────────────┘
```

Le service Java appelle le service PHP via le DNS Docker `http://microservicephp`.

### Fichier docker-compose.yml

Contenu du fichier `docker-compose.yml` à la racine du projet :

```yaml
version: "3.8"

services:
  rentalservice:
    image: kevmdf/rentalservice
    container_name: rentalservice
    ports:
      - "8080:8080"
    depends_on:
      - microservicephp
    environment:
      - SPRING_PROFILES_ACTIVE=default
      - customer.service.url=http://microservicephp
    networks:
      - mynetwork

  microservicephp:
    image: kevmdf/microservice-php-kevin
    container_name: microservicephp
    ports:
      - "80:80"
    networks:
      - mynetwork

networks:
  mynetwork:
```

### Configuration Spring Boot

Fichier `application.properties` du service Java :

```properties
server.port=8080
spring.application.name=RentalService
customer.service.url=http://php-service
```

L'URL est surchargée par la variable d'environnement dans Docker Compose.

### Lancement

Compilation du service Java si nécessaire :

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
./gradlew clean build
cd ..
```

Démarrage des conteneurs :

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot
docker-compose up
```

Mode détaché :

```powershell
docker-compose up -d
```

Reconstruction des images :

```powershell
docker-compose up --build
```

### Tests

| URL | Résultat |
|-----|----------|
| http://localhost:8080/customer/Jean%20Dupont | `Kevin Jean Dupont` |
| http://localhost:80 | `Kevin` |

Le service Java récupère "Kevin" depuis le service PHP et le concatène avec le paramètre passé en URL.

### Arrêt

```powershell
docker-compose down
```

Avec suppression des volumes :

```powershell
docker-compose down -v
```

---

## TP3 – Kubernetes avec Minikube

### Structure des fichiers

```
k8s/
├── java/
│   ├── java-deployment.yaml
│   └── java-service.yaml
└── php/
    ├── php-deployment.yaml
    └── php-service.yaml
```

### Fichiers de déploiement

**java-deployment.yaml** :

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rentalservice
spec:
  replicas: 1
  selector:
    matchLabels:
      app: rentalservice
  template:
    metadata:
      labels:
        app: rentalservice
    spec:
      containers:
        - name: rentalservice
          image: kevmdf/rentalservice
          ports:
            - containerPort: 8080
          env:
            - name: CUSTOMER_SERVICE_URL
              value: http://php-service:80
```

**java-service.yaml** :

```yaml
apiVersion: v1
kind: Service
metadata:
  name: rentalservice
spec:
  selector:
    app: rentalservice
  ports:
    - port: 8080
      targetPort: 8080
  type: NodePort
```

**php-deployment.yaml** :

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: php-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: php-service
  template:
    metadata:
      labels:
        app: php-service
    spec:
      containers:
        - name: php
          image: kevmdf/microservice-php-kevin
          ports:
            - containerPort: 80
```

**php-service.yaml** :

```yaml
apiVersion: v1
kind: Service
metadata:
  name: php-service
spec:
  selector:
    app: php-service
  ports:
    - port: 80
      targetPort: 80
  type: ClusterIP
```

### Déploiement

Démarrage de Minikube :

```powershell
minikube start
```

Application des configurations :

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\k8s
kubectl apply -f java/
kubectl apply -f php/
```

Vérification :

```powershell
kubectl get pods
kubectl get svc
kubectl get deployments
```

Résultat attendu pour `kubectl get svc` :

```
NAME            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
kubernetes      ClusterIP   10.96.0.1       <none>        443/TCP          10m
php-service     ClusterIP   10.96.xxx.xxx   <none>        80/TCP           1m
rentalservice   NodePort    10.96.xxx.xxx   <none>        8080:3xxxx/TCP   1m
```

### Mise à jour des images

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
docker build -t rentalservice:latest .
minikube image load rentalservice:latest
kubectl rollout restart deployment rentalservice
```

### Accès au service

Via minikube :

```powershell
minikube service rentalservice
```

Via port-forward :

```powershell
kubectl port-forward service/rentalservice 8080:8080
```

Test : http://127.0.0.1:<NodePort>/customer/Kevin

Résultat : `Kevin Kevin`

### Arrêt

```powershell
minikube stop
```

Suppression complète :

```powershell
minikube delete
```

---

## Liens Docker Hub

| Service | Image |
|---------|-------|
| RentalService | [kevmdf/rentalservice](https://hub.docker.com/r/kevmdf/rentalservice) |
| Microservice PHP | [kevmdf/microservice-php-kevin](https://hub.docker.com/r/kevmdf/microservice-php-kevin) |

---

## Conclusion

Ce projet met en place une architecture microservices complète :

1. **Conteneurisation** : chaque service a son Dockerfile et son image sur Docker Hub
2. **Orchestration** : Docker Compose gère la communication entre les services via un réseau bridge
3. **Déploiement Kubernetes** : les services sont déployés sur Minikube avec gestion DNS interne (ClusterIP pour le PHP, NodePort pour le Java)

Les deux services communiquent via HTTP grâce au DNS interne de Docker ou Kubernetes.

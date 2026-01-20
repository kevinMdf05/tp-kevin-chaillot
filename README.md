# TP Docker, Docker Compose & Kubernetes – Microservices

## Auteur : Kevin CHAILLOT

---

## 📋 Sommaire

1. [Présentation du projet](#présentation-du-projet)
2. [TP1 – Service Java (RentalService)](#tp1--service-java-rentalservice)
   - [Prérequis](#prérequis)
   - [Exécution sans Docker](#exécution-sans-docker)
   - [Exécution avec Docker](#exécution-avec-docker)
   - [Publication sur Docker Hub](#publication-sur-docker-hub)
3. [TP2 – Service PHP (microservice-php-kevin)](#tp2--service-php-microservice-php-kevin)
   - [Création du service](#création-du-service)
   - [Dockerfile PHP](#dockerfile-php)
   - [Build et exécution](#build-et-exécution)
   - [Publication sur Docker Hub](#publication-sur-docker-hub-1)
4. [TP2 bis – Docker Compose](#tp2-bis--docker-compose)
   - [Architecture](#architecture)
   - [Fichier docker-compose.yml](#fichier-docker-composeyml)
   - [Lancement des services](#lancement-des-services)
   - [URLs de test](#urls-de-test)
5. [TP3 – Kubernetes avec Minikube](#tp3--kubernetes-avec-minikube)
   - [Structure des fichiers K8s](#structure-des-fichiers-k8s)
   - [Déploiement](#déploiement)
   - [Accès aux services](#accès-aux-services)
6. [Liens Docker Hub](#liens-docker-hub)
7. [Captures d'écran](#captures-décran)

---

## Présentation du projet

Ce projet met en œuvre une architecture **microservices** composée de deux services communiquant via HTTP :

| Service | Technologie | Port | Description |
|---------|-------------|------|-------------|
| **RentalService** | Java 21 / Spring Boot 3.2.1 | 8080 | Service principal qui appelle le service PHP |
| **microservice-php-kevin** | PHP 8.2 / Apache | 80 | Service secondaire retournant le prénom "Kevin" |

Le projet couvre trois niveaux de déploiement :
- **Docker** : Conteneurisation individuelle des services
- **Docker Compose** : Orchestration locale des microservices
- **Kubernetes** : Déploiement sur cluster Minikube

---

## TP1 – Service Java (RentalService)

### Prérequis

- **Java JDK 21** installé sur la machine
- Vérification de la version :

```powershell
java -version
```

> Si Java 21 n'est pas installé :
> ```powershell
> winget install EclipseAdoptium.Temurin.21.JDK
> ```

### Exécution sans Docker

#### 1. Se positionner dans le dossier du service Java

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
```

#### 2. Compiler le projet avec Gradle

```powershell
./gradlew build
```

#### 3. Lancer l'application

```powershell
java -jar build/libs/RentalService-0.0.1-SNAPSHOT.jar
```

#### 4. Tester dans le navigateur

```
http://localhost:8080/bonjour
```

> ⚠️ Pour arrêter le serveur : `Ctrl + C`

### Exécution avec Docker

#### 1. Contenu du Dockerfile

Créer un fichier `Dockerfile` dans le dossier `RentalService` :

```dockerfile
FROM eclipse-temurin:21
VOLUME /tmp
EXPOSE 8080
ADD ./build/libs/RentalService-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

#### 2. Construire l'image Docker

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
docker build -t rentalservice .
```

#### 3. Lancer le conteneur

> ⚠️ S'assurer que le serveur Java local est arrêté avant de lancer Docker.

```powershell
docker run -p 8080:8080 rentalservice
```

#### 4. Tester dans le navigateur

```
http://localhost:8080/bonjour
```

### Publication sur Docker Hub

#### 1. Se connecter à Docker Hub

```powershell
docker login
```

#### 2. Taguer l'image

```powershell
docker tag rentalservice kevmdf/rentalservice:latest
```

#### 3. Pousser l'image

```powershell
docker push kevmdf/rentalservice:latest
```

---

## TP2 – Service PHP (microservice-php-kevin)

### Création du service

#### 1. Créer le dossier du service PHP

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot
mkdir microservice-php-kevin
cd microservice-php-kevin
```

#### 2. Créer le fichier index.php

Créer un fichier `index.php` avec le contenu suivant :

```php
<?php
header("Content-Type: text/plain");
echo "Kevin";
```

Ce service retourne simplement le prénom "Kevin" en texte brut.

### Dockerfile PHP

Créer un fichier `Dockerfile` dans le dossier `microservice-php-kevin` :

```dockerfile
FROM php:8.2-apache

COPY index.php /var/www/html/index.php

EXPOSE 80
```

### Build et exécution

#### 1. Construire l'image Docker

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\microservice-php-kevin
docker build -t microservice-php-kevin .
```

#### 2. Lancer le conteneur

```powershell
docker run -p 8081:80 microservice-php-kevin
```

#### 3. Tester dans le navigateur

```
http://localhost:8081
```

**Résultat attendu :**
```
Kevin
```

### Publication sur Docker Hub

```powershell
docker login
docker tag microservice-php-kevin kevmdf/microservice-php-kevin:latest
docker push kevmdf/microservice-php-kevin:latest
```

---

## TP2 bis – Docker Compose

### Architecture

L'orchestration Docker Compose permet aux deux microservices de communiquer via un réseau Docker interne.

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

**Communication** : Le service Java appelle le service PHP via le nom DNS Docker `http://microservicephp`.

### Fichier docker-compose.yml

Créer un fichier `docker-compose.yml` à la racine du projet :

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

Le fichier `application.properties` du service Java contient :

```properties
server.port=8080
spring.application.name=RentalService
customer.service.url=http://php-service
```

> Note : L'URL du service PHP est surchargée par la variable d'environnement dans Docker Compose.

### Lancement des services

#### 1. Compiler le service Java (si modifications)

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
./gradlew clean build
cd ..
```

#### 2. Démarrer les conteneurs

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot
docker-compose up
```

Ou en mode détaché (arrière-plan) :

```powershell
docker-compose up -d
```

#### 3. Reconstruire les images si nécessaire

```powershell
docker-compose up --build
```

### URLs de test

| URL | Résultat attendu |
|-----|------------------|
| `http://localhost:8080/customer/Jean%20Dupont` | `Kevin Jean Dupont` |
| `http://localhost:80` | `Kevin` |

**Explication du flux :**
1. L'utilisateur appelle `/customer/Jean Dupont` sur le service Java
2. Le service Java appelle `http://microservicephp` et récupère "Kevin"
3. Le service Java concatène la réponse avec le paramètre et retourne "Kevin Jean Dupont"

### Arrêt des conteneurs

```powershell
docker-compose down
```

Avec suppression des volumes :

```powershell
docker-compose down -v
```

---

## TP3 – Kubernetes avec Minikube

### Structure des fichiers K8s

```
k8s/
├── java/
│   ├── java-deployment.yaml    # Deployment du service Java
│   └── java-service.yaml       # Service NodePort pour accès externe
└── php/
    ├── php-deployment.yaml     # Deployment du service PHP
    └── php-service.yaml        # Service ClusterIP pour accès interne
```

### Fichiers de déploiement

#### java-deployment.yaml

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

#### java-service.yaml

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

#### php-deployment.yaml

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

#### php-service.yaml

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

#### 1. Démarrer Minikube

```powershell
minikube start
```

#### 2. Appliquer les configurations Kubernetes

```powershell
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\k8s
kubectl apply -f java/
kubectl apply -f php/
```

Ou tout en une commande :

```powershell
kubectl apply -f .
```

#### 3. Vérifier le déploiement

```powershell
# Voir les pods
kubectl get pods

# Voir les services
kubectl get svc

# Voir les déploiements
kubectl get deployments
```

**Résultat attendu :**

```
NAME            TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
kubernetes      ClusterIP   10.96.0.1       <none>        443/TCP          10m
php-service     ClusterIP   10.96.xxx.xxx   <none>        80/TCP           1m
rentalservice   NodePort    10.96.xxx.xxx   <none>        8080:3xxxx/TCP   1m
```

### Mise à jour des images

Si vous modifiez le code et souhaitez redéployer :

```powershell
# Reconstruire l'image Java
cd C:\Users\kevmo\Desktop\tp-kevin-chaillot\RentalService
docker build -t rentalservice:latest .

# Charger l'image dans Minikube
minikube image load rentalservice:latest

# Redémarrer le déploiement
kubectl rollout restart deployment rentalservice
```

### Accès aux services

#### Option 1 : Commande minikube service

```powershell
minikube service rentalservice
```

Cette commande ouvre automatiquement le navigateur avec l'URL correcte.

#### Option 2 : Port-forward manuel

```powershell
kubectl port-forward service/rentalservice 8080:8080
```

Puis accéder via : `http://localhost:8080/customer/Kevin`

### Test final

**URL de test :**
```
http://127.0.0.1:<NodePort>/customer/Kevin
```

**Résultat attendu :**
```
Kevin Kevin
```

> Le NodePort est attribué dynamiquement. Utilisez `kubectl get svc` pour le récupérer.

### Arrêt du cluster

```powershell
minikube stop
```

Ou pour supprimer complètement :

```powershell
minikube delete
```

---

## Liens Docker Hub

| Service | Image Docker Hub |
|---------|------------------|
| RentalService | [kevmdf/rentalservice](https://hub.docker.com/r/kevmdf/rentalservice) |
| Microservice PHP | [kevmdf/microservice-php-kevin](https://hub.docker.com/r/kevmdf/microservice-php-kevin) |

---

## Captures d'écran

> 📸 **À compléter** : Insérer les captures d'écran pour illustrer chaque étape.

### Captures recommandées :

#### TP1 – Service Java
- [ ] Résultat de `java -version`
- [ ] Build Gradle réussi (`./gradlew build`)
- [ ] Application Java lancée sans Docker
- [ ] Test navigateur `http://localhost:8080/bonjour`
- [ ] Build Docker réussi
- [ ] Application lancée avec Docker
- [ ] Push sur Docker Hub réussi

#### TP2 – Service PHP
- [ ] Contenu du fichier `index.php`
- [ ] Build Docker du service PHP
- [ ] Test navigateur `http://localhost:8081`
- [ ] Push sur Docker Hub réussi

#### TP2 bis – Docker Compose
- [ ] Fichier `docker-compose.yml`
- [ ] Lancement avec `docker-compose up`
- [ ] Test `http://localhost:8080/customer/Jean%20Dupont` → "Kevin Jean Dupont"
- [ ] Test `http://localhost:80` → "Kevin"

#### TP3 – Kubernetes
- [ ] Résultat de `kubectl get pods`
- [ ] Résultat de `kubectl get svc`
- [ ] Accès via `minikube service rentalservice`
- [ ] Test navigateur avec le NodePort

---

## Conclusion

Ce projet démontre la mise en place complète d'une architecture microservices :

1. **Conteneurisation** : Chaque service possède son propre Dockerfile et son image Docker Hub
2. **Orchestration locale** : Docker Compose permet de lancer et faire communiquer les services
3. **Déploiement cloud-ready** : Kubernetes assure un déploiement scalable avec gestion DNS interne

Les services communiquent via HTTP grâce au DNS interne de Docker (Compose) ou Kubernetes, permettant une architecture découplée et facilement maintenable.

---

*Projet réalisé dans le cadre des TPs Docker, Docker Compose et Kubernetes.*

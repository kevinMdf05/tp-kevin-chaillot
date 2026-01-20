# TP Docker – RentalService
## Kevin CHAILLOT

---

## 1. Installation
La version Java JDK 21 était déjà installée sur ma machine.

---

## 2. Tester le programme sans Docker

### Builder le projet
./gradlew build

### Tester le projet
java -jar build/libs/RentalService-0.0.1-SNAPSHOT.jar

---

## 3. Création du fichier Dockerfile
Créer le fichier `Dockerfile` dans le dossier `RentalService`.

FROM eclipse-temurin:21  
VOLUME /tmp  
EXPOSE 8080  
ADD ./build/libs/RentalService-0.0.1-SNAPSHOT.jar app.jar  
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

---

## 4. Création de l’image Docker
Depuis le dossier `RentalService` :
docker build -t rentalservice .

---

## 5. Tester le programme avec Docker
docker run -p 8080:8080 rentalservice

URL de test :
http://localhost:8080/bonjour

---

## 6. Publier l’image dans Docker Hub
docker login  
docker tag rentalservice kevmdf/rentalservice:latest  
docker push kevmdf/rentalservice:latest  

---

## 7. TP2 – Microservices avec Docker Compose

Architecture :
- RentalService : microservice Java (Spring Boot)
- MicroservicePHP : microservice PHP
- Communication HTTP
- Orchestration via Docker Compose

Lancement :
docker-compose up

Test :
http://localhost:8080/customer/Jean%20Dupont

Résultat attendu :
Kevin Jean Dupont

Commandes utiles :
cd RentalService  
./gradlew clean build  
docker build -t kevmdf/rentalservice .  
cd ..  
docker-compose down  
docker-compose up  

---

## 8. TP3 – Déploiement avec Kubernetes (Minikube)

Architecture Kubernetes :
- Deployment rentalservice (Spring Boot)
- Deployment php-service (PHP)
- Service ClusterIP (PHP)
- Service NodePort (Java)
- DNS interne Kubernetes

Configuration Spring Boot :
server.port=8080  
spring.application.name=RentalService  
customer.service.url=http://php-service  

Déploiement :
minikube start  
kubectl apply -f .  

Mise à jour de l’image :
cd RentalService  
docker build -t rentalservice:latest .  
minikube image load rentalservice:latest  
kubectl rollout restart deployment rentalservice  

Vérification :
kubectl get svc  

Accès :
minikube service rentalservice  
Exemple :
http://127.0.0.1:31390/customer/Kevin

Résultat attendu :
Bonjour Kevin

---

## Conclusion
Ce projet met en œuvre une architecture microservices composée d’un service Java et d’un service PHP communiquant via HTTP. Les services sont conteneurisés avec Docker, orchestrés avec Docker Compose, puis déployés sur Kubernetes via Minikube.

In env vars (.env file at the project root):

    - replace the ip adress in EUREKA_SERVER_URL with your own
    - fill in the SECRET_KEY and GOOGLE_CLIENT_SECRET, and API_KEY with the secrets
    - if using prod make sure to include PROD_USER_NAME and PROD_USER_PSWD

# Development
Clone the repository and change directory to the project root  
Run env.py to create .env files for each microservice ```python env.py --env dev```    
Run ```./gradlew clean``` (if necessary)  
Run ```./gradlew makeSharedObjs```  
Run ```./gradlew build```  
Go to the EurekaService ```cd .\EurekaService\```  
Run ```docker compose -f docker-compose.dev.yml up``` to start the Eureka server
Change directory to the service you want to run ```cd ..\service``` (replace service with the path to the service)  
Run ```docker compose -f docker-compose.dev.yml up --build``` to build the Docker image and start the service  

# Production
Clone the repository and change directory to the project root  
Run env.py to create .env files for each microservice ```python env.py --env prod```    
Run ```./gradlew clean``` (if necessary)  
Run ```./gradlew makeSharedObjs```  
Run ```./gradlew build```  
Change directory to the service you want to run ```cd .\service``` (replace service with the path to the service)  
Run ```docker compose -f docker-compose.prod.yml up --build``` to build the Docker image and start the service  

### Notes

Make sure that the most recent SharedObjs its built```gradlew :SharedObjs:build``` and published ```gradlew :SharedObjs:publish``` before building any microservices as they use the published SharedObjs in their build.  
Classes used in multiple microservices are put in SharedObjs.

Note that an endpoint in a service might require another service to be running as it is dependent on information provided by the other service.
The Eureka server also needs to be started for inter-service communication to work.

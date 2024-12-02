In env vars:

    - replace the ip adress in EUREKA_SERVER_URL with your own
    - fill in the SECRET_KEY and GOOGLE_CLIENT_SECRET with the secrets

# Development
python env.py --env dev
./gradlew makeSharedObjs
./gradlew service:build
docker network create netw
cd service
docker-compose -f docker-compose.dev.yml up --build

# Production
python env.py --env prod
./gradlew makeSharedObjs
./gradlew service:build
cd service
docker-compose -f docker-compose.prod.yml up --build



Make sure that the most recent SharedObjs its built```gradlew :SharedObjs:build``` and published ```gradlew :SharedObjs:publish```
If adding a class that will be used in multiple microservices, put it in SharedObjs.
use the new ```./gradlew fullBuild``` in place of clean build due to ShareObjs needing to be built first

Start the eureka server with docker compose (other services try to connect to it so will cause errors if not running)

Start other services as needed with docker compose

In env vars:

    - replace the ip adress in EUREKA_SERVER_URL with your own
    - fill in the SECRET_KEY and GOOGLE_CLIENT_SECRET with the secrets

From the project root directory, run ```python env.py```

Create the docker external network ```docker network create netw```

Make sure that the most recent SharedObjs its built```gradlew :SharedObjs:build``` and published ```gradlew :SharedObjs:publish```
If adding a class that will be used in multiple microservices, put it in SharedObjs.
use the new ```./gradlew fullBuild``` in place of clean build due to ShareObjs needing to be built first

Start the eurika server with docker compose

Start other services as needed with docker compose

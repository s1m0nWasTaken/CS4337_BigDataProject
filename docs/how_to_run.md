In env vars:

    - replace the ip adress in EUREKA_SERVER_URL with your own
    - fill in the SECRET_KEY and GOOGLE_CLIENT_SECRET with the secrets

From the project root directory, run ```python env.py```

Create the docker external network ```docker network create netw```

Start the eurika server with docker compose

Start other services as needed with docker compose

import os
import subprocess
import http.client
import json
import time

script_dir = os.path.dirname(os.path.realpath(__file__))
conf_env = os.path.join(script_dir, "ConfigService/.env")
with open(conf_env, "w") as env_file:
    env_file.write("""\
#shared-config
export ENV=dev
export USER_NAME=root
export USER_PSWD=1234
export NETWORK_NAME=netw

#config service
export CNFG_SERVER_PORT=8888
export CNFG_CONTAINER_PORT=8888
""")

# add to as you add services
target_dirs = ["UserService", "ShopService", "MessagingService", "AuthService", "BanService", "ConfigService"]

docker_auth = ""
if os.name == "posix":
    print(f"Linux detected \n")
    docker_auth = "sudo "

os.chdir(os.path.join(script_dir, 'ConfigService'))
build_command = docker_auth + "docker build -t configservice ."
run_command = docker_auth + "docker compose up -d"
#start config service
check_command = docker_auth + "docker ps"
result = subprocess.run(build_command, shell=True, capture_output=True, text=True)
if result.returncode != 0:
    print(f"Error during build: {result.stderr}")
else:
    print(f"Build success: {result.stdout}")

result = subprocess.run(run_command, shell=True, capture_output=True, text=True)
if result.returncode != 0:
    print(f"Error during run: {result.stderr}")
else:
    print(f"Container up")

#sleep so the container is ready for connections
time.sleep(10)

#get env vars from config service
conn = http.client.HTTPConnection("localhost", 8888)
conn.request("GET", "/test/dev")
response = conn.getresponse()
response_content = response.read().decode()

data = json.loads(response_content)
conn.close()

#turn the vars into usable env vars
def generate_env_vars(data):
    services = {
        "user": None,
        "shop": None,
        "msg": None,
        "ban": None,
        "auth": None
    }

    script = ""

    # Iterate over the property sources
    for source in data['propertySources']:
        name = source["name"]
        service = name.split("/")[-1].split("-")[0]  # Extract service name
        
        # Ensure the service exists in the services dictionary
        if service not in services:
            continue

        script += f"#{service} service\n"
        properties = source["source"]

        for key, value in properties.items():
            # Transform keys like "server.port" to "SERVER_PORT"
            env_var = key.replace(".", "_").upper()

            if service == "user":
                script += f"export USER_{env_var}={value}\n"
            elif service == "shop":
                script += f"export SHOP_{env_var}={value}\n"
            elif service == "msg":
                script += f"export MSG_{env_var}={value}\n"
            elif service == "ban":
                script += f"export BAN_{env_var}={value}\n"
            elif service == "auth":
                script += f"export AUTH_{env_var}={value}\n"

    return script

#vars that are ok to define locally
non_dynamic_vars ="""\
#shared-config
export ENV=dev
export USER_NAME=root
export USER_PSWD=1234
export NETWORK_NAME=netw
export SECRET_KEY=
#config service
export CNFG_SERVER_PORT=8888
export CNFG_CONTAINER_PORT=8888
#auth service
export GOOGLE_CLIENT_ID=529138320852-h26t99u2jh694u7q3u3c2oaqma07oabe.apps.googleusercontent.com
export GOOGLE_CLIENT_SECRET=
export GOOGLE_REDIRECT_URI=http://localhost:8082/grantcode
#container names
export BAN_CONTAINER=ban-service
export MSG_CONTAINER=messaging-service
export SHOP_CONTAINER=shop-service
export USER_CONTAINER=user-service
export AUTH_CONTAINER=auth
"""

env_vars = non_dynamic_vars + generate_env_vars(data)

# Loop through each target directory
for target_dir in target_dirs:
    # Ensure the directory exists
    dir_path = os.path.join(script_dir, target_dir)
    os.makedirs(dir_path, exist_ok=True)
    
    env_path = os.path.join(dir_path, ".env")
    with open(env_path, "w") as env_file:
        env_file.write(env_vars)

print(f".envs made")
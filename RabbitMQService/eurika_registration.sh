EUREKA_HOST=${EUREKA_SERVER_URL}
SERVICE_NAME="RABBITMQSERVICE"
INSTANCE_ID=$(hostname)
RABBITMQ_HOST="rabbitmq-host"

echo $EUREKA_SERVER_URL
echo $(hostname)

# Register RabbitMQ service with Eureka
apt update
apt install -y curl
curl -X POST \
  -H "Content-Type: application/json" \
  -d "{
      \"instance\": {
          \"instanceId\": \"$INSTANCE_ID\",
          \"hostName\": \"$RABBITMQ_HOST\",
          \"app\": \"$SERVICE_NAME\",
          \"ipAddr\": \"$RABBITMQ_HOST\",
          \"status\": \"UP\",
          \"port\": {\"$\": 5672, \"@enabled\": \"true\"},
          \"securePort\": {\"$\": 15672, \"@enabled\": \"true\"},
          \"healthCheckUrl\": \"http://$RABBITMQ_HOST:15672/api/health\",
          \"homePageUrl\": \"http://$RABBITMQ_HOST:15672\",
          \"metadata\": {
              \"version\": \"3.8.0\"
          },
          \"dataCenterInfo\": {
              \"@class\": \"com.netflix.appinfo.InstanceInfo\$DefaultDataCenterInfo\",
              \"name\": \"MyOwn\"
          }
      }
  }" \
  "$EUREKA_HOST""apps/$SERVICE_NAME"
#!/bin/bash
TARGET_DIRS=("UserService" "ShopService" "MessagingService" "BanService")

#NB!! remember to make the external network(nessesary to let container http eachother):
#`docker network create netw`
    #export NETWORK_NAME=netw
    #user service
    #export USER_HOST_PORT=9090
    #export USER_CONTAINER_PORT=9090
    #export USER_CONTAINER=user-service
    #export USER_MYSQL_HOST_PORT=3306
    #export USER_MYSQL_CONTAINER_PORT=3306

SCRIPT_DIR=$(dirname "$(realpath "$0")")
#build with `docker build -t msgservice .` idk why otherwise env vars dont work
for TARGET_DIR in "${TARGET_DIRS[@]}"; do
    touch $SCRIPT_DIR/$TARGET_DIR/.env
    {
        echo "export NETWORK_NAME=netw"
        echo "#user service"
        echo "export USER_HOST_PORT=9090"
        echo "export USER_CONTAINER_PORT=9090"
        echo "export USER_CONTAINER=user-service"
        echo "export USER_MYSQL_HOST_PORT=3306"
        echo "export USER_MYSQL_CONTAINER_PORT=3306"
        echo "#shop service"
        echo "export SHOP_HOST_PORT=8080"
        echo "export SHOP_CONTAINER_PORT=8080"
        echo "export SHOP_CONTAINER=shop-service"
        echo "export SHOP_MYSQL_HOST_PORT=3308"
        echo "export SHOP_MYSQL_CONTAINER_PORT=3306"
        echo "#messaging service"
        echo "export MSG_HOST_PORT=8081"
        echo "export MSG_CONTAINER_PORT=8081"
        echo "export MSG_CONTAINER=messaging-service"
        echo "export MSG_MYSQL_HOST_PORT=3308"
        echo "export MSG_MYSQL_CONTAINER_PORT=3306"
    } > "$SCRIPT_DIR/$TARGET_DIR/.env"
done
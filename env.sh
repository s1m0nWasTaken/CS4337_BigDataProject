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

for TARGET_DIR in "${TARGET_DIRS[@]}"; do
    touch $SCRIPT_DIR/$TARGET_DIR/.env
    {
        echo "NETWORK_NAME=netw"
        echo "#user service"
        echo "USER_HOST_PORT=9090"
        echo "USER_CONTAINER_PORT=9090"
        echo "USER_CONTAINER=user-service"
        echo "USER_MYSQL_HOST_PORT=3306"
        echo "USER_MYSQL_CONTAINER_PORT=3306"
        echo "#shop service"
        echo "SHOP_HOST_PORT=8080"
        echo "SHOP_CONTAINER_PORT=8080"
        echo "SHOP_CONTAINER=shop-service"
        echo "SHOP_MYSQL_HOST_PORT=3308"
        echo "SHOP_MYSQL_CONTAINER_PORT=3306"
        echo "#messaging service"
        echo "MSG_HOST_PORT=8081"
        echo "MSG_CONTAINER_PORT=8081"
        echo "MSG_CONTAINER=messaging-service"
        echo "MSG_MYSQL_HOST_PORT=3308"
        echo "MSG_MYSQL_CONTAINER_PORT=3306"
    } > "$SCRIPT_DIR/$TARGET_DIR/.env"
done
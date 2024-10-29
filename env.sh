#!/bin/bash
TARGET_DIRS=("UserService" "ShopService" "MessagingService" "BanService")

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
    } > "$SCRIPT_DIR/$TARGET_DIR/.env"
done
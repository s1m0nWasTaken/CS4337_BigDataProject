#!/bin/bash
TARGET_DIRS=("UserService" "ShopService" "MessagingService" "BanService" "ConfigService")
#refrence ports, ect in https://github.com/AlB1111122/BigDataConfig/tree/main/ports to update
SCRIPT_DIR=$(dirname "$(realpath "$0")")
#build with `docker build -t msgservice .` idk why otherwise env vars dont work
for TARGET_DIR in "${TARGET_DIRS[@]}"; do
    touch $SCRIPT_DIR/$TARGET_DIR/.env
    {
        echo "export ENV=dev"
        echo "export USER_NAME=root"
        echo "export USER_PSWD=1234"
        echo "export NETWORK_NAME=netw"
        echo "#config service"
        echo "export CNFG_SERVER_PORT=8888"
        echo "export CNFG_CONTAINER_PORT=8888"
        echo "#user service"
        echo "export USER_SERVER_PORT=9090"
        echo "export USER_CONTAINER_PORT=9090"
        echo "export USER_CONTAINER=user-service"
        echo "export USER_MYSQL_SERVER_PORT=3306"
        echo "export USER_MYSQL_CONTAINER_PORT=3306"
        echo "#shop service"
        echo "export SHOP_SERVER_PORT=8080"
        echo "export SHOP_CONTAINER_PORT=8080"
        echo "export SHOP_CONTAINER=shop-service"
        echo "export SHOP_MYSQL_SERVER_PORT=3307"
        echo "export SHOP_MYSQL_CONTAINER_PORT=3306"
        echo "#messaging service"
        echo "export MSG_SERVER_PORT=8081"
        echo "export MSG_CONTAINER_PORT=8081"
        echo "export MSG_CONTAINER=messaging-service"
        echo "export MSG_MYSQL_SERVER_PORT=3308"
        echo "export MSG_MYSQL_CONTAINER_PORT=3306"
    } > "$SCRIPT_DIR/$TARGET_DIR/.env"
done
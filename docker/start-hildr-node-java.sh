#!/bin/sh
set -e

DEVNET=""

if [ $NETWORK = "custom" ] || [ $NETWORK = "devnet" ]
then
    NETWORK="./rollup.json"
    if [ "$NETWORK" = "devnet" ]
    then
        DEVNET="--devnet"
    fi
fi

if [[ $LOG_LEVEL = "" ]]
then
  LOG_LEVEL="INFO"
fi

if [ $SYNC_MODE = "full" ] || [ $SYNC_MODE = "execution-layer" ]
then
    exec java --enable-preview \
        -cp $HILDR_JAR $HILDR_MAIN_CLASS \
        --network $NETWORK \
        --jwt-secret $JWT_SECRET \
        --l1-rpc-url $L1_RPC_URL \
        --l1-ws-rpc-url $L1_WS_RPC_URL \
        --l1-beacon-url $L1_BEACON_RPC_URL \
        --l2-rpc-url http://${EXECUTION_CLIENT}:${EXECUTION_CLIENT_RPC_PORT} \
        --l2-engine-url http://${EXECUTION_CLIENT}:${EXECUTION_CLIENT_AUTH_RPC_PORT} \
        --rpc-port $RPC_PORT \
        $DEVNET \
        --sync-mode $SYNC_MODE \
        --log-level $LOG_LEVEL
elif [ $SYNC_MODE = "checkpoint"]
then
    exec java --enable-preview \
        -cp $HILDR_JAR $HILDR_MAIN_CLASS \
        --network $NETWORK \
        --jwt-secret $JWT_SECRET \
        --l1-rpc-url $L1_RPC_URL \
        --l1-ws-rpc-url $L1_WS_RPC_URL \
        --l1-beacon-url $L1_BEACON_RPC_URL \
        --l2-rpc-url http://${EXECUTION_CLIENT}:${EXECUTION_CLIENT_RPC_PORT} \
        --l2-engine-url http://${EXECUTION_CLIENT}:${EXECUTION_CLIENT_AUTH_RPC_PORT} \
        --rpc-port $RPC_PORT \
        $DEVNET \
        --sync-mode $SYNC_MODE \
        --checkpoint-sync-url $CHECKPOINT_SYNC_URL \
        --checkpoint-hash $CHECKPOINT_HASH \
        --log-level $LOG_LEVEL
else
    echo "Sync mode not recognized. Available options are full and checkpoint"
fi

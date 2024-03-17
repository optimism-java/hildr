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

if [ $SYNC_MODE = "full" ]
then
    exec hildr-node \
        --network $NETWORK \
        --jwt-secret $JWT_SECRET \
        --l1-rpc-url $L1_WS_RPC_URL \
        --l1-ws-rpc-url $L1_WS_RPC_URL \
        --l1-beacon-url $L1_BEACON_RPC_URL \
        --l2-rpc-url http://${EXECUTION_CLIENT}:8545 \
        --l2-engine-url http://${EXECUTION_CLIENT}:8551 \
        --rpc-port $RPC_PORT \
        $DEVNET \
        --sync-mode $SYNC_MODE \
        --log-level $LOG_LEVEL
elif [ $SYNC_MODE = "checkpoint"]
then
    exec hildr-node \
        --network $NETWORK \
        --jwt-secret $JWT_SECRET \
        --l1-rpc-url $L1_WS_RPC_URL \
        --l1-ws-rpc-url $L1_WS_RPC_URL \
        --l1-beacon-url $L1_BEACON_RPC_URL \
        --l2-rpc-url http://${EXECUTION_CLIENT}:8545 \
        --l2-engine-url http://${EXECUTION_CLIENT}:8551 \
        --rpc-port $RPC_PORT \
        $DEVNET \
        --sync-mode $SYNC_MODE \
        --log-level $LOG_LEVEL \
        --checkpoint-sync-url $CHECKPOINT_SYNC_URL \
        --checkpoint-hash $CHECKPOINT_HASH
else
    echo "Sync mode not recognized. Available options are full and checkpoint"
fi

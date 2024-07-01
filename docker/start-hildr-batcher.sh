#!/bin/sh
set -e

exec java --enable-preview \
    -cp $HILDR_BATCHER_JAR $HILDR_BATCHER_MAIN_CLASS \
    --l1-rpc-url $L1_RPC_URL \
    --l1-signer $L1_SIGNER \
    --batch-inbox-address $BATCH_INBOX_ADDRESS \
    --l2-rpc-url http://${EXECUTION_CLIENT}:${EXECUTION_CLIENT_RPC_PORT} \
    --rollup-rpc-url http://${ROLLUP_CLIENT}:${ROLLUP_RPC_PORT} \
    --log-level $LOG_LEVEL
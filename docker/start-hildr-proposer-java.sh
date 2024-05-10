#!/bin/sh
set -e

exec java --enable-preview \
    -cp $HILDR_PROPOSER_JAR $HILDR_PROPOSER_MAIN_CLASS \
    --l1-rpc-url $L1_RPC_URL \
    --l2-rpc-url http://${EXECUTION_CLIENT}:${EXECUTION_CLIENT_RPC_PORT} \
    --l2-chain-id $L2_CHAIN_ID \
    --l2-signer $L2_SIGNER \
    --l2oo-address $L2OO_ADDRESS \
    --l2dgf-address $L2DGF_ADDRESS \
    --rollup-rpc-url http://${ROLLUP_CLIENT}:${ROLLUP_RPC_PORT} \
    --log-level $LOG_LEVEL
[![hildr CI](https://github.com/optimism-java/hildr/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/optimism-java/hildr/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-MIT-blue)](https://opensource.org/licenses/MIT)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

# Hildr

This work is supported by an [Optimism governance grant](https://app.charmverse.io/op-grants/proposals?id=e5613e76-a26f-41e4-9f0d-4e2dbfccf5b8).

Hildr is an OP Stack rollup client written in Java 21 with GraalVM native.

Follow the [spec](https://github.com/ethereum-optimism/optimism/blob/develop/specs/rollup-node.md)

- [Rust](https://github.com/a16z/magi)
- [Go](https://github.com/ethereum-optimism/optimism/tree/develop/op-node)

## System requirements
To run a `hildr-node` and `op-geth` node, at least 4C8G and 100GB of disk is required, as well as the installation of Java version 21 and Go version 1.20.8, must be lower than v1.21.

## Running `hildr-node`

First, use Docker start an `op-geth` container in hildr project root directory:

```shell
cd ./docker && docker compose -f docker/docker-compose.yml up op-geth
```

### Use shell
Then, once op-geth has been started, start up the `hildr-node` in `hildr` project root directory:
```shell
./gradlew :hildr-node:build -x test \
	&& nohup java --enable-preview \
		-cp hildr-node/build/libs/hildr-node-{version}.jar io.optimism.Hildr \
		--network optimism-sepolia \
		--jwt-secret $JWT_SECRET \
		--l1-rpc-url $L1_RPC_URL \
		--l1-ws-rpc-url $L1_WS_RPC_URL \
		--l1-beacon-url $L1_BEACON_RPC_URL \
		--l2-rpc-url $L2_RPC_URL \
		--l2-engine-url $L2_AUTH_RPC_URL \
		--rpc-port $HILDR_RPC_PORT \ # Choose any available port.
		--log-level $LOG_LEVEL \ # can be either: "DEBUG","TRACE","INFO","WARN","ERROR"
		--sync-mode full >l2-hildr-node.log 2>&1 &
```

### Use docker

Running a native hildr container on optimism-sepolia network:
```shell
docker run -it ghcr.io/optimism-java/hildr:latest-native \
	--network optimism-sepolia \
	--jwt-secret $JWT_SECRET \
	--l1-rpc-url $L1_RPC_URL \
	--l1-ws-rpc-url $L1_WS_RPC_URL \
	--l1-beacon-url $L1_BEACON_RPC_URL \
	--l2-rpc-url $L2_RPC_URL \
	--l2-engine-url $L2_AUTH_RPC_URL \
	--rpc-port $HILDR_RPC_PORT \
	--log-level $LOG_LEVEL \ # can be either: "DEBUG","TRACE","INFO","WARN","ERROR"
	--sync-mode full
```

Running a java hildr container on optimism-sepolia network:
```shell
docker run -it ghcr.io/optimism-java/hildr:latest \
	--network optimism-sepolia \
	--jwt-secret $JWT_SECRET \
	--l1-rpc-url $L1_RPC_URL \
	--l1-ws-rpc-url $L1_WS_RPC_URL \
	--l2-rpc-url $L2_RPC_URL \
	--l2-engine-url $L2_AUTH_RPC_URL \
	--rpc-port $HILDR_RPC_PORT \
	--log-level $LOG_LEVEL \ # can be either: "DEBUG","TRACE","INFO","WARN","ERROR"
	--sync-mode full
```

## Installing Hildr Node

### Building `hildr-node` from source


Install `op-geth` via the `Makefile` in the workspace root:

```shell
git clone git@github.com:optimism-java/hildr.git \
cd hildr && ./gradlew build -x test
```

This command will generate the `hildr-node` jar file in `hildr/hildr-node/build/libs`

## Running on Optimism Sepolia

You will need three things to run `hildr-node`:
1. An archival L1 node, synced to the settlement layer of the OP Stack chain you want to sync (e.g. reth, geth, besu, nethermind, etc.)
2. An Optimism Execution Engine (e.g. op-geth, op-reth, op-erigon, etc.)
3. An instance of hildr-node.
For this example, we'll start an Optimism Sepolia Node.


### Installing an Optimism Execution Engine

Next, you'll need to install a [Optimism L2 Execution Engine](https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md), which is the equivalent to the Execution Client on the OP Stack.
Available options include:
1. [op-geth](https://github.com/ethereum-optimism/op-geth)
2. [reth](https://github.com/paradigmxyz/reth)
3. [op-erigon](https://github.com/testinprod-io/op-erigon)

We'll use the reference implementation of the Optimism Execution Engine maintained by OP Labs, the `op-geth`.

The `op-geth` can be built from source or pulled from a [Docker image available on Google Cloud](https://console.cloud.google.com/artifacts/docker/oplabs-tools-artifacts/us/images/op-geth).

We'll run it by docker image.

### Modify `.env` file

There has a .env.default file in the `./docker` directory. Copy it as `.env` and modify the fields value below:

```properties
# Set the network value to `devnet` in the configuration.
NETWORK=optimism-sepolia

# The HTTP RPC endpoint of an L1 node. Generate by Alchemy.com
L1_RPC_URL=https://eth-goerli.g.alchemy.com/v2/<Alchemy App Key>

# The WebSocket RPC endpoint of an L1 node. Generate by Alchemy.com
L1_WS_RPC_URL=wss://eth-goerli.g.alchemy.com/v2/<Alchemy App Key>

# JWT secret for the L2 engine API
JWT_SECRET=bf549f5188556ce0951048ef467ec93067bc4ea21acebe46ef675cd4e8e015ff

# The exeuction client Auth RPC port.
EXECUTION_CLIENT_AUTH_RPC_PORT=5551

# The execution client RPC port.
EXECUTION_CLIENT_RPC_PORT=5545

# The execution client WebSocket port.
EXECUTION_CLIENT_WS_PORT=5546

```

### Running devnet

You also can run a hildr-node on [devnet](./docs/devnet.md)

## Build Hildr

### Prerequisites
- Install [GraalVM latest version](https://www.graalvm.org/latest/docs/getting-started/)

### Build jar

```shell
./gradlew build
```

## Javadoc

For the latest javadocs for the `main` branch, run `./gradlew javadoc` and open
the document under the `hildr-node/build/docs/javadoc/index.html` in your browser.

## Testing

```
./gradlew test
```

## Running from docker image

Next copy `.env.default` to `.env`
```sh
cd docker/
cp .env.default .env
```

In the `.env` file, modify the `L1_RPC_URL` field to contain a valid Ethereum RPC. For the Optimism and Base testnets, this must be a Goerli RPC URL. This RPC can either be from a local node, or a provider such as Alchemy or Infura.

By default, the `NETWORK` field in `.env` is `optimism-goerli`, however `base-goerli` is also supported.

Start the docker containers
```sh
docker compose up -d
```

The docker setup contains a Grafana dashboard. To view sync progress, you can check the dashboard at `http://localhost:3000` with the username `hildr` and password `passw0rd`. Alternatively, you can view Hildr's logs by running `docker logs hildr-node --follow`.

## Contribution
To help hildr grow, follow [Contributing to hildr](CONTRIBUTING.md).

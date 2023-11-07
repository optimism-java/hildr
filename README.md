[![sui4j CI](https://github.com/GrapeBaBa/hildr/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/GrapeBaBa/hildr/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Coverage](.github/badges/jacoco.svg)
![Branches](.github/badges/branches.svg)

# Hildr

This work is support by an [Optimism governance grant](https://app.charmverse.io/op-grants/proposals?id=e5613e76-a26f-41e4-9f0d-4e2dbfccf5b8).

Hildr is an OP Stack services written in the Java 21 with GraalVM native.

Follow the [spec](https://github.com/ethereum-optimism/optimism/blob/develop/specs/rollup-node.md)

- [Rust](https://github.com/a16z/magi)
- [Go](https://github.com/ethereum-optimism/optimism/tree/develop/op-node)

## Build

### Prerequisites
- Install [GraalVM latest version](https://www.graalvm.org/latest/docs/getting-started/)

### Build jar

```shell
./gradlew build
```

### Build native

```shell
# use graalvm native plugin
./gradlew nativeCompile

# use native-image directly
./gradlew buildBinary

# build static binary
./gradlew buildBinaryStatic
```

## Javadoc

For the latest javadocs for the `main` branch, run `./gradlew javadoc` and open
the document under the `hildr-node/build/docs/javadoc/index.html` in your browser.

## Testing

```
./gradlew test
```

## Running

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

# async-stream-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fasync--stream--kotlin-blue.svg)](https://github.com/KotlinMania/async-stream-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/async-stream-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/async-stream-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/async-stream-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/async-stream-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`tokio-rs/async-stream`](https://github.com/tokio-rs/async-stream).

**Original Project:** This port is based on [`tokio-rs/async-stream`](https://github.com/tokio-rs/async-stream). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `tokio-rs/async-stream`

> The text below is reproduced and lightly edited from [`https://github.com/tokio-rs/async-stream`](https://github.com/tokio-rs/async-stream). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## Asynchronous streams for Rust

Asynchronous stream of elements.

Provides two macros, `stream!` and `try_stream!`, allowing the caller to
define asynchronous streams of elements. These are implemented using `async`
& `await` notation. This crate works without unstable features.

The `stream!` macro returns an anonymous type implementing the [`Stream`]
trait. The `Item` associated type is the type of the values yielded from the
stream. The `try_stream!` also returns an anonymous type implementing the
[`Stream`] trait, but the `Item` associated type is `Result<T, Error>`. The
`try_stream!` macro supports using `?` notation as part of the
implementation.

## Usage

A basic stream yielding numbers. Values are yielded using the `yield`
keyword. The stream block must return `()`.

```rust
use async_stream::stream;

use futures_util::pin_mut;
use futures_util::stream::StreamExt;

#[tokio::main]
async fn main() {
    let s = stream! {
        for i in 0..3 {
            yield i;
        }
    };

    pin_mut!(s); // needed for iteration

    while let Some(value) = s.next().await {
        println!("got {}", value);
    }
}
```

Streams may be returned by using `impl Stream<Item = T>`:

```rust
use async_stream::stream;

use futures_core::stream::Stream;
use futures_util::pin_mut;
use futures_util::stream::StreamExt;

fn zero_to_three() -> impl Stream<Item = u32> {
    stream! {
        for i in 0..3 {
            yield i;
        }
    }
}

#[tokio::main]
async fn main() {
    let s = zero_to_three();
    pin_mut!(s); // needed for iteration

    while let Some(value) = s.next().await {
        println!("got {}", value);
    }
}
```

Streams may be implemented in terms of other streams - `async-stream` provides `for await`
syntax to assist with this:

```rust
use async_stream::stream;

use futures_core::stream::Stream;
use futures_util::pin_mut;
use futures_util::stream::StreamExt;

fn zero_to_three() -> impl Stream<Item = u32> {
    stream! {
        for i in 0..3 {
            yield i;
        }
    }
}

fn double<S: Stream<Item = u32>>(input: S)
    -> impl Stream<Item = u32>
{
    stream! {
        for await value in input {
            yield value * 2;
        }
    }
}

#[tokio::main]
async fn main() {
    let s = double(zero_to_three());
    pin_mut!(s); // needed for iteration

    while let Some(value) = s.next().await {
        println!("got {}", value);
    }
}
```

Rust try notation (`?`) can be used with the `try_stream!` macro. The `Item`
of the returned stream is `Result` with `Ok` being the value yielded and
`Err` the error type returned by `?`.

```rust
use tokio::net::{TcpListener, TcpStream};

use async_stream::try_stream;
use futures_core::stream::Stream;

use std::io;
use std::net::SocketAddr;

fn bind_and_accept(addr: SocketAddr)
    -> impl Stream<Item = io::Result<TcpStream>>
{
    try_stream! {
        let mut listener = TcpListener::bind(addr).await?;

        loop {
            let (stream, addr) = listener.accept().await?;
            println!("received on {:?}", addr);
            yield stream;
        }
    }
}
```

## Implementation

The `stream!` and `try_stream!` macros are implemented using proc macros.
The macro searches the syntax tree for instances of `yield $expr` and
transforms them into `sender.send($expr).await`.

The stream uses a lightweight sender to send values from the stream
implementation to the caller. When entering the stream, an `Option<T>` is
stored on the stack. A pointer to the cell is stored in a thread local and
`poll` is called on the async block. When `poll` returns.
`sender.send(value)` stores the value that cell and yields back to the
caller.

[`Stream`]: https://docs.rs/futures-core/*/futures_core/stream/trait.Stream.html

## Supported Rust Versions

The current minimum supported Rust version is 1.65.

## License

This project is licensed under the [MIT license](https://github.com/tokio-rs/async-stream/blob/HEAD/LICENSE).

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in `async-stream` by you, shall be licensed as MIT, without any
additional terms or conditions.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:async-stream-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`tokio-rs/async-stream`](https://github.com/tokio-rs/async-stream). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the async-stream authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`tokio-rs/async-stream`](https://github.com/tokio-rs/async-stream) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.

# features/net/

Optional native HTTP/2 + HTTP/3 stack if you outgrow OkHttp on the Java side.
Recommended: **nghttp2** + **ngtcp2** over **mbedTLS**. Lets the C layer own
streaming + the stream-vs-download decision entirely.

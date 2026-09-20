package cn.devicebase.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A local HTTP server that records what it was sent, so a test can assert on the
 * method, path, query, headers and body of each call.
 *
 * <p>Uses the JDK's built-in {@code com.sun.net.httpserver}, so the SDK's tests
 * need no HTTP mocking dependency.</p>
 */
final class RecordingServer implements AutoCloseable {

    /** One request the server received. */
    static final class Recorded {
        final String method;
        final String path;
        final String query;
        final String body;
        final String authorization;

        Recorded(String method, String path, String query, String body, String authorization) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.body = body;
            this.authorization = authorization;
        }
    }

    private final HttpServer server;
    private final List<Recorded> requests = new CopyOnWriteArrayList<>();

    private volatile int status = 200;
    private volatile String responseBody = "{\"code\":200,\"message\":\"success\",\"data\":{}}";
    private volatile byte[] responseBytes;

    RecordingServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.server.createContext("/", this::handle);
        this.server.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
        String body;
        try (InputStream in = exchange.getRequestBody()) {
            body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        requests.add(new Recorded(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getRawPath(),
                exchange.getRequestURI().getQuery(),
                body,
                exchange.getRequestHeaders().getFirst("Authorization")
        ));

        byte[] payload = responseBytes != null
                ? responseBytes
                : responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, payload.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(payload);
        }
    }

    /** Returns the base URL to point a client at. */
    String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    /** Returns the most recent request. */
    Recorded last() {
        if (requests.isEmpty()) {
            throw new AssertionError("no request was sent");
        }
        return requests.get(requests.size() - 1);
    }

    void respondWith(int statusCode, String body) {
        this.status = statusCode;
        this.responseBody = body;
        this.responseBytes = null;
    }

    void respondWithBytes(int statusCode, byte[] body) {
        this.status = statusCode;
        this.responseBytes = body;
    }

    /** Reads a query parameter from the last request, or null. */
    static String queryParam(Recorded request, String name) {
        if (request.query == null) {
            return null;
        }
        for (String pair : request.query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }

    /** Returns the recorded requests as a map of query parameter to value. */
    static Map<String, String> queryParams(Recorded request) {
        Map<String, String> params = new java.util.LinkedHashMap<>();
        if (request.query == null) {
            return params;
        }
        for (String pair : request.query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                params.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return params;
    }

    @Override
    public void close() {
        server.stop(0);
    }
}

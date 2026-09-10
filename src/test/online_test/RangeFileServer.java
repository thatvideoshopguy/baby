import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Static file server for the CheerpJ browser build, run by the "serve" profile in pom.xml
 * as {@code java RangeFileServer.java <port> <directory>} (single-file source launch, JDK 11+).
 *
 * The JDK's own jwebserver would do this in one line, but it answers every request with the
 * whole file, and CheerpJ refuses to start without the Range header ("HTTP server does not
 * support the 'Range' header. CheerpJ cannot run."), as it reads the jar a piece at a time.
 * So this serves 206 partial responses, which is the only reason it exists.
 */
public class RangeFileServer {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        Path root = Paths.get(args[1]).toAbsolutePath().normalize();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> {
            try {
                handle(exchange, root);
            } finally {
                exchange.close();
            }
        });
        server.start();

        String url = "http://localhost:" + port;
        System.out.println("Serving " + root + " on " + url + " (Ctrl-C to stop)");
        openBrowser(url);
    }

    /** Opens the default browser, on whichever OS this is. Just prints the URL if it can't. */
    private static void openBrowser(String url) {
        try {
            if (!GraphicsEnvironment.isHeadless()
                    && Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            // no browser to open, so leave the printed URL for the user to click
        }
    }

    private static void handle(HttpExchange exchange, Path root) throws IOException {
        String method = exchange.getRequestMethod();
        if (!method.equals("GET") && !method.equals("HEAD")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String requested = exchange.getRequestURI().getPath();
        if (requested.endsWith("/")) {
            requested += "index.html";
        }
        Path file = root.resolve(requested.substring(1)).normalize();

        // don't serve anything outside the directory we were pointed at
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        long size = Files.size(file);
        exchange.getResponseHeaders().set("Content-Type", contentType(file));
        exchange.getResponseHeaders().set("Accept-Ranges", "bytes");

        long[] range = parseRange(exchange.getRequestHeaders().getFirst("Range"), size);
        if (range == null) {
            exchange.sendResponseHeaders(200, method.equals("HEAD") ? -1 : size);
            if (!method.equals("HEAD")) {
                Files.copy(file, exchange.getResponseBody());
            }
            return;
        }

        long from = range[0];
        long length = range[1] - range[0] + 1;
        exchange.getResponseHeaders().set("Content-Range", "bytes " + from + "-" + range[1] + "/" + size);
        exchange.sendResponseHeaders(206, method.equals("HEAD") ? -1 : length);
        if (method.equals("HEAD")) {
            return;
        }

        // parseRange has clamped the range to the file, so one transferTo covers it
        try (FileChannel in = FileChannel.open(file)) {
            in.transferTo(from, length, Channels.newChannel(exchange.getResponseBody()));
        }
    }

    /**
     * Parses a single "bytes=" range, the only form a browser sends for this.
     * Returns {first, last} inclusive, or null to send the whole file instead.
     */
    private static long[] parseRange(String header, long size) {
        if (header == null || !header.startsWith("bytes=") || header.indexOf(',') >= 0) {
            return null;
        }
        String spec = header.substring("bytes=".length()).trim();
        int dash = spec.indexOf('-');
        if (dash < 0) {
            return null;
        }
        try {
            String start = spec.substring(0, dash).trim();
            String end = spec.substring(dash + 1).trim();
            long first;
            long last;
            if (start.isEmpty()) {
                // "bytes=-500" is the last 500 bytes
                long suffix = Long.parseLong(end);
                first = Math.max(0, size - suffix);
                last = size - 1;
            } else {
                first = Long.parseLong(start);
                last = end.isEmpty() ? size - 1 : Math.min(Long.parseLong(end), size - 1);
            }
            return (first > last || first >= size) ? null : new long[]{first, last};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String contentType(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".js")) return "text/javascript";
        if (name.endsWith(".css")) return "text/css";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".ico")) return "image/vnd.microsoft.icon";
        if (name.endsWith(".jar")) return "application/java-archive";
        return "application/octet-stream";
    }
}

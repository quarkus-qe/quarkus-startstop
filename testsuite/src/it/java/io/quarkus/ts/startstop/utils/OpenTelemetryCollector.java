package io.quarkus.ts.startstop.utils;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.grpc.common.GrpcMessage;
import io.vertx.grpc.server.GrpcServer;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This simplistic collector allows us to test Vert.x-based traces exporter in Quarkus without starting a container.
 */
public class OpenTelemetryCollector implements UnitTestResource {

    private static final String HELLO_ENDPOINT_OPERATION_NAME = "GET /hello";
    private static final String GET_HELLO_INVOCATION_NOT_TRACED = HELLO_ENDPOINT_OPERATION_NAME + " invocation not traced";
    /**
     * If you change this port, you must also change respective 'quarkus.otel.exporter.otlp.traces.endpoint' value.
     */
    private static final int OTEL_COLLECTOR_PORT = 4317;
    private static final String GET_HELLO_TRACES_PATH = "/recorded-traces/get-hello";
    static final String GET_HELLO_TRACES_URL = "http://localhost:" + OTEL_COLLECTOR_PORT + GET_HELLO_TRACES_PATH;
    static final String GET_HELLO_INVOCATION_TRACED = HELLO_ENDPOINT_OPERATION_NAME + " invocation traced";

    private final Closeable closeable;
    private final RequestHandler requestHandler;


    public OpenTelemetryCollector() {
        this.closeable = createGrpcServer();
        this.requestHandler = new RequestHandler();
    }

    private Closeable createGrpcServer() {
        Vertx vertx = Vertx.vertx();
        GrpcServer grpcProxy = GrpcServer.server(vertx);

        // record incoming traces
        grpcProxy.callHandler(reqFromQuarkus -> reqFromQuarkus.messageHandler(requestHandler::onReceivedTraces));

        HttpServer httpServer = vertx.createHttpServer(new HttpServerOptions().setPort(OTEL_COLLECTOR_PORT));
        httpServer.requestHandler(httpServerRequest -> {
            if (httpServerRequest.path().contains(GET_HELLO_TRACES_PATH)) {
                requestHandler.handleTracesRequest(httpServerRequest);
            } else {
                grpcProxy.handle(httpServerRequest);
            }
        }).listen();

        // close resources
        return () -> {
            httpServer.close().toCompletionStage().toCompletableFuture().join();
            vertx.close().toCompletionStage().toCompletableFuture().join();
        };
    }

    @Override
    public void close() throws IOException {
        closeable.close();
    }

    @Override
    public void reset() {
        requestHandler.resetTraces();
    }

    private static class RequestHandler {

        private final AtomicBoolean helloEndpointCallTraced = new AtomicBoolean(false);

        private void handleTracesRequest(HttpServerRequest request) {
            final String response;
            if (helloEndpointCallTraced.get()) {
                response = GET_HELLO_INVOCATION_TRACED;
            } else {
                response = GET_HELLO_INVOCATION_NOT_TRACED;
            }
            request.response().end(response);
        }

        private void onReceivedTraces(GrpcMessage exportedTraces) {
            if (!helloEndpointCallTraced.get() && helloEndpointCallTraced(exportedTraces)) {
                helloEndpointCallTraced.set(true);
            }
        }

        private void resetTraces() {
            helloEndpointCallTraced.set(false);
        }

        private static boolean helloEndpointCallTraced(GrpcMessage msgFromQuarkus) {
            return msgFromQuarkus.payload().toString().contains(HELLO_ENDPOINT_OPERATION_NAME);
        }
    }
}

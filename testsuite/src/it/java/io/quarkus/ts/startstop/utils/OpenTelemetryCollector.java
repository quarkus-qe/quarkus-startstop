package io.quarkus.ts.startstop.utils;

import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.Span;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import org.jboss.logging.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This simplistic collector allows us to test Vert.x-based traces exporter in Quarkus without starting a container.
 */
public class OpenTelemetryCollector implements UnitTestResource {
    private static final Logger LOGGER = Logger.getLogger(OpenTelemetryCollector.class.getName());
    private static final String HELLO_ENDPOINT_OPERATION_NAME = "GET /hello";
    private static final String GET_HELLO_INVOCATION_NOT_TRACED = HELLO_ENDPOINT_OPERATION_NAME + " invocation not traced";
    /**
     * If you change this port, you must also change respective 'quarkus.otel.exporter.otlp.traces.endpoint' value.
     */
    private static final int OTEL_COLLECTOR_PORT = 4317;
    private static final int WEB_ENDPOINT = 16686; // Can be changed to anything, currently is the same as Jaeger.
    private static final String GET_HELLO_TRACES_PATH = "/recorded-traces/get-hello";
    static final String GET_HELLO_INVOCATION_TRACED = HELLO_ENDPOINT_OPERATION_NAME + " invocation traced";
    static final String GET_HELLO_TRACES_URL = "http://localhost:" + WEB_ENDPOINT + GET_HELLO_TRACES_PATH;

    private final Vertx vertx;
    private final Closeable backEnd;
    private final Closeable frontEnd;
    private final AtomicBoolean helloEndpointCallTraced = new AtomicBoolean(false);


    public OpenTelemetryCollector() {
        vertx = Vertx.vertx();
        this.backEnd = new GRPCTraceHandler(vertx);
        this.frontEnd = new FrontEnd(vertx);
    }

    @Override
    public void close() throws IOException {
        frontEnd.close();
        backEnd.close();
        vertx.close().toCompletionStage().toCompletableFuture().join();
    }

    @Override
    public void reset() {
        helloEndpointCallTraced.set(false);
    }

    private class FrontEnd implements Closeable {
        private final HttpServer httpServer;

        public FrontEnd(Vertx vertx) {
            httpServer = vertx
                    .createHttpServer()
                    .requestHandler(this::handleTracesRequest);
            httpServer.listen(WEB_ENDPOINT);
        }

        private void handleTracesRequest(HttpServerRequest request) {
            final String response;
            boolean isTraced = helloEndpointCallTraced.get();
            if (isTraced) {
                response = GET_HELLO_INVOCATION_TRACED;
            } else {
                response = GET_HELLO_INVOCATION_NOT_TRACED;
            }
            request.response().end(response);
        }

        @Override
        public void close() {
            LOGGER.info("Closing the server");
            httpServer.close().toCompletionStage().toCompletableFuture().join();
            LOGGER.info("The server was closed");
        }
    }

    class GRPCTraceHandler implements Closeable {
        private final HttpServer httpServer;

        public GRPCTraceHandler(Vertx vertx) {
            GrpcServer grpcHandler = GrpcServer.server(vertx);

            // record incoming traces
            grpcHandler.callHandler(TraceServiceGrpc.getExportMethod(), request -> {
                // https://vertx.io/docs/vertx-grpc/java/#_streaming_request, because Quarkus uses streaming since 3.13
                request.handler((ExportTraceServiceRequest tracesRequest) -> {
                    LOGGER.info("Processing traces");
                    List<String> traces = tracesRequest.getResourceSpansList().stream()
                            .flatMap(resourceSpans -> resourceSpans.getScopeSpansList().stream())
                            .flatMap(scopeSpans -> scopeSpans.getSpansList().stream())
                            .map(Span::getName)
                            .toList();

                    for (String trace : traces) {
                        if (trace.contains(HELLO_ENDPOINT_OPERATION_NAME)) {
                            LOGGER.info("Received trace for " + HELLO_ENDPOINT_OPERATION_NAME);
                            helloEndpointCallTraced.compareAndSet(false, true);
                        }
                    }
                });
                request.endHandler(v -> {
                    // https://opentelemetry.io/docs/specs/otlp/#full-success
                    request.response().end(ExportTraceServiceResponse.newBuilder().build());
                });
                request.exceptionHandler(err -> { // https://opentelemetry.io/docs/specs/otlp/#failures
                    request.response().status(GrpcStatus.INVALID_ARGUMENT).end();
                });
            });
            httpServer = vertx
                    .createHttpServer()
                    .requestHandler(grpcHandler);
            httpServer.listen(OTEL_COLLECTOR_PORT);
            LOGGER.info("The listener started!");
        }

        @Override
        public void close() {
            LOGGER.info("Closing the listener");
            httpServer.close().toCompletionStage().toCompletableFuture().join();
            LOGGER.info("The listener was closed");
        }
    }
}

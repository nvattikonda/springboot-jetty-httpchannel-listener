package com.nv.jetty.server.httpchannel;

import com.nv.jetty.server.httpchannel.diagnostic.DiagnosticTag;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Component
@ConditionalOnWebApplication
@Slf4j
@ConditionalOnProperty(value = "jetty.httpchannel.listener", havingValue = "true", matchIfMissing = false)
public class DefaultListenerImpl {

    @Value("${request.parsingTimeInMillis:5000}")
    private long requestParsingTimeInMillis;
    @Value("${response.DispatchTimeInMillis:5000}")
    private long responseDispatchTimeInMillis;
    private QueuedThreadPool queuedThreadPool;
    private final String DIAGNOSTIC_CONTAINER = "diagnosticContainer";
    private AtomicInteger requestCounter = new AtomicInteger();

    @Bean
    public JettyServerCustomizer httpChannelListener() {
        return server -> {
            ThreadPool threadPool = server.getThreadPool();
            if (threadPool instanceof QueuedThreadPool) {
                queuedThreadPool = (QueuedThreadPool) threadPool;
            } else {
                log.warn("QueuedThreadPool is not leveraged, ThreadPool:{} is leveraged", threadPool);
            }
            Arrays.stream(server.getConnectors()).filter(connector -> {
                if (connector instanceof ServerConnector)
                    return true;
                return false;
            }).
                    forEach(connector -> {
                        connector.addBean(new HttpChannel.Listener() {

                            @Override
                            public void onRequestBegin(Request request) {
                                requestCounter.incrementAndGet();
                                captureDiagnosticData(request, DiagnosticTag.RequestBegin);
                                logRequestQueuing();
                            }

                            @Override
                            public void onBeforeDispatch(Request request) {
                                captureDiagnosticData(request, DiagnosticTag.BeforeDispatch);
                            }

                            @Override
                            public void onRequestContent(Request request, ByteBuffer content) {
                                captureDiagnosticData(request, DiagnosticTag.RequestContent);
                            }

                            @Override
                            public void onRequestContentEnd(Request request) {
                                captureDiagnosticData(request, DiagnosticTag.RequestContentEnd);
                            }

                            @Override
                            public void onRequestEnd(Request request) {
                                captureDiagnosticData(request, DiagnosticTag.RequestEnd);
                                logConditionally(request, DiagnosticTag.RequestEnd);
                            }

                            @Override
                            public void onResponseBegin(Request request) {
                                captureDiagnosticData(request, DiagnosticTag.ResponseBegin);
                            }

                            @Override
                            public void onResponseCommit(Request request) {
                                captureDiagnosticData(request, DiagnosticTag.ResponseCommit);
                            }

                            @Override
                            public void onResponseContent(Request request, ByteBuffer content) {
                                captureDiagnosticData(request, DiagnosticTag.RequestContent);
                            }

                            @Override
                            public void onResponseEnd(Request request) {
                                captureDiagnosticData(request, DiagnosticTag.ResponseEnd);
                                logConditionally(request, DiagnosticTag.ResponseEnd);
                            }

                            @Override
                            public void onComplete(Request request) {
                                requestCounter.decrementAndGet();
                            }
                        });
                    });
        };
    }

    private void logConditionally(Request request, DiagnosticTag diagnosticTag) {
        Map<String, String> diagnosticContainer = (HashMap) request.getAttribute(DIAGNOSTIC_CONTAINER);
        if (diagnosticContainer != null) {
            try {
                switch (diagnosticTag) {
                    case RequestEnd:
                        if (Long.valueOf(diagnosticContainer.get(diagnosticTag.name())) - Long.valueOf(diagnosticContainer.get(DiagnosticTag.RequestBegin.name())) > requestParsingTimeInMillis)
                            log(request, new DiagnosticTag[]{DiagnosticTag.RequestBegin, DiagnosticTag.BeforeDispatch, DiagnosticTag.RequestContent, DiagnosticTag.RequestContentEnd, DiagnosticTag.RequestEnd});
                        break;
                    case ResponseEnd:
                        if (Long.valueOf(diagnosticContainer.get(diagnosticTag.name())) - Long.valueOf(diagnosticContainer.get(DiagnosticTag.ResponseEnd.name())) > responseDispatchTimeInMillis)
                            log(request, new DiagnosticTag[]{DiagnosticTag.ResponseBegin, DiagnosticTag.ResponseCommit, DiagnosticTag.ResponseContent, DiagnosticTag.ResponseEnd});
                        break;
                    default:
                        log.warn("DiagnosticTag:{} is not configured", diagnosticTag != null ? diagnosticTag.name() : "NULL");
                        break;
                }
            } catch (NumberFormatException e) {
                log.error("Encountered Error Processing DiagnosticTag:" + diagnosticTag != null ? diagnosticTag.name() : "NULL", e);
            }
        }
    }

    private void log(Request request, DiagnosticTag diagnosticTags[]) {
        try {
            if (request != null) {
                StringBuilder stringBuilder = new StringBuilder();
                Enumeration<String> headers = request.getHeaderNames();
                while (headers.hasMoreElements()) {
                    String header = headers.nextElement();
                    stringBuilder.append(header).append(":").append(request.getHeader(header));
                    if (headers.hasMoreElements())
                        stringBuilder.append("~");
                }
                if (diagnosticTags != null) {
                    stringBuilder.append("|");
                    Map<String, String> diagnosticContainer = (HashMap) request.getAttribute(DIAGNOSTIC_CONTAINER);
                    for (int i = 0; i < diagnosticTags.length; i++) {
                        stringBuilder.append(diagnosticTags[i]).append(":").append(diagnosticContainer.get(diagnosticTags[i].name()));
                        if (i < (diagnosticTags.length - 1))
                            stringBuilder.append("~");
                    }
                }
                log.info(stringBuilder.toString());
            }
        } catch (Exception e) {
            log.error("Encountered Error Processing DiagnosticTags:" + diagnosticTags != null ? Arrays.toString(diagnosticTags) : "NULL", e);
        }
    }

    private void captureDiagnosticData(Request request, DiagnosticTag diagnosticTag) {
        //In future, can also capture request/response content chunks if needed via Object
        Map<String, Object> diagnosticContainer = (Map) request.getAttribute(DIAGNOSTIC_CONTAINER);
        try {
            if (diagnosticContainer != null) {
                diagnosticContainer.putIfAbsent(diagnosticTag.name(), String.valueOf(Instant.now().toEpochMilli()));
            } else {
                diagnosticContainer = new HashMap();
                request.setAttribute(DIAGNOSTIC_CONTAINER, diagnosticContainer);
                diagnosticContainer.putIfAbsent(diagnosticTag.name(), String.valueOf(Instant.now().toEpochMilli()));
            }
        } catch (Exception e) {
            log.error("Encountered Error Capturing DiagnosticData for DiagnosticTag:" + diagnosticTag != null ? diagnosticTag.name() : "NULL", e);
        }
    }

    private void logRequestQueuing() {
        try {
            if (queuedThreadPool != null && requestCounter.intValue() > queuedThreadPool.getMaxThreads()) {
                log.warn("Requests getting queued, requestCount:{},configuredMaxThreads:{},queueSize:{}", requestCounter.intValue(), queuedThreadPool.getMaxThreads(), queuedThreadPool.getQueueSize());
            }
        } catch (Exception e) {
            log.error("Encountered Error Comparing server requestCounter with maxThreads", e);
        }
    }
}

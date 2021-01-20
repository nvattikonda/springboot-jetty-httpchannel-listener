package com.nv.jetty.server.httpchannel.diagnostic;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component("nv.diagnosticsManager")
@ConditionalOnWebApplication
@ConditionalOnProperty(value = "nv.jetty.httpchannel.diagnostics", havingValue = "true", matchIfMissing = false)
public class DiagnosticsManager {
    @Value("${nv.jetty.request.parsingTimeThresholdInMillis:5000}")
    private long requestParsingThresholdTimeInMillis;
    @Value("${nv.jetty.response.dispatchTimeThresholdInMillis:5000}")
    private long responseDispatchThresholdTimeInMillis;
    @Value("${nv.jetty.response.responseBeginThresholdInMillis:5000}")
    private long responseBeginThresholdInMillis;

    private QueuedThreadPool queuedThreadPool;
    private final String DIAGNOSTIC_CONTAINER = "diagnosticContainer";
    private final String REQUEST_URI = "requestURI";
    private AtomicInteger requestCounter = new AtomicInteger();
    private Logger logger = LoggerFactory.getLogger(DiagnosticsManager.class);

    public void setQueuedThreadPool(QueuedThreadPool queuedThreadPool) {
        this.queuedThreadPool = queuedThreadPool;
    }

    public void logConditionally(Request request, DiagnosticTag diagnosticTag) {
        Map<String, String> diagnosticContainer = (HashMap) request.getAttribute(DIAGNOSTIC_CONTAINER);
        if (diagnosticContainer != null) {
            try {
                switch (diagnosticTag) {
                    case RequestEnd:
                        if (Long.valueOf(diagnosticContainer.get(diagnosticTag.name())) - Long.valueOf(diagnosticContainer.get(DiagnosticTag.RequestBegin.name())) > requestParsingThresholdTimeInMillis)
                            log(request, new DiagnosticTag[]{DiagnosticTag.RequestBegin, DiagnosticTag.BeforeDispatch, DiagnosticTag.RequestContent, DiagnosticTag.RequestContentEnd, DiagnosticTag.RequestEnd});
                        break;
                    case ResponseBegin:
                        if (Long.valueOf(diagnosticContainer.get(diagnosticTag.name())) - Long.valueOf(diagnosticContainer.get(DiagnosticTag.RequestEnd.name())) > responseBeginThresholdInMillis)
                            log(request, new DiagnosticTag[]{DiagnosticTag.RequestEnd, DiagnosticTag.ResponseBegin});
                        break;
                    case ResponseEnd:
                        if (Long.valueOf(diagnosticContainer.get(diagnosticTag.name())) - Long.valueOf(diagnosticContainer.get(DiagnosticTag.ResponseBegin.name())) > responseDispatchThresholdTimeInMillis)
                            log(request, new DiagnosticTag[]{DiagnosticTag.ResponseBegin, DiagnosticTag.ResponseCommit, DiagnosticTag.ResponseContent, DiagnosticTag.ResponseEnd});
                        break;
                    default:
                        logger.warn("DiagnosticTag:{} is not configured", diagnosticTag != null ? diagnosticTag.name() : "NULL");
                        break;
                }
            } catch (NumberFormatException e) {
                logger.error("Encountered Error Processing DiagnosticTag:" + diagnosticTag != null ? diagnosticTag.name() : "NULL", e);
            }
        }
    }

    public void log(Request request, DiagnosticTag diagnosticTags[]) {
        try {
            if (request != null) {
                StringBuilder logBuilder = new StringBuilder();
                logBuilder.append(REQUEST_URI).append(":").append(request.getRequestURI()).append("~");
                logBuilder.append(getHeadersAsString(request));
                if (diagnosticTags != null) {
                    logBuilder.append("|");
                    Map<String, Object> diagnosticContainer = (HashMap) request.getAttribute(DIAGNOSTIC_CONTAINER);
                    for (int i = 0; i < diagnosticTags.length; i++) {
                        //TODO In future, when request/response content chunks are captured below code needs to be updated
                        logBuilder.append(diagnosticTags[i]).append(":").append(diagnosticContainer.get(diagnosticTags[i].name()));
                        if (i < (diagnosticTags.length - 1))
                            logBuilder.append("~");
                    }
                }
                logger.warn(logBuilder.toString());
            }
        } catch (Exception e) {
            logger.error("Encountered Error Processing DiagnosticTags:" + diagnosticTags != null ? Arrays.toString(diagnosticTags) : "NULL", e);
        }
    }

    public void captureDiagnosticData(Request request, DiagnosticTag diagnosticTag) {
        //TODO In future, can also capture request/response content chunks if needed via Object
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
            logger.error("Encountered Error Capturing DiagnosticData for DiagnosticTag:" + diagnosticTag != null ? diagnosticTag.name() : "NULL", e);
        }
    }

    private void logRequestQueuing(Request request, int currentRequestCount) {
        try {
            if (queuedThreadPool != null && currentRequestCount > queuedThreadPool.getMaxThreads()) {
                logger.warn("Request getting queued, requestURI:{},currentRequestProcessingCount:{},configuredMaxThreads:{},threadPoolQueueSize:{}, requestHeaders:{}", request.getRequestURI(), currentRequestCount, queuedThreadPool.getMaxThreads(), queuedThreadPool.getQueueSize(), getHeadersAsString(request));
            }
        } catch (Exception e) {
            logger.error("Encountered Error Comparing server requestCounter with maxThreads", e);
        }
    }

    private String getHeadersAsString(Request request) {
        StringBuilder requestHeaderBuilder = new StringBuilder();
        if (request != null) {
            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                requestHeaderBuilder.append(header).append(":").append(request.getHeader(header));
                if (headers.hasMoreElements())
                    requestHeaderBuilder.append("~");
            }
        }
        return requestHeaderBuilder.toString();
    }

    public int incrementRequestCounter(Request request) {
        int currentRequestCount = requestCounter.incrementAndGet();
        logRequestQueuing(request, currentRequestCount);
        return currentRequestCount;
    }

    public int decrementRequestCounter(Request request) {
        return requestCounter.decrementAndGet();
    }
}

package com.nv.jetty.server.httpchannel.diagnostic;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
public class DiagnosticsManager {
    @Value("${request.parsingTimeInMillis:5000}")
    private long requestParsingTimeInMillis;
    @Value("${response.DispatchTimeInMillis:5000}")
    private long responseDispatchTimeInMillis;

    private QueuedThreadPool queuedThreadPool;
    private final String DIAGNOSTIC_CONTAINER = "diagnosticContainer";
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
                        if (Long.valueOf(diagnosticContainer.get(diagnosticTag.name())) - Long.valueOf(diagnosticContainer.get(DiagnosticTag.RequestBegin.name())) > requestParsingTimeInMillis)
                            log(request, new DiagnosticTag[]{DiagnosticTag.RequestBegin, DiagnosticTag.BeforeDispatch, DiagnosticTag.RequestContent, DiagnosticTag.RequestContentEnd, DiagnosticTag.RequestEnd});
                        break;
                    case ResponseEnd:
                        if (Long.valueOf(diagnosticContainer.get(diagnosticTag.name())) - Long.valueOf(diagnosticContainer.get(DiagnosticTag.ResponseEnd.name())) > responseDispatchTimeInMillis)
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
                logger.info(stringBuilder.toString());
            }
        } catch (Exception e) {
            logger.error("Encountered Error Processing DiagnosticTags:" + diagnosticTags != null ? Arrays.toString(diagnosticTags) : "NULL", e);
        }
    }

    public void captureDiagnosticData(Request request, DiagnosticTag diagnosticTag) {
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
            logger.error("Encountered Error Capturing DiagnosticData for DiagnosticTag:" + diagnosticTag != null ? diagnosticTag.name() : "NULL", e);
        }
    }

    private void logRequestQueuing(Request request) {
        try {
            if (queuedThreadPool != null && requestCounter.intValue() > queuedThreadPool.getMaxThreads()) {
                logger.warn("Requests getting queued, requestCount:{},configuredMaxThreads:{},queueSize:{}", requestCounter.intValue(), queuedThreadPool.getMaxThreads(), queuedThreadPool.getQueueSize());
            }
        } catch (Exception e) {
            logger.error("Encountered Error Comparing server requestCounter with maxThreads", e);
        }
    }

    public int incrementRequestCounter(Request request){
        requestCounter.incrementAndGet();
        logRequestQueuing(request);
        return requestCounter.intValue();
    }

    public int decrementRequestCounter(Request request){
        return requestCounter.decrementAndGet();
    }
}

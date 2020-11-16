package com.nv.jetty.server.httpchannel;

import com.nv.jetty.server.httpchannel.diagnostic.DiagnosticTag;
import com.nv.jetty.server.httpchannel.diagnostic.DiagnosticsManager;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;


@Component("nv.defaultHttpChannelListener")
@ConditionalOnWebApplication
@ConditionalOnProperty(value = "nv.jetty.httpchannel.listener", havingValue = "true", matchIfMissing = false)
public class DefaultHttpChannelListener implements HttpChannel.Listener {

    private DiagnosticsManager diagnosticsManager;

    @Autowired
    @Qualifier("nv.diagnosticsManager")
    public void setDiagnosticsManager(DiagnosticsManager diagnosticsManager) {
        this.diagnosticsManager = diagnosticsManager;
    }

    @Override
    public void onRequestBegin(Request request) {
        diagnosticsManager.incrementRequestCounter(request);
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.RequestBegin);
    }

    @Override
    public void onBeforeDispatch(Request request) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.BeforeDispatch);
    }

    @Override
    public void onRequestContent(Request request, ByteBuffer content) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.RequestContent);
    }

    @Override
    public void onRequestContentEnd(Request request) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.RequestContentEnd);
    }

    @Override
    public void onRequestEnd(Request request) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.RequestEnd);
        diagnosticsManager.logConditionally(request, DiagnosticTag.RequestEnd);
    }

    @Override
    public void onResponseBegin(Request request) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.ResponseBegin);
    }

    @Override
    public void onResponseCommit(Request request) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.ResponseCommit);
    }

    @Override
    public void onResponseContent(Request request, ByteBuffer content) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.RequestContent);
    }

    @Override
    public void onResponseEnd(Request request) {
        diagnosticsManager.captureDiagnosticData(request, DiagnosticTag.ResponseEnd);
        diagnosticsManager.logConditionally(request, DiagnosticTag.ResponseEnd);
    }

    @Override
    public void onComplete(Request request) {
        diagnosticsManager.decrementRequestCounter(request);
    }
}

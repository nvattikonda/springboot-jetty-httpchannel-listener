package com.nv.springboot.web.server;

import com.nv.jetty.server.httpchannel.diagnostic.DiagnosticsManager;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.eclipse.jetty.server.HttpChannel;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("nv.defaultWebServerFactoryCustomizer")
@ConditionalOnWebApplication
public class DefaultWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private Logger logger = LoggerFactory.getLogger(DefaultWebServerFactoryCustomizer.class);
    private HttpChannel.Listener httpChannelListener;
    private DiagnosticsManager diagnosticsManager;

    @Autowired
    @Qualifier("nv.defaultHttpChannelListener")
    public void setHttpChannelListener(HttpChannel.Listener httpChannelListener) {
        this.httpChannelListener = httpChannelListener;
    }

    @Autowired
    @Qualifier("nv.diagnosticsManager")
    public void setDiagnosticsManager(DiagnosticsManager diagnosticsManager) {
        this.diagnosticsManager = diagnosticsManager;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory configurableServletWebServerFactory) {
        if (configurableServletWebServerFactory != null && configurableServletWebServerFactory instanceof JettyServletWebServerFactory) {
            JettyServletWebServerFactory jettyServletWebServerFactory = (JettyServletWebServerFactory) configurableServletWebServerFactory;
            jettyServletWebServerFactory.addServerCustomizers(server -> {
                ThreadPool threadPool = server.getThreadPool();
                if (threadPool instanceof QueuedThreadPool) {
                    QueuedThreadPool queuedThreadPool = (QueuedThreadPool) threadPool;
                    diagnosticsManager.setQueuedThreadPool(queuedThreadPool);
                } else {
                    logger.warn("QueuedThreadPool is not leveraged, ThreadPool:{} is leveraged", threadPool);
                }
                Arrays.stream(server.getConnectors()).filter(connector -> {
                    if (connector instanceof ServerConnector)
                        return true;
                    return false;
                }).forEach(connector -> {
                    logger.info("connector:{}",connector);
                    connector.addBean(httpChannelListener);
                });
            });
        }
    }
}
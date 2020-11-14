package com.nv.springboot.web.server;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnWebApplication
public class DefaultWebServerFactoryCustomizerImpl implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory configurableServletWebServerFactory) {
        if (configurableServletWebServerFactory != null && configurableServletWebServerFactory instanceof JettyServletWebServerFactory) {
            JettyServletWebServerFactory jettyServletWebServerFactory = (JettyServletWebServerFactory) configurableServletWebServerFactory;
        }
    }

}
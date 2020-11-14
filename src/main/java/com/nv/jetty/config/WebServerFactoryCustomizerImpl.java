package com.nv.jetty.config;

import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebServerFactoryCustomizerImpl implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory configurableServletWebServerFactory) {
        if (configurableServletWebServerFactory != null && configurableServletWebServerFactory instanceof JettyServletWebServerFactory) {
            JettyServletWebServerFactory jettyServletWebServerFactory = (JettyServletWebServerFactory) configurableServletWebServerFactory;
        }
    }

}
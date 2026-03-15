package com.wms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    private String from;
    private String verifyBaseUrl;
    private String resetBaseUrl;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getVerifyBaseUrl() {
        return verifyBaseUrl;
    }

    public void setVerifyBaseUrl(String verifyBaseUrl) {
        this.verifyBaseUrl = verifyBaseUrl;
    }

    public String getResetBaseUrl() {
        return resetBaseUrl;
    }

    public void setResetBaseUrl(String resetBaseUrl) {
        this.resetBaseUrl = resetBaseUrl;
    }
}

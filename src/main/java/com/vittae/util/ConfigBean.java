package com.vittae.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("configBean")
@ApplicationScoped
public class ConfigBean {
	public String getApiBaseUrl() {
		return ConfigUtil.get("api.base.url");
	}
}
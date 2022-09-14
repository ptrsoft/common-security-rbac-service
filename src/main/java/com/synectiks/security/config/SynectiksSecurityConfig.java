/**
 * 
 */
package com.synectiks.security.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.synectiks.security.models.SecurityRule;
import com.synectiks.security.util.IUtils;

/**
 * @author Rajesh
 */
@Configuration
public class SynectiksSecurityConfig {

	private static final Logger logger = LoggerFactory.getLogger(SynectiksSecurityConfig.class);

	@Value("${synectiks.shiro.secure.urls}")
	private String secureUrls;
	@Value("${synectiks.shiro.public.urls}")
	private String publicUrls;

	/*@Bean
	public ShiroFilterChainDefinition shiroFilterChainDefinition() {
		DefaultShiroFilterChainDefinition filter = new DefaultShiroFilterChainDefinition();
		// String secureUrls = env.getProperty(IConsts.SECURE_URLS, "");
		// Add secure urls
		List<String> lst = IUtils.getListFromString(secureUrls, null);
		lst.forEach(item -> {
			filter.addPathDefinition(item, "rest");
		});
		// String publicUrls = env.getProperty(IConsts.PUBLIC_URLS, "");
		// add public urls
		lst = IUtils.getListFromString(publicUrls, null);
		lst.forEach(item -> {
			filter.addPathDefinition(item, "anon");
		});

		return filter;
	}*/

	/**
	 * Alternate properties loader to dynamically load properties into config
	 * @return
	 */
	public static Properties readPropertiesFile() {
		Properties prop = new Properties();

		try(InputStream input = SynectiksSecurityConfig.class.getClassLoader()
				.getResourceAsStream("application.properties")) {
			// load a properties file
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return prop;
	}

	/**
	 * Check initialize of the properties
	 */
	private void checkProps() {
		if (IUtils.isNull(secureUrls)) {
			Properties props = readPropertiesFile();
			if (!IUtils.isNull(props)) {
				secureUrls = props.getProperty("synectiks.shiro.secure.urls");
				publicUrls = props.getProperty("synectiks.shiro.public.urls");
			}
		}
	}

	@Bean
	public ShiroFilterFactoryBean shiroFilterFactoryBean() {
		checkProps();
		ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
		factoryBean.setSecurityManager(securityManager());

		Map<String, String> filters = new LinkedHashMap<String, String>();
		// Add secure urls
		if (IUtils.isNullOrEmpty(secureUrls)) {
			logger.info("Secure urls: " + secureUrls);
			for (String item : SecurityRule.secureUrls) {
				logger.info("Secure url: " + item);
				SecurityRule rule = IUtils.getObjectFromValue(item, SecurityRule.class);
				filters.put(rule.getKey(), rule.getValue());
			}
		} else {
			String[] lst = IUtils.getArrayFromJsonString(secureUrls);
			logger.info("Secure urls: " + lst);
			for (String item : lst) {
				logger.info("Secure url: " + item);
				SecurityRule rule = IUtils.getObjectFromValue(item, SecurityRule.class);
				filters.put(rule.getKey(), rule.getValue());
			}
		}
		// add public urls
		if (IUtils.isNullOrEmpty(publicUrls)) {
			logger.info("Public urls: " + secureUrls);
			for (String item : SecurityRule.publicUtls) {
				logger.info("Public url: " + item);
				filters.put(item, "anon");
			}
		} else {
			String[] lst = IUtils.getArrayFromJsonString(publicUrls);
			logger.info("Public urls: " + lst);
			for (String item : lst) {
				logger.info("Public url: " + item);
				filters.put(item, "anon");
			}
		}

		factoryBean.setFilterChainDefinitionMap(filters);
		return factoryBean;
	}

	@Bean
	public DefaultWebSecurityManager securityManager() {
		final DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(realm());
		return securityManager;
	}

	@Bean
	@DependsOn("lifecycleBeanPostProcessor")
	public DefaultAdvisorAutoProxyCreator proxyCreator() {
		return new DefaultAdvisorAutoProxyCreator();
	}

	@Bean
	@DependsOn("lifecycleBeanPostProcessor")
	public Realm realm() {
		SynectiksRealm realm = new SynectiksRealm();
		realm.setCredentialsMatcher(credentialsMatcher());
		return realm;
	}

	@Bean
	public PasswordMatcher credentialsMatcher() {
		final PasswordMatcher credentialsMatcher = new PasswordMatcher();
		credentialsMatcher.setPasswordService(passwordService());
		return credentialsMatcher;
	}

	@Bean
	public DefaultPasswordService passwordService() {
		return new DefaultPasswordService();
	}

	@Bean
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

}

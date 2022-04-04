package com.synectiks.security.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class TemplateReader {
	
	private static final Logger logger = LoggerFactory.getLogger(TemplateReader.class);
	
	private ResourceLoader resourceLoader;
	
	@Autowired
    public TemplateReader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
	
	public String readTemplate(String templateFile){
		logger.info("Reading template file");
		String fileData = null;
		try {
			try (InputStream inputStream = getClass().getResourceAsStream(templateFile);
				    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
				    fileData = reader.lines().collect(Collectors.joining(System.lineSeparator()));
			}
			return fileData;
		}catch(Exception e) {
			logger.error("Exception while reading template file. ",e);
		}
		return null;
	}
}

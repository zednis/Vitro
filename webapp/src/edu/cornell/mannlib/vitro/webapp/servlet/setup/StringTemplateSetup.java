package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.antlr.stringtemplate.StringTemplateGroup;

import edu.cornell.mannlib.vitro.webapp.template.stringtemplate.Page;
/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class StringTemplateSetup implements ServletContextListener {

	// Set default theme based on themes present on the file system
	public void contextInitialized(ServletContextEvent event) {

		ServletContext sc = event.getServletContext();	
		String templatePath = sc.getRealPath("templates/stringtemplates");
		StringTemplateGroup templates = new StringTemplateGroup("stGroup", templatePath); 
		templates.setRefreshInterval(0); // don't cache templates (change in production);
		Page.templates = templates;
	}

	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}
}


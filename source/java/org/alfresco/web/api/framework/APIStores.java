/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.api.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;


/**
 * API Store Access
 * 
 * @author davidc
 */
public class APIStores implements ApplicationContextAware, ApplicationListener
{
    private ApplicationContext applicationContext;
    private ProcessorLifecycle lifecycle = new ProcessorLifecycle();
    private APITemplateProcessor templateProcessor;
    private APIScriptProcessor scriptProcessor;

    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
        this.lifecycle.setApplicationContext(applicationContext);        
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        lifecycle.onApplicationEvent(event);
    }

    /**
     * Sets the template processor
     *  
     * @param templateProcessor
     */
    public void setTemplateProcessor(APITemplateProcessor templateProcessor)
    {
        this.templateProcessor = templateProcessor;
    }

    /**
     * Sets the script processor
     *  
     * @param scriptProcessor
     */
    public void setScriptProcessor(APIScriptProcessor scriptProcessor)
    {
        this.scriptProcessor = scriptProcessor;
    }
    
    /**
     * Gets all API Stores
     * 
     * @return  all API Stores
     */
    @SuppressWarnings("unchecked")
    public Collection<APIStore> getAPIStores()
    {
        return applicationContext.getBeansOfType(APIStore.class, false, false).values();
    }

    /**
     * Gets the Template Processor
     * 
     * @return  template processor
     */
    public APITemplateProcessor getTemplateProcessor()
    {
        return templateProcessor;
    }

    /**
     * Gets the Script Processor
     * 
     * @return  script processor
     */
    public APIScriptProcessor getScriptProcessor()
    {
        return scriptProcessor;
    }

    /**
     * Register template loader from each API Store with Template Processor 
     */
    protected void initTemplateProcessor()
    {        
        List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
        for (APIStore apiStore : getAPIStores())
        {
            TemplateLoader loader = apiStore.getTemplateLoader();
            if (loader == null)
            {
                throw new APIException("Unable to retrieve template loader for api store " + apiStore.getBasePath());
            }
            loaders.add(loader);
        }
        MultiTemplateLoader loader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[loaders.size()]));
        templateProcessor.setTemplateLoader(loader);
    }

    /**
     * Register script loader from each API Store with Script Processor
     */
    protected void initScriptProcessor()
    {
        List<ScriptLoader> loaders = new ArrayList<ScriptLoader>();
        for (APIStore apiStore : getAPIStores())
        {
            ScriptLoader loader = apiStore.getScriptLoader();
            if (loader == null)
            {
                throw new APIException("Unable to retrieve script loader for api store " + apiStore.getBasePath());
            }
            loaders.add(loader);
        }
        MultiScriptLoader loader = new MultiScriptLoader(loaders.toArray(new ScriptLoader[loaders.size()]));
        scriptProcessor.setScriptLoader(loader);
    }

    /**
     * Hooks into Spring Application Lifecycle
     */
    private class ProcessorLifecycle extends AbstractLifecycleBean
    {
        @Override
        protected void onBootstrap(ApplicationEvent event)
        {
            initTemplateProcessor();
            initScriptProcessor();
        }
    
        @Override
        protected void onShutdown(ApplicationEvent event)
        {
        }
    }
       
}
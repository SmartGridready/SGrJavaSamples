/*
 * Copyright(c) 2024 Verein SmartGridready Switzerland
 * 
 * This Open Source Software is BSD 3 clause licensed:
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.smartgridready.communicator.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartgridready.communicator.common.api.GenDeviceApi;
import com.smartgridready.communicator.common.api.SGrDeviceBuilder;
import com.smartgridready.communicator.common.api.values.Float64Value;
import com.smartgridready.communicator.example.helper.EidLoader;
import com.smartgridready.communicator.rest.exception.RestApiAuthenticationException;
import com.smartgridready.driver.api.common.GenDriverException;
import com.smartgridready.driver.api.http.GenHttpClientFactory;
import com.smartgridready.driver.api.http.GenHttpRequest;
import com.smartgridready.driver.api.http.GenHttpResponse;
import com.smartgridready.driver.api.http.GenUriBuilder;
import com.smartgridready.driver.api.http.HttpMethod;


/**
 * This is a sample implementation of communicator that uses the SmartGridready communication handler.
 * <p>
 * The communication handler uses the SmartGridready generic interface to communicate with any attached
 * device/product in a generic way. The communication handler converts the generic API commands to
 * commands understood by the external interface of the device/product.
 * <p>
 * The command translation is done by the communication handler based on a device description loaded from
 * a device description XML file.
 * <p>
 * There are several communication protocols/technologies available to communicate with the device:
 * <ul>
 * <li>Modbus RTU</li>
 * <li>Modbus TCP</li>
 * <li>http / REST Swagger</li>
 * </ul>
 * The communicator is responsible of loading the appropriate descriptions and parameters of the attached
 * devices/products.
 * <p>
 * The example shows the basic steps to set up the communicator to talk to a simple SmartGridready REST
 * device and read/writes a value from/to the device.
 * <p>
 * The example also uses the recommended <b>new</b> SGrDeviceBuilder method.
 */
public class RestSampleCommunicator
{
    private static final Logger LOG = LoggerFactory.getLogger(RestSampleCommunicator.class);

    /** This example is tied to this EID-XML. */
    private static final String DEVICE_DESCRIPTION_FILE_NAME = "SGr_01_mmmm_dddd_Shelly_TRV_RestAPILocal_V0.1.xml";
    /** This URI is not important, can have any value. */
    private static final String BASE_URI = "https://example.com/"; 
    
    public static void main(String[] args)
    {
        // This example uses a mocked REST driver factory to create the driver instance.
        // You may change the factory implementation or just use the default, in order to
        // create actual REST devices.
        
        Properties configProperties = new Properties();
        LOG.info("Configuring base_uri to '{}'.", BASE_URI);
        configProperties.setProperty("base_uri", BASE_URI);

        GenDeviceApi sgcpDevice;

        try
        {
            // Use the SGrDeviceBuilder class to load the device description (EID) from
            // an XML file, input stream or text content.
            // Use properties to replace configuration place holders in EID.
            // Create the SGr device instance by calling build().
            sgcpDevice = new SGrDeviceBuilder()
                // mandatory: inject device description (EID)
                .eid(EidLoader.getDeviceDescriptionInputStream(DEVICE_DESCRIPTION_FILE_NAME))
                // optional: inject the configuration according to the used EID (in this case required)
                .properties(configProperties)
                // optional: inject the REST mock (only for this example)
                .useRestServiceClientFactory(new RestClientFactory())
                .build();
        }
        catch (GenDriverException | RestApiAuthenticationException | IOException e)
        {
            LOG.error("Error loading device description. ", e);
            return;
        }
        
        try
        {
            // Connect the device instance. Initializes the attached transport.
            LOG.info("Connecting ...");
            sgcpDevice.connect();
        
            // Read the current temperature
            final var PROFILE_NAME = "Thermostat";
            final var temp = sgcpDevice.getVal(PROFILE_NAME, "Temperature");
            LOG.info("Current Temperature is '{}'", temp.getFloat64());
            
            // Change the target temperature 
            final var newTargetTemp = Float64Value.of(22.2);
            LOG.info("Writing TargetTemperature '{}'", newTargetTemp);
            sgcpDevice.setVal(PROFILE_NAME, "TargetTemperature", newTargetTemp);
            
            // Read the target temperature
            final var readTargetTemp = sgcpDevice.getVal(PROFILE_NAME, "TargetTemperature");
            LOG.info("Current TargetTemperature is now '{}'", readTargetTemp.getFloat64());
            
            // Read again the current temperature
            final var temp2 = sgcpDevice.getVal(PROFILE_NAME, "Temperature");
            LOG.info("Current Temperature is now '{}'", temp2.getFloat64());
        }
        catch (Exception e)
        {
            LOG.error("Error accessing device. ", e);
        }
        finally
        {
            // Disconnect from device instance. Closes the attached transport.
            if (sgcpDevice.isConnected())
            {
                try
                {
                    LOG.info("Disconnecting ...");
                    sgcpDevice.disconnect();
                }
                catch (GenDriverException e)
                {
                    LOG.error("Error disconnecting device.", e);
                }
            }
        }
    }

}


/**
 * Mock of a REST client for the EID-XML "SGr_01_mmmm_dddd_Shelly_TRV_RestAPILocal_V0.1.xml".
 */
class RestClientFactory implements GenHttpClientFactory
{
    private static final String INITIAL_TEMP = "25.5";
    /** fake temperature memory */
    private static String lastSetTargetTemperature = INITIAL_TEMP;

    @Override
    public GenHttpRequest createHttpRequest()
    {
        return createHttpRequest(true);
    }

    @Override
    public GenHttpRequest createHttpRequest(boolean verifyCertificate)
    {
        return new RestHttpRequest();
    }

    @Override
    public GenUriBuilder createUriBuilder(String baseUri) throws URISyntaxException
    {
        return new RestUriBuilder();
    }

    /**
     * Mock of a REST HTTP request.
     */
    static class RestHttpRequest implements GenHttpRequest
    {
        private static final Logger LOG = LoggerFactory.getLogger(RestHttpRequest.class);
        
        private URI uri;
        private HttpMethod httpMethod;
        private Map<String,String> headerMap = new HashMap<>();
        private String body;
        private Map<String,String> formParamMap = new HashMap<>();
        
        @Override
        public GenHttpResponse execute() throws IOException
        {
            LOG.debug("httpMethod={}; uri={}; headerMap={}; body={}; formParamMap={}", 
                      httpMethod, uri, headerMap, body, formParamMap);

            // this device sends always all parameters
            var response = 
                    MessageFormat.format(
                            "'{' \"tmp\"      : '{' \"value\" : {0} '}', "
                              + "\"target_t\" : '{' \"value\" : {1} '}' '}'",
                            lastSetTargetTemperature, lastSetTargetTemperature);
            return GenHttpResponse.of(response);
        }

        @Override
        public GenHttpRequest setUri(URI uri)
        {
            this.uri = uri;
            
            if (uri.getQuery() != null)
            {
                var query = uri.getQuery();
                var parts = query.split("=");
                lastSetTargetTemperature = parts[1]; 
            }
            
            return this;
        }

        @Override
        public void setHttpMethod(HttpMethod httpMethod)
        {
            this.httpMethod = httpMethod;
        }

        @Override
        public void addHeader(String key, String value)
        {
            this.headerMap.put( key, value );
        }

        @Override
        public void setBody(String body)
        {
            this.body = body;
        }

        @Override
        public void addFormParam(String key, String value)
        {
            this.formParamMap.put(key, value);
        }
        
    }

    /**
     * Mock of a REST URI builder.
     */
    static class RestUriBuilder implements GenUriBuilder
    {
        private static final Logger LOG = LoggerFactory.getLogger(RestUriBuilder.class);
        
        private String queryString;
        private String path;
        private Map<String,String> queryParameterMap = new HashMap<>();

        @Override
        public GenUriBuilder setQueryString(String queryString)
        {
            this.queryString = queryString;
            return this;
        }

        @Override
        public GenUriBuilder addPath(String path)
        {
            this.path = path;
            return this;
        }

        @Override
        public GenUriBuilder addQueryParameter(String name, String value)
        {
            this.queryParameterMap.put(name, value);
            return this;
        }

        @Override
        public URI build() throws URISyntaxException
        {
            LOG.debug("path={}; queryString={}; queryParameterMap={}", path, queryString, queryParameterMap);
            final var query = new StringBuilder(); 
        
            if ((queryString != null) || (!queryParameterMap.isEmpty()))
            {
                query.append("?");
                
                if (queryString != null)
                {
                    query.append(queryString + " ");
                }
                
                queryParameterMap.forEach( (key,value) -> query.append(key + "=" + value + ","));
                
                if (query.toString().endsWith(","))
                {
                    query.deleteCharAt(query.length() - 1);
                }
            }
            
            return URI.create(path + query);
        }
        
    }

}

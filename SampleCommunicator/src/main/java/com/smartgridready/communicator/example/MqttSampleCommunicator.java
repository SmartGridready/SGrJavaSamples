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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartgridready.communicator.common.api.GenDeviceApi;
import com.smartgridready.communicator.common.api.SGrDeviceBuilder;
import com.smartgridready.communicator.common.api.values.Float32Value;
import com.smartgridready.communicator.example.helper.EidLoader;
import com.smartgridready.communicator.rest.exception.RestApiAuthenticationException;
import com.smartgridready.driver.api.common.GenDriverException;
import com.smartgridready.driver.api.messaging.GenMessagingClient;
import com.smartgridready.driver.api.messaging.GenMessagingClientFactory;
import com.smartgridready.driver.api.messaging.MessageFilterHandler;
import com.smartgridready.driver.api.messaging.model.Message;
import com.smartgridready.driver.api.messaging.model.MessagingInterfaceDescription;
import com.smartgridready.driver.api.messaging.model.MessagingPlatformType;

import io.vavr.control.Either;

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
 * <li>MQTT</li>
 * </ul>
 * The communicator is responsible of loading the appropriate descriptions and parameters of the attached
 * devices/products.
 * <p>
 * The example shows the basic steps to set up the communicator to talk to a simple SmartGridready MQTT
 * device and read/writes a value from/to the device.
 * <p>
 * The example also uses the recommended <b>new</b> SGrDeviceBuilder method.
 */
public class MqttSampleCommunicator
{
    private static final Logger LOG = LoggerFactory.getLogger(MqttSampleCommunicator.class);

    /** This example is tied to this EID-XML. */
    private static final String DEVICE_DESCRIPTION_FILE_NAME = "SGr_04_0014_0000_WAGO_Testsystem_MQTT_V1.0.xml";
    

    public static void main(String[] args)
    {
        // This example uses a mocked MQTT driver factory to create the driver instance.
        // You may change the factory implementation or just use the default, in order to
        // create actual MQTT devices.

        GenDeviceApi sgcpDevice;
        
        try
        {
            // Use the SGrDeviceBuilder class to load the device description (EID) from
            // an XML file, input stream or text content.
            // No configuration, taking defaults for mock.
            // Create the SGr device instance by calling build().
            sgcpDevice = new SGrDeviceBuilder()
                // mandatory: inject device description (EID)
                .eid(EidLoader.getDeviceDescriptionInputStream(DEVICE_DESCRIPTION_FILE_NAME))
                // optional: inject the MQTT mock (only for this example)
                .useMessagingClientFactory(new MockMessagingClientFactory(), MessagingPlatformType.MQTT5)
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
        
            final var PROFILE_DC_OUT_1 = "VoltageDC_OUT_1";
            final var DATA_POINT_DC = "VoltageDC";
            
            if (sgcpDevice.canSubscribe())
            {
                LOG.info("Subscribing ...");
                sgcpDevice.subscribe(PROFILE_DC_OUT_1, DATA_POINT_DC, result -> {
                    if (result.isRight())   LOG.info("Got result: {}", result.get());
                    else                    LOG.info("Got error: {}", result.getLeft());
                });
            }
            
            // Read the current voltage
            final var voltageDc1 = sgcpDevice.getVal(PROFILE_DC_OUT_1, DATA_POINT_DC);
            LOG.info("Current VoltageDC is '{}'", voltageDc1.getFloat32());
            
            // Set the current voltage
            LOG.info("Increasing VoltageDC by 10");
            sgcpDevice.setVal(PROFILE_DC_OUT_1, DATA_POINT_DC, Float32Value.of( voltageDc1.getFloat32() + 10 ));
            
            final var voltageDc1New = sgcpDevice.getVal(PROFILE_DC_OUT_1, DATA_POINT_DC);
            LOG.info("Current VoltageDC is now '{}'", voltageDc1New.getFloat32());

            if (sgcpDevice.canSubscribe())
            {
                LOG.info("Unsubscribing ...");
                sgcpDevice.unsubscribe(PROFILE_DC_OUT_1, DATA_POINT_DC);
            }
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
 * Mock of a MQTT client factory for the EID-XML "SGr_04_0014_0000_WAGO_Testsystem_MQTT_V1.0.xml".
 */
class MockMessagingClientFactory implements GenMessagingClientFactory
{

    @Override
    public GenMessagingClient create(MessagingInterfaceDescription interfaceDescription)
    {
        return new MqttMessagingClient();
    }

    @Override
    public Set<MessagingPlatformType> getSupportedPlatforms()
    {
        return Set.of(MessagingPlatformType.MQTT5);
    }

    /**
     * Mock of a MQTT MessagingClientMock.
     */
    static class MqttMessagingClient implements GenMessagingClient
    {
        private static final Logger LOG = LoggerFactory.getLogger(MqttMessagingClient.class);

        private static final Double INITIAL_VOLTAGE = 22.2;
        private Double currentVoltage = INITIAL_VOLTAGE;
        
        private Consumer<Either<Throwable, Message>> callback;
        
        @Override
        public void close() throws IOException
        {
            LOG.debug("closing ...");
        }

        @Override
        public void sendSync(String topic, Message message)
        {
            LOG.debug("sendSync for topic '{}', message.payload is '{}'", topic, message.getPayload());
            currentVoltage = Double.parseDouble(message.getPayload());
            
            if (callback != null)
            {
                callback.accept(Either.right(message));
            }
        }

        @Override
        public Either<Throwable, Message> readSync(String readCmdMessageTopic,
                                                   Message readCmdMessage,
                                                   String inMessageTopic,
                                                   MessageFilterHandler messageFilterHandler,
                                                   long timeoutMs)
        {
            LOG.debug("readSync for topic '{}'", readCmdMessageTopic);
            return Either.right(Message.of(currentVoltage.toString()));
        }

        @Override
        public CompletableFuture<Either<Throwable, Void>> sendAsynch(String topic, Message message)
        {
            // REMARK: unused in this example
            LOG.debug("sendAsynch for topic '{}', message.payload is '{}'", topic, message.getPayload());
            return CompletableFuture.supplyAsync( () ->
                {
                    sendSync(topic, message);
                    return Either.right(null);
                } );
        }
        
        @Override
        public void subscribe(String topic,
                              MessageFilterHandler messageFilterHandler,
                              Consumer<Either<Throwable, Message>> callback) throws GenDriverException
        {
            LOG.debug("subscribing to topic '{}'", topic);
            this.callback = callback;
        }

        @Override
        public void unsubscribe(String topic) throws GenDriverException
        {
            LOG.debug("unsubscribing from topic '{}'", topic);
            this.callback = null;
        }
    }
}

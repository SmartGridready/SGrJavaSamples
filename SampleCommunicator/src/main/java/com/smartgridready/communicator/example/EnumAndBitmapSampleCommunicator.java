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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartgridready.communicator.common.api.GenDeviceApi;
import com.smartgridready.communicator.common.api.SGrDeviceBuilder;
import com.smartgridready.communicator.common.api.values.BitmapValue;
import com.smartgridready.communicator.common.api.values.EnumValue;
import com.smartgridready.communicator.example.helper.EidLoader;
import com.smartgridready.communicator.example.helper.MockModbusClientFactory;
import com.smartgridready.communicator.rest.exception.RestApiAuthenticationException;
import com.smartgridready.driver.api.common.GenDriverException;

/**
 * This class provides examples on how to handle enumerations and bitmaps,
 * using the current SmartGridready commhandler library.
 * <br>
 * The program uses a mocked Modbus driver and can be run without an attached device/product.
 * All configuration parameters of the EID are hard-coded, therefore no configuration properties need to be set.
 */
public class EnumAndBitmapSampleCommunicator
{
    private static final Logger LOG = LoggerFactory.getLogger(EnumAndBitmapSampleCommunicator.class);

    private static final String HEAT_PUMP_BASE_PROFILE = "HeatPumpBase";
    private static final String HEAT_PUMP_OP_CMD = "HPOpModeCmd";
    private static final String HEAT_PUMP_OP_STATE = "HPOpState";

    private static final String DEVICE_DESCRIPTION_FILE_NAME = "SampleExternalInterfaceFile.xml";

    public static void main(String[] argv)
    {
        // Use the SGrDeviceBuilder class to load the device description (EID) from
        // an XML file, input stream or text content.
        // Create the SGr device instance by calling build().
        //
        // This example uses a mocked Modbus driver factory to create the driver instance.
        // You may change the factory implementation or just use the default, in order to
        // create actual Modbus devices with serial or TCP connection.
        //
        GenDeviceApi sgcpDevice;

        try
        {
            sgcpDevice = new SGrDeviceBuilder()
                // mandatory: inject device description (EID)
                .eid(EidLoader.getDeviceDescriptionInputStream(DEVICE_DESCRIPTION_FILE_NAME))
                // optional: inject the ModbusFactory mock
                .useModbusClientFactory(new MockModbusClientFactory(true))
                .build();
        }
        catch ( GenDriverException | RestApiAuthenticationException | IOException e )
        {
            LOG.error("Error loading device description. ", e);
            return;
        }
        
        try
        {
            // Connect the device instance. Initializes the attached transport.
            // In case of Modbus RTU this initializes the COM port.
            // In case of Modbus TCP this initializes the TCP connection.
            // In case of messaging this connects to the MQTT broker.
            //
            sgcpDevice.connect();

            // Now we can write set status commands using enum and bitmap values.

            // ENUMS
            // =====
            // The next command sets the heat pump to comfort operation, which is defined as an enum.
            // To determine valid enum strings for a given data point, have a look at the EI-XML file.
            // ('./resources/SampleExternalInterfaceFile.xml' in our case)
            // Make sure that the type of the value instance you are feeding into the data point
            // matches the data point's data type, e.g. EnumValue if the data point's data type is enum.
            // If not, conversion errors may occur. This is a library issue which is yet to be resolved.
            sgcpDevice.setVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD, EnumValue.of("WP_DOM_WATER_OP"));
            // It is also possible to set the value as an ordinal.
            sgcpDevice.setVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD, EnumValue.of(5));
            LOG.info("Did set HPOpModeCmd to 'WP_DOM_WATER_OP'");

            // To read back an enum value use getVal(...).getEnum() which returns an enum record.
            final var opState = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD).getEnum();
            LOG.info("OP-State literal={}", opState.getLiteral());
            LOG.info("OP-State ordinal={}", opState.getOrdinal());
            LOG.info("OP-State description={}", opState.getDescription());

            // You can also use Value.getString() and Value.toString()
            final var  opStateVal = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD);
            LOG.info("OP-State EnumValue.getString() = {}", opStateVal.getString());
            LOG.info("OP-State EnumValue.toString() = {}", opStateVal);

            // BITMAPS
            // =======
            // The next command reads the heat pump operation state. Within the EI-XML, the operation state is defined as bitmap.
            // Use getVel(...).getBitmap() to read bitmaps from the device. The result is a Map that contains the literals of all
            // bits in the bitmap and an according boolean value whether the bit is set or not.
            final var bitmap = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_STATE).getBitmap();
            LOG.info("OP-State bitmap values read:");
            bitmap.forEach((literal, isBitSet) -> LOG.info("\t{} = {}", literal, isBitSet));

            // You can now modify some bits and write them back:
            //
            // IMPORTANT NOTE: If the bitmap is represented by a register, you need to read the bitmap first to
            // get all bits with their state. Then modify the bits you want to change in the 'bitmap' variable represented
            // as Map<Sting, Boolean>. Now you have to write the complete modified bitmap (containing the unmodified bits too)
            // back to the register. If the Map does not contain all bits and their value, the missing bits will be set to 'false'.
            bitmap.put("HP_PUMP_ON", false);
            bitmap.put("HP_IN_HEATING_MODE", false);
            sgcpDevice.setVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_STATE, BitmapValue.of(bitmap));

            // You can also use Value.getString() and Value.toString() to determine the status of the bitmap:
            final var  bitmapValue = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_STATE);
            LOG.info("OP-State BitmapValue.getString() = {}", bitmapValue.getString());
            LOG.info("OP-State BitmapValue.toString() = {}", bitmapValue);
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
                catch ( GenDriverException e )
                {
                    LOG.error("Error disconnecting device.", e);
                }
            }
        }
    }
}

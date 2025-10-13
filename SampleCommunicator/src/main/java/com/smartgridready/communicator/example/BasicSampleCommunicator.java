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
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartgridready.communicator.common.api.GenDeviceApi;
import com.smartgridready.communicator.common.api.SGrDeviceBuilder;
import com.smartgridready.communicator.common.api.dto.ConfigurationValue;
import com.smartgridready.communicator.common.api.dto.DataPoint;
import com.smartgridready.communicator.common.api.dto.DataPointValue;
import com.smartgridready.communicator.common.api.dto.DeviceInfo;
import com.smartgridready.communicator.common.api.dto.FunctionalProfile;
import com.smartgridready.communicator.common.api.dto.GenericAttribute;
import com.smartgridready.communicator.example.helper.EidLoader;
import com.smartgridready.communicator.example.helper.MockModbusClientFactory;
import com.smartgridready.communicator.rest.exception.RestApiAuthenticationException;
import com.smartgridready.driver.api.common.GenDriverException;

/** 
 * <p>
 * This is a sample implementation of communicator that uses the SmartGridready communication
 * handler. The communication handler uses the SmartGridready generic interface to communicate
 * with any attached device/product in a generic way. The communication handler converts the
 * generic API commands to commands understood by the external interface of the device/product.
 * <p> 
 * The command translation is done by the communication handler based on a device description
 * loaded from a device description XML file.
 * <p>
 * There are several communication protocols/technologies available to communicate with the device:
 * <ul>
 * 		<li>Modbus RTU</li>
 * 		<li>Modbus TCP</li>
 * 		<li>http / REST Swagger</li>
 * </ul>
 * The communicator is responsible of loading the appropriate descriptions and parameters of the 
 * attached devices/products.  
 * <p>
 * The example shows the basic steps to set up the communicator to talk to a simple 
 * SmartGridready Modbus device and read a value from the device.
 * <p>
 * The example also uses the recommended <b>new</b> SGrDeviceBuilder method.
 */
public class BasicSampleCommunicator
{
	private static final Logger LOG = LoggerFactory.getLogger(BasicSampleCommunicator.class);

	private static final String PROFILE_VOLTAGE_AC = "VoltageAC";
	private static final String DEVICE_DESCRIPTION_FILE_NAME = "SGr_00_0014_0000_WAGO_SmartMeter_V0.3.xml";
	private static final String SERIAL_PORT_NAME = "COM3";
    private static final String SERIAL_PARITY = "NONE";

	public static void main(String[] argv)
	{
		// Step 1: 
		// Use the SGrDeviceBuilder class to load the device description (EID) from
		// an XML file, input stream or text content.
		// Use properties to replace configuration placeholders in EID.
		// Create the SGr device instance by calling build().
		//
		// This example uses a mocked Modbus driver factory to create the driver instance.
		// You may change the factory implementation or just use the default, in order to
		// create actual Modbus devices with serial or TCP connection.
		//
		final var configProperties = new Properties();
		configProperties.setProperty("serial_port", SERIAL_PORT_NAME);
        configProperties.setProperty("serial_parity", SERIAL_PARITY);

		GenDeviceApi sgcpDevice;

        try
        {
            sgcpDevice = new SGrDeviceBuilder()
                // mandatory: inject device description (EID)
                .eid(EidLoader.getDeviceDescriptionInputStream(DEVICE_DESCRIPTION_FILE_NAME))
                // optional: inject the ModbusFactory mock
                .useModbusClientFactory(new MockModbusClientFactory(false))
                // optional: use shared Modbus RTU gateway registry to allow multiple devices on same RS-485 bus
                .useSharedModbusRtu(true)
                // optional: inject the configuration
                .properties(configProperties)
                .build();
        }
        catch ( GenDriverException | RestApiAuthenticationException | IOException e )
        {
            LOG.error("Error loading device description. ", e);
            return;
        }

        try
        {
			// Step 2: 
			// Connect the device instance. Initializes the attached transport.
			// In case of Modbus RTU this initializes the COM port.
			// In case of Modbus TCP this initializes the TCP connection.
			// In case of messaging this connects to the MQTT broker.
			//
			sgcpDevice.connect();

			// Read specific values from the device.
			// - "PROFILE_VOLTAGE_AC" is the name of the functional profile.
			// - "VoltageACL1_N", "VoltageACL2_N" and "VoltageACL3_N" are the names of the data points that
			//   report the values corresponding to their names.
			//
			// Hint: You can only read values for functional profiles and data points that exist
			// in the device description (EID).
			//
			final var val1 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageACL1_N").getFloat32();
			final var val2 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageACL2_N").getFloat32();
			final var val3 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageACL3_N").getFloat32();
			final var log = String.format("WAGO-Meter, %s: L1=%.2fV, L2=%.2fV, L3=%.2fV", PROFILE_VOLTAGE_AC, val1, val2, val3);
			LOG.info(log);
			
			final var val4 = sgcpDevice.getVal("CurrentDirection", "CurrentDirL1");
			final var log2 = String.format("WAGO-Meter, %s: L1=%s", "CurrentDirection", val4.getString());
            LOG.info(log2);
			
			// REMARK: An example for setVal() you find in EnumAndBitmapSampleCommunicator

            // Read all values from the device.
            final var values = sgcpDevice.getValues();
            LOG.info(valsToString(values));
            
            // Get device info
            final var deviceInfo = sgcpDevice.getDeviceInfo();
            LOG.info(diToString(deviceInfo));

            // Or simply just the device configuration info.
            final var configurationInfo = sgcpDevice.getDeviceConfigurationInfo();
            LOG.info("Device configuration info:" + ciToString(configurationInfo, 1));
            
            // Or just the functional profiles.
            final var functionalProfiles = sgcpDevice.getFunctionalProfiles();
            LOG.info("Functional profiles:" + fpsToString(functionalProfiles, 1, true));
            
            // Get a specific functional profile.
            final var functionalProfile = sgcpDevice.getFunctionalProfile(functionalProfiles.get(0).getName());
            LOG.info("Functional profile:" + fpToString(functionalProfile, 1, false));
            
            // Get data points of a specific functional profile.
            final var dataPoints = sgcpDevice.getDataPoints(functionalProfile.getName());
            LOG.info("Data points:" + dpsToString(dataPoints, 1, true));
            
            // Get a specific data point of a specific functional profile.
            final var dataPoint = sgcpDevice.getDataPoint(functionalProfile.getName(), dataPoints.get(0).getName());
            LOG.info("Data point:" + dpToString(dataPoint, 1, false));
		}
		catch (Exception e)
		{
			LOG.error("Error accessing device. ", e);
		}
		finally
		{
            // last Step:
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
	
	private static String valsToString(List<DataPointValue> values)
	{
        final var sb = new StringBuilder();
        final var tabs = tabsToString(1);
        sb.append("Values:");
        values.forEach(value -> sb.append("\n" + tabs + value));
        return sb.toString();
	}
	
	private static String diToString(DeviceInfo deviceInfo)
	{
        final var tabs = tabsToString(1);
	    
        return "DeviceInfo:"
        + "\n" + tabs + "name:                 " + deviceInfo.getName()
        + "\n" + tabs + "manufacturer:         " + deviceInfo.getManufacturer()
        + "\n" + tabs + "versionNumber:        " + deviceInfo.getVersionNumber()
        + "\n" + tabs + "softwareVersion:      " + deviceInfo.getSoftwareVersion()
        + "\n" + tabs + "hardwareVersion:      " + deviceInfo.getHardwareVersion()
        + "\n" + tabs + "deviceCategory:       " + deviceInfo.getDeviceCategory()
        + "\n" + tabs + "interfaceType:        " + deviceInfo.getInterfaceType()
        + "\n" + tabs + "operationEnvironment: " + deviceInfo.getOperationEnvironment()
        + "\n" + tabs + "genericAttributes:    " + gaToString(deviceInfo.getGenericAttributes(), 2)
        + "\n" + tabs + "configurationInfo:    " + ciToString(deviceInfo.getConfigurationInfo(), 2)
        + "\n" + tabs + "functionalProfiles:   " + fpsToString(deviceInfo.getFunctionalProfiles(), 2, true); 
	}
	
    private static String gaToString(List<GenericAttribute> genericAttribute, int numOfTabs)
    {
        if (genericAttribute.isEmpty()) return "[]";
        
        final var sb = new StringBuilder();
        final var tabs = tabsToString(numOfTabs);
        genericAttribute.forEach(ga -> 
            {
                sb.append("\n" + tabs + "name:     " + ga.getName());
                sb.append("\n" + tabs + "value:    " + ga.getValue());
                sb.append("\n" + tabs + "dataType: " + ga.getDataType().getTypeName());
                sb.append("\n" + tabs + "unit:     " + ga.getUnit());
                sb.append("\n" + tabs + "children: " + ga.getChildren());
                sb.append("\n" + tabs + "---");
            });
        return sb.toString();
    }
    
	private static String ciToString(List<ConfigurationValue> configurationInfo, int numOfTabs)
	{
        if (configurationInfo.isEmpty()) return "[]";
        
	    final var sb = new StringBuilder();
        final var tabs = tabsToString(numOfTabs);
        configurationInfo.forEach(ci -> 
            {
                sb.append("\n" + tabs + "name:         " + ci.getName());
                sb.append("\n" + tabs + "defaultValue: " + ci.getDefaultValue()); 
                sb.append("\n" + tabs + "dataType:     " + ci.getDataType().getTypeName());
                sb.append("\n" + tabs + "descriptions: " + ci.getDescriptions());
                sb.append("\n" + tabs + "---");
            });
        return sb.toString();
	}

	private static String fpsToString(List<FunctionalProfile> functionalProfiles, int numOfTabs, boolean shortLog)
	{
        if (functionalProfiles.isEmpty()) return "[]";
        
        final var sb = new StringBuilder();
        functionalProfiles.forEach(fp ->
            {
                sb.append(fpToString( fp, numOfTabs, shortLog));
            });
        return sb.toString();
	}
	
    private static String fpToString(FunctionalProfile fp, int numOfTabs, boolean shortLog)
    {
        final var sb = new StringBuilder();
        final var tabs = tabsToString(numOfTabs);
        
        sb.append("\n" + tabs + "name: " + fp.getName());
        
        if (!shortLog)
        {
            sb.append("\n" + tabs + "profile type:       " + fp.getProfileType());
            sb.append("\n" + tabs + "category:           " + fp.getCategory());
            sb.append("\n" + tabs + "generic attributes: " + gaToString(fp.getGenericAttributes(), numOfTabs + 1));
            sb.append("\n" + tabs + "data points:        " + dpsToString(fp.getDataPoints(), numOfTabs + 1, true));
            sb.append("\n" + tabs + "---");
        }
        return sb.toString();
    }
    
    private static String dpsToString(List<DataPoint> dataPoints, int numOfTabs, boolean shortLog)
    {
        if (dataPoints.isEmpty()) return "[]";

        final var sb = new StringBuilder();
        dataPoints.forEach(dp ->
            {
                sb.append(dpToString(dp, numOfTabs, shortLog));
            });
        return sb.toString();
    }
	
    private static String dpToString(DataPoint dp, int numOfTabs, boolean shortLog)
    {
        final var sb = new StringBuilder();
        final var tabs = tabsToString(numOfTabs);
        sb.append("\n" + tabs + "name:                  " + dp.getName());
        
        if (!shortLog)
        {
            sb.append("\n" + tabs + "functionalProfileName: " + dp.getFunctionalProfileName());
            sb.append("\n" + tabs + "dataType:              " + dp.getDataType().getTypeName());
            sb.append("\n" + tabs + "getUnit:               " + dp.getUnit());
            sb.append("\n" + tabs + "permissions:           " + dp.getPermissions());
            sb.append("\n" + tabs + "minimumValue:          " + dp.getMinimumValue());
            sb.append("\n" + tabs + "maximumValue:          " + dp.getMaximumValue());
            sb.append("\n" + tabs + "arrayLen:              " + dp.getArrayLen());
            sb.append("\n" + tabs + "genericAttributes:     " + gaToString(dp.getGenericAttributes(), numOfTabs + 1));
            sb.append("\n" + tabs + "---");
        }
        
        return sb.toString();
    }
    
    private static String tabsToString(int numOfTabs)
    {
        final var sb = new StringBuilder();
        
        while (numOfTabs-- > 0 )
        {
            sb.append("\t");
        }

        return sb.toString();
    }
    
}

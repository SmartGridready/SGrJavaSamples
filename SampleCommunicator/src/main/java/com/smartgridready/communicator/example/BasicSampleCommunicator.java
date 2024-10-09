/**
*Copyright(c) 2021 Verein SmartGridready Switzerland
 *
This Open Source Software is BSD 3 clause licensed:
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in 
   the documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from 
   this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.

This Module includes automatically generated code, generated from SmartGridready Modus XML Schema definitions
check for "EI-Modbus" and "Generic" directories in our Namespace http://www.smartgridready.ch/ns/SGr/V0/

*/
package com.smartgridready.communicator.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartgridready.communicator.common.api.GenDeviceApi;
import com.smartgridready.communicator.common.api.SGrDeviceBuilder;
import com.smartgridready.communicator.example.helper.MockModbusGatewayFactory;
import com.smartgridready.communicator.modbus.api.ModbusGatewayFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
 * 
 **/
public class BasicSampleCommunicator {

	private static final Logger LOG = LoggerFactory.getLogger(BasicSampleCommunicator.class);

	private static final ModbusGatewayFactory mockModbusFactory = new MockModbusGatewayFactory();

	private static final String PROFILE_VOLTAGE_AC = "VoltageAC";
	private static final String DEVICE_DESCRIPTION_FILE_NAME = "SGr_04_0014_0000_WAGO_SmartMeterV0.2.1.xml";
	private static final String SERIAL_PORT_NAME = "COM3";

	public static void main(String[] argv) {
		
		try {
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
			Properties configProperties = new Properties();
			configProperties.setProperty("serial_port", SERIAL_PORT_NAME);

			GenDeviceApi sgcpDevice = new SGrDeviceBuilder()
				.useModbusGatewayFactory(mockModbusFactory)
				.eid(getDeviceDescriptionFile(DEVICE_DESCRIPTION_FILE_NAME))
				.properties(configProperties)
				.build();

			// Step 2: 
			// Connect the device instance. Initializes the attached transport.
			// In case of Modbus RTU this initializes the COM port.
			// In case of Modbus TCP this initializes the TCP connection.
			// In case of messaging this connects to the MQTT broker.
			//
			sgcpDevice.connect();

			// Step 3:
			// Read the values from the device.
			// - "PROFILE_VOLTAGE_AC" is the name of the functional profile.
			// - "VoltageL1", "VoltageL2" and "VoltageL3" are the names of the Datapoints that
			//   report the values corresponding to their names.
			//
			// Hint: You can only read values for functional profiles and datapoints that exist
			// in the device description (EID).
			//
			float val1 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageL1").getFloat32();
			float val2 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageL2").getFloat32();
			float val3 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageL3").getFloat32();
			String log = String.format("Wago-Meter CurrentAC:  %.2fV,  %.2fV,  %.2fV", val1, val2, val3);
			LOG.info(log);

			// Step 4:
			// Disconnect from device instance. Closes the attached transport.
			//
			sgcpDevice.disconnect();
		} catch (Exception e) {
			LOG.error("Error loading device description. ", e);
		}									
	}

	private static InputStream getDeviceDescriptionFile(String fileName) throws IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream istr = classloader.getResourceAsStream(fileName);
		if (istr != null) {
			return istr;
		}

		throw new FileNotFoundException("Unable to load device description file: " + DEVICE_DESCRIPTION_FILE_NAME);
	}
}

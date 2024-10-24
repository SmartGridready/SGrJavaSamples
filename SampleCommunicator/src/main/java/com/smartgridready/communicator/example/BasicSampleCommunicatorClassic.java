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

import com.smartgridready.communicator.common.helper.DeviceDescriptionLoader;
import com.smartgridready.communicator.example.helper.GenDriverAPI4ModbusMock;
import com.smartgridready.communicator.modbus.impl.SGrModbusDevice;
import com.smartgridready.driver.api.modbus.DataBits;
import com.smartgridready.driver.api.modbus.GenDriverAPI4Modbus;
import com.smartgridready.driver.api.modbus.Parity;
import com.smartgridready.driver.api.modbus.StopBits;
import com.smartgridready.ns.v0.DeviceFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URL;
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
 * The communicator is responsible instantiate/load the suitable driver for the attached 
 * devices/products.  
 * <p>
 * The example shows the basic steps to set up the communicator to talk to a simple 
 * SmartGridready Modbus device and read a value from the device.
 * <p>
 * Note that this example uses the classic method to create device instances, which is deprecated.
 * 
 **/
public class BasicSampleCommunicatorClassic {

	private static final Logger LOG = LoggerFactory.getLogger(BasicSampleCommunicator.class);

	private static final String PROFILE_VOLTAGE_AC = "VoltageAC";
	private static final String DEVICE_DESCRIPTION_FILE_NAME = "SGr_04_0014_0000_WAGO_SmartMeterV0.2.1.xml";
	private static final String SERIAL_PORT_NAME = "COM3";

	public static void main(String[] argv) {
		
		try {
			// Step 1: 
			// Use the DeviceDescriptionLoader class to Load the device description from an XML file.
			// Use properties to replace configuration placeholders in EID.
			//
			Properties configProperties = new Properties();
			configProperties.setProperty("port_name", SERIAL_PORT_NAME);
			String deviceDescFilePath = getDeviceDescriptionFilePath();
			DeviceDescriptionLoader loader = new DeviceDescriptionLoader();
			DeviceFrame sgcpMeter = loader.load( "", deviceDescFilePath);
			
			// Step 2: 
			// Load the suitable device driver to communicate with the device. The example below uses
			// mocked driver for modbus RTU.
			//
			// Change the driver to the real driver, suitable for your device. For example:
			// - GenDriverAPI4Modbus mbTCP = new GenDriverAPI4ModbusTCP("127.0.0.1", 502)
			// - GenDriverAPI4Modbus mbRTU = new GenDriverAPI4ModbusRTU("COM1")
			//
			GenDriverAPI4Modbus mbRTUMock = new GenDriverAPI4ModbusMock(SERIAL_PORT_NAME, 9600, Parity.EVEN, DataBits.EIGHT, StopBits.ONE);
			
			// Step 2 (Modbus RTU only):
			// Initialise the serial COM port used by the modbus transport service.
			//
			mbRTUMock.connect();
				
			// Step 3:
			// Instantiate a modbus device. Provide the device description and the device driver
			// instance to be used for the device.
			SGrModbusDevice sgcpDevice = new SGrModbusDevice(sgcpMeter, mbRTUMock );

			// Step 4:
			// Read the values from the device.
			// - "PROFILE_VOLTAGE_AC" is the name of the functional profile.
			// - "VoltageL1", "VoltageL2" and "VoltageL3" are the names of the Datapoints that
			//   report the values corresponding to their names.
			//
			// Hint: You can only read values for functional profiles and datapoints that exist
			// in the device description XML.
			//
			float val1 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageL1").getFloat32();
			float val2 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageL2").getFloat32();
			float val3 = sgcpDevice.getVal(PROFILE_VOLTAGE_AC, "VoltageL3").getFloat32();
			String log = String.format("Wago-Meter CurrentAC:  %.2fV,  %.2fV,  %.2fV", val1, val2, val3);
			LOG.info(log);

			// Step 5:
			// Close transport when no longer needed.
			//
			mbRTUMock.disconnect();
		} catch (Exception e) {
			LOG.error("Error loading device description. ", e);
		}									
	}

	private static String getDeviceDescriptionFilePath() throws FileNotFoundException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		URL deviceDesc = classloader.getResource(DEVICE_DESCRIPTION_FILE_NAME);
		if (deviceDesc != null && deviceDesc.getPath() != null) {
			return deviceDesc.getPath();
		} else {
			throw new FileNotFoundException("Unable to load device description file: " + DEVICE_DESCRIPTION_FILE_NAME);
		}
	}
}

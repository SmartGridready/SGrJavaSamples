/**
*Copyright(c) 2022-2024 Verein SmartGridready Switzerland
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

This Module references automatically generated code, generated from SmartGridready Modbus XML Schema definitions
check for "EI-Modbus" and "Generic" directories in our Namespace http://www.smartgridready.ch/ns/SGr/V0/

author: IBT/cb, FHNW/mkr
*/

package com.smartgridready.communicator.example;

import com.smartgridready.communicator.common.api.GenDeviceApi;
import com.smartgridready.communicator.common.api.SGrDeviceBuilder;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides an example on how to communicate with a WAGO smart meter
 * over Modbus RTU (RS-485), using the current SmartGridready commhandler library.
 * <br>
 * The device is instantiated the new fashioned way, using the device builder.
 * A shared Modbus driver registry is used in order to support multiple SGr devices on the same serial connection.
 * <br>
 * The program requires an actual serial port and an attached device.
 */
public class WagoSmartMeterCommunicator {

	private static final Logger LOG = LoggerFactory.getLogger(WagoSmartMeterCommunicator.class);
	private static final String DEVICE_DESCRIPTION_FILE_NAME = "SGr_04_0014_0000_WAGO_SmartMeterV0.2.1.xml";
	private static final String SERIAL_PORT_NAME = "COM3";
	
	public static void main(String[] argv) {

		try {
			// configuration placeholders to be replaced in EID
			Properties configProperties = new Properties();
			configProperties.setProperty("serial_port", SERIAL_PORT_NAME);

			GenDeviceApi device = new SGrDeviceBuilder()
					.useSharedModbusRtu(true)
					.eid(getDeviceDescriptionFileStream())
					.properties(configProperties)
					.build();

			device.connect();

			LOG.info("Device-name {}", device.getDeviceInfo().getName());
			LOG.info("Device-interface {}", device.getDeviceInfo().getInterfaceType());

			// Read the values from all data points and log them
			var deviceData = device.getDeviceInfo().getValues();
			deviceData.forEach(dataPointValue -> LOG.info(dataPointValue.toString()));

			// close transport
			device.disconnect();
		} catch (Exception e) {
			LOG.error("Error loading device description.", e);
		}							
	}

	private static InputStream getDeviceDescriptionFileStream() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		return classloader.getResourceAsStream(DEVICE_DESCRIPTION_FILE_NAME);
	}

}

# SGr-JavaSamples

## Index
[Summary](#summary)<br>
[Installation](#installation)<br>
[Architecture](#architecture)<br>
[Code description](#code-description-for-the-samplecommunicator)<br>

## Summary
SGr-JavaSamples provides sample projects that demonstrate the use of the SGr Communication Handler Library. The goal is to set up a test environment that allows to connect SGr components and different 'products' (heat pump, charging station, inverter, battery, electricity meter etc.) through the SGr communication interface. 
[German Version](README_DE.md)

## Installation

### Requirements / Prerequisites
- Gradle version >= 8.7. Note: If no IDE with Gradle integration is used, Gradle must first be installed locally: https://gradle.org/install/
- Java JDK version >= Java 11

### Clone
- Create a new folder for the project (eg. 'SmartGridCommunicator').
- Clone the following projects to the new folder:
- https://github.com/SmartgridReady/SGrJavaSamples.git
- https://github.com/SmartgridReady/SGrSpecifications.git

Rem: The folder [your-local-project-folder]/SGrSpecifications/XMLInstances/ExtInterfaces contains the device description XML-files used by the SGrJavaSamples.

### Build
- Open the [your-local-project-folder]\SGrJavaSamples\SampleCommunicator with your IDE.
- Run gradle 'build' target in your IDE 
- You can also run Gradle from the command line. Change to the [your-local-project-folder]\SGrJavaSamples\SampleCommunicator directory and run:<br>
```bash>gradle clean build```

<br><br>

## Architecture

![SGr Architecture Overview](doc/img/SGr-Architecture-Overview.png "SGr Architecture Overview")


### Component: Communicator
<table valign="top">
    <tr><td><b>Implementor:</b></td><td>Communicator Provider (3rd Party)</td></tr>
    <tr><td><b>Description:</b></td><td>The 'Communicator' communicates with one or more 'Products' through the SGr 'Generic Interface'. 
    <tr><td valign="top"><b>Responsibilities:</b></td><td>
                <p>For each product, the communicator instantiates a Communication Handler to which a description of the product interface in XML is given.</p>
                <p>The Communicator loads a device driver for the communication interface of the product (e.g. Modbus RTU/TCP, REST...).</p>
                <p>The communicator reads or sets (analyses and/or controls) the data points.</p>
                <p></p> </td></tr>        
    <tr><td><b>SGrProject:</b></td>
    <td><a href="https://github.com/SmartgridReady/SGrJavaSamples/tree/master/SampleCommunicator">SGrJavaSamples/SampleCommunicator</a></td></tr>
    </r>
</table>

<br><br>

### Component: Generic Interface
<table valign="top">
    <tr><td><b>Implementor:</b></td><td>SGr Core Team</td></tr>
    <tr><td><b>Description:</b></td><td>SGr-defined 'Product'-independent interface. 
    <tr><td valign="top"><b>Responsibilities:</b></td><td>
                <p>The Generic Interface is used by the Communicator to communicate with the products in the SGr network.​</p>
                <p></p> </td></tr>        
    <tr><td><b>SGrProject:</b></td>
    <td><a href="https://github.com/SmartgridReady/SGrSpecifications/tree/master/SchemaDatabase/SGr/Generic">SmartgridReady/SGrSpecifications/SchemaDatabase/SGr/Generic</a></td></tr>
    </r>
</table>  

<br><br>

### Component: Communication Handler
<table valign="top">
    <tr><td><b>Implementor:</b></td><td>SGr Core Team</td></tr>
    <tr><td><b>Description:</b></td><td>This is the core component of the SGr software and it is responsible for the processing and implementation of the SGr 'Generic Interface' on the 'External Interface' of the 'Product'.<br>
    Instantiated by the 'Communicator' and used to communicate with the connected 'Product'.
    <tr><td valign="top"><b>Responsibilities:</b></td><td>
                <p>​Responsibilities are:<br>
- Reading the XML device profiles <br>
- Processing commands of the Generic Interface <br>
- Execute the commands on the External Interface of the product (device-specific interface)<br>
- Sending the commands to the Product through the transport service specified by the Product.
</p>
                <p></p> </td></tr>
    <tr><td><b>Library:</b></td><td>sgr-commhandler-2.0.0.jar</td>                                                                                          
    <tr><td><b>SGrProject:</b></td>
    <td><a href="https://github.com/SmartgridReady/SGrJava/tree/master/CommHandler">SmartgridReady/SGrJava/CommHandler</a></td></tr>
    </r>
</table> 

<br><br>

### Component: XML (XML-Profile)
<table valign="top">
    <tr><td><b>Implementor:</b></td><td>Provider of the 'Product'</td></tr>
    <tr><td><b>Description:</b></td><td>​The XML file describes the ''function profiles', data points and attributes that can be addressed over the SGr interface. The XML file also provides general information about the 'Product'.
    <tr><td valign="top"><b>Responsibilities:</b></td><td>
                <p>Providing general data on the Product.</p>
                <p>Provide the data necessary for mapping the SGr Generic Interface with the External Interface.</p> </td></tr>        
    <tr><td><b>SGrProject:</b></td>
    <td><a href="https://github.com/SmartgridReady/SGrSpecifications/tree/master/XMLInstances/ExtInterfaces">SmartgridReady/SGrSpecifications/XMLInstances/ExtInterfaces</a></td></tr>
    </r>
</table> 

<br><br>

### Component: Transport Layer (Transport Service)
<table valign="top">
    <tr><td><b>Implementor:</b></td><td>- SGr Core Team<br>- 3rd Party Provider</td></tr>
    <tr><td><b>Description:</b></td><td>The 'TransportService' is the link to the physical communication interface of the 'Product'. The SGr Core Team provides the EasyModbus Library for Modbus.
    <tr><td valign="top"><b>Responsibilities:</b></td><td>
                <p>​The SGr Transport Service supports the following communication technologies to provide the following transport services:<br>
                - Modbus​, REST/JSON​, Sunspec​<br>
                - Support is planned for:​ OCPP 2.0​, IEC-61968-9​, IEC-608070-5-104</p>
                <p></p> </td></tr> 
    <tr><td><b>Library:</b></td>
    <td>easymodbus.jar</td>                            
    <tr><td><b>SGrProject:</b></td>    
    <td>für Modbus:<br><a href="https://github.com/SmartgridReady/SGrJavaDrivers/tree/master/EasyModbus">SmartgridReady/SGrJavaDrivers/EasyModbus</a></td></tr>
    </r>
</table> 

<br><br>

### Component: External Interface (EI)
<table valign="top">
    <tr><td><b>Implementor:</b></td><td>Manufacturer of the 'Product'</td></tr>
    <tr><td><b>Description:</b></td><td>The 'External Interface' is the interface provided by the 'Product'.
    <tr><td valign="top"><b>Responsibilities:</b></td><td>
                <p>Provides the External Interface of the product. This is described in the Product XML Profile.</p></td></tr>                
</table> 

<br><br>

### Komponente: Product
<table valign="top">
    <tr><td><b>Implementor:</b></td><td>Manufacturer of the 'Product'</td></tr>
    <tr><td><b>Description:</b></td><td>The Product is a device that provides properties, data points and control options.
E.g. heat pump, charging station, inverter, battery, electricity meter 
    <tr><td valign="top"><b>Responsibilities:</b></td><td>
                <p>
</p></td></tr>
</table>

<br><br>

## Description of the Code Samples

The code samples demonstrate how to use the commhandler library with a communicator application.
These samples use mocked-devices run without any real device: 
<ul>
    <li>BasicSampleCommunicator</li>
    <li>EnumAndBitmapSampleCommunicator</li>
    <li>AsynchronousSampleCommunicatorTest</li>
</ul>

This sample runs with a real WAGO SmartMeter device:
<ul>
    <li>WagoSmartMeterCommunicator</li>
</ul>

### BasicSampleCommunicator

Step 1:
Use the DeviceDescriptionLoader class to load the device description from an XML file.
<br><br>
```String deviceDescFilePath = getDeviceDescriptionFilePath();```<br>
```DeviceDescriptionLoader<DeviceFrame> loader = new DeviceDescriptionLoader<>();```<br>
```DeviceFrame sgcpMeter = loader.load( "", deviceDescFilePath);```
<br><br>

Step2:
Load the suitable device driver to communicate with the device. The example below uses a mocked driver for modbus RTU.
Change the driver to the real driver, suitable for your device. For example:
<br><br>
```GenDriverAPI4Modbus mbTCP = new GenDriverAPI4ModbusTCP("127.0.0.1", 502);```<br>
```GenDriverAPI4Modbus mbRTU = new GenDriverAPI4ModbusRTU("COM1");```<br>
```GenDriverAPI4Modbus mbRTUMock = new GenDriverAPI4ModbusRTUMock("COM1");```
<br><br>

Step 2:
Instantiate a modbus device. Provide the device description and the device driver instance to be used for the device.<br><br>
```SGrModbusDevice sgcpDevice = new SGrModbusDevice(sgcpMeter, mbRTUMock);```
<br><br>

Step 3:
Initialize the serial COM port or TCP connection used by the modbus transport service.
```sgcpDevice.connect();```
<br><br>

Step 4: Read the values from the device. 
- "CurrentAC" is the name of the functional profile.
- "CurrentACL1", "CurrentACL2", "CurrentACL3" and "ActiveNetACN" are the names of the Datapoints that report the values corresponding to their names.

<i>Hint: You can only read values for functional profiles and datapoints that exist in the device description XML.</i><br>
```String val1 = sgcpDevice.getVal("VoltageAC", "VoltageL1");```<br>
```String val2 = sgcpDevice.getVal("VoltageAC", "VoltageL2");```<br>
```String val3 = sgcpDevice.getVal("VoltageAC", "VoltageL3");```<br><br>

Step 5: Disconnect device.
```sgcpDevice.disconnect();```
<br><br>

The complete sample code can be found on github:<br>
https://github.com/SmartgridReady/SGrJavaSamples/blob/master/SampleCommunicator/src/main/java/ch/smartgridready/communicator/example/BasicSampleCommunicator.java

### EnumAndBitmapSampleCommunicator

Sample code on how to read and write enums and bitmap registers using the commhandler library.<br>
You find the sample code on github:
https://github.com/SmartgridReady/SGrJavaSamples/blob/master/SampleCommunicator/src/main/java/ch/smartgridready/communicator/example/EnumAndBitmapSampleCommunicator.java

### AsynchronousSampleCommunicator

Sample code on how to read multiple devices in parallel using the async features of the commhandler library.
This sample a JUnit5 test. Use the Junit test runner to run the sample code.
You find the sample code on github:
https://github.com/SmartgridReady/SGrJavaSamples/blob/master/SampleCommunicator/src/main/java/ch/smartgridready/communicator/example/AsynchronousSampleCommunicatorTest.java

### WagoSmartMeterCommunicator

If you are happy owner of a WAGO Smart-Meter you can read the read values from the real device with this sample communicator application.<br>
You find the sample code on github:<br>
https://github.com/SmartgridReady/SGrJavaSamples/blob/master/SampleCommunicator/src/main/java/ch/smartgridready/communicator/example/WagoSmartMeterCommunicator.java

## Further information / contact information

Glossary: *ToDo Link*

Webssite: https://smartgridready.ch/ <br>
E-Mail: info@smartgridready.ch

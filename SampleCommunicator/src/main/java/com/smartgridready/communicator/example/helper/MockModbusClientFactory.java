package com.smartgridready.communicator.example.helper;

import com.smartgridready.driver.api.modbus.DataBits;
import com.smartgridready.driver.api.modbus.GenDriverAPI4Modbus;
import com.smartgridready.driver.api.modbus.GenDriverAPI4ModbusFactory;
import com.smartgridready.driver.api.modbus.Parity;
import com.smartgridready.driver.api.modbus.StopBits;

// copied from SGrJava due to inacessible classes
public class MockModbusClientFactory implements GenDriverAPI4ModbusFactory {

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort) {
        return new GenDriverAPI4ModbusMock(comPort);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort, int baudRate) {
        return new GenDriverAPI4ModbusMock(comPort, baudRate);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort, int baudRate, Parity parity) {
        return new GenDriverAPI4ModbusMock(comPort, baudRate, parity);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort, int baudRate, Parity parity, DataBits dataBits) {
        return new GenDriverAPI4ModbusMock(comPort, baudRate, parity, dataBits);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort, int baudRate, Parity parity, DataBits dataBits,
            StopBits stopBits) {
        return new GenDriverAPI4ModbusMock(comPort, baudRate, parity, dataBits, stopBits);
    }

    @Override
    public GenDriverAPI4Modbus createTcpTransport(String ipAddress) {
        return new GenDriverAPI4ModbusMock(ipAddress);
    }

    @Override
    public GenDriverAPI4Modbus createTcpTransport(String ipAddress, int port) {
        return new GenDriverAPI4ModbusMock(ipAddress, port);
    }

    @Override
    public GenDriverAPI4Modbus createUdpTransport(String ipAddress) {
        return new GenDriverAPI4ModbusMock(ipAddress);
    }

    @Override
    public GenDriverAPI4Modbus createUdpTransport(String ipAddress, int port) {
        return new GenDriverAPI4ModbusMock(ipAddress, port);
    }
}

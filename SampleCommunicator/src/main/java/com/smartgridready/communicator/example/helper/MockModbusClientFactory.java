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

package com.smartgridready.communicator.example.helper;

import com.smartgridready.driver.api.modbus.DataBits;
import com.smartgridready.driver.api.modbus.GenDriverAPI4Modbus;
import com.smartgridready.driver.api.modbus.GenDriverAPI4ModbusFactory;
import com.smartgridready.driver.api.modbus.Parity;
import com.smartgridready.driver.api.modbus.StopBits;

/**
 * Mock for a {@code ModbusClientFactory} that returns {@link GenDriverAPI4ModbusMock}.
 */
public class MockModbusClientFactory implements GenDriverAPI4ModbusFactory
{
    private final boolean returnInteger;
    
    /**
     * Constructor.
     * 
     * @param returnInteger
     *        indicates whether the {@link GenDriverAPI4ModbusMock} should return integer {@code true} or
     *        float {@code false} values
     */
    public MockModbusClientFactory(boolean returnInteger)
    {
        this.returnInteger = returnInteger;
    }
    
    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort, int baudRate)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort, int baudRate, Parity parity)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort,
                                                  int baudRate,
                                                  Parity parity,
                                                  DataBits dataBits)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createRtuTransport(String comPort,
                                                  int baudRate,
                                                  Parity parity,
                                                  DataBits dataBits,
                                                  StopBits stopBits)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createTcpTransport(String ipAddress)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createTcpTransport(String ipAddress, int port)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createTcpTransport(String ipAddress, int port, int timeout)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createTcpTransport(String ipAddress, int port, int timeout, boolean rtuOverTcp)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createUdpTransport(String ipAddress)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }

    @Override
    public GenDriverAPI4Modbus createUdpTransport(String ipAddress, int port)
    {
        return new GenDriverAPI4ModbusMock(returnInteger);
    }
}

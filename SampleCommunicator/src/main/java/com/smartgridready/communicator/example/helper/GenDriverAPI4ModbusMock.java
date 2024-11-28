package com.smartgridready.communicator.example.helper;

import java.nio.IntBuffer;

import com.smartgridready.driver.api.common.GenDriverException;
import com.smartgridready.driver.api.modbus.DataBits;
import com.smartgridready.driver.api.modbus.GenDriverAPI4Modbus;
import com.smartgridready.driver.api.modbus.Parity;
import com.smartgridready.driver.api.modbus.StopBits;

public class GenDriverAPI4ModbusMock implements GenDriverAPI4Modbus {

    private static final int[] REGISTER_INT_VAL = new int[]{0x00000000, 0x00000005};

    private static final int[] REGISTER_FLOAT_VAL = new int[]{0x0000435c, 0x000051ec};

    private boolean returnInteger;
    private boolean isConnected = false;

    public void setIsIntegerType(boolean returnInteger) {
        this.returnInteger = returnInteger;
    }

    public GenDriverAPI4ModbusMock() {}

    public GenDriverAPI4ModbusMock(String comPort) {}

    public GenDriverAPI4ModbusMock(String comPort, int baudRate) {}

    public GenDriverAPI4ModbusMock(String comPort, int baudRate, Parity parity) {}

    public GenDriverAPI4ModbusMock(String comPort, int baudRate, Parity parity, DataBits dataBits) {}

    public GenDriverAPI4ModbusMock(String comPort, int baudRate, Parity parity, DataBits dataBits, StopBits stopBits) {}

    @Override
    public int[] ReadInputRegisters(int startingAddress, int quantity) {
        return prepareReturnValue(quantity);
    }

    @Override
    public int[] ReadHoldingRegisters(int startingAddress, int quantity) {
        return prepareReturnValue(quantity);
    }

    @Override
    public void disconnect() {
        isConnected = false;
    }

    @Override
    public boolean[] ReadDiscreteInputs(int startingAddress, int quantity) {
        throw new UnsupportedOperationException("mocking not implemented yet");
    }

    @Override
    public boolean[] ReadCoils(int startingAddress, int quantity) {
        throw new UnsupportedOperationException("mocking not implemented yet");
    }

    @Override
    public void WriteMultipleCoils(int startingAdress, boolean[] values) {
        // implementation not required yet
    }
    

    @Override
    public void WriteSingleCoil(int startingAdress, boolean value) {
        // implementation not required yet
    }

    @Override
    public void WriteMultipleRegisters(int startingAdress, int[] values) {
        // implementation not required yet
    }

    @Override
    public void WriteSingleRegister(int startingAdress, int value) {
        // implementation not required yet
    }

    @Override
    public boolean connect() throws GenDriverException {
        if (isConnected) {
            throw new GenDriverException("Do not connect twice");
        }

        isConnected = true;
        return isConnected;
    }

    private int[] prepareReturnValue(int quantity) {

        int[] registers = returnInteger ? REGISTER_INT_VAL:REGISTER_FLOAT_VAL;

        int[] result;
        if (quantity==1) {
            result = new int[1];
            System.arraycopy(registers, 1, result, 0, 1);
            return result;
        } else {
            IntBuffer buffer = IntBuffer.allocate(quantity);
            for (int i=0; i<quantity/2; i++) {
                buffer.put(registers);
            }
            return buffer.array();
        }
    }


    @Override
    public boolean isConnected() {
        return isConnected;
    }
}
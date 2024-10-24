package com.smartgridready.communicator.example.helper;

import com.smartgridready.ns.v0.ModbusInterfaceDescription;
import com.smartgridready.driver.api.common.GenDriverException;
import com.smartgridready.communicator.modbus.api.ModbusGatewayFactory;
import com.smartgridready.communicator.modbus.api.ModbusGateway;
import com.smartgridready.communicator.modbus.helper.ModbusUtil;

// copied from SGrJava due to inacessible classes
public class MockModbusGatewayFactory implements ModbusGatewayFactory {

    @Override
    public ModbusGateway create(ModbusInterfaceDescription interfaceDescription) throws GenDriverException {
        String identifier = ModbusUtil.getModbusGatewayIdentifier(interfaceDescription);
        return new ModbusGateway(identifier, interfaceDescription, new GenDriverAPI4ModbusMock());
    }
}

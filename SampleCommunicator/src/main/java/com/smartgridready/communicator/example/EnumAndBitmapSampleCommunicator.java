package com.smartgridready.communicator.example;

import com.smartgridready.ns.v0.DeviceFrame;
import com.smartgridready.driver.api.modbus.DataBits;
import com.smartgridready.driver.api.modbus.GenDriverAPI4Modbus;
import com.smartgridready.driver.api.modbus.Parity;
import com.smartgridready.driver.api.modbus.StopBits;
import com.smartgridready.communicator.common.api.values.BitmapValue;
import com.smartgridready.communicator.common.api.values.EnumRecord;
import com.smartgridready.communicator.common.api.values.EnumValue;
import com.smartgridready.communicator.common.api.values.Value;
import com.smartgridready.communicator.common.helper.DeviceDescriptionLoader;
import com.smartgridready.communicator.example.helper.GenDriverAPI4ModbusMock;
import com.smartgridready.communicator.modbus.impl.SGrModbusDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Map;

/**
 * This class provides examples on how to handle enumerations and bitmaps,
 * using the current SmartGridready commhandler library.
 * <br>
 * The program uses a mocked modbus driver and can be run without an attached device/product.
 * All configuration parameters of the EID are hard-coded, therefore no configuration properties need to be set.
 */
public class EnumAndBitmapSampleCommunicator {

    private static final Logger LOG = LoggerFactory.getLogger(EnumAndBitmapSampleCommunicator.class);

    private static final String HEAT_PUMP_BASE_PROFILE = "HeatPumpBase";
    private static final String HEAT_PUMP_OP_CMD = "HPOpModeCmd";
    private static final String HEAT_PUMP_OP_STATE = "HPOpState";

    private static final String DEVICE_DESCRIPTION_FILE_NAME = "SampleExternalInterfaceFile.xml";
    private static final String SERIAL_PORT_NAME = "COM3";

    public static void main(String[] argv) {

        try {
            // Prepare the communication handler (SGrModbusDevice) for usage:
            // See 'BasicSampleCommunicator' for details.

            // load device description
            String deviceDescFilePath = getDeviceDescriptionFilePath();
            DeviceDescriptionLoader loader = new DeviceDescriptionLoader();
            DeviceFrame sgcpMeter = loader.load("", deviceDescFilePath);

            // initialize transport
            GenDriverAPI4Modbus mbRTUMock = createMockModbusDriver(SERIAL_PORT_NAME, 9600, Parity.EVEN, DataBits.EIGHT, StopBits.ONE);
            mbRTUMock.connect();

            // create device instance
            SGrModbusDevice sgcpDevice = new SGrModbusDevice(sgcpMeter, mbRTUMock);

            // Now we can write set status commands using enum and bitmap values.

            // ENUMS
            // =====
            // The next command sets the heat pump to comfort operation, which is defined as an enum.
            // To determine valid enum strings for a given data point, have a look at the EI-XML file.
            // ('./resources/SampleExternalInterfaceFile.xml' in our case)
            sgcpDevice.setVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD, EnumValue.of("WP_DOM_WATER_OP"));
            // It is also possible to set the value as an ordinal.
            sgcpDevice.setVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD, EnumValue.of(5));
            LOG.info("Did set HPOpModeCmd to 'WP_DOM_WATER_OP'");

            // To read back an enum value use getVal(...).getEnum() which returns an enum record.
            EnumRecord opState = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD).getEnum();
            LOG.info("OP-State literal={}", opState.getLiteral());
            LOG.info("OP-State ordinal={}", opState.getOrdinal());
            LOG.info("OP-State description={}", opState.getDescription());

            // You can also use Value.getString() and Value.toString()
            Value opStateVal = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_CMD);
            LOG.info("OP-State EnumValue.getString() = {}", opStateVal.getString());
            LOG.info("OP-State EnumValue.toString() = {}", opStateVal);

            // BITMAPS
            // =======
            // The next command reads the heat pump operation state. Within the EI-XML, the operation state is defined as bitmap.
            // Use getVel(...).getBitmap() to read bitmaps from the device. The result is a Map that contains the literals of all
            // bits in the bitmap and an according boolean value whether the bit is set or not.
            Map<String, Boolean> bitmap = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_STATE).getBitmap();
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
            Value bitmapValue = sgcpDevice.getVal(HEAT_PUMP_BASE_PROFILE, HEAT_PUMP_OP_STATE);
            LOG.info("OP-State BitmapValue.getString() = {}", bitmapValue.getString());
            LOG.info("OP-State BitmapValue.toString() = {}", bitmapValue);

            // close transport
            mbRTUMock.disconnect();
        } catch (Exception e) {
            LOG.error("Error running EnumAndBitmapSampleCommunicator: {}", e.getMessage());
        }
    }


    static String getDeviceDescriptionFilePath() throws FileNotFoundException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL deviceDesc = classloader.getResource(DEVICE_DESCRIPTION_FILE_NAME);
        if (deviceDesc != null && deviceDesc.getPath() != null) {
            return deviceDesc.getPath();
        } else {
            throw new FileNotFoundException("Unable to load device description file: " + DEVICE_DESCRIPTION_FILE_NAME);
        }
    }

    static GenDriverAPI4Modbus createMockModbusDriver(String comPort, int baudRate, Parity parity, DataBits dataBits, StopBits stopBits) {
        GenDriverAPI4ModbusMock mbRTUMock = new GenDriverAPI4ModbusMock(comPort, baudRate, parity, dataBits, stopBits);
        mbRTUMock.setIsIntegerType(true);
        return mbRTUMock;
    }
}

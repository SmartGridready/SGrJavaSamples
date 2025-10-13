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

import com.smartgridready.driver.api.common.GenDriverException;
import com.smartgridready.driver.api.modbus.GenDriverModbusException;
import com.smartgridready.driver.api.modbus.GenDriverSocketException;
import com.smartgridready.communicator.async.process.Parallel;
import com.smartgridready.communicator.async.process.Processor;
import com.smartgridready.communicator.async.process.ReadExec;
import com.smartgridready.communicator.async.process.Sequence;
import com.smartgridready.communicator.async.process.WriteExec;
import com.smartgridready.communicator.common.api.values.Float32Value;
import com.smartgridready.communicator.common.api.values.Float64Value;
import com.smartgridready.communicator.common.api.values.StringValue;
import com.smartgridready.communicator.common.api.values.Value;
import com.smartgridready.communicator.modbus.impl.SGrModbusDevice;
import com.smartgridready.communicator.rest.exception.RestApiAuthenticationException;
import com.smartgridready.communicator.rest.exception.RestApiResponseParseException;
import com.smartgridready.communicator.rest.exception.RestApiServiceCallException;
import com.smartgridready.communicator.rest.impl.SGrRestApiDevice;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * This test class provides an example on how to use the asynchronous features of
 * the SmartgridReady commmhandler library.
 * <p>
 * The demo can be run as Junit5 unit-test.
 * <p>
 * The program demonstrates how to communicate with devices in parallel (or serial
 * if required) and wait until all results are ready. The parallel communication runs in a
 * separate thread for each device. The commhandler uses RxJava Observables to achieve
 * concurrent, asynchronous communication with the devices/products.
 * <p>
 * The program uses Mockito mocks to mock the SmartgridReady communication handler.
 */
@SuppressWarnings({"java:S2629", "java:S112", "java:S1192"}) // since we are not in a 'test' folder
@ExtendWith(value = MockitoExtension.class)
public class AsynchronousSampleCommunicatorTest {

    private static final Logger LOG = LoggerFactory.getLogger(AsynchronousSampleCommunicatorTest.class);

    private static final String DEVICE_ERROR = "DEVICE ERROR";

    @Mock
    SGrModbusDevice wagoModbusDevice;

    @Mock
    SGrRestApiDevice clemapRestApiDevice1;

    @Mock
    SGrRestApiDevice clemapRestApiDevice2;

    @Mock
    SGrModbusDevice garoModbusDeviceA;

    @Mock
    SGrModbusDevice garoModbusDeviceB;

    /**
     * Processing demo for the happy case.
     * @throws Exception -
     */
    @Test
    void buildAndRunDataStructure() throws Exception {
        initStubs();
        doBuildAndRunDatastructureTest();
    }

    /**
     * Processing demo for the error case.
     * @throws Exception -
     */
    @Test
    void buildAndRunDataStructureWithException() throws Exception {
        initStubsWithException();
        doBuildAndRunDatastructureTest();
    }

    private void doBuildAndRunDatastructureTest() {

        // 1. Setup READ tasks
        // To set up a read task for a device use the ReadExec<Value> class. The ReadExec wraps a commhandler getVal() call
        // that is handled asynchronously in its own thread.
        //
        // Just define the return type of getVal() (Value), the functional-profile name, the data point name and the getter method of the
        // commhandler API to be called. When ReadExec is executed it will call the callable and save the result within an AsyncResult member.
        ReadExec<Value> wagoVoltageACL1 = new ReadExec<>("VoltageAC", "VoltageACL1_N", wagoModbusDevice::getVal);
        ReadExec<Value> wagoVoltageACL2 = new ReadExec<>("VoltageAC", "VoltageACL2_N", wagoModbusDevice::getVal);
        ReadExec<Value> wagoVoltageACL3 = new ReadExec<>("VoltageAC", "VoltageACL3_N", wagoModbusDevice::getVal);
        ReadExec<Value> clemapActPowerACtot1 = new ReadExec<>("ActivePowerAC", "ActivePowerACtot", clemapRestApiDevice1::getVal);
        ReadExec<Value> clemapActPowerACtot2 = new ReadExec<>("ActivePowerAC", "ActivePowerACtot", clemapRestApiDevice2::getVal);

        // 2. Setup WRITE tasks
        // Similar to setting up the read tasks, write tasks can be set up with the WriteExec<Value> class.
        WriteExec<Value> garoWallboxAHemsCurrLim = new WriteExec<>("Curtailment", "HemsCurrentLimit", garoModbusDeviceA::setVal);
        WriteExec<Value> garoWallboxBHemsCurrLim = new WriteExec<>("Curtailment", "HemsCurrentLimit", garoModbusDeviceB::setVal);

        // 3. Wire the tasks to define which tasks can be executed in parallel and which ones must be executed in sequence
        // (example: The executables access the same device via Modbus and therefore cannot be executed in parallel).
        // Do create a new Parallel() or a new Sequence() and add ReadExec and WriteExec instances to them.
        // To achieve this, you can create either a new 'Parallel()' or 'Sequence()' processing chain and add 'ReadExec'
        // and 'WriteExec' instances to them as needed. Additionally, you have the flexibility to nest 'Parallel()' and 'Sequence()'
        // constructs within existing chains to create more complex processing hierarchies.
        // Finally, you can call await( ReadExec/WriteExec .....) to define for which executables you want to wait for
        // the results. The thread that calls process of the read or write chain will then wait until all results are
        // available.
        Processor readChain = new Parallel()          // 2000
                .add(new Sequence()                   // 1500
                        .add(wagoVoltageACL1)
                        .add(wagoVoltageACL2)
                        .add(wagoVoltageACL3))
                .add(new Parallel()                    // 2000
                        .add(clemapActPowerACtot1)     // 750
                        .add(clemapActPowerACtot2))    // 2000
                .await(wagoVoltageACL1,
                        wagoVoltageACL2,
                        wagoVoltageACL3,
                        clemapActPowerACtot1,
                        clemapActPowerACtot2);

        Processor writeChain = new Parallel()
                .add(garoWallboxAHemsCurrLim)
                .add(garoWallboxBHemsCurrLim)
                .await(garoWallboxAHemsCurrLim,
                        garoWallboxBHemsCurrLim);

        // 4. Run readChain read chain by calling the process() method. The process method
        // blocks until all ReadExec and WriteExec have results or failed with an error.
        readChain.process();

        // 5. Get the results from the read chain.
        // As an example to get the result from the wagoVoltageACL1 ReadExec:
        LOG.info("Status        of wagoVoltageACL1: {}", wagoVoltageACL1.getExecStatus());
        LOG.info("Value         of wagoVoltageACL1: {}", wagoVoltageACL1.getReadValue() != null ? wagoVoltageACL1.getReadValue().getFloat32() : "undef");
        LOG.info("Exception     of wagoVoltageACL1:   ", wagoVoltageACL1.getExecThrowable());
        LOG.info("Request time  of wagoVoltageACL1: {}", wagoVoltageACL1.getRequestTime());
        LOG.info("Response time of wagoVoltageACL1: {}", wagoVoltageACL1.getResponseTime());
        // or use the toString() method of ReadExec:
        LOG.info("toString()    of wagoVoltageACL1: {}", wagoVoltageACL1);
        LOG.info(wagoVoltageACL1.toString());
        LOG.info(wagoVoltageACL2.toString());
        LOG.info(wagoVoltageACL3.toString());
        LOG.info(clemapActPowerACtot1.toString());

        // 6. Depending on the results of the ReadExec we could do some calculations and control
        // some output devices using the write-chain.
        garoWallboxAHemsCurrLim.setWriteValue(Float32Value.of(10f));
        garoWallboxBHemsCurrLim.setWriteValue(Float32Value.of(5f));
        // Process the write-chain:
        writeChain.process();
        // and again log the results...
        LOG.info(garoWallboxAHemsCurrLim.toString());
        LOG.info(garoWallboxBHemsCurrLim.toString());

        // 7. Cleanup the resources used by the ReadExec/WriteExec (and the underlying RxJava)
        wagoVoltageACL1.cleanup();
        wagoVoltageACL2.cleanup();
        wagoVoltageACL3.cleanup();
        clemapActPowerACtot1.cleanup();
        garoWallboxAHemsCurrLim.cleanup();
        garoWallboxBHemsCurrLim.cleanup();
    }

    private void initStubs() throws
            GenDriverException, GenDriverModbusException, GenDriverSocketException,
            RestApiResponseParseException, RestApiServiceCallException, IOException {

        when(wagoModbusDevice.getVal(any(), any())).thenAnswer(
                (Answer<Value>) invocation -> withDelay(500, Float32Value.of(220f)));

        when(clemapRestApiDevice1.getVal(any(), any())).thenAnswer(
                (Answer<Value>) invocation -> withDelay(750, Float32Value.of(20f)));

        when(clemapRestApiDevice2.getVal(any(), any())).thenAnswer(
                (Answer<Value>) invocation -> withDelay(2000, Float64Value.of(50d)));

        doAnswer((Answer<Void>) invocation -> {
            withDelay(500);
            return null;
        }).when(garoModbusDeviceA).setVal(any(), any(), any());

        doAnswer((Answer<Void>) invocation -> {
            withDelay(250);
            return null;
        }).when(garoModbusDeviceB).setVal(any(), any(), any());
    }

    private void initStubsWithException() throws
            GenDriverException, GenDriverModbusException, GenDriverSocketException,
            RestApiResponseParseException, RestApiServiceCallException, IOException {

        when(wagoModbusDevice.getVal(any(), any())).thenAnswer(
                (Answer<Value>) invocation -> {
                    withDelay(500, StringValue.of("220"));
                    throw new GenDriverModbusException(DEVICE_ERROR);
                });

        when(clemapRestApiDevice1.getVal(any(), any())).thenAnswer(
                (Answer<Value>) invocation -> {
                    withDelay(750, StringValue.of("20.0"));
                    throw new RestApiAuthenticationException(DEVICE_ERROR);
                });

        when(clemapRestApiDevice2.getVal(any(), any())).thenAnswer(
                (Answer<Value>) invocation -> {
                    withDelay(2000, StringValue.of("50.0"));
                    throw new RestApiAuthenticationException(DEVICE_ERROR);
                });

        doAnswer((Answer<Void>) invocation -> {
            withDelay(1000);
            throw new GenDriverModbusException(DEVICE_ERROR);
        }).when(garoModbusDeviceA).setVal(any(), any(), any());

        doAnswer((Answer<Void>) invocation -> {
            withDelay(1000);
            throw new GenDriverModbusException(DEVICE_ERROR);
        }).when(garoModbusDeviceB).setVal(any(), any(), any());
    }

    private Value withDelay(long delay, Value value) {
        Awaitility.await().pollDelay(Duration.ofMillis(delay)).until(() -> true);
        LOG.debug("Delay {}ms is over.", delay);
        return value;
    }

    private void withDelay(long delay) {
        Awaitility.await().pollDelay(Duration.ofMillis(delay)).until(() -> true);
        LOG.debug("Delay {}ms is over.", delay);
    }
}

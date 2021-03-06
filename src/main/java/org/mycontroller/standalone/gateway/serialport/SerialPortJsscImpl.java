/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.gateway.serialport;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.GatewayInfo;
import org.mycontroller.standalone.gateway.IMySensorsGateway;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialPortJsscImpl implements IMySensorsGateway {
    private static final Logger _logger = LoggerFactory.getLogger(SerialPortJsscImpl.class.getName());
    private SerialPort serialPort;
    private GatewayInfo gatewayInfo = new GatewayInfo();

    public SerialPortJsscImpl() {
        this.initialize();
    }

    @Override
    public synchronized void write(RawMessage rawMessage) {
        try {
            serialPort.writeBytes(rawMessage.getGWBytes());
        } catch (Exception ex) {
            gatewayInfo.getData().put(SerialPortCommon.IS_CONNECTED, false);
            _logger.error("Exception while writing data, ", ex);
        }
    }

    private void initialize() {
        String[] portNames = SerialPortList.getPortNames();
        _logger.debug("Number of serial port available:{}", portNames.length);
        for (int portNo = 0; portNo < portNames.length; portNo++) {
            _logger.debug("SerialPortJson[{}]:{}", portNo + 1, portNames[portNo]);
        }
        //Update Gateway Info
        gatewayInfo.setType(ObjectFactory.getAppProperties().getGatewayType());
        gatewayInfo.setData(new HashMap<String, Object>());

        gatewayInfo.getData().put(SerialPortCommon.IS_CONNECTED, false);
        gatewayInfo.getData().put(SerialPortCommon.DRIVER_TYPE,
                ObjectFactory.getAppProperties().getGatewaySerialPortDriver());
        gatewayInfo.getData().put(SerialPortCommon.SELECTED_DRIVER_TYPE,
                AppProperties.SERIAL_PORT_DRIVER.JSSC.toString());
        gatewayInfo.getData().put(SerialPortCommon.PORT_NAME,
                ObjectFactory.getAppProperties().getGatewaySerialPortName());
        gatewayInfo.getData().put(SerialPortCommon.BAUD_RATE,
                ObjectFactory.getAppProperties().getGatewaySerialPortBaudRate());

        // create an instance of the serial communications class
        serialPort = new SerialPort(ObjectFactory.getAppProperties().getGatewaySerialPortName());
        try {
            serialPort.openPort();//Open port
            serialPort.setParams(
                    ObjectFactory.getAppProperties().getGatewaySerialPortBaudRate(),
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE,
                    SerialPort.DATABITS_8);
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR + SerialPort.MASK_ERR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            // create and register the serial data listener
            serialPort.addEventListener(new SerialDataListenerJssc(serialPort, gatewayInfo));//Add SerialPortEventListener
            _logger.debug("Serial port initialized with the driver:{}, PortName:{}, BaudRate:{}",
                    ObjectFactory.getAppProperties().getGatewaySerialPortDriver(),
                    ObjectFactory.getAppProperties().getGatewaySerialPortName(),
                    ObjectFactory.getAppProperties().getGatewaySerialPortBaudRate());
            gatewayInfo.getData().put(SerialPortCommon.CONNECTION_STATUS, "Connected Successfully");
            gatewayInfo.getData().put(SerialPortCommon.IS_CONNECTED, true);
            gatewayInfo.getData().put(SerialPortCommon.LAST_SUCCESSFUL_CONNECTION, System.currentTimeMillis());
        } catch (SerialPortException ex) {
            gatewayInfo.getData().put(SerialPortCommon.CONNECTION_STATUS, "ERROR: " + ex.getMessage());
            if (ex.getMessage().contains("Port not found")) {
                _logger.error("Failed to load serial port: {}", ex.getMessage());
            } else {
                _logger.error("Failed to load serial port, ", ex);
            }
        }
    }

    public void close() {
        try {
            this.serialPort.closePort();
            _logger.debug("serialPort{} closed", serialPort.getPortName());
        } catch (SerialPortException ex) {
            if (ex.getMessage().contains("Port not opened")) {
                _logger.debug("unable to close the port, Error: {}", ex.getMessage());
            } else {
                _logger.error("unable to close the port{}", serialPort.getPortName(), ex);
            }
        }

    }

    @Override
    public GatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

}

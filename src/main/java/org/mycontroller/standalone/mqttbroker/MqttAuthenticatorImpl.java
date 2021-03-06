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
package org.mycontroller.standalone.mqttbroker;

import java.nio.charset.StandardCharsets;

import org.eclipse.moquette.spi.impl.security.IAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MqttAuthenticatorImpl implements IAuthenticator {
    private static final Logger _logger = LoggerFactory.getLogger(MqttAuthenticatorImpl.class.getName());

    @Override
    public boolean checkValid(String username, byte[] passwordBytes) {
        @SuppressWarnings("unused")
        String password = new String(passwordBytes, StandardCharsets.UTF_8);
        _logger.debug("Authentication Request from User:{}", username);
        return true;
    }

}

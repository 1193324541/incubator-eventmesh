/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.connector.kafka.config;

import org.apache.eventmesh.common.Constants;
import org.apache.eventmesh.connector.kafka.common.EventMeshConstants;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ConfigurationWrapper {
    public static final Logger logger = LoggerFactory.getLogger(ConfigurationWrapper.class);

    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    public String getProp(String key) {
        return StringUtils.isEmpty(key) ? null : properties.getProperty(key, null);
    }

    /**
     * Load kafka properties file from classpath and conf home.
     * The properties defined in conf home will override classpath.
     */
    private void loadProperties() {
        try (InputStream resourceAsStream = ConfigurationWrapper.class.getResourceAsStream(
            "/" + EventMeshConstants.EVENTMESH_CONF_FILE)) {
            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
            }
        } catch (IOException e) {
            logger.error("Error while loading from classpath:", e);
            throw new RuntimeException(String.format("Load %s.properties file from classpath error", EventMeshConstants.EVENTMESH_CONF_FILE));
        }
        try {
            String configPath = Constants.EVENTMESH_CONF_HOME + File.separator + EventMeshConstants.EVENTMESH_CONF_FILE;
            if (new File(configPath).exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(configPath);
                     InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
                     BufferedReader reader = new BufferedReader(inputStreamReader)) {
                    properties.load(reader);
                }
            }
        } catch (IOException e) {
            logger.error("Error while loading from conf home: ", e);
            throw new IllegalArgumentException(String.format("Cannot load %s file from conf", EventMeshConstants.EVENTMESH_CONF_FILE));
        }
    }
}

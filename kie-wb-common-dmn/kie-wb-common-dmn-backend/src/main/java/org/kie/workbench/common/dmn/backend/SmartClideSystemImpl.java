/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.dmn.backend;

import org.kie.workbench.common.dmn.api.SmartClideSystem;

public class SmartClideSystemImpl implements SmartClideSystem {

    @Override
    public String getTheiaURL() {
        org.slf4j.LoggerFactory.getLogger(SmartClideSystemImpl.class).info(System.getProperty("smartclide.theia.url"));
        return System.getProperty("smartclide.theia.url");
    }

    @Override
    public String getServiceDiscoveryURL() {
        org.slf4j.LoggerFactory.getLogger(SmartClideSystemImpl.class).info(System.getProperty("smartclide.service.discovery.url"));
        return System.getProperty("smartclide.service.discovery.url");
    }
}
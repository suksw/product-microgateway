/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.choreo.connect.tests.testcases.withapim.websocket.throttle;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.choreo.connect.tests.apim.ApimBaseTest;
import org.wso2.choreo.connect.tests.apim.ApimResourceProcessor;
import org.wso2.choreo.connect.tests.apim.utils.StoreUtils;
import org.wso2.choreo.connect.tests.util.TestConstant;
import org.wso2.choreo.connect.tests.util.Utils;
import org.wso2.choreo.connect.tests.util.websocket.WsClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Websocket Throttling for Applications in APIM 4.1.0
 *
 * Supported
 * - Event count throttling via Type REQUESTCOUNTLIMIT
 * - Bandwidth throttling via Type BANDWIDTHLIMIT
 *
 * Not supported
 * - Request count throttling
 */
public class WebsocketAppThrottleClientTestCase extends ApimBaseTest {
    private static final String API_CONTEXT = "websocket-basic"; // Reusing the WebSocketBasicAPI
    private static final String API_VERSION = "1.0.0";
    private static final String BANDWIDTH_THROTTLE_APPLICATION_NAME = "WebSocketAppBandwidthThrottleApp";
    private static final String EVENT_COUNT_THROTTLE_APPLICATION_NAME = "WebSocketAppEventCountThrottleApp";
    private static final int throttleBandwidth = 3;
    private static final int throttleCount = 5;

    private String endpoint;

    @BeforeClass(alwaysRun = true, description = "Create access token and define endpoint URL")
    void setEnvironment() throws Exception {
        super.initWithSuperTenant();
        endpoint = Utils.getServiceURLWebSocket(API_CONTEXT + "/" + API_VERSION);
    }

    @Test(description = "Test API level throttling with default limits")
    public void testAppClientBandwidthThrottling() throws Exception {
        String applicationId = ApimResourceProcessor.applicationNameToId.get(BANDWIDTH_THROTTLE_APPLICATION_NAME);
        String accessToken = StoreUtils.generateUserAccessToken(apimServiceURLHttps, applicationId,
                user, storeRestClient);
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.AUTHORIZATION_HEADER, "Bearer " + accessToken);

        WsClient wsClient = new WsClient(endpoint, headers);
        Assert.assertTrue(wsClient.isClientBandwidthThrottled(throttleBandwidth),
                "Websocket connection was not throttled on bandwidth.");
    }

    @Test(description = "Test API level throttling with default limits")
    public void testAppClientEventCountThrottling() throws Exception {
        String applicationId = ApimResourceProcessor.applicationNameToId.get(EVENT_COUNT_THROTTLE_APPLICATION_NAME);
        String accessToken = StoreUtils.generateUserAccessToken(apimServiceURLHttps, applicationId,
                user, storeRestClient);
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.AUTHORIZATION_HEADER, "Bearer " + accessToken);

        WsClient wsClient = new WsClient(endpoint, headers);
        Assert.assertTrue(wsClient.isClientEventCountThrottled(throttleCount),
                "Websocket connection was not throttled on event count.");
    }
}

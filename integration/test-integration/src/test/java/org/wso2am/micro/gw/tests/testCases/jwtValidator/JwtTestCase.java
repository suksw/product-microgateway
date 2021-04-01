/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2am.micro.gw.tests.testCases.jwtValidator;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2am.micro.gw.tests.util.HttpResponse;
import org.wso2am.micro.gw.tests.util.HttpsClientRequest;
import org.wso2am.micro.gw.tests.util.TestConstant;
import org.wso2am.micro.gw.tests.util.TokenUtil;
import org.wso2am.micro.gw.tests.util.Utils;
import java.util.HashMap;
import java.util.Map;


/**
 * Jwt test cases.
 *
 */
public class JwtTestCase {
    private String jwtWithoutScope;
    private String jwtWithScope;
    private String jwtWithMultipleScopes;
    private String jwtWithMultipleInvalidScopes;

    @BeforeClass(description = "initialise the setup")
    void start() throws Exception {
        jwtWithoutScope = TokenUtil.getJwtForPetstore(TestConstant.KEY_TYPE_PRODUCTION, null,
                false);
        jwtWithScope = TokenUtil.getJwtForPetstore(TestConstant.KEY_TYPE_PRODUCTION, "write:pets",
                false);
        jwtWithMultipleScopes = TokenUtil.getJwtForPetstore(TestConstant.KEY_TYPE_PRODUCTION,
                "write:pets read:pets", false);
        jwtWithMultipleInvalidScopes = TokenUtil.getJwtForPetstore(TestConstant.KEY_TYPE_PRODUCTION, "foo bar",
                false);
    }

    @Test(description = "Test to check the JWT auth working")
    public void invokeJWTHeaderSuccessTest() throws Exception {

        // Set header
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderNames.AUTHORIZATION.toString(), "Bearer " + jwtWithoutScope);
        HttpResponse response = HttpsClientRequest.doGet(Utils.getServiceURLHttps(
                "/v2/pet/2") , headers);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_OK,"Response code mismatched");
    }

    @Test(description = "Test to check the JWT auth validate invalida signature token")
    public void invokeJWTHeaderInvalidTokenTest() throws Exception {

        // Set header
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderNames.AUTHORIZATION.toString(), "Bearer " + TestConstant.INVALID_JWT_TOKEN);
        HttpResponse response = HttpsClientRequest.doGet(Utils.getServiceURLHttps(
                "/v2/pet/2") , headers);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_UNAUTHORIZED, "Response code mismatched");
    }

    @Test(description = "Test to check the JWT auth validate expired token")
    public void invokeJWTHeaderExpiredTokenTest() throws Exception {

        // Set header
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaderNames.AUTHORIZATION.toString(), "Bearer " + TestConstant.EXPIRED_JWT_TOKEN);
        HttpResponse response = HttpsClientRequest.doGet(Utils.getServiceURLHttps(
                "/v2/pet/2") , headers);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_UNAUTHORIZED,
                "Response code mismatched");
    }
}
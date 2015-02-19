/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mb.integration.tests.amqp.functional;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.mb.integration.common.clients.AndesClient;
import org.wso2.mb.integration.common.clients.operations.utils.AndesClientUtils;
import org.wso2.mb.integration.common.utils.backend.MBIntegrationBaseTest;

/**
 * Testing for multi tenant - queues specific test case
 */
public class MultiTenantQueueTestCase extends MBIntegrationBaseTest {

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_USER);
        AndesClientUtils.sleepForInterval(15000);
    }

    @Test(groups = "wso2.mb", description = "Single Tenant with multiple Users Test")
    public void performSingleTenantMultipleUserQueueTestCase() {
        int sendMessageCount = 200;
        int runTime = 40;
        int expectedMessageCount = 200;

        // Start receiving clients (admin, user1, user2)

        AndesClient adminReceivingClient = new AndesClient("receive", "127.0.0.1:5672", "queue:topictenant1.com/tenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "admin!topictenant1.com", "admin");
        adminReceivingClient.startWorking();

        // Start sending clients (tenant1, tenant2 and admin)
        AndesClient tenant1SendingClient = new AndesClient("send", "127.0.0.1:5672", "queue:topictenant1.com/tenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(sendMessageCount), "1",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendMessageCount, "","topictenantuser1!topictenant1.com", "topictenantuser1");

        tenant1SendingClient.startWorking();

        boolean adminReceiveSuccess =  AndesClientUtils.waitUntilMessagesAreReceived(adminReceivingClient,
                expectedMessageCount, runTime);

        boolean tenant1SendSuccess = AndesClientUtils.getIfSenderIsSuccess(tenant1SendingClient, sendMessageCount);

        Assert.assertTrue(tenant1SendSuccess, "Sending failed for tenant 1 user 1.");
        Assert.assertEquals(expectedMessageCount,adminReceivingClient.getReceivedTopicMessagecount());
        Assert.assertTrue(adminReceiveSuccess, "Message receiving failed for admin of tenant 1.");

    }


    @Test(groups = "wso2.mb", description = "Multiple Tenant Single Users Test")
    public void performMultipleTenantQueueTestCase() {
        int sendMessageCount = 100;
        int runTime = 20;
        int expectedMessageCount = 200;

        // Start receiving clients (tenant1, tenant2 and admin)
        AndesClient tenant1ReceivingClient = new AndesClient("receive", "127.0.0.1:5672", "queue:topictenant1.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "topictenantuser1!topictenant1.com", "topictenantuser1");
        tenant1ReceivingClient.startWorking();

        AndesClient tenant2ReceivingClient = new AndesClient("receive", "127.0.0.1:5672", "queue:topictenant2.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(expectedMessageCount),
                "1", "listener=true,ackMode=1,delayBetweenMsg=0,stopAfter=" + expectedMessageCount, "",
                "topictenantuser1!topictenant2.com", "topictenantuser1");
        tenant2ReceivingClient.startWorking();

        // Start sending clients (tenant1, tenant2 and admin)
        AndesClient tenant1SendingClient = new AndesClient("send", "127.0.0.1:5672", "queue:topictenant2.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(sendMessageCount), "1",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendMessageCount, "",
                "topictenantuser1!topictenant2.com", "topictenantuser1");

        tenant1SendingClient.startWorking();

        AndesClient tenant2SendingClient = new AndesClient("send", "127.0.0.1:5672", "queue:topictenant1.com/multitenantTopic",
                "100", "false", Integer.toString(runTime), Integer.toString(sendMessageCount), "1",
                "ackMode=1,delayBetweenMsg=0,stopAfter=" + sendMessageCount, "",
                "topictenantuser1!topictenant1.com", "topictenantuser1");
        tenant2SendingClient.startWorking();

        boolean tenet1ReceiveSuccess = AndesClientUtils.waitUntilMessagesAreReceived(tenant1ReceivingClient,
                expectedMessageCount, runTime);
        boolean tenant2ReceiveSuccess =  AndesClientUtils.waitUntilMessagesAreReceived(tenant2ReceivingClient,
                expectedMessageCount, runTime);

        boolean tenant1SendSuccess = AndesClientUtils.getIfSenderIsSuccess(tenant1SendingClient, sendMessageCount);
        boolean tenant2SendSuccess = AndesClientUtils.getIfSenderIsSuccess(tenant2SendingClient, sendMessageCount);

        Assert.assertTrue(tenant1SendSuccess, "Sending failed for tenant 1 user 1.");
        Assert.assertTrue(tenant2SendSuccess, "Sending failed for tenant 2 user 1.");
        Assert.assertEquals(tenant2ReceivingClient.getReceivedqueueMessagecount(),sendMessageCount,"Tenant 2 client received the message published to Tenant1");
        Assert.assertEquals(tenant1ReceivingClient.getReceivedqueueMessagecount(),sendMessageCount,"Tenant 1 client received the message published to Tenant2");
    }
}
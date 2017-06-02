/*
 * Copyright (c)2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mb.migration;

import org.apache.log4j.Logger;

import java.io.IOException;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {


        try {

            //Create new processor to read, modify and write data into the database
            Processor processor = new Processor();

            //Create DLC message router since it was not present in WSO2MB 3.1.0
            processor.creteDlcMessageRouter();

            //Modify bindings since the format of the binding details string is different in WSO2MB 3.1.0 and WSO2MB 3.2.0
            processor.modifyBindings();

            //Modify data in multiple tables making queue name references all simple.
            processor.makeQueueNamesAllSimple();

            logger.info("Migration completed successfully");

        } catch (IOException | ClassNotFoundException e) {
            logger.error("Could not initiate the migration", e);
        } catch (MigrationException e) {
            logger.error("Error during migration. Migration will not proceed", e);
        }

    }
}
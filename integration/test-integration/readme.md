# Integration Tests

## Standalone Mode - How to Add a Testcase 
1. Add the testcase class to an existing or a new folder in 
   `src/test/java/org/wso2/choreo/connect/tests/testcases/standalone`
2. If an api needs to be deployed,     
   i. add its openapi/swagger definition to `src/test/resources/openAPIs`       
   ii. create and deploy the project in the class `src/test/java/org/wso2/choreo/connect/tests/setup/standalone/CcWithDefaultConf.java`
      - reason to create and deploy all apis in one location is to reduce the waiting time to deploy
      - `CcWithDefaultConf.java` is the Choreo Connect with the default config toml. Other such classes in the 
        `setup/standalone` folder represents different Choreo Connect instances with different configurations. 
        You can pick accordingly.
3. Within the testcase, assume that the api has already been deployed and invoke as needed.
4. Test classes does not have a base test class in the standalone mode. Therefore, the test class does not need to extend 
   a parent class.
5. Importantly, add the testcase to the `src/test/resources/testng-cc-standalone.xml` file. Pick the group
   according to the class you deployed your API in 2. ii. 

## WithAPIM Mode - How to Add a Testcase
1. Add the APIs, Applications, and Subscriptions that needs to be created, to the json files in 
   `src/test/resources/apimApisAppsSubs`. These will be added to API Manager before 
   starting any of the testcases.
2. Add a testcase class to `src/test/java/org/wso2/choreo/connect/tests/testcases/withapim`. 
3. Extend the class `org.wso2.choreo.connect.tests.apim.ApimBaseTest` and use `super.initWithSuperTenant()` **or**
   `super.init(userMode)` as required.
4. Assume that API-M and CC have already started and invoke APIs using the util methods. 
   NOTE: Currently, only APIs, Applications, and Subscriptions are deployed via the above json files. Therefore, changes
   on admin side needs to be deployed within the testcase and cleaned within the testcase as well.
5. Finally, add the same class to one or both of the following test groups in the `src/test/resources/testng-cc-with-apim.xml`. in the `src/test/resources/testng-cc-with-apim.xml`.
      - `apis-apps-subs-pulled-at-startup`: Here, we create the APIs, Applications, and Subscriptions first 
        and start CC afterwords. Thus, CC pulls them during startup, rather than pulling it after getting a 
        notification from eventhub. Since only the 1st method of the class actually tests whether the startup pull was
        successful, we only run the 1st method of the test class. Ex:
        ```
        <class name="org.wso2.choreo.connect.tests.testcases.withapim.BlockedApiTestCase">
            <methods><include name="testPublishedStateAPI"/></methods>
        </class>
        ```
      - `all-events-received-via-eventhub`: In this group, we test the eventhub scenario.
        By the time this group starts running, CC has already started.
        Therefore, the `ApimPreparer` first deletes the APIs, Applications, and Subscriptions that were already created.
        Then creates the same resources again, before running the tests.

### How to Avoid API Manager Restarting Everytime the Tests are Run
1. Run the tests ones
2. Copy the `apim` folder in the `integration/test-integration/target` to a different location
3. cd into that `apim` folder and run `docker-compose up`
4. Search and comment the three `Apim...Executer` the test classes in `integration/test-integration/src/test/resources/testng-cc-with-apim.xml`. 
   The three classes are `ApimStartupExecutor`, `ApimRestartExecutor` and `ApimShutdownExecutor`.
5. Now run the tests with `mvn clean install` while in the `integration/test-integration` folder
6. Once done, uncomment the previously commented `ApimStartupShutdownExecutor` test class, stop the running apim 
   instance, and run all the tests with `mvn clean install`.
   
## Standalone Mode and Withapim Mode - How to Test with a new CC Instance
NOTE: Only if an instance with a completely new config is extremely necessary
1. Add the new config toml to `src/test/resources/configs` and rename it to resemble the requirement
2. Create a `setup` class
    i. for standalone mode, create a new class in `src/test/java/org/wso2/choreo/connect/tests/setup/standalone`
   with a name starting with `Cc`. This will start CC and also stop CC
    ii. for withapim mode, create a new class in `src/test/java/org/wso2/choreo/connect/tests/setup/withapim` 
   with a name starting with `CcStartupExecutor`. This will start CC. The existing class `CcShutdownExecutor` can be 
   commonly used to stop any `CcStartupExecutor`
3. Refer to the existing `CcStartupExecutor` and start the CC instance by specifying the config names added during step
   `1.`
4. in the testng file,
   i. for standalone mode, add a new test tag group, and add the new `Cc` class as the first in the group
   ii. for withapim mode, create a test group, and follow the pattern given below
   ```
   <test>
        <class name="ApimPreparer"/>
        <class name="CcStartupExecutor"/>
        .
        . . test classes to run . .
        .
        <class name="CcShutdownExecutor"/>
   </test>
   ```

## Withapim Mode - How to Test with a new API-M instance
NOTE: Not encouraged at all
1. Create a class similar to `org.wso2.choreo.connect.tests.setup.withapim.ApimStartupExecutor` and add a
   `beforeSuite` methods that starts the apim instance. (Don't have to write a new "shutdownExecuter",
   the existing can be used as it is. Yet, will have to make the `ApimInstance` class configurable like `CcInstance`)
2. Add a new testng file and follow the pattern given below
    ```
   <test>
        <class name="ApimStartupShutdownExecutorWithSpecialDepYaml"/>
        <class name="ApimClientsPreparer"/>
   </test>
   <test>
   </test>
   <test>
        <class name="ApimShutdownExecutor"/>
   </test>
   ```
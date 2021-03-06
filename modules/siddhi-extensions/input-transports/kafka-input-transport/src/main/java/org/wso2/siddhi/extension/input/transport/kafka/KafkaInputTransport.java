/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.extension.input.transport.kafka;

import kafka.consumer.ConsumerConfig;
import org.apache.log4j.Logger;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.stream.input.source.InputTransport;
import org.wso2.siddhi.core.util.transport.OptionHolder;

import java.util.Properties;
import java.util.concurrent.*;

@Extension(
        name = "kafka",
        namespace = "inputtransport",
        description = ""
)
public class KafkaInputTransport extends InputTransport {

    private SourceEventListener sourceEventListener;
    private ScheduledExecutorService executorService;
    private static ThreadPoolExecutor threadPoolExecutor;
    private OptionHolder optionHolder;
    private ConsumerKafkaGroup consumerKafkaGroup;

    //    public static final int ADAPTER_CORE_POOL_SIZE = 8;
//    public final static String ADAPTER_CORE_POOL_SIZE_NAME = "kafka";
    public final static String ADAPTER_NAME = "adapter.name";
    public static final String EVENTS_DUPLICATED_IN_CLUSTER_NAME = "events.duplicated.in.cluster";
    public static final boolean EVENTS_DUPLICATED_IN_CLUSTER = false;
    public final static String ADAPTOR_SUBSCRIBER_TOPIC = "topic";
    public final static String ADAPTOR_SUBSCRIBER_GROUP_ID = "group.id";
    public final static String ADAPTOR_SUBSCRIBER_ZOOKEEPER_CONNECT_SERVERS = "bootstrap.servers";
    public final static String ADAPTOR_SUBSCRIBER_THREADS = "threads";
    public final static String ADAPTOR_SUBSCRIBER_PARTITION_NO_LIST = "partition.no.list";
    public final static String ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES = "optional.configuration";
    public static final int AXIS_TIME_INTERVAL_IN_MILLISECONDS = 10000;

    private static final Logger log = Logger.getLogger(KafkaInputTransport.class);

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder) {
        this.sourceEventListener = sourceEventListener;
        this.optionHolder = optionHolder;
    }

    @Override
    public void connect() throws ConnectionUnavailableException {
        createKafkaAdaptorListener();
    }

    @Override
    public void disconnect() {
        if (consumerKafkaGroup != null) {
            consumerKafkaGroup.shutdown();
            String topic = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_TOPIC);
            log.debug("Adapter " + topic + " disconnected " + topic);
        }
    }

    @Override
    public void destroy() {

    }

    private void createKafkaAdaptorListener() throws ConnectionUnavailableException{

        String zkServerList = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_ZOOKEEPER_CONNECT_SERVERS);
        String groupID = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_GROUP_ID);
        String partitionList = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_PARTITION_NO_LIST);

        //todo: consumers same group id still gets the same message
        String threadsStr = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_THREADS);
        int threads = Integer.parseInt(threadsStr);
        threads = 1;
        String topic = optionHolder.validateAndGetStaticValue(ADAPTOR_SUBSCRIBER_TOPIC);
        consumerKafkaGroup = new ConsumerKafkaGroup(topic, partitionList, KafkaInputTransport.createConsumerConfig(zkServerList, groupID));
        consumerKafkaGroup.run(threads, sourceEventListener);
    }

    private static Properties createConsumerConfig(String zkServerList, String groupId) {
        Properties props = new Properties();
        props.put(ADAPTOR_SUBSCRIBER_ZOOKEEPER_CONNECT_SERVERS, zkServerList);
        props.put(ADAPTOR_SUBSCRIBER_GROUP_ID, groupId);
        props.put("session.timeout.ms", "30000");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    private static ConsumerConfig createConsumerConfig2(String zookeeper, String groupId,
                                                       String optionalConfigs) throws ConnectionUnavailableException{
        try {
            Properties props = new Properties();
            props.put(ADAPTOR_SUBSCRIBER_ZOOKEEPER_CONNECT_SERVERS, zookeeper);
            props.put(ADAPTOR_SUBSCRIBER_GROUP_ID, groupId);
            if (optionalConfigs != null) {
                String[] optionalProperties = optionalConfigs.split(",");
                if (optionalProperties != null) {
                    for (String header : optionalProperties) {
                        String[] configPropertyWithValue = header.split(":", 2);
                        if (configPropertyWithValue.length == 2) {
                            props.put(configPropertyWithValue[0], configPropertyWithValue[1]);
                        } else {
                            log.warn("Optional configuration property not defined in the correct format.\nRequired - property_name1:property_value1,property_name2:property_value2\nFound - " + optionalConfigs);
                        }
                    }
                }
            }
            return new ConsumerConfig(props);
        } catch (NoClassDefFoundError e) {
            throw new ConnectionUnavailableException("Cannot access kafka context due to missing jars", e);
        }
    }
}
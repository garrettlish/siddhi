/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.stream.event;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.event.stream.converter.ConversionStreamEventChunk;
import org.wso2.siddhi.core.event.stream.converter.StreamEventConverter;
import org.wso2.siddhi.core.event.stream.converter.ZeroStreamEventConverter;


public class ComplexEventChunkTestCase {
    private int count;
    private StreamEventConverter streamEventConverter;

    @Before
    public void init() {
        count = 0;
        streamEventConverter = new ZeroStreamEventConverter();
    }


    @Test
    public void EventChunkTest() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 1l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 2l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 3l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter, streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            StreamEvent event = streamEventChunk.next();
            Assert.assertEquals(count * 1l, event.getOutputData()[2]);
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void EventChunkRemoveTest1() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 1l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 2l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 3l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter, streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            streamEventChunk.next();
            if (count == 1) {
                streamEventChunk.remove();
            }
        }
        Assert.assertEquals(streamEvent2, streamEventChunk.getFirst());
    }

    @Test
    public void EventChunkRemoveTest2() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 1l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 2l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 3l});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 3);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700l, 4l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter, streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            streamEventChunk.next();
            if (count == 1 || count == 2) {
                streamEventChunk.remove();
            }
        }
        StreamEvent streamEvent = streamEventChunk.getFirst();
        Assert.assertEquals(streamEvent3, streamEvent);
        Assert.assertEquals(streamEvent4, streamEvent.getNext());
    }

    @Test
    public void EventChunkRemoveTest3() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 3);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter, streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            streamEventChunk.next();
            streamEventChunk.remove();
        }

        Assert.assertNull(streamEventChunk.getFirst());
    }

    @Test
    public void EventChunkRemoveTest4() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent3 = new StreamEvent(0, 0, 3);
        streamEvent3.setOutputData(new Object[]{"WSO2", 700l, 100l});

        StreamEvent streamEvent4 = new StreamEvent(0, 0, 3);
        streamEvent4.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);
        streamEvent2.setNext(streamEvent3);
        streamEvent3.setNext(streamEvent4);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter, streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        while (streamEventChunk.hasNext()) {
            count++;
            streamEventChunk.next();
            if (count == 2 || count == 4) {
                streamEventChunk.remove();
            }
        }
        StreamEvent streamEvent = streamEventChunk.getFirst();
        Assert.assertEquals(streamEvent1, streamEvent);
        Assert.assertEquals(streamEvent3, streamEvent.getNext());
        Assert.assertNull(streamEvent.getNext().getNext());
    }

    @Test(expected = IllegalStateException.class)
    public void EventChunkRemoveTest5() {
        StreamEvent streamEvent1 = new StreamEvent(0, 0, 3);
        streamEvent1.setOutputData(new Object[]{"IBM", 700l, 100l});

        StreamEvent streamEvent2 = new StreamEvent(0, 0, 3);
        streamEvent2.setOutputData(new Object[]{"WSO2", 700l, 100l});

        streamEvent1.setNext(streamEvent2);

        StreamEventPool streamEventPool = new StreamEventPool(0, 0, 3, 5);
        ConversionStreamEventChunk streamEventChunk = new ConversionStreamEventChunk(streamEventConverter, streamEventPool);
        streamEventChunk.convertAndAssign(streamEvent1);

        streamEventChunk.remove();
        streamEventChunk.remove();
    }


}

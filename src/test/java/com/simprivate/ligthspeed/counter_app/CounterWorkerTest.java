package com.simprivate.ligthspeed.counter_app;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CounterWorkerTest {
    @Mock
    CounterService counterService = Mockito.mock(CounterService.class);

    void prepareData(String fileBuff) {
        when(counterService.updateBuffer(any(byte[].class), any(int.class))).then(answer -> {
            byte[] buffer = answer.getArgument(0);
            int bufferLen = buffer.length - 1;
            int dataSize = Math.min(fileBuff.length(), bufferLen);
            System.arraycopy(fileBuff.getBytes(), 0, buffer,0, dataSize);
            return dataSize;
        });
    }

    @Test
    public void validIpReadTest() {
        CounterWorker counterWorker = new CounterWorker(0, counterService);
        prepareData("4.3.2.1");

        counterWorker.rereadBuffer(0);
        int addr = counterWorker.getAddress();

        assertEquals(Integer.parseUnsignedInt("01020304", 16), addr);

        verify(counterService).updateBuffer(any(), anyInt());
    }

    @Test
    public void invalidIpByteTest() {
        CounterWorker counterWorker = new CounterWorker(0, counterService);
        prepareData("444.3.2.1");

        counterWorker.rereadBuffer(0);
        Exception ex = assertThrows( IllegalArgumentException.class, counterWorker::getAddress);
        assertTrue(ex.getMessage().contains("444"));
    }

    @Test
    public void invalidIpSymbol1Test() {
        CounterWorker counterWorker = new CounterWorker(0, counterService);
        prepareData("4,3.2.1");

        counterWorker.rereadBuffer(0);
        Exception ex = assertThrows( IllegalArgumentException.class, counterWorker::getAddress);
        assertTrue(ex.getMessage().contains("','"));
    }

    @Test
    public void invalidIpSymbolXTest() {
        CounterWorker counterWorker = new CounterWorker(0, counterService);
        prepareData("4.x.2.1");

        counterWorker.rereadBuffer(0);
        Exception ex = assertThrows( IllegalArgumentException.class, counterWorker::getAddress);
        assertTrue(ex.getMessage().contains("'x'"));
    }

    @Test
    public void invalidIpSymbol0Test() {
        CounterWorker counterWorker = new CounterWorker(0, counterService);
        prepareData("4.3.2.");

        counterWorker.rereadBuffer(0);
        Exception ex = assertThrows( IllegalArgumentException.class, counterWorker::getAddress);
        assertTrue(ex.getMessage().contains("ascii (10)"));
    }

    @Test
    public void invalidIpSymbolNTest() {
        CounterWorker counterWorker = new CounterWorker(0, counterService);
        prepareData("4.3.2");

        counterWorker.rereadBuffer(0);
        Exception ex = assertThrows( IllegalArgumentException.class, counterWorker::getAddress);
        assertTrue(ex.getMessage().contains("ascii (10)"));
    }

    @Test
    public void invalidIpSpaceTest() {
        CounterWorker counterWorker = new CounterWorker(0, counterService);
        prepareData("4.3 .2.1");

        counterWorker.rereadBuffer(0);
        Exception ex = assertThrows( IllegalArgumentException.class, counterWorker::getAddress);
        assertTrue(ex.getMessage().contains("' '"));
    }
}
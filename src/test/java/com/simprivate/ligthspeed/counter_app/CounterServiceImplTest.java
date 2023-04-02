package com.simprivate.ligthspeed.counter_app;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class CounterServiceImplTest {

    @Test
    void checkAndIncrement1() {
        CounterServiceImpl counterService = new CounterServiceImpl();
        AtomicLong count = new AtomicLong();

        counterService.checkAndIncrement(0x10101010, count);
        assertEquals(1, count.get());
        assertEquals(1, counterService.getItemsCount());

        counterService.checkAndIncrement(0x10101010, count);
        assertEquals(1, count.get());
        assertEquals(2, counterService.getItemsCount());
    }

    @Test
    void checkAndIncrement2() {
        CounterServiceImpl counterService = new CounterServiceImpl();
        AtomicLong count = new AtomicLong();

        counterService.checkAndIncrement(0x00000000, count);
        assertEquals(1, count.get());
        assertEquals(1, counterService.getItemsCount());

        counterService.checkAndIncrement(0x00000001, count);
        assertEquals(2, count.get());
        assertEquals(2, counterService.getItemsCount());
    }

    @Test
    void checkAndIncrement3() {
        CounterServiceImpl counterService = new CounterServiceImpl();
        AtomicLong count = new AtomicLong();

        counterService.checkAndIncrement(0x10101010, count);
        assertEquals(1, count.get());
        assertEquals(1, counterService.getItemsCount());

        counterService.checkAndIncrement(0x01010101, count);
        assertEquals(2, count.get());
        assertEquals(2, counterService.getItemsCount());
    }
}
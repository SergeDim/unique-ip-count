package com.simprivate.ligthspeed.counter_app;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public interface CounterService {
    void openFile(String fileName) throws FileNotFoundException;

    void closeFile() throws IOException;

    long getItemsCount();

    long getFileOffset();

    int updateBuffer(byte[] buffer, int buffSize);

    void checkAndIncrement(int addr, AtomicLong count);
}

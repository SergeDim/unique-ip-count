package com.simprivate.ligthspeed.counter_app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class CounterServiceImpl implements CounterService
{
    private final byte[] arrIP = new byte[0x2000_0000]; // 1_0000_0000 / 8 bit
    private final Object[] arrMonitor = new Object[0x10000];
    private FileInputStream is;
    private final AtomicLong itemsCount = new AtomicLong(0);
    private long fileOffset;

    public CounterServiceImpl() {
        for (int i = 0; i < arrMonitor.length; i++) {
            arrMonitor[i] = new Object();
        }
    }

    @Override
    public void openFile(String fileName) throws FileNotFoundException {
        is = new FileInputStream(fileName);
        fileOffset = 0;
    }

    @Override
    public void closeFile() throws IOException {
        is.close();
    }

    @Override
    public void checkAndIncrement(int addr, AtomicLong count) {
        itemsCount.incrementAndGet();
        int byteIdx = addr >>> 3;
        int bitIdx = addr & 7;
        byte bitVal = (byte)(1 << bitIdx);
        synchronized (arrMonitor[byteIdx & 0xFFFF]) {
            byte a = arrIP[byteIdx];
            if ((a & bitVal) == 0) {
                count.incrementAndGet();
                arrIP[byteIdx] = (byte) (a | bitVal);
            }
        }
    }

    @Override
    public synchronized
    int updateBuffer(byte[] buffer, final int buff_size) {
        //System.out.println("updateBuffer "+ Thread.currentThread().getName());
        int len = -1;
        try {
            len = is.read(buffer, 0, buff_size);
            if (len <= 0)
                return len;
            // добиваем IP до конца адреса
            byte ch = buffer[len - 1];
            while (ch != '\r' && ch != '\n') {
                ch = (byte) is.read();
                if (ch < 0) {
                    break;
                }
                buffer[len++] = ch;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (len > 0) {
            fileOffset += len;
        }
        return len;
    }

    @Override
    public long getItemsCount() {
        return itemsCount.get();
    }

    @Override
    public long getFileOffset() {
        return fileOffset;
    }
}

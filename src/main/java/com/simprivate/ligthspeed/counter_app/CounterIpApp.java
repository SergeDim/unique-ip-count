package com.simprivate.ligthspeed.counter_app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CounterIpApp {

    CounterService counterService = new CounterServiceImpl();
    String fileName;

    public CounterIpApp(String fileName) {
        this.fileName = fileName;
    }

    public long run() {
        long count = 0;
        try {
            counterService.openFile(fileName);
            int nProc = Runtime.getRuntime().availableProcessors();
            //nProc = 2;
            List<CounterWorker> list = new ArrayList<>();
            for (int i = 0; i < nProc; i++) {
                CounterWorker worker = new CounterWorker(i, counterService);
                list.add(worker);
                worker.start();
            }
            try {
                for (CounterWorker worker : list) {
                    worker.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counterService.closeFile();

            count = list.stream().mapToLong(CounterWorker::getCount).sum();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void main(String[] args) {
        boolean nostat = (args.length > 1 && "-nostat".equals(args[1]));
        if (!nostat) {
            System.out.println("Unique IPs counter (c) SergeDim for Lightspeed");
        }
        if (args.length < 1) {
            System.out.println("Usage:");
            System.out.println("java -jar count-unique-ip.jar <ip_file.txt> [-nostat]");
            return;
        }
        CounterIpApp application = new CounterIpApp(args[0]);
        if (!nostat) {
            application.runStatisticTimer(1000);
        }
        long time = System.currentTimeMillis();
        long count = application.run();
        long endTime = System.currentTimeMillis();

        if (nostat) {
            System.out.println(count);
        } else {
            System.out.printf("\nUnique IPs = %,d", count);
            System.out.printf("\nTime = %.3f (c)\n", (endTime - time) / 1000.0);
        }
    }

    private void runStatisticTimer(int period_ms) {
        Timer timer = new Timer("LogTimer", true);
        timer.schedule(new TimerTask() {
            long lastVal = 0;
            @Override
            public void run() {
                long newVal = counterService.getItemsCount();
                System.out.printf("offset: %,d Mb;  %,d (ip/sec)\r", counterService.getFileOffset() >>> 20, newVal - lastVal);
                lastVal = newVal;
            }
        }, period_ms, period_ms);
    }
}

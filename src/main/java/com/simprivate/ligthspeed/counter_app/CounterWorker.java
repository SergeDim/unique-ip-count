package com.simprivate.ligthspeed.counter_app;

import java.util.concurrent.atomic.AtomicLong;

public class CounterWorker extends Thread
{
    private static final int BUFF_SIZE = 0x10_0000;// 1Mb
    private static final int MAX_IP_LEN = 4*4;

    private final CounterService counterService;

    int idx = 0;
    byte[] buffer = new byte[BUFF_SIZE + MAX_IP_LEN];
    AtomicLong count = new AtomicLong(0);

    public CounterWorker(int i, CounterService counterService) {
        super("CounterWorker-" + i);
        this.counterService = counterService;
    }

    public void run() {
        int len = 0;
        idx = 0;
        while ((len = rereadBuffer(len)) > 0) {
            // пропускаем \r\n \t
            skipSpaces(len);

            if (idx < len) try {
                int addr = getAddress();
                counterService.checkAndIncrement(addr, count);
            } catch (IllegalArgumentException ex) {
                System.err.println(ex.getMessage());
                // пропускаем этот адрес до перевода строки
                skipToEol(len);
            }
        }
    }

    private void skipToEol(int len) {
        byte ch = buffer[idx];
        while (idx < len && !(ch == '\r' || ch == '\n')) {
            ch = buffer[++idx];
        }
    }

    private void skipSpaces(int len) {
        byte ch = buffer[idx];
        while (idx < len && (ch == '\r' || ch == '\n' || ch == ' ' || ch == '\t')) {
            ch = buffer[++idx];
        }
    }

    protected int getAddress() {
        int addr = 0;
        int shift = 0;
        for (int i = 0; i < 4; i++) {
            int b = 0;
            int idx0 = idx;
            while (buffer[idx] >= '0' && buffer[idx] <= '9') {
                b = b * 10 + buffer[idx++] - '0';
            }
            // ---- Проверка ошибок - здесь ----
            if (b > 255)  {
                String msg = "Invalid IP byte value: " + b;
                throw new IllegalArgumentException(msg);
            }
            byte ch = buffer[idx];
            if (i < 3 && ch != '.' || i == 3 && ch != '\n' && ch != '\r' && ch != ' ' && ch != '\t'
                    || idx == idx0) { // между точками не цифра
                String msg = String.format("Invalid IP symbol: ascii (%d) '%c'", ch, (ch >= 0x20 ? (char) ch : ' '));
                throw new IllegalArgumentException(msg);
            }
            // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

            // пропускаем точку или перевод строки
            idx++;
            // собираем 16-ричный адрес в обратном порядке байт (чтобы разнести в хэше близкие адреса)
            addr += (b << shift);
            shift += 8;
        }
        return addr;
    }

    public long getCount() {
        return count.get();
    }

    public int rereadBuffer(int len) {
        if (idx >= len) {
            len = counterService.updateBuffer(buffer, BUFF_SIZE);
            idx = 0;
            // Закрываем буфер, если последняя строка в файле без EOL
            // чтобы всё время не проверять на EOF
            if (len > 0)
                buffer[len] = '\n';
            else
                return -1;
        }
        return len;
    }
}

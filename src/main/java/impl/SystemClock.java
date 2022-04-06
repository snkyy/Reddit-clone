package impl;

import forum.services.Clock;

// zegar systemowy
public class SystemClock implements Clock {
    @Override
    public long time() {
        return System.currentTimeMillis() / 1000;
    }
}

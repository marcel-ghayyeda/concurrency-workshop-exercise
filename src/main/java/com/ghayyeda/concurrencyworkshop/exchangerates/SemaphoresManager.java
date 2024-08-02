package com.ghayyeda.concurrencyworkshop.exchangerates;

import java.util.List;
import java.util.concurrent.Semaphore;

public class SemaphoresManager {

    private final List<Semaphore> semaphores;

    public SemaphoresManager(List<Semaphore> semaphores) {
        this.semaphores = semaphores;
    }

    boolean tryAcquireAll() {
        boolean notAcquired = false;
        for (int i = 0; i < semaphores.size(); i++) {
            boolean acquired = semaphores.get(i).tryAcquire();
            if (!acquired) {
                for (int j = 0; j < i; j++) {
                    semaphores.get(j).release();
                }
                notAcquired = true;
                break;
            }
        }
        return !notAcquired;
    }

}

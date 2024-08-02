package com.ghayyeda.concurrencyworkshop.exchangerates;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.Semaphore;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemaphoresManagerTest {

    @Test
    void shouldAcquireAllSemaphore() {
        //given
        Semaphore sem1 = new Semaphore(1);
        Semaphore sem2 = new Semaphore(2);
        Semaphore sem3 = new Semaphore(3);
        Semaphore sem4 = new Semaphore(1);
        SemaphoresManager semaphoresManager = new SemaphoresManager(List.of(sem1, sem2, sem3, sem4));

        //when
        boolean acquired = semaphoresManager.tryAcquireAll();

        //then
        assertTrue(acquired);
        assertEquals(0, sem1.availablePermits());
        assertEquals(1, sem2.availablePermits());
        assertEquals(2, sem3.availablePermits());
        assertEquals(0, sem4.availablePermits());
    }

    @Test
    void shouldReleaseAllWhenAtLeastOneCannotBeAcquired() {
        //given
        Semaphore sem1 = new Semaphore(1);
        Semaphore sem2 = new Semaphore(0);
        Semaphore sem3 = new Semaphore(3);
        Semaphore sem4 = new Semaphore(1);
        SemaphoresManager semaphoresManager = new SemaphoresManager(List.of(sem1, sem2, sem3, sem4));

        //when
        boolean acquired = semaphoresManager.tryAcquireAll();

        //then
        assertFalse(acquired);
        assertEquals(1, sem1.availablePermits());
        assertEquals(0, sem2.availablePermits());
        assertEquals(3, sem3.availablePermits());
        assertEquals(1, sem4.availablePermits());

    }
}
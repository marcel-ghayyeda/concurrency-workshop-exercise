package com.ghayyeda.concurrencyworkshop.multipleservers;

@FunctionalInterface
public interface Server {

    byte[] fetchData();

}

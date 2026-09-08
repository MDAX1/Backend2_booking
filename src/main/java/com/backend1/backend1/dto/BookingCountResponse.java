package com.backend1.backend1.dto;

public class BookingCountResponse {

    private long count;

    public BookingCountResponse() {
    }

    public BookingCountResponse(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}


package org.techtown.urp;

public class Device {
    String name;
    String address;

    public Device(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setMobile(String address) {
        this.address = address;
    }
}

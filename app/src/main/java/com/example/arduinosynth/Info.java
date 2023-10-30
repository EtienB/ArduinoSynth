package com.example.arduinosynth;

public class Info
{
    private String deviceName, deviceHardwareAddress;

    public Info(){}

    public Info(String deviceName, String deviceHardwareAddress)
    {
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    public String getDeviceName(){return deviceName;}

    public String getDeviceHardwareAddress(){return deviceHardwareAddress;}
}

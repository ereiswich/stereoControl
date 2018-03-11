#!/usr/bin/python

import serial
from time import sleep

ser = serial.Serial('/dev/tty.usbmodem1411',9600, timeout=3)
ser.open()

def controlRelay():
        ser.write('1')
        sleep(1)
        ser.write('0')
        sleep(1)
        ser.write('1')

controlRelay()
ser.close()
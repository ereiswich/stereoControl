
'''
Created on 14.07.2013

@author: reiswich
'''
import os
from time import sleep

if __name__ == '__main__':
    os.system("echo as | cec-client -s")
    os.system("sudo systemctl restart mpd")
    sleep(5)
    os.system("mpc volume 90")
    os.system("mpc play 1")
'''
Created on 03/05/2013

@author: Ruben Miguel Corcoba
'''

'''
Created on 12/05/2013

@author: antitot
'''


import serial
import struct
import pprint

from time import sleep
from bluetooth import *


#Init arduino and wait for 2 seconds until serial in arduino is initializated
print 'Setting up Arduino'

ser = serial.Serial('/dev/ttyACM0', 115200)
sleep(2)

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",0))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "00001101-0000-1000-8000-00805F9B34FB"

advertise_service( server_sock, "N64BlueToothServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ],
#                   protocols = [ OBEX_UUID ]
                    )
                   
print "Waiting for connection on RFCOMM channel %d" % port

client_sock, client_info = server_sock.accept()
print "Accepted connection from ", client_info

try:
    while True:
        data = client_sock.recv(20)
        #if len(data) == 0: break
        print "received [%s]" % data
        
        #2 bytes for buttons and 2 bytes for stick
        byte1 = 0
        byte2 = 0
        stickX = 0
        stickY = 0
        
        ###First Byte
        #B Button
        if data == 'B':
            #print 'B'
            byte1 = byte1 | 0x80
            
        #A Button (Attack in ocarina of time): shaking wiimote or pressing button 4 (B in wiimote)
        if data == 'A':
            attack_state = 0
            #print "A"
            byte1 = byte1 | 0x40
            
        #Z Button
        if data == 'Z':
           # print 'Z'
            byte1 = byte1 | 0x20
            
        if data.find("/") != -1:
            #print 'stick'
            stick = data.split("/");
            stick1 = stick[0].split(",");
            
            
            #stickX   
            stickX = stick1[0]
                      
            #stickY   
            stickY = stick1[1]
                         
        #Start Button
        if data == 'start':
            print 'Start'
            byte1 = byte1 | 0x10
            
        
        #Add the pad here. It is not used in ocarina of time
        
        
        ### Second byte
        # Two first bits are 0
        
        # L Butto is not used in ocarina of time :P
        
        #R Button (shield)
        if data == 'R':
            #print 'R'
            byte2 = byte2 | 0x10
            
        #C-up Button
        if data == 'C-up':
            #print 'C-up Button'
            byte2 = byte2 | 0x08
        
        
        #C-down Button
        if data == 'C-dow':
            print 'C-down Button'
            byte2 = byte2 | 0x04
            
        #C-left Button
        if data == 'C-left':
            #print 'C-left Button'
            byte2 = byte2 | 0x02
            
        #C-right Button
        if data == 'C-right':
            #print 'C-right Button'
            byte2 = byte2 | 0x01
        
        
        #Send data: Last two bytes are sent from arduino (and are read in this app) because for an misterious reason, arduino does not read 4 bytes right
        ser.write(struct.pack('B', byte1));
        ser.write(struct.pack('B', byte2));
        ser.write(struct.pack('b', int(stickX)+20));
        ser.read(1)
        ser.write(struct.pack('b', int(stickY)+20));
        ser.read(1)
      
except IOError:
    print 'ioerror'
    pass

print "disconnected"

client_sock.close()
server_sock.close()

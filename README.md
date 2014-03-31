Meade Weather Station Server
============================

Receive data from a [Meade weather station](http://www.meade.com/product_pages/weatherstations/weathertime/te256w.php)
sensor and expose it with a web interface with server-push updates using a websocket.
An Android app is also provided to view the data on a phone.

Data is received using a generic 433 MHz OOK RF receiver module.  I'm using one from [Linx Technologies]
(https://www.linxtechnologies.com/en/products/modules/lr-rf-transmitter-receiver) but any similar module will work.
A PIC microcontroller decodes the Manchester-coded RF bit stream and sends it to a server running on a Raspberry Pi 
(or a linux PC) over USB. The PIC microcontroller also provides 5 ADC inputs for analog sensors that can be 
polled from the server.  The schematic is provided in KiCad format.

I reverse-engineered the Meade sensor bitstream format, but have not been able to identify the purpose 
of some parts of the data.  They are likely checksums, but without knowing the exact encoding or checksum
algorithm it is difficult to know for certain. After taking enough samples, I was able to identify a 
pattern so as to extract the temperature and humidity values.

The server is built using [netty](http://netty.io/) and configured to run as an [Apache commons daemon]
(http://commons.apache.org/proper/commons-daemon/jsvc.html).  You will need to install jsvc from your linux 
distribution's repository.  An ```init.d``` script is provided in the ```bin``` folder that should be copied 
to ```/etc/init.d``` and set to start with stop with the system using ```update-rc.d```.  An ```if.d``` script 
is provided to force restart of the server in case it starts before networking is available.  The ```if.d``` 
script should be copied to ```/etc/network/if-up.d```.

Note that this project depends on shared code in the ```common``` repository which needs to be built first.

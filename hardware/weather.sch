EESchema Schematic File Version 2
LIBS:power
LIBS:device
LIBS:transistors
LIBS:conn
LIBS:linear
LIBS:regul
LIBS:74xx
LIBS:cmos4000
LIBS:adc-dac
LIBS:memory
LIBS:xilinx
LIBS:special
LIBS:microcontrollers
LIBS:dsp
LIBS:microchip
LIBS:analog_switches
LIBS:motorola
LIBS:texas
LIBS:intel
LIBS:audio
LIBS:interface
LIBS:digital-audio
LIBS:philips
LIBS:display
LIBS:cypress
LIBS:siliconi
LIBS:opto
LIBS:atmel
LIBS:contrib
LIBS:valves
LIBS:weather-cache
EELAYER 27 0
EELAYER END
$Descr USLetter 11000 8500
encoding utf-8
Sheet 1 1
Title "Weather Station Receiver"
Date "25 mar 2014"
Rev "1.0"
Comp "Victor Su"
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L PIC18F2450 U1
U 1 1 52DEBB86
P 4250 2800
F 0 "U1" H 3400 4200 60  0000 C CNN
F 1 "PIC18F2450" H 4900 4200 60  0000 C CNN
F 2 "" H 4250 2800 60  0000 C CNN
F 3 "" H 4250 2800 60  0000 C CNN
	1    4250 2800
	1    0    0    -1  
$EndComp
$Comp
L C C2
U 1 1 52DED51A
P 2050 3900
F 0 "C2" V 2200 3950 40  0000 L CNN
F 1 "22pF" V 2200 3750 40  0000 L CNN
F 2 "~" H 2088 3750 30  0000 C CNN
F 3 "~" H 2050 3900 60  0000 C CNN
	1    2050 3900
	0    -1   -1   0   
$EndComp
$Comp
L C C3
U 1 1 52DED529
P 2050 4600
F 0 "C3" V 2200 4650 40  0000 L CNN
F 1 "22pF" V 2200 4450 40  0000 L CNN
F 2 "~" H 2088 4450 30  0000 C CNN
F 3 "~" H 2050 4600 60  0000 C CNN
	1    2050 4600
	0    -1   -1   0   
$EndComp
$Comp
L CRYSTAL X1
U 1 1 52DED551
P 2400 4250
F 0 "X1" V 2450 4550 60  0000 C CNN
F 1 "4MHz" V 2350 4500 60  0000 C CNN
F 2 "~" H 2400 4250 60  0000 C CNN
F 3 "~" H 2400 4250 60  0000 C CNN
	1    2400 4250
	0    -1   -1   0   
$EndComp
$Comp
L CP1 C1
U 1 1 52DED560
P 1400 4200
F 0 "C1" H 1450 4300 50  0000 L CNN
F 1 "1uF" H 1450 4100 50  0000 L CNN
F 2 "~" H 1400 4200 60  0000 C CNN
F 3 "~" H 1400 4200 60  0000 C CNN
	1    1400 4200
	1    0    0    -1  
$EndComp
$Comp
L CP1 C6
U 1 1 52DED56F
P 3750 6400
F 0 "C6" H 3800 6500 50  0000 L CNN
F 1 "1uF" H 3800 6300 50  0000 L CNN
F 2 "~" H 3750 6400 60  0000 C CNN
F 3 "~" H 3750 6400 60  0000 C CNN
	1    3750 6400
	1    0    0    -1  
$EndComp
$Comp
L CP1 C9
U 1 1 52DED87C
P 8350 6250
F 0 "C9" H 8400 6350 50  0000 L CNN
F 1 "10uF" H 8400 6150 50  0000 L CNN
F 2 "~" H 8350 6250 60  0000 C CNN
F 3 "~" H 8350 6250 60  0000 C CNN
	1    8350 6250
	1    0    0    -1  
$EndComp
$Comp
L CP1 C7
U 1 1 52DED882
P 4650 6400
F 0 "C7" H 4700 6500 50  0000 L CNN
F 1 "10uF" H 4700 6300 50  0000 L CNN
F 2 "~" H 4650 6400 60  0000 C CNN
F 3 "~" H 4650 6400 60  0000 C CNN
	1    4650 6400
	1    0    0    -1  
$EndComp
$Comp
L INDUCTOR L2
U 1 1 52DED8A1
P 5600 6900
F 0 "L2" V 5550 6900 40  0000 C CNN
F 1 "470uH" V 5700 6900 40  0000 C CNN
F 2 "~" H 5600 6900 60  0000 C CNN
F 3 "~" H 5600 6900 60  0000 C CNN
	1    5600 6900
	0    -1   -1   0   
$EndComp
$Comp
L INDUCTOR L1
U 1 1 52DED8AE
P 4200 5850
F 0 "L1" V 4150 5850 40  0000 C CNN
F 1 "470uH" V 4300 5850 40  0000 C CNN
F 2 "~" H 4200 5850 60  0000 C CNN
F 3 "~" H 4200 5850 60  0000 C CNN
	1    4200 5850
	0    -1   -1   0   
$EndComp
$Comp
L C C5
U 1 1 52DED8B9
P 2600 2850
F 0 "C5" H 2400 2900 40  0000 L CNN
F 1 "100nF" H 2300 2800 40  0000 L CNN
F 2 "~" H 2638 2700 30  0000 C CNN
F 3 "~" H 2600 2850 60  0000 C CNN
	1    2600 2850
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR11
U 1 1 52DED9C4
P 8350 5500
F 0 "#PWR11" H 8350 5460 30  0001 C CNN
F 1 "+3.3V" H 8350 5610 30  0000 C CNN
F 2 "" H 8350 5500 60  0000 C CNN
F 3 "" H 8350 5500 60  0000 C CNN
	1    8350 5500
	1    0    0    -1  
$EndComp
$Comp
L LP2951 U2
U 1 1 52DEE04B
P 6500 5400
F 0 "U2" H 6550 5450 60  0000 C CNN
F 1 "LP2951" H 7150 5450 60  0000 C CNN
F 2 "" H 6500 5400 60  0000 C CNN
F 3 "" H 6500 5400 60  0000 C CNN
	1    6500 5400
	1    0    0    -1  
$EndComp
$Comp
L LED D1
U 1 1 52DEE069
P 7300 4700
F 0 "D1" V 7250 4850 50  0000 C CNN
F 1 "LED" V 7350 4850 50  0000 C CNN
F 2 "~" H 7300 4700 60  0000 C CNN
F 3 "~" H 7300 4700 60  0000 C CNN
	1    7300 4700
	0    1    1    0   
$EndComp
$Comp
L LED D2
U 1 1 52DEE078
P 7600 4700
F 0 "D2" V 7550 4850 50  0000 C CNN
F 1 "LED" V 7650 4850 50  0000 C CNN
F 2 "~" H 7600 4700 60  0000 C CNN
F 3 "~" H 7600 4700 60  0000 C CNN
	1    7600 4700
	0    1    1    0   
$EndComp
$Comp
L USB-MINI-B CON1
U 1 1 52DEE4DE
P 2350 6150
F 0 "CON1" H 2600 6600 60  0000 C CNN
F 1 "USB-MICRO-B" H 2400 5700 60  0000 C CNN
F 2 "" H 2350 6150 60  0000 C CNN
F 3 "" H 2350 6150 60  0000 C CNN
	1    2350 6150
	-1   0    0    -1  
$EndComp
$Comp
L RES R3
U 1 1 52DEEBD1
P 3750 5150
F 0 "R3" H 3850 5100 50  0000 C CNN
F 1 "100k" H 3900 5000 50  0000 C CNN
F 2 "" H 3900 5000 60  0000 C CNN
F 3 "" H 3900 5000 60  0000 C CNN
	1    3750 5150
	1    0    0    -1  
$EndComp
$Comp
L RES R2
U 1 1 52DEEBEE
P 3350 5150
F 0 "R2" H 3450 5100 50  0000 C CNN
F 1 "100k" H 3500 5000 50  0000 C CNN
F 2 "" H 3500 5000 60  0000 C CNN
F 3 "" H 3500 5000 60  0000 C CNN
	1    3350 5150
	1    0    0    -1  
$EndComp
$Comp
L RES R4
U 1 1 52DEEC05
P 7300 3900
F 0 "R4" H 7400 3700 50  0000 C CNN
F 1 "330" H 7450 3600 50  0000 C CNN
F 2 "" H 7450 3750 60  0000 C CNN
F 3 "" H 7450 3750 60  0000 C CNN
	1    7300 3900
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR6
U 1 1 52DEF0EF
P 5100 5500
F 0 "#PWR6" H 5100 5590 20  0001 C CNN
F 1 "+5V" H 5100 5590 30  0000 C CNN
F 2 "" H 5100 5500 60  0000 C CNN
F 3 "" H 5100 5500 60  0000 C CNN
	1    5100 5500
	1    0    0    -1  
$EndComp
$Comp
L RES R7
U 1 1 52DEF27B
P 7900 5650
F 0 "R7" H 8000 5600 50  0000 C CNN
F 1 "169k" H 8050 5500 50  0000 C CNN
F 2 "" H 8050 5500 60  0000 C CNN
F 3 "" H 8050 5500 60  0000 C CNN
	1    7900 5650
	1    0    0    -1  
$EndComp
$Comp
L RES R8
U 1 1 52DEF28B
P 7900 6350
F 0 "R8" H 8000 6300 50  0000 C CNN
F 1 "100k" H 8050 6200 50  0000 C CNN
F 2 "" H 8050 6200 60  0000 C CNN
F 3 "" H 8050 6200 60  0000 C CNN
	1    7900 6350
	1    0    0    -1  
$EndComp
$Comp
L ANT AE1
U 1 1 52DEF6BF
P 9700 3050
F 0 "AE1" H 9750 3150 60  0000 C CNN
F 1 "ANT" H 9950 3150 60  0000 C CNN
F 2 "" H 9700 3050 60  0000 C CNN
F 3 "" H 9700 3050 60  0000 C CNN
	1    9700 3050
	1    0    0    -1  
$EndComp
$Comp
L C C4
U 1 1 52DEF8EB
P 2350 6900
F 0 "C4" V 2500 6950 40  0000 L CNN
F 1 "NC" V 2500 6750 40  0000 L CNN
F 2 "~" H 2388 6750 30  0000 C CNN
F 3 "~" H 2350 6900 60  0000 C CNN
	1    2350 6900
	0    -1   -1   0   
$EndComp
$Comp
L DGND #PWR5
U 1 1 52DEFF76
P 4650 7000
F 0 "#PWR5" H 4650 7000 40  0001 C CNN
F 1 "DGND" H 4650 6930 40  0000 C CNN
F 2 "" H 4650 7000 60  0000 C CNN
F 3 "" H 4650 7000 60  0000 C CNN
	1    4650 7000
	1    0    0    -1  
$EndComp
$Comp
L RES R1
U 1 1 52DF00CF
P 2950 1000
F 0 "R1" H 3100 900 50  0000 C CNN
F 1 "10k" H 3100 800 50  0000 C CNN
F 2 "" H 3100 850 60  0000 C CNN
F 3 "" H 3100 850 60  0000 C CNN
	1    2950 1000
	1    0    0    -1  
$EndComp
$Comp
L AGND #PWR7
U 1 1 52DEFF85
P 6050 7000
F 0 "#PWR7" H 6050 7000 40  0001 C CNN
F 1 "AGND" H 6050 6930 50  0000 C CNN
F 2 "" H 6050 7000 60  0000 C CNN
F 3 "" H 6050 7000 60  0000 C CNN
	1    6050 7000
	1    0    0    -1  
$EndComp
$Comp
L +3.3V #PWR12
U 1 1 52DF0BDE
P 8600 2400
F 0 "#PWR12" H 8600 2360 30  0001 C CNN
F 1 "+3.3V" H 8600 2510 30  0000 C CNN
F 2 "" H 8600 2400 60  0000 C CNN
F 3 "" H 8600 2400 60  0000 C CNN
	1    8600 2400
	1    0    0    -1  
$EndComp
$Comp
L RES R6
U 1 1 52DF0990
P 7800 2550
F 0 "R6" H 7900 2500 50  0000 C CNN
F 1 "10k" H 7950 2400 50  0000 C CNN
F 2 "" H 7950 2400 60  0000 C CNN
F 3 "" H 7950 2400 60  0000 C CNN
	1    7800 2550
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR2
U 1 1 52DF1BA2
P 2600 2500
F 0 "#PWR2" H 2600 2590 20  0001 C CNN
F 1 "+5V" H 2600 2590 30  0000 C CNN
F 2 "" H 2600 2500 60  0000 C CNN
F 3 "" H 2600 2500 60  0000 C CNN
	1    2600 2500
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR4
U 1 1 52DF1BB1
P 2950 950
F 0 "#PWR4" H 2950 1040 20  0001 C CNN
F 1 "+5V" H 2950 1040 30  0000 C CNN
F 2 "" H 2950 950 60  0000 C CNN
F 3 "" H 2950 950 60  0000 C CNN
	1    2950 950 
	1    0    0    -1  
$EndComp
$Comp
L DGND #PWR3
U 1 1 52DF1E4F
P 2600 3200
F 0 "#PWR3" H 2600 3200 40  0001 C CNN
F 1 "DGND" H 2600 3130 40  0000 C CNN
F 2 "" H 2600 3200 60  0000 C CNN
F 3 "" H 2600 3200 60  0000 C CNN
	1    2600 3200
	1    0    0    -1  
$EndComp
$Comp
L DGND #PWR1
U 1 1 52DF1E5E
P 1400 4650
F 0 "#PWR1" H 1400 4650 40  0001 C CNN
F 1 "DGND" H 1400 4580 40  0000 C CNN
F 2 "" H 1400 4650 60  0000 C CNN
F 3 "" H 1400 4650 60  0000 C CNN
	1    1400 4650
	1    0    0    -1  
$EndComp
$Comp
L DGND #PWR10
U 1 1 52DF1E6D
P 7450 5000
F 0 "#PWR10" H 7450 5000 40  0001 C CNN
F 1 "DGND" H 7450 4930 40  0000 C CNN
F 2 "" H 7450 5000 60  0000 C CNN
F 3 "" H 7450 5000 60  0000 C CNN
	1    7450 5000
	1    0    0    -1  
$EndComp
$Comp
L AGND #PWR13
U 1 1 52DF1E7C
P 8600 4050
F 0 "#PWR13" H 8600 4050 40  0001 C CNN
F 1 "AGND" H 8600 3980 50  0000 C CNN
F 2 "" H 8600 4050 60  0000 C CNN
F 3 "" H 8600 4050 60  0000 C CNN
	1    8600 4050
	1    0    0    -1  
$EndComp
Connection ~ 2600 3150
Connection ~ 1400 4600
Connection ~ 7450 4950
Wire Wire Line
	7450 5000 7450 4950
Connection ~ 8600 4000
Wire Wire Line
	2950 1000 2950 950 
Wire Wire Line
	2850 1550 3050 1550
Wire Wire Line
	2950 1550 2950 1500
Connection ~ 7800 3450
Wire Wire Line
	9400 3250 9600 3250
Wire Wire Line
	9500 4000 9500 3350
Wire Wire Line
	8600 4000 9500 4000
Wire Wire Line
	7800 3450 8050 3450
Wire Wire Line
	7800 3050 7800 3550
Wire Wire Line
	5450 3650 7600 3650
Wire Wire Line
	5450 3750 7300 3750
Wire Wire Line
	7600 4950 7600 4900
Wire Wire Line
	7300 4950 7600 4950
Wire Wire Line
	7300 4900 7300 4950
Wire Wire Line
	7600 4500 7600 4400
Wire Wire Line
	7300 4500 7300 4400
Wire Wire Line
	6100 4750 6100 2750
Wire Wire Line
	3100 4750 6100 4750
Wire Wire Line
	3100 6150 3100 4750
Wire Wire Line
	2900 6150 3100 6150
Wire Wire Line
	3200 6000 2900 6000
Wire Wire Line
	3200 4850 3200 6000
Wire Wire Line
	6200 4850 3200 4850
Wire Wire Line
	6200 2650 5450 2650
Wire Wire Line
	6200 2650 6200 4850
Wire Wire Line
	6100 2750 5450 2750
Connection ~ 6050 6900
Wire Wire Line
	6050 7000 6050 6900
Wire Wire Line
	5800 5000 5800 3450
Wire Wire Line
	5800 3450 5450 3450
Connection ~ 3750 5000
Connection ~ 3350 6900
Wire Wire Line
	3350 5650 3350 6900
Wire Wire Line
	3750 5650 3750 6200
Wire Wire Line
	3750 5000 3750 5150
Wire Wire Line
	3350 5000 5800 5000
Wire Wire Line
	3350 5000 3350 5150
Connection ~ 8350 5550
Connection ~ 4650 5850
Wire Wire Line
	3750 6900 3750 6600
Connection ~ 4650 6900
Connection ~ 3750 6900
Wire Wire Line
	2950 6450 2900 6450
Wire Wire Line
	4650 6600 4650 7000
Connection ~ 2950 6450
Connection ~ 3750 5850
Wire Wire Line
	5100 5850 4500 5850
Wire Wire Line
	2900 5850 3900 5850
Wire Wire Line
	2950 6900 2950 6450
Wire Wire Line
	2550 6900 5300 6900
Wire Wire Line
	1750 6450 1800 6450
Wire Wire Line
	1750 6900 1750 6450
Wire Wire Line
	2150 6900 1750 6900
Connection ~ 8850 4000
Wire Wire Line
	9500 3350 9600 3350
Wire Wire Line
	8850 4000 8850 3950
Wire Wire Line
	8600 3950 8600 4050
Connection ~ 6900 6900
Connection ~ 7900 5550
Wire Wire Line
	8350 5500 8350 6050
Connection ~ 7900 6900
Wire Wire Line
	8350 6900 8350 6450
Wire Wire Line
	7900 6900 7900 6850
Wire Wire Line
	6900 6900 6900 6700
Connection ~ 7900 6250
Wire Wire Line
	7600 6250 7900 6250
Wire Wire Line
	7900 6150 7900 6350
Wire Wire Line
	7600 5550 8350 5550
Wire Wire Line
	7900 5650 7900 5550
Connection ~ 2950 2950
Wire Wire Line
	2600 3050 2600 3200
Wire Wire Line
	2950 3150 2100 3150
Wire Wire Line
	2950 2950 3050 2950
Wire Wire Line
	2950 2750 2950 3150
Wire Wire Line
	2950 2750 3050 2750
Connection ~ 2600 2550
Wire Wire Line
	3050 2550 2600 2550
Wire Wire Line
	2600 2500 2600 2650
Connection ~ 1750 4600
Wire Wire Line
	1400 4400 1400 4650
Wire Wire Line
	1400 3700 1400 4000
Wire Wire Line
	1400 3700 3050 3700
Wire Wire Line
	1750 3900 1850 3900
Wire Wire Line
	1750 4600 1750 3900
Wire Wire Line
	1400 4600 1850 4600
Connection ~ 2400 4600
Wire Wire Line
	2750 4000 3050 4000
Wire Wire Line
	2750 4600 2750 4000
Wire Wire Line
	2400 4600 2400 4550
Wire Wire Line
	2250 4600 2750 4600
Connection ~ 2400 3900
Wire Wire Line
	2400 3950 2400 3900
Wire Wire Line
	2250 3900 3050 3900
Wire Wire Line
	7800 3550 5450 3550
Wire Wire Line
	5450 3250 8050 3250
$Comp
L LINX-LC-RX U3
U 1 1 52DF0A32
P 8350 2850
F 0 "U3" H 8400 2900 60  0000 C CNN
F 1 "LINX-LC-RX" H 9200 2900 60  0000 C CNN
F 2 "" H 8350 2850 60  0000 C CNN
F 3 "" H 8350 2850 60  0000 C CNN
	1    8350 2850
	1    0    0    -1  
$EndComp
$Comp
L SW_PUSH SW1
U 1 1 52DF2610
P 2550 1550
F 0 "SW1" H 2450 1650 50  0000 C CNN
F 1 "SW_PUSH" H 2550 1470 50  0000 C CNN
F 2 "~" H 2550 1550 60  0000 C CNN
F 3 "~" H 2550 1550 60  0000 C CNN
	1    2550 1550
	1    0    0    -1  
$EndComp
Connection ~ 2950 1550
Wire Wire Line
	2100 3150 2100 1550
Wire Wire Line
	2100 1550 2250 1550
$Comp
L CONN_8 P1
U 1 1 52DF287B
P 6600 1800
F 0 "P1" V 6550 1800 60  0000 C CNN
F 1 "CONN_8" V 6650 1800 60  0000 C CNN
F 2 "" H 6600 1800 60  0000 C CNN
F 3 "" H 6600 1800 60  0000 C CNN
	1    6600 1800
	1    0    0    -1  
$EndComp
$Comp
L +5V #PWR8
U 1 1 52DF288A
P 6200 1400
F 0 "#PWR8" H 6200 1490 20  0001 C CNN
F 1 "+5V" H 6200 1490 30  0000 C CNN
F 2 "" H 6200 1400 60  0000 C CNN
F 3 "" H 6200 1400 60  0000 C CNN
	1    6200 1400
	1    0    0    -1  
$EndComp
Wire Wire Line
	5450 1550 6250 1550
Wire Wire Line
	5450 1650 6250 1650
Wire Wire Line
	5450 1750 6250 1750
Wire Wire Line
	5450 1850 6250 1850
Wire Wire Line
	5450 2050 5700 2050
Wire Wire Line
	5700 2050 5700 1950
Wire Wire Line
	5700 1950 6250 1950
Wire Wire Line
	6250 2050 6200 2050
Wire Wire Line
	6200 2050 6200 2200
Wire Wire Line
	6200 2150 6250 2150
Connection ~ 6200 2150
Wire Wire Line
	6250 1450 6200 1450
Wire Wire Line
	6200 1450 6200 1400
$Comp
L DGND #PWR9
U 1 1 52DF2C53
P 6200 2200
F 0 "#PWR9" H 6200 2200 40  0001 C CNN
F 1 "DGND" H 6200 2130 40  0000 C CNN
F 2 "" H 6200 2200 60  0000 C CNN
F 3 "" H 6200 2200 60  0000 C CNN
	1    6200 2200
	1    0    0    -1  
$EndComp
$Comp
L C C8
U 1 1 52DF2DFD
P 5100 6400
F 0 "C8" H 5250 6450 40  0000 L CNN
F 1 "100nF" H 5250 6350 40  0000 L CNN
F 2 "~" H 5138 6250 30  0000 C CNN
F 3 "~" H 5100 6400 60  0000 C CNN
	1    5100 6400
	1    0    0    -1  
$EndComp
Wire Wire Line
	5100 6900 5100 6600
Wire Wire Line
	5100 5500 5100 6200
Wire Wire Line
	5100 5550 6200 5550
Connection ~ 5100 5850
Connection ~ 5100 6900
Wire Wire Line
	5900 6900 8350 6900
Connection ~ 5100 5550
$Comp
L RES R5
U 1 1 52DEEBFF
P 7600 3900
F 0 "R5" H 7700 3700 50  0000 C CNN
F 1 "330" H 7750 3600 50  0000 C CNN
F 2 "" H 7750 3750 60  0000 C CNN
F 3 "" H 7750 3750 60  0000 C CNN
	1    7600 3900
	1    0    0    -1  
$EndComp
Wire Wire Line
	7300 3750 7300 3900
Wire Wire Line
	7600 3650 7600 3900
Wire Wire Line
	4650 6200 4650 5850
Wire Wire Line
	7800 2450 7800 2550
Wire Wire Line
	7800 2450 8600 2450
Wire Wire Line
	8600 2400 8600 2550
Connection ~ 8600 2450
$EndSCHEMATC

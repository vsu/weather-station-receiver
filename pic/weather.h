/*
 *  WEATHER.H
 *  Header file for Weather station receiver
 *
 */


#ifndef __WEATHER_H__
#define __WEATHER_H__


#include "types.h"

#define _PIR1                   0xF9E
#define _TMR1IF                 0

#define RF_IN                   PIN_B0
#define RF_OUT                  PIN_B1
#define USB_SENSE               PIN_B2
#define RF_ENABLE               PIN_B3
#define RF_LED                  PIN_B4
#define USB_LED                 PIN_B5
#define SER_TX                  PIN_C6
#define SER_RX                  PIN_C7

#define rf_asserted()           input(RF_IN)

#define assert_rf()             output_high(RF_OUT)
#define deassert_rf()           output_low(RF_OUT)

#define rf_on()                 output_float(RF_ENABLE)
#define rf_off()                output_low(RF_ENABLE)

#define rf_led_on()             output_high(RF_LED)
#define rf_led_off()            output_low(RF_LED)

#define usb_led_on()            output_high(USB_LED)
#define usb_led_off()           output_low(USB_LED)

#define MC_RF_BUFFER_SIZE       12
#define MC_RF_MID_BIT_TICKS     2000

#define DATA_BUFFER_SIZE        MC_RF_BUFFER_SIZE

#define FSM_STATE_REQ           0
#define FSM_STATE_ID1           1
#define FSM_STATE_ID2           2
#define FSM_STATE_ARG1          3
#define FSM_STATE_ARG2          4
#define FSM_STATE_TERM          5

#define REQ_RESET               'R'
#define REQ_RF                  'F'
#define REQ_SENSOR              'S'

#define RESP_RF_PUSH            'P'
#define RESP_RF                 'F'
#define RESP_SENSOR             'S'
#define RESP_ACK                'K'
#define RESP_NAK                'X'

#define CH_TERM                 '\r'

#use rs232(baud=57600, xmit=SER_TX, rcv=SER_RX)

#define USB_CDC_DELAYED_FLUSH
#define USB_CDC_DATA_LOCAL_SIZE  128


///////////////////////////////////////////////////////////////////////////// 
// 
// If you are using a USB connection sense pin, define it here.  If you are 
// not using connection sense, comment out this line.  Without connection 
// sense you will not know if the device gets disconnected. 
//       (connection sense should look like this: 
//                             100k 
//            VBUS-----+----/\/\/\/\/\----- (I/O PIN ON PIC) 
//                     | 
//                     +----/\/\/\/\/\-----GND 
//                             100k 
//        (where VBUS is pin1 of the USB connector) 
// 
///////////////////////////////////////////////////////////////////////////// 
#define USB_CABLE_IS_ATTACHED()  input(USB_SENSE)


// ================================================================
// Global variables
//

uint8 data_buffer[DATA_BUFFER_SIZE];
uint8 fsm_state;



// ================================================================
// Forward declarations
//

void initialize (void);
void process_rf (void);
void send_serial (uint8 *buffer, uint8 length);
bool send_usb (uint8 type, uint8 id, uint8 *data, uint8 length);
void process_usb (void);


#endif
// __WEATHER_H__

/*
 *  WEATHER.C
 *  Weather station receiver
 *
 *  Device: PIC18F2450
 *  Pin allocation:
 *    port B (RB0) : RF input
 *           (RB1) : RF output
 *           (RB2) : USB connection sense
 *           (RB3) : RF enable
 *           (RB4) : RF status LED
 *           (RB5) : USB status LED
 *    port C (RC4) : USB D-
 *           (RC5) : USB D+
 *           (RC6) : Serial console output
 *           (RC7) : Serial console input
 *
 *  Modification Log
 *  ====================================================================
 *    1.0  27 Nov 2013  vcs  initial version
 *    1.1   9 Jan 2014  vcs  USB interface
 *
 */


#include <18F2450.H>
#device ADC=10

#fuses XTPLL, PLL1, CPUDIV4, USBDIV, VREGEN, NOIESO, NOFCMEN, \
       WDT, WDT512, PUT, MCLR, NOBROWNOUT, NOPBADEN, NODEBUG, \
       NOXINST, NOLVP, STVREN, NOPROTECT_0, NOPROTECT_1, NOCPB, \ 
       NOWRT, NOWRTB, NOWRTC, NOEBTR, NOEBTRB, NOLPT1OSC

#use delay(clock=16000000)

#include "weather.h"
#include <stddef.h>
#include <stdlibm.h>
#include <usb_cdc.h>
#include "ascii.c"           // ASCII utility library
#include "mc_rf.c"



/**********************************************************************
 *
 * Name:        initialize
 *
 * Purpose:     Initialization function.
 *
 * Parameters:  None
 *
 * Returns:     Nothing
 *
 **********************************************************************/
void initialize (void)
{
  // Set tri-state for ports
  set_tris_a (0x2F);
  set_tris_b (0x0D);
  set_tris_c (0xB0);
  
  rf_led_off();
  usb_led_off();
  rf_on();
  
  // Initialize timer 1
  setup_timer_1 (T1_INTERNAL | T1_DIV_BY_1);
    
  // Initialize ADC
  setup_adc (ADC_CLOCK_INTERNAL);
  setup_adc_ports (AN0_TO_AN4);
  
  // Initialize data structures
  memset (data_buffer, 0, DATA_BUFFER_SIZE);
  fsm_state = FSM_STATE_REQ;
  
  // Initialize USB (non-blocking)
  // usb_task() needs to be called in your loop to finish USB initialization
  usb_init_cs();
}


/**********************************************************************
 *
 * Name:        process_rf
 *
 * Purpose:     Processes RF packet.
 *
 * Parameters:  None
 *
 * Returns:     Nothing
 *
 **********************************************************************/
void process_rf (void)
{
  uint8 i;
  uint8 length;
  uint8 in_buffer[DATA_BUFFER_SIZE];
  
  rf_led_on();

  memset (in_buffer, 0, DATA_BUFFER_SIZE);
  length = receive_mc_rf (in_buffer, MC_RF_MID_BIT_TICKS);
  
  // 8 is an arbitrary length to ignore bad RF data with
  // one or two received bits caused by interference or noise 
  if (length > 8)
  {
    // copy the input buffer to the data buffer in reverse
    // order with MSB first
    for (i = 0; i < DATA_BUFFER_SIZE; i++)
    {
      data_buffer[i] = in_buffer[DATA_BUFFER_SIZE - i - 1];
    }
    
    //printf ("[%u] ", length);
    //send_serial (data_buffer, DATA_BUFFER_SIZE);
    
    // rf push data uses a special id of 0 to indicate broadcast
    send_usb (RESP_RF_PUSH, 0, data_buffer, DATA_BUFFER_SIZE);
  }
  
  rf_led_off();
}


/**********************************************************************
 *
 * Name:        send_serial
 *
 * Purpose:     Sends buffer data over the serial port.
 *
 * Parameters:  buffer  buffer array
 *              length  data length
 *
 * Returns:     Nothing
 *
 **********************************************************************/
void send_serial (uint8 *buffer, uint8 length)
{
  uint8 value;
  uint8 i;

  for (i = 0; i < length; i++)
  {
    value = buffer[i - 1];
    putc (int_to_hex (value >> 4));
    putc (int_to_hex (value)); 
    putc (' ');
  }

  putc ('\r');
  putc ('\n');
}


/**********************************************************************
 *
 * Name:        send_usb
 *
 * Purpose:     Sends a data packet to the USB port.
 *
 * Parameters:  type    packet type
 *              id      request id
 *              data    data array
 *              length  data length
 *
 * Returns:     Nothing
 *
 **********************************************************************/
bool send_usb (uint8 type, uint8 id, uint8 *data, uint8 length) 
{
  bool result = false;
  uint8 * str;
  uint8 value;
  uint8 i;
  
  str = malloc (length * 2 + 5);
  if (str != NULL) 
  {
    str[0] = type;
    str[1] = int_to_hex (id >> 4);
    str[2] = int_to_hex (id);
    
    for (i = 0; i < length; i++)
    {
      value = data[i];
      str[2 * i + 3] = int_to_hex (value >> 4);
      str[2 * i + 4] = int_to_hex (value);
    }
    
    str[length * 2 + 3] = CH_TERM;
    str[length * 2 + 4] = 0;
    
    result = usb_cdc_puts (str);
    free (str);
  } 

  return result;
}


/**********************************************************************
 *
 * Name:        service_usb
 *
 * Purpose:     Processes USB data.
 *
 * Parameters:  None
 *
 * Returns:     Nothing
 *
 **********************************************************************/
void process_usb (void) 
{
  static uint8 req;
  static uint8 arg;
  static uint8 id;
  uint16 value;
  uint8 data[3];
  uint8 ch;
  bool error = false;

  ch = usb_cdc_getc();

  switch (fsm_state) 
  {
    case FSM_STATE_REQ:
      req = ch;
      id = 0;
      fsm_state = FSM_STATE_ID1;
      break;

    case FSM_STATE_ID1:
      ch = hex_to_int (ch);
      if (ch != 0xFF) 
      {
        id = (ch << 4);
        fsm_state = FSM_STATE_ID2;
      } 
      else 
      {
        error = true;
      }

      break;

    case FSM_STATE_ID2:
      ch = hex_to_int (ch);
      if (ch != 0xFF) 
      {
        id += ch;
        fsm_state = (req == REQ_SENSOR) ? FSM_STATE_ARG1 : FSM_STATE_TERM;
      } 
      else 
      {
        error = true;
      }

      break;

    case FSM_STATE_ARG1:
      ch = hex_to_int (ch);
      if (ch != 0xFF) 
      {
        arg = (ch << 4);
        fsm_state = FSM_STATE_ARG2;
      }
      else
      {
        error = true;
      }
     
      break;

    case FSM_STATE_ARG2:
      ch = hex_to_int (ch);
      if (ch != 0xFF) 
      {
        arg += ch;
        fsm_state = FSM_STATE_TERM;
      }
      else
      {
        error = true;
      }
 
      break;

    case FSM_STATE_TERM:
      if (ch == CH_TERM) 
      {
        if (req == REQ_RESET) 
        {
          send_usb (RESP_ACK, id, null, 0);
          reset_cpu();
        }
        else if (req == REQ_RF)
        {
          send_usb (RESP_RF, id, data_buffer, DATA_BUFFER_SIZE);
          fsm_state = FSM_STATE_REQ;
        }
        else if (req == REQ_SENSOR)
        {
          if (arg < 5)
          {
            set_adc_channel (arg);
            delay_us (10);
            value = read_adc ();

            data[0] = arg;
            data[1] = (uint8)(value >> 8);
            data[2] = (uint8)value;

            send_usb (RESP_SENSOR, id, data, 3);
            fsm_state = FSM_STATE_REQ;
          }
          else
          {
            error = true;
          }
        }
        else
        {
          error = true;
        }
      }
      
      break;
  }
  
  if (error)
  {
    send_usb (RESP_NAK, id, null, 0);
    fsm_state = FSM_STATE_REQ;
  }
}


/**********************************************************************
 *
 * Name:        main
 *
 * Purpose:     Program entry point.
 *
 * Parameters:  None
 *
 * Returns:     Nothing
 *
 **********************************************************************/
void main (void)
{
  initialize ();

  while (true)
  {
    // service low level USB operations
    usb_task();

    if (usb_enumerated())
    {
      usb_led_on();

      if (usb_cdc_kbhit())
      {
        process_usb();
      }

      if (rf_asserted())
      {
        process_rf();
      }
    }
    else
    {
      usb_led_off();
    }
    
    restart_wdt();
  }
}

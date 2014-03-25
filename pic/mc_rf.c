/*
 *  MC_RF.C
 *  PIC firmware library
 *
 *  Manchester-coded RF protocol library
 *
 *  A "0" is encoded as a mark followed by a space.
 *  A "1" is encoded as a space followed by a mark.
 *
 *  Modification Log
 *  ====================================================================
 *    1.0  28 Nov 2013  vcs  initial version
 *
 */


#ifndef __MC_RF_C__
#define __MC_RF_C__


#include "types.h"
#include "rf.c"


#ifndef MC_RF_BUFFER_SIZE
  #error MC_RF_BUFFER_SIZE must be defined
#endif



/********************************************************************
 *
 * F O R W A R D   D E C L A R A T I O N S
 *
 ********************************************************************
 */

uint8 receive_mc_rf (uint8 *buffer, uint16 mid_bit_ticks);



/********************************************************************
 *
 * R F   I N P U T   F U N C T I O N
 *
 ********************************************************************
 */

/**********************************************************************
 *
 * Name:        receive_mc_rf
 *
 * Purpose:     Receives Manchester-coded RF data.
 *              Bits are assembled into buffer with buffer[0] as LSB.
 *
 * Parameters:  buffer         data buffer
 *              mid_bit_ticks  the mid-bit time T in timer ticks
 *
 * Returns:     Number of bits received
 *
 **********************************************************************/
uint8 receive_mc_rf (uint8 *buffer, uint16 mid_bit_ticks)
{
  uint8 i; 
  uint8 bit;
  uint16 length;

  
  bit = rf_asserted();
  i = 0;
  while (i < (8 * MC_RF_BUFFER_SIZE))
  {
    if (bit == 1)
    {
      if (!find_space (4 * mid_bit_ticks))
      {
        break;
      }

      length = return_mark_len (4 * mid_bit_ticks);

      if (length == 0xFFFF)
      {
        break;
      }

      shift_left (buffer, MC_RF_BUFFER_SIZE, bit);

      if (length > (3 * mid_bit_ticks / 2))
      {
        bit = 0;
      }
    }
    else
    {
      if (!find_mark (4 * mid_bit_ticks))
      {
        break;
      }
      
      length = return_space_len (4 * mid_bit_ticks);

      if (length == 0xFFFF)
      {
        break;
      }

      shift_left (buffer, MC_RF_BUFFER_SIZE, bit);

      if (length > (3 * mid_bit_ticks / 2))
      {
        bit = 1;
      }
    }

    i++;   
  }
  
  return i;
}

#endif
// __MC_RF_C__

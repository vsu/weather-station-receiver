/*
 *  ASCII.C
 *  PIC firmware library
 *
 *  ASCII utilities
 *
 *  PIC information: device-independent
 *
 *  Modification Log
 *  =====================================================================
 *    1.0   8 Jun 1998  vcs  initial version
 *    1.1   8 Jul 1998  vcs  fixed to_uppercase()
 *    1.2  10 Jul 1998  vcs  split into bit_math.c
 *    1.3  13 Mar 2000  vcs  fixed hex_to_int() for PCM 2.705
 *
 */


#ifndef __ASCII_C__
#define __ASCII_C__


#include "types.h"



/********************************************************************
 *
 * F O R W A R D   D E C L A R A T I O N S
 *
 ********************************************************************
 */

uint8 hex_to_int (char hex);
char  int_to_hex (uint8 b);
char  to_uppercase (char ch);



/********************************************************************
 *
 * A S C I I   U T I L I T I E S
 *
 ********************************************************************
 */

/**********************************************************************
 *
 * Name:        hex_to_int
 *
 * Purpose:     Converts a hex ASCII character to integer.
 *
 * Parameters:  hex  hex character
 *
 * Returns:     binary equivalent or 0xFF if input is not in
 *              the range [0-9], [A-F].
 *
 **********************************************************************/
uint8 hex_to_int (char hex)
{
  if ((hex >= '0') && (hex <= '9'))
  {
    return (hex - '0');
  }
  else
  {
    if ((hex >= 'A') && (hex <= 'F'))
    {
      return (hex - 'A' + 0x0A);
    }
    else
    {
      return 0xFF;
    }
  }
}


/**********************************************************************
 *
 * Name:        int_to_hex
 *
 * Purpose:     Converts an integer to a hex ASCII character.
 *
 * Parameters:  b  integer (only lower 4-bits are significant)
 *
 * Returns:     hex character
 *
 **********************************************************************/
char int_to_hex (uint8 b)
{
  b &= 0x0F;
  if (b < 0x0A)
  {
    return b + '0';
  }
  else
  {
    return b + ('A' - 0x0A);
  }
}


/**********************************************************************
 *
 * Name:        to_uppercase
 *
 * Purpose:     Converts an ASCII character to its uppercase counterpart.
 *
 * Parameters:  ch  ASCII character
 *
 * Returns:     Uppercase character
 *
 **********************************************************************/
char to_uppercase (char ch)
{
  if ((ch >= 'a') && (ch <= 'z'))
  {
    ch &= 0xDF;
    // ch -= ('a' - 'A');
  }
  return ch;
}


#endif
// __ASCII_C__

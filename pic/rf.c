/*
 *  RF.C
 *  PIC firmware library
 *
 *  Low-level RF communication library
 *
 *  The following functions/macros must be defined externally:
 *    rf_asserted()
 *
 *  Modification Log
 *  ====================================================================
 *    1.0  28 Nov 2013  vcs  initial version
 *
 */


#ifndef __RF_C__
#define __RF_C__


#include "types.h"


#ifndef rf_asserted
  #error rf_asserted must be defined
#endif

#ifndef assert_rf
  #error assert_rf must be defined
#endif

#ifndef deassert_rf
  #error deassert_rf must be defined
#endif


// Timer 1 interrupt flag macros
#bit t1if = _PIR1._TMR1IF
#define t1if_is_asserted()   t1if
#define clear_t1if()         t1if = 0


#define ir_geq_margin(actual, expected, margin) \
  (actual > (expected - margin))

#define ir_eq_margin(actual, expected, margin) \
  ((actual > (expected - margin)) && (actual < (expected + margin)))

#define ir_decr_duration(sample, duration) \
{                                          \
  if (duration > sample)                   \
    sample = 0;                            \
  else                                     \
    sample -= duration;                    \
}



/********************************************************************
 *
 * F O R W A R D   D E C L A R A T I O N S
 *
 ********************************************************************
 */

void send_rf_mark (uint16 ticks);
void send_rf_space (uint16 ticks);
 
void send_rf_bit (uint16 on_ticks, uint16 off_ticks);

bool find_space (uint16 space_timeout);
bool find_mark (uint16 mark_timeout);

uint16 return_space_len (uint16 space_timeout);
uint16 return_mark_len (uint16 mark_timeout);



/********************************************************************
 *
 * L O W - L E V E L   I R   O U T P U T   F U N C T I O N S
 *
 ********************************************************************
 */

/**********************************************************************
 *
 * Name:        send_rf_mark
 *
 * Purpose:     Sends an RF mark.
 *
 * Parameters:  ticks   mark time
 *
 * Returns:     Nothing
 *
 **********************************************************************/
void send_rf_mark (uint16 ticks)
{
  set_timer1 (0xFFFF - ticks);
  clear_t1if ();
  assert_rf ();
  while (!t1if_is_asserted());
  deassert_rf ();
}


/**********************************************************************
 *
 * Name:        send_rf_space
 *
 * Purpose:     Sends an RF space.
 *
 * Parameters:  ticks   space time
 *
 * Returns:     Nothing
 *
 **********************************************************************/
void send_rf_space (uint16 ticks)
{
  set_timer1 (0xFFFF - ticks);
  clear_t1if ();
  while (!t1if_is_asserted());
}
 


/********************************************************************
 *
 * L O W - L E V E L   R F   I N P U T   F U N C T I O N
 *
 ********************************************************************
 */

/**********************************************************************
 *
 * Name:        find_space
 *
 * Purpose:     Waits for a space to end with timeout.
 *
 * Parameters:  space_timeout  wait timeout
 *
 * Returns:     1 if space is less than timeout, 0 otherwise
 *
 **********************************************************************/
bool find_space (uint16 space_timeout)
{
  // Look for end of space
  set_timer1 (0);

  while (!rf_asserted ())
  {
    if (get_timer1 () > space_timeout) return 0;  // Timeout
  }

  return 1;                                       // Success
}


/**********************************************************************
 *
 * Name:        find_mark
 *
 * Purpose:     Waits for a mark to end with timeout.
 *
 * Parameters:  mark_timeout  wait timeout
 *
 * Returns:     1 if mark is less than timeout, 0 otherwise
 *
 **********************************************************************/
bool find_mark (uint16 mark_timeout)
{
  // Look for end of mark
  set_timer1 (0);

  while (rf_asserted ())
  {
    if (get_timer1 () > mark_timeout) return 0;   // Timeout
  }

  return 1;                                       // Success
}


/**********************************************************************
 *
 * Name:        return_space_len
 *
 * Purpose:     Returns the length of a space signal in units of ticks.
 *              Assumes a deasserted state (space) when called.
 *
 * Parameters:  space_timeout  wait timeout
 *
 * Returns:     Space length in ticks or 0xFFFF if not space or timeout
 *
 **********************************************************************/
uint16 return_space_len (uint16 space_timeout)
{
  uint16 length;


  length = 0xFFFF;
  set_timer1 (0);

  while (!rf_asserted ())
  {
    length = get_timer1 ();
    if (length > space_timeout)
    {
      length = 0xFFFF;
      break;
    }
  }

  return length;
}


/**********************************************************************
 *
 * Name:        return_mark_len
 *
 * Purpose:     Returns the length of a mark signal in units of ticks.
 *              Assumes that an asserted state (mark) when called.
 *
 * Parameters:  mark_timeout  wait timeout
 *
 * Returns:     Mark length in ticks or 0xFFFF if not mark or timeout
 *
 **********************************************************************/
uint16 return_mark_len (uint16 mark_timeout)
{
  uint16 length;


  length = 0xFFFF;
  set_timer1 (0);

  while (rf_asserted ())
  {
    length = get_timer1 ();
    if (length > mark_timeout)
    {
      length = 0xFFFF;
      break;
    }
  }

  return length;
}

#endif
// __RF_C__

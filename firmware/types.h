#nolist

/*
 *  TYPES.H
 *  PIC firmware library
 *
 *  Global types and definitions
 *
 *  Modification Log
 *  =====================================================================
 *    1.0  30 Jan 2003  vcs  initial version
 *
 */


#ifndef __TYPES_H__
#define __TYPES_H__


/*---------------------------------------------------------------------------*/
/* Type definitions for CCS C Compiler                                       */
/*---------------------------------------------------------------------------*/
#if (defined(__PCB__) || defined(__PCM__) || defined(__PCH__))

typedef int1 bool;
typedef signed int8 sint8;
typedef signed int16 sint16;
typedef signed int32 sint32;
typedef unsigned int8 uint8;
typedef unsigned int16 uint16;
typedef unsigned int32 uint32;

#endif /* (defined(__PCB__) || defined(__PCM__) || defined(__PCH__)) */


#endif
// __TYPES_H__

#list

#! /bin/sh
# Reload daemon when an interface comes up, to allow it to start
# listening on new addresses.

set -e

# Don't bother to restart daemon when lo is configured.
if [ "$IFACE" = lo ]; then
        exit 0
fi

# Only run from ifup.
if [ "$MODE" != start ]; then
        exit 0
fi

# Daemon only cares about inet and inet6. Get ye gone, strange people
# still using ipx.
if [ "$ADDRFAM" != inet ] && [ "$ADDRFAM" != inet6 ]; then
        exit 0
fi

if [ ! -f /var/run/wsd.pid ] || \
   [ "$(ps -p "$(cat /var/run/wsd.pid)" -o comm=)" != jsvc ]; then
        exit 0
fi

# We'd like to use 'reload' here, but it has some problems; see #502444.
if [ -x /usr/sbin/invoke-rc.d ]; then
        invoke-rc.d wsd restart >/dev/null 2>&1 || true
else
        /etc/init.d/wsd restart >/dev/null 2>&1 || true
fi

exit 0

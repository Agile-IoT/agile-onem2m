#!/bin/sh
#-------------------------------------------------------------------------------
# Copyright (C) 2018 SRC Solution / Pilot Things.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
# 
# Contributors:
#     SRC Solution - Pilot Things / GFA - initial API and implementation
#-------------------------------------------------------------------------------

MODULE=${1:-all}
DEPS=`realpath ./deps`

if [ ! -e "$DEPS" ]; then
  echo "Installing dependencies"
  ./scripts/install-deps.sh
fi


TOEXPORT=""

if [ ! -z "$DISPLAY" ]; then
  echo ">> DISPLAY available, reusing current display"
else
  export DISPLAY=:0
  TOEXPORT="\n$TOEXPORT\nexport DISPLAY=$DISPLAY"
fi

ME=`whoami`

if [ ! -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
  echo ">> DBUS_SESSION_BUS_ADDRESS available, reusing current instance"
else

  if [ `pgrep -U $ME dbus-daemon -c` -gt 0 ]; then

    echo ">> DBus session available"

    MID=`sed "s/\n//" /var/lib/dbus/machine-id`
    DISPLAYID=`echo $DISPLAY | sed "s/://"`
    SESSFILEPATH="/home/$ME/.dbus/session-bus/$MID-$DISPLAYID"

    if [ -e $SESSFILEPATH ]; then
      echo ">> Loading DBus session instance address from local file"
      echo ">> Source: $SESSFILEPATH"
      . "$SESSFILEPATH"
    else
      echo "Cannot get Dbus session address. Panic!"
    fi

  else
    export `dbus-launch`
    sleep 2
    echo "++ Started a new DBus session instance"
  fi

fi

TOEXPORT="\n$TOEXPORT\nexport DBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"

if [ -z "$DBUS_SESSION_BUS_ADDRESS" ]; then
  echo "!! Cannot export DBUS_SESSION_BUS_ADDRESS. Exit"
  exit 1
fi
export DBUS_SESSION_BUS_ADDRESS

export MAVEN_OPTS_BASE="-Djava.library.path=$DEPS:$DEPS/lib -DDISPLAY=$DISPLAY -DDBUS_SESSION_BUS_ADDRESS=$DBUS_SESSION_BUS_ADDRESS"
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DEPS:$DEPS/lib:/usr/lib:/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/jre/lib/arm

mvn="mvn"

if [ $MODULE = 'all' ] || [ $MODULE = 'om2m' ]; then
  pkill -f org.eclipse.equinox.launcher
  cd om2m/ && sh service.sh && cd ../
  sleep 60
  ./scripts/stop.sh "protocol.om2m"
  java -cp org.eclipse.agail.protocol.ONEM2M/target/agile-om2m-protocol-1.0.0-jar-with-dependencies.jar -Djava.library.path=deps:deps/lib org.eclipse.agail.protocol.om2m.OneM2MProtocol &
  
  echo "Started AGILE om2m protocol"
fi


echo "Module launched use this variables in the shell:"
echo $TOEXPORT
echo ""

wait

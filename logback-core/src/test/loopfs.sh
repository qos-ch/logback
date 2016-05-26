#!/bin/bash

IMG=/tmp/toto.img
MNT_POINT=/mnt/loop
LOOP_DEV=/dev/loop0
BIG_FILE=$MNT_POINT/big

SETUP_LOG=/tmp/loopfs.log
LOG=/tmp/loopfs.log

CMD=$1

# ===================================
setup()  {
  echo setup
  if [ -e $IMG ]; then
    rm $IMG
  fi 
  if [ -e $LOG ]; then rm $LOG; fi
  if [ -e $SETUP_LOG ]; then rm $SETUP_LOG; fi

  dd if=/dev/zero of=$IMG count=1000 >> $SETUP_LOG 2>&1
  losetup $LOOP_DEV $IMG >> $SETUP_LOG 2>&1
  mkfs.ext2 $LOOP_DEV >> $SETUP_LOG 2>&1
  mount $LOOP_DEV $MNT_POINT  >> $SETUP_LOG 2>&1
  chown -R ceki:ceki $MNT_POINT
}

shake() { 
  echo "shake"
  declare -i i=0
  while [ $i -lt 5 ]
  do 
    i+=1;  
    sleep 0.5
    dd if=/dev/zero of=$BIG_FILE count=1000 >> $LOG 2>&1
    echo "dd `date`" >> $LOG
    sleep 0.5;
    rm $BIG_FILE >> $LOG 2>&1
    echo "rm $BIG_FILE" >> $LOG
  done
}

teardown()  {
  echo teardown    
  umount $MNT_POINT
  losetup -d $LOOP_DEV
}

# ===========================================

case $CMD in 
  setup)
    setup
    ;;
  shake)
    shake
    ;;
   teardown)
    teardown
    ;;
esac

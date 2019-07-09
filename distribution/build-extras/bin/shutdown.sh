# stop daemon    
if [ `which apk` ]; then
   pid=`ps uax | grep BINDAAS_INSTANCE | head -n 1 | awk '{print $1}'`
else
   pid=`ps uax | grep BINDAAS_INSTANCE | head -n 1 | awk '{print $2}'`   
fi

echo "Killing $pid"
kill -9 $pid

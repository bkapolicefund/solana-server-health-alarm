#!/bin/sh
cd /opt/chksvr/
java -jar /opt/chksvr/solana-server-health-alarm.jar http://<my validator ip address>:8899 http://<email server ip address>:25 myemailaddress@mydomain.com

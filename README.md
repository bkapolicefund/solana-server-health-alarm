This is a Java application that will email you when it sees
that the validator node is not caught up and has bad health.

A sample shell script to call this is seen in this repo as the file chksvr.sh 
and you will see it uses the URL of your server and port number as the
first command line parameter. Then your email address is the second parameter.

First make a dir on ubuntu 20 of /opt/chksvr/

Place full contents of the /dist/ directory in this /opt/chksvr and be sure
you have the lib directory in there so that it is /opt/chksvr/lib or the java
app will not see its library jar files in this lib directory.

You must add a line to your crontab like this if you want to check
your server every 10 minutes and it will email you till you fix it
every ten minutes:

########################################################
1,11,21,31,41,51 * * * * root /opt/chksvr/chksvr.sh > /dev/null
#######################################################

Note that a Solana DEV says to query solana validators
and check the delinquency field for your node so we need 
another app to do this with a solana Shell call inside java

NOTE: YOU MUST INSTALL THE JAVA JDK AND THE POSTFIX MTA

BUT BEFORE YOU SET UP POSTFIX YOU SHOULD SET THE HOSTNAME AND
MAILNAME FIRST LIKE THIS

put the domain name only with no subdomain in the /etc/hostname file

like this

    echo 'mydomain.com' > /etc/hostname

then set the hostname in the server memory

    hostname -F /etc/hostname

THEN ADD THE POSTFIX MTA

    apt install postfix openjdk-11-jdk

be sure to set the mailname of your validator postfix mta to 
only the domain name that you own and do not put subdomains
or the postfix mta will have all email sent bounced back to it

the postfix install will ask you what to use as the server mail name
and this must be only mydomain.com and do not put sol.mydomain.com or
all email will bounce sent from it.

set up the postfix mta to relay all email to your email server 
if you like and this is easy to do in ubuntu 20 when you install 
it as the install will ask you what kind of server you are setting
up and just select the internet server type if you do not want to 
relay the emails to your email server directly then. one of the
postfix setup choices will be to set up to relay all email to your
own email server too.

you may have trouble sending email from your validator node for three reasons:

1)
one reason is that you must set the mailname of this validator server to a different
domain name than the domain name of the emails that you will send the alarm emails to
or the email will get routed inside your validator node and never relayed to the real
email server for your domain name of your alarm email.
Just buy a $4 dollar domain at namecheap.com like mexi.co or another if thats not available

2)
one is your Datacenter may filter port 25 outbound to stop email from your server
unless you ask the NOC to open your server port 25 out.

3)
another is you must set the hostname and mailname of your server node to 
a real domain name that you own so that your email server will accept your
emails from your validator node postfix MTA

set the ubuntu 20 hostname like this:

put the domain name only with no subdomain in the /etc/hostname file

like this

    echo 'mydomain.com' > /etc/hostname

then set the hostname in the server memory

    hostname -F /etc/hostname

then restart postfix

    systemctl restart postfix

#!/usr/bin/env python

from datetime import date
import commands
import os
now = date.today()

#this is an example of a backup script for the mysql databases
# you should be able to run this from a cron job
# bdc34 2006-07-14

##################################### 
### MySQL Configuration Variables ### 
##################################### 

# MySQL Hostname 
DBHOST = 'localhost' 
# MySQL Username 
DBUSER = 'backup' 
# MySQL Password 
DBPASSWD = '4.0.2feb' 

#databases to NOT backup
dont_backup = ['mysql','pacinventory','test','Database']

#backup directory
BACKUPDIR=now.strftime("/usr/local/backups/mysql/%Y.%m/")

#mysqldump program with path
MYSQLDUMP = '/usr/local/mysql/bin/mysqldump'
MYSQL = '/usr/local/mysql/bin/mysql'

#mysqldump options
MYOPTS = ''

########## end configuration ##########
#backup file suffix
SUFFIX=now.strftime("%Y.%m.%d")

#commands
mysqlCmd = MYSQL + ' -u'+DBUSER+' -p'+DBPASSWD+' -h'+DBHOST
dumpCmd = MYSQLDUMP + ' -u'+DBUSER+' -p'+DBPASSWD+' -h'+DBHOST + ' '

# get a list of databases
CMD = 'echo "show databases" | ' + mysqlCmd
failure, DBS = commands.getstatusoutput( CMD )
if failure: print 'could not run command: ', CMD

#makd the dir
try:
    os.mkdir( BACKUPDIR )
    os.chmod( BACKUPDIR, 0770 ) #notice that 0770 is an octal literal
except OSError:
    pass
    
for DATABASE in DBS.split('\n') :
    if DATABASE not in dont_backup :        
        filename = BACKUPDIR + SUFFIX + '-' + DATABASE + '.sql'
        cmd = dumpCmd + DATABASE + ' > ' + filename
        failure,out = commands.getstatusoutput( cmd )
        if failure: print 'could not run command: ' ,cmd , '\n' , out
        
        # make backups only available to owner for security; passwords etc.
        os.chmod( filename , 0400 )

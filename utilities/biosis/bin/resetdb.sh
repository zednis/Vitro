echo 'drop database vivo3;' | mysql -uroot -pRedRed  && echo 'dropped'
echo 'create database vivo3;' | mysql -uroot -pRedRed && echo 'created'
mysql -uroot -pRedRed vivo3 < $@ && echo 'loaded'
echo "update users set md5password = upper(md5('RedRed')) where username = 'brian';" | mysql -uroot -pRedRed vivo3 && echo 'brian set'

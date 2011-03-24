#what the heck are these things?
#what standard defines them?
#I know the ascii part but what is this encodeing called?
#if you know email me, bdc34@cornell.edu and briancaruso@gmail.com
#there must be a better way to do this

s/%%/% /g

s/%20/ /g
s/%21/!/g
s/%22/"/g
s/%23/#/g
s/%24/$/g
s/%25/%/g
s/%26/\&/g
s/%27/'/g
s/%28/(/g
s/%29/)/g
s/%2A/*/g
s/%2B/+/g
s/%2C/,/g
s/%2D/-/g
s/%2E/./g
s|%2F|/|g
s/%30/0/g
s/%31/1/g
s/%32/2/g
s/%33/3/g
s/%34/4/g
s/%35/5/g
s/%36/6/g
s/%37/7/g
s/%38/8/g
s/%39/9/g
s/%3A/:/g
s/%3B/;/g
s/%3C/</g
s/%3D/=/g
s/%3E/>/g
s/%3F/?/g
s/%40/@/g
s/%5B/[/g
s/%5C/\\/g
s/%5D/]/g
s/%5E/^/g
s/%5F/_/g
s/%60/`/g
s/%7B/{/g
s/%7C/|/g
s/%7D/}/g
s/%7E/~/g

s/% /%/g
s/+/ /g
# convert dates
# corgie.mannlib.cornell.edu - - [14/Mar/2005:12:55:17 -0500] "HEAD / HTTP/1.0" 200 - "-" "BigBrother/1.8c"
# to
# corgie.mannlib.cornell.edu - - [2005-03-14 12:55:17] "HEAD / HTTP/1.0" 200 - "-" "BigBrother/1.8c"

#convert the months
s|/Jan/|/01/|g
s|/Feb/|/02/|g     
s|/Mar/|/03/|g
s|/Apr/|/04/|g
s|/May/|/05/|g
s|/Jun/|/06/|g
s|/Jul/|/07/|g
s|/Aug/|/08/|g
s|/Sep/|/09/|g
s|/Oct/|/10/|g
s|/Nov/|/11/|g
s|/Dec/|/12/|g

#swap around the timestamp fields
s|\[\([0-9]*\)/\(..\)/\([0-9]*\):\(..:..:..\) -....\]|[\3-\2-\1 \4]|g

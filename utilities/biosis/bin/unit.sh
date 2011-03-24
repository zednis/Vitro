# this is the unit test 
# the diffs from the find should show no differences
# between the baseline logs and the generated logs

# it will build the classes,
# drop the database indicated by props file
# load the test.sql database
# run the biosis load
# and compare the outputs

# This test uses the following files:
# sql/test.sql
# logs/*.log.baseline
# data/baseline.dat

rm -f logs/*.log
ant clean
ant compile && \
./resetdb.sh sql/test.sql && \
ant run -Drun.class=ImportBiosis -Drun.args="data/baseline.dat true"  && \
/usr/bin/find.exe logs -name "*.log" -exec diff --brief {} {}.baseline \;

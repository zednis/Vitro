#!/bin/bash

echo "drop database calsdump; create database calsdump;" | mysql -uroot -pRedRed 
 mysql -uroot -pRedRed calsdump < calsdumpDBmodel.sql

#!/bin/bash

echo "downloading the dbdump file  $2 from s3"

aws s3 cp  s3://appkube/security-service/$2 .

export PGPASSWORD='postgres'

echo "Dropping $1"
echo password | sudo -S -u postgres psql -c "DROP DATABASE $1"

echo "Creating $1"

echo password | sudo -S -u postgres psql -c "CREATE DATABASE $1"

echo "Importing $2 into $1"

echo password | sudo -S -u postgres psql  -d $1 -f $2
echo "All done!!"

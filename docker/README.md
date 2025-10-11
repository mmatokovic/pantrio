Connecting from Within the Container
You can connect to the Oracle Database server by running a SQL*Plus command from within the container using one of the following commands:

$ docker exec -it <oracle-db> sqlplus / as sysdba
$ docker exec -it <oracle-db> sqlplus sys/<your_password>@XE as sysdba
$ docker exec -it <oracle-db> sqlplus system/<your_password>@XE
$ docker exec -it <oracle-db> sqlplus pdbadmin/<your_password>@XEPDB1

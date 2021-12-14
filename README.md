# r1c1

## How to Compile

```
$ sbt clean compile
```

## How to Run

```
$ sbt [ -Dcsv.output=/tmp/test.csv ] "run /path/to/properties"
```

If `csv.output` is not specified, it will write to "r1c1.csv" in the
working directory by default.

## Configuration

Queries and connection information are organized by alias, which consists of
alphanumeric characters or underscores.  We will annotate it as *db_alias*
below to facilitate our description.  We will use *sql_alias* for similar
purpose, but for SQL statements.

| Property Name              | Description                    | Example                                    |
|----------------------------|--------------------------------|--------------------------------------------|
| *db_alias*.url             | Connection URL to the database | "jdbc:mysql://localhost:3306/my\_database" |
| *db_alias*.user            | Username (optional)            | "joe"                                      |
| *db_alias*.password        | Password (optional)            | "mysecret"                                 |
| *db_alias*.sql.*sql_alias* | SQL statement                  | "SELECT COUNT(\*) FROM some\_tbl"          |


Here is a more complete example:

```
mysql.url=jdbc:mysql://localhost:3306/gamedb
mysql.user=ro_mysql_user
mysql.password=secret
mysql.sql.user_count=SELECT COUNT(*) FROM regular_user
mysql.sql.game_count=SELECT COUNT(*) FROM game

psql.url=jdbc:postgresql://localhost:5432/gamesdb
psql.user=ro_psql_user
psql.password=ro_password
psql.sql.user_count=SELECT COUNT(*) FROM gamesdb.regular_user
psql.sql.game_count=SELECT COUNT(*) FROM gamesdb.game

hive.url=jdbc:hive2://10.10.1.42:10000/default
hive.user=ro_hive_user
hive.sql.user_count=SELECT COUNT(*) FROM gamedb.regular_user
hive.sql.game_count=SELECT COUNT(*) FROM gamedb.game
```

### SSH Port-Forwarding

In many cases nowadays, databases are usually protected behind some sort of
bastion servers. When we need to access databases in that kind of setups, we
will to connect through SSH port-forwarding, also known as SSH tunneling.

The following configurations are designed for the purpose:

| Property Name                       | Description                              | Example                                         |
|-------------------------------------|------------------------------------------|-------------------------------------------------|
| *db_alias*.sshLogin                 | SSH login string (required)              | myuser@somebastion.com                          |
| *db_alias*.sshPortForwarding        | SSH port-forwarding string (required)    | 1234:remotehost.net:5432                        |
| *db_alias*.sshKey                   | RSA encrypted key file path              | /home/john/.ssh/id\_rsa                         |
| *db_alias*.sshPassphrase            | Key passphrase                           | secret                                          |
| *db_alias*.sshKnownHosts            | Known hosts file path                    | /home/john/.ssh/known\_hosts                    |
| *db_alias*.sshPassword              | SSH login password                       | top\_secret                                     |
| *db_alias*.sshStrictHostKeyChecking | Strict host key checking ("yes" or "no") | no                                              |

Notes:

- login and port-forwarding strings are required for SSH port-forwarding to work.
- login string has a format of "login@ssh\_host".
- port-forwarding string has to be in the form of "local\_port:remote\_host:remote\_port" or "local\_binding:local\_port:remote\_host:remote\_port".
- the use of passphrase or password is not recommended because it will be stored as plain text in the properties file.
- the use of "no" strict host key checking is not recommended.

## How to Create a Package

To generate a universal zip file, do:

```
$ sbt universal:packageBin
```

or if you want to generate a tgz file instead, do:

```
$ sbt universal:packageZipTarball
```

There are even more options that you try and experiment.  Here is the list:

| Tasks                         | Description                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `universal:packageBin`        | Generates a universal zip file                                       |
| `universal:packageZipTarball` | Generates a universal tgz file                                       |
| `debian:packageBin`           | Generates a deb                                                      |
| `docker:publishLocal`         | Builds a Docker image using the local Docker server                  |
| `rpm:packageBin`              | Generates an rpm                                                     |
| `universal:packageOsxDmg`     | Generates a DMG file with the same contents as the universal zip/tgz |
| `windows:packageBin`          | Generates an MSI                                                     |

Check out [sbt-native-packager][1] for more information if you are interested.

[1]: <https://www.scala-sbt.org/sbt-native-packager/index.html>

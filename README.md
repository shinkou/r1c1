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

Check out [sbt-native-packager](1) for more information if you are interested.

[1]: <https://www.scala-sbt.org/sbt-native-packager/index.html>

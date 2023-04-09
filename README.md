# Rule Evaluator

Rule Evaluator is a command-line application written in Scala 3 that allows you to evaluate a set of conditions on a CSV file
Rule Evaluator
This application is currently a work in progress.

Rule Evaluator is a Scala 3 application that allows you to evaluate rules on a CSV file.

# Usage
To use Rule Evaluator, you must have a CSV file with your data and a set of conditions that you want to evaluate for each row of data. Conditions should be in the format of [columnName] operator value, where operator can be any of the following: `=`, `!=`, `<`, `>`, `<=`, `>=`.

Example conditions:
```
[columnA] > 15 AND [columnB] == 'value'
[columnC] < 5 OR [columnD] != 'foo'
```

The application will then evaluate each row in the CSV file against the specified conditions and output the result for each row. If a row fails the conditions, the application will also output a list of reasons why the conditions failed.

# Development
To build and test the application, you can use the following commands:

```
$ sbt compile     # compile the application
$ sbt test        # run tests
```

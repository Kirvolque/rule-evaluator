[![Language](https://img.shields.io/badge/language-scala-brightgreen.svg)](https://www.scala-sbt.org/)
[![License](http://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/Kirvolque/rule-evaluator)

# Rule Evaluator

Rule Evaluator is a command-line application written in Scala 3 that allows you to evaluate a set of conditions on a CSV file
Rule Evaluator
This application is currently a work in progress.

Rule Evaluator is a Scala 3 application that allows you to evaluate rules on a CSV file.

This project was developed with the assistance of the GPT-3.5-based language model called ChatGPT, which was used to generate tests, documentation, and some parts of the code.

## Usage
To use Rule Evaluator, you must have a CSV file with your data and a set of conditions that you want to evaluate for each row of data.

### Example Run Command
```
sbt run --rule rules.txt --csv example.csv
```

### Example Rule File
```
[Column1] = 1 AND [Column2] = "abc"
```


### Example CSV File
```
Column1,Column2,Column3
1,abc,2
2,cbe,3
```

### Example Output
For the above example rule and CSV files, the output would be:

```
row: 1 status: Pass
row: 2 status: Fail: Column1 | Column2
```
The application will evaluate each row in the CSV file against the specified conditions and output the result for each row. If a row fails the conditions, the application will also output a list of reasons why the conditions failed.
## Technologies Used

This project uses the following technologies:

- [Scala 3](https://docs.scala-lang.org/scala3/) - The programming language used to write the application.
- [sbt](https://www.scala-sbt.org/) - The build tool used to manage dependencies and build the project.
- [ScalaTest](https://www.scalatest.org/) - The testing framework used to write and run tests.
- [Cats](https://typelevel.org/cats/) - The library used to provide type classes such as `Monoid` for functional programming in Scala.
- [ZIO](https://zio.dev/) - The library used for managing side effects and concurrency.


## Development
To build and test the application, you can use the following commands:

```
$ sbt compile     # compile the application
$ sbt test        # run tests
```

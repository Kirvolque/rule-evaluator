package ruleevaluator

import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite
import ruleevaluator.csv.CsvRow
import ruleevaluator.exception.{CharacterNotFoundException, InvalidRuleSyntaxException, NoSuchFieldException}
import ruleevaluator.rule.{RuleLine, RulesFileContent}

class RuleEvaluatorTest extends AnyFunSuite with Matchers {

  test("should return successful result on true conditions") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2")
    )
    val conditions = createConditions(
      "([field1] = \"1\")", "[field1] <= 1"
    )
    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Right(result) =>
        result.successful shouldBe true
      case Left(error) =>
        fail(s"Expected successful result, but got error: ${error.getMessage}")
    }
  }

  test("should return unsuccessful result if one of conditions is false") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2")
    )
    val conditions = createConditions("[field1] = \"1\"", "[field2] > 2")
    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Right(result) =>
        result.successful shouldBe false
        result.failReasons.size shouldBe 1
        result.failReasons should contain("field2")
      case Left(error) =>
        fail(s"Expected unsuccessful result, but got error: ${error.getMessage}")
    }
  }

  test("AND operator should have higher priority than OR operator") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla")
    )
    val conditions = createConditions("[field1] = 1 OR [field2] = 2 AND [field3] != \"bla\"")
    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Right(result) =>
        result.successful shouldBe true
      case Left(error) =>
        fail(s"Expected successful result, but got error: ${error.getMessage}")
    }
  }

  test("expression in brackets should have highest priority") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla")
    )

    val condition1 = createConditions("([field1] = 1 OR [field2] = 3) AND [field3] = \"bla\"")
    val condition2 = createConditions("([field1] = 1 OR [field2] = 3) AND ([field3] = 2 OR [field3] = \"bla\")")

    val eitherResult1 = RuleEvaluator.checkRules(condition1, csv)
    val eitherResult2 = RuleEvaluator.checkRules(condition2, csv)

    eitherResult1 match {
      case Right(result1) =>
        result1.successful shouldBe true
      case Left(error) =>
        fail(s"Expected successful result, but got error: ${error.getMessage}")
    }

    eitherResult2 match {
      case Right(result2) =>
        result2.successful shouldBe true
      case Left(error) =>
        fail(s"Expected successful result, but got error: ${error.getMessage}")
    }
  }

  test("should fail if closing bracket is absent") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla")
    )
    val conditions = createConditions(
      "[field1] = 1 OR [field2] = 3 AND ([field2] = \"bla\"" // Отсутствует закрывающая скобка ')'
    )

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error: CharacterNotFoundException) =>
        error.getMessage should include("Missing ')' in line 1") // Исправлено сообщение
      case Left(error) =>
        fail(s"Expected CharacterNotFoundException, but got: ${error.getClass.getSimpleName}")
      case Right(_) =>
        fail("Expected failure, but got a successful result")
    }
  }

  test("should fail if argument is not used") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "1")
    )
    val conditions = createConditions("[field1] [field2]") // Отсутствует оператор между полями

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error: InvalidRuleSyntaxException) =>
        error.getMessage should include("Invalid condition in line 1.") // Исправлено сообщение
      case Left(error) =>
        fail(s"Expected InvalidRuleSyntaxException, but got: ${error.getClass.getSimpleName}")
      case Right(_) =>
        fail("Expected failure, but got a successful result")
    }
  }

  test("should fail if field with specified name is absent in CSV") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla")
    )
    val conditions = createConditions("[nonexistent field] = 1") // Поле отсутствует в CSV

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error: NoSuchFieldException) =>
        error.getMessage should include("CSV file doesn't have field") // Сообщение тут соответствует вашему исключению
      case Left(error) =>
        fail(s"Expected NoSuchFieldException, but got: ${error.getClass.getSimpleName}")
      case Right(_) =>
        fail("Expected failure, but got a successful result")
    }
  }

  test("should fail if operator has no argument") {
    val csv = createCsv(
      ("field1", "1")
    )
    val conditions = createConditions(
      "[field1] = 1 OR" // Отсутствует аргумент для оператора OR
    )

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error: InvalidRuleSyntaxException) =>
        error.getMessage should include("Missing Argument(s) for operator: Or in line: 1.") // Исправлено сообщение
      case Left(error) =>
        fail(s"Expected InvalidRuleSyntaxException, but got: ${error.getClass.getSimpleName}")
      case Right(_) =>
        fail("Expected failure, but got a successful result")
    }
  }

  // Остальные тесты следует адаптировать в похожем виде...

  private def createConditions(lines: String*): RulesFileContent = {
    val conditionLines: List[RuleLine] = lines.indices
      .map(i => RuleLine(i + 1, lines(i)))
      .toList
    new RulesFileContent(conditionLines)
  }

  private def createCsv(fields: (String, String)*): CsvRow = {
    new CsvRow(fields.toMap)
  }
}

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
        fail(s"Expected successful result, but got error: ${error.message}")
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
        fail(s"Expected unsuccessful result, but got error: ${error.message}")
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
        fail(s"Expected successful result, but got error: ${error.message}")
    }
  }

  test("should fail if closing bracket is absent") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla")
    )
    val conditions = createConditions(
      "[field1] = 1 OR [field2] = 3 AND ([field2] = \"bla\""
    )

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error) =>
        error.message should include("Missing ')' in line 1")
      case Right(_) =>
        fail("Expected failure, but got a successful result")
    }
  }

  test("should fail if argument is not used") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "1")
    )
    val conditions = createConditions("[field1] [field2]")

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error) =>
        error.message should include("Invalid condition in line 1.")
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
    val conditions = createConditions("[nonexistent field] = 1")

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error) =>
        error.message should include("CSV file doesn't have field")
      case Right(_) =>
        fail("Expected failure, but got a successful result")
    }
  }

  test("should fail if operator has no argument") {
    val csv = createCsv(
      ("field1", "1")
    )
    val conditions = createConditions(
      "[field1] = 1 OR"
    )

    val eitherResult = RuleEvaluator.checkRules(conditions, csv)

    eitherResult match {
      case Left(error) =>
        error.message should include("Missing Argument(s) for operator: Or in line: 1.") // Проверяем сообщение
      case Right(_) =>
        fail("Expected failure, but got a successful result")
    }
  }
  
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
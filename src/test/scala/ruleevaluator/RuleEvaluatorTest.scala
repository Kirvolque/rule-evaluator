package ruleevaluator

import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite
import ruleevaluator.csv.CsvRow
import ruleevaluator.exception.{CharacterNotFoundException, InvalidConditionException, MissingArgumentException, NoSuchFieldException}
import ruleevaluator.rule.{RuleLine, RulesFileContent}

class RuleEvaluatorTest extends AnyFunSuite with Matchers {

  test("should return successful result on true conditions") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"))
    val conditions = createConditions(
      "([field1] = \"1\")", "[field1] <= 1")
    val result = RuleEvaluator.checkRules(conditions, csv)
    result.successful shouldBe true
  }

  test("should return unsuccessful result if one of conditions is false") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"))
    val conditions = createConditions("[field1] = \"1\"", "[field2] > 2")
    val result = RuleEvaluator.checkRules(conditions, csv)
    result.successful shouldBe false
    result.failReasons.size shouldBe 1
    result.failReasons should contain("field2")
  }

  test("AND operator should have higher priority than OR operator") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla"))
    val conditions = createConditions("[field1] = 1 OR [field2] = 2 AND [field3] != \"bla\"")
    val result = RuleEvaluator.checkRules(conditions, csv)
    result.successful shouldBe true
  }

  test("expression in brackets should have highest priority") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla"))

    val condition1 = createConditions("([field1] = 1 OR [field2] = 3) AND [field3] = \"bla\"")
    val condition2 = createConditions("([field1] = 1 OR [field2] = 3) AND ([field3] = 2 OR [field3] = \"bla\")")

    val result1 = RuleEvaluator.checkRules(condition1, csv)
    val result2 = RuleEvaluator.checkRules(condition2, csv)

    result1.successful shouldBe true
    result2.successful shouldBe true
  }


  test("should work with nested expressions") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "3"))

    val condition1 = createConditions(
      "[field2] = 1 OR ([field2] = 2 AND ([field3] = 2 OR [field1] = 1))")
    val condition2 = createConditions(
      "[field2] = 1 OR ([field2] = 2 AND ([field3] = 2 AND [field1] = 1))")

    val result1 = RuleEvaluator.checkRules(condition1, csv)
    val result2 = RuleEvaluator.checkRules(condition2, csv)

    result1.successful shouldBe true
    result2.successful shouldBe false

    val failReasons = result2.failReasons
    failReasons.size shouldBe 2
    failReasons should contain allOf("field2", "field3")
  }

  test("multiple conditions should be combined with AND") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla"))
    val conditions1 = createConditions(
      "[field1] = 1 OR [field2] = 3",
      "[field2] > 1",
      "[field3] = \"bla\"")
    val conditions2 = createConditions("[field1] = 1 ", "[field2] <= 1")
    val successful = RuleEvaluator.checkRules(conditions1, csv)
    val unsuccessful = RuleEvaluator.checkRules(conditions2, csv)

    successful.successful shouldBe true
    unsuccessful.successful shouldBe false
    unsuccessful.failReasons.size shouldBe 1
    unsuccessful.failReasons should contain("field2")
  }

  test("should compare numeric fields as numbers") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "1.0"))
    val condition1 = createConditions("[field1] = [field2]")
    val condition2 = createConditions("[field1] != [field2]")
    val result1 = RuleEvaluator.checkRules(condition1, csv)
    val result2 = RuleEvaluator.checkRules(condition2, csv)

    result1.successful shouldBe true
    result2.successful shouldBe false
    result2.failReasons.size shouldBe 2
    result2.failReasons should contain allOf("field1", "field2")
  }

  test("should support negative number comparison") {
    val csv = createCsv(
      ("field1", "-1")
    )
    val condition1 = createConditions("[field1] > -5")
    val condition2 = createConditions("[field1] > 1")

    val result1 = RuleEvaluator.checkRules(condition1, csv)
    val result2 = RuleEvaluator.checkRules(condition2, csv)

    result1.successful shouldBe true
    result2.successful shouldBe false
  }

  test("should compare string fields") {
    val csv = createCsv(
      ("field1", "abc"),
      ("field2", "xyz"),
      ("field3", "123")
    )

    val condition1 = createConditions(
      "[field1] = [field2]"
    )
    val condition2 = createConditions(
      "[field1] = [field3]"
    )
    val condition3 = createConditions(
      "[field1] != [field2]"
    )

    val result1 = RuleEvaluator.checkRules(condition1, csv)
    val result2 = RuleEvaluator.checkRules(condition2, csv)
    val result3 = RuleEvaluator.checkRules(condition3, csv)

    result1.successful shouldBe false
    result1.failReasons should contain allOf("field1", "field2")
    result2.successful shouldBe false
    result2.failReasons should contain allOf("field1", "field3")
    result3.successful shouldBe true
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

    assertThrows[CharacterNotFoundException](
      RuleEvaluator.checkRules(conditions, csv)
    )
  }

  test("should fail if field with specified name is absent in CSV") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "2"),
      ("field3", "bla")
    )
    val conditions = createConditions("[nonexistent field] = 1")

    assertThrows[NoSuchFieldException](
      RuleEvaluator.checkRules(conditions, csv)
    )
  }

  test("should fail if argument is not used") {
    val csv = createCsv(
      ("field1", "1"),
      ("field2", "1")
    )
    val conditions = createConditions("[field1] [field2]")
    assertThrows[InvalidConditionException] {
      RuleEvaluator.checkRules(conditions, csv)
    }
  }

  test("should fail if operator has no argument") {
    val csv = createCsv(
      ("field1", "1")
    )
    val conditions = createConditions(
      "[field1] = 1 OR"
    )
    assertThrows[MissingArgumentException] {
      RuleEvaluator.checkRules(conditions, csv)
    }
  }

  test("should fail if there is no operator between arguments") {
    val csv = createCsv(
      ("field1", "1")
    )
    val conditions1 = createConditions("[field1] 123")

    assertThrows[InvalidConditionException] {
      RuleEvaluator.checkRules(conditions1, csv)
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

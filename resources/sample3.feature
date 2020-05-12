Feature: Step data and linebreaks in Scenarios and Scenario Outlines
  Step data is parsed correctly in Scenarios and Scenario Outlines. Also, steps
  can contain linebreaks.

  Scenario Outline: The Steps of Scenario Outlines can have step data and line breaks.
    Given a <thing> synchronized
      between Mufu and Bugu
      | thing_id | parent_id | owner_id | environment |
      | 3170     | 651       | 13       | local       |
      | 3175     | 831       | 13       | staging     |
    When the <thing> is pinched in Bugu
    Then it yelps in Mufu

    Examples: things
    | thing   |
    | blargon |
    | blorgon |

  Scenario: The steps of Scenarios can have step data and line breaks.
    Given an Inspector Spacetime
      with attributes
      | cool?   | loquacious? | precocious? |
      | mos def | nope        | indubitably |
    When he travels throught time
      | speed of light    |
      | 299 792 458 m / s |
    Then he does not travel through space

  Scenario: Some steps of Scenarios lack step data and line breaks.
    Given a cat
    When she sees a dog
    Then she maintains composure

  Scenario Outline: Some steps of Scenario Outlines lack step data and line breaks.
    Given a <thing> synchronized between Mufu and Bugu
    When the <thing> is pinched in Bugu
    Then it yelps in Mufu

    Examples: things
    | thing   |
    | blargon |
    | blorgon |

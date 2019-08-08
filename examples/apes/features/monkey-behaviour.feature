# This is a comment about this feature

@monkeys
Feature: Monkeys behave as expected
  Experimenters want to ensure that their monkey simulations are behaving
  correctly.

  # This is a comment about this scenario ...
  @fruit-reactions
  Scenario Outline: Monkeys behave as expected when offered various foods.
    Given a monkey
    When I give him a <fruit>
    Then he is <response>
    But he doesn't eat it
    And he looks at me <manner_of_looking>
   
  Examples: monkey characteristics:
  | fruit  | response  | manner_of_looking  |
  | banana | happy     | quizzically         |
  | pear   | sad       | loathingly         |
   
  # This is a comment about this scenario outline...
  @orangutan
  Scenario: Monkeys adore orangutans.
    Given a monkey
    When I present him with an orangutan
    Then he is happy


# Bonobos are ... different

@bonobos
Feature: Bonobos behave in their own way
  Experimenters want to ensure that their bonobo simulations are behaving
  correctly.

  @fruit-reactions
  Scenario Outline: Bonobos behave as expected when offered various foods.
    Given a bonobo
    When I give him a <fruit>
    Then he is <response>
    But he doesn't eat it
    And he looks at me <manner_of_looking>
   
  Examples: bonobo characteristics:
  | fruit  | response    | manner_of_looking  |
  | banana | indifferent | indifferently      |
  | pear   | indifferent | indifferently      |
   
  # This is a comment about this scenario outline...
  @orangutan
  Scenario: Bonobos are indifferent to orangutans.
    Given a bonobo
    When I present him with an orangutan
    Then he is moderately nonchalantly bemused

# This is a comment about this feature

@chimpanzees
Feature: Chimpanzees behave as expected
  Experimenters want to ensure that their chimpanzee simulations are behaving
  correctly.

  # This is a comment about this scenario ...
  @fruit-reactions
  Scenario Outline: Chimpanzees behave as expected when offered various foods.
    Given a chimpanzee
    When I give him a <fruit>
    Then he is <response>
    But he doesn't eat it
    And he looks at me <manner_of_looking>

  Examples: chimpanzee characteristics:
  | fruit  | response  | manner_of_looking  |
  | banana | happy     | quizzically        |
  | pear   | sad       | loathingly         |

  # This is a comment about this scenario outline...
  @orangutan
  Scenario: Chimpanzees adore orangutans.
    Given a chimpanzee
    When I present him with an orangutan
    Then he is happy


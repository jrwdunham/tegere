@chimpanzees
Feature: Chimpanzees behave as expected
  Experimenters want chimpanzee sims to behave correctly.

  @fruit-reactions
  Scenario: Chimpanzees behave as expected when offered various foods.
    Given a chimpanzee
    When I give him a papaya
    Then he is happy

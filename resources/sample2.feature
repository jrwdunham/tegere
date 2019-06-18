# HTML Email Generation Feature
#
# To run this feature file, supply the tag for this feature and the URL and
# access token for a running Document Generator Service instance::
#
#     $ behave \
#           --tags=email-gen \
#           -D dgs_url=http://127.0.0.1:61780/micros/dgs/v1/api/ \
#           -D dgs_access_token=<DGS_TOKEN> \
#
# To target a specific template, e.g., the
# ar_friendly_reminder_consolidated_email template row of the "templates and
# contexts" table), add the following tag::
#
#           --tags=template.template_key.ar_friendly_reminder_consolidated_email \

@email-gen
Feature: HTML Email Generation
  Clients of the Document Generation and Distribution APIs want to be able to
  generate HTML email documents using the Document Generator Service (DGS).

  @generate-ar-cons-emails @template.template_key.<template_key>
  Scenario Outline: Myrtle wants to generate the Accounts Receivable (AR) HTML email documents using the DGS and confirm that the generated documents have the expected properties.
    Given a DGS instance containing an up-to-date template <template_key>, including its template dependencies
    When a document of type <output_type> is generated from template <template_key> using data context <context_path>
    Then the generated document is stored in the MDS
    And the generated document is rendered correctly

    Examples: templates and contexts
    | template_key                               | output_type | context_path                           |
    | ar_friendly_reminder_consolidated_email    | text/html   | ar-friendly-reminder-consolidated.json |
    | ar_past_due_consolidated_email             | text/html   | ar-past-due-consolidated.json          |
    | ar_demand_consolidated_email               | text/html   | ar-demand-consolidated.json            |
    | ar_final_warning_consolidated_email        | text/html   | ar-final-warning-consolidated.json     |
    | ar_final_notice_consolidated_email         | text/html   | ar-final-notice-consolidated.json      |

  @generate-ar-gen-cons-emails @template.template_key.<template_key>
  Scenario Outline: Mikhail wants to generate the Accounts Receivable (AR) general invoice notice HTML email documents using the DGS and confirm that the generated documents have the expected properties.
    Given a DGS instance containing an up-to-date template <template_key>, including its template dependencies
    When a document of type <output_type> is generated from template <template_key> using data context <context_path>
    Then the generated document is stored in the MDS
    And the generated document is rendered correctly

    Examples: templates and contexts
    | template_key                               | output_type | context_path                              |
    | ar_gen_past_due_consolidated_email         | text/html   | ar-gen-past-due-consolidated.json         |
    | ar_gen_demand_consolidated_email           | text/html   | ar-gen-demand-consolidated.json           |
    | ar_gen_final_warning_consolidated_email    | text/html   | ar-gen-final-warning-consolidated.json    |
    | ar_gen_final_notice_consolidated_email     | text/html   | ar-gen-final-notice-consolidated.json     |

  @generate-ar-task-emails @template.template_key.<template_key>
  Scenario Outline: Alastor wants to generate the Accounts Receivable (AR) CSR Task HTML email documents using the DGS and confirm that the generated documents have the expected properties.
    Given a DGS instance containing an up-to-date template <template_key>, including its template dependencies
    When a document of type <output_type> is generated from template <template_key> using data context <context_path>
    Then the generated document is stored in the MDS
    And the generated document is rendered correctly

    Examples: templates and contexts
    | template_key                         | output_type | context_path                        |
    | task_ar_follow_up_email              | text/html   | task-ar-follow-up.json              |
    | task_ar_final_notice_follow_up_email | text/html   | task-ar-final-notice-follow-up.json |

  @generate-ar-gen-task-emails @template.template_key.<template_key>
  Scenario Outline: Billy wants to generate the Accounts Receivable (AR) General (Gen) CSR Task HTML email documents using the DGS and confirm that the generated documents have the expected properties.
    Given a DGS instance containing an up-to-date template <template_key>, including its template dependencies
    When a document of type <output_type> is generated from template <template_key> using data context <context_path>
    Then the generated document is stored in the MDS
    And the generated document is rendered correctly

    Examples: templates and contexts
    | template_key                             | output_type | context_path                            |
    | task_ar_gen_begin_internal_email         | text/html   | task-ar-gen-begin-internal.json         |
    | task_ar_gen_complete_internal_email      | text/html   | task-ar-gen-complete-internal.json      |
    | task_ar_gen_send_collections_email       | text/html   | task-ar-gen-send-collections.json       |
    | task_ar_gen_recall_collections_email     | text/html   | task-ar-gen-recall-collections.json     |
    | task_ar_gen_final_notice_follow_up_email | text/html   | task-ar-gen-final-notice-follow-up.json |

  @last-scenario
  Scenario: A
    Given x
    When y
    Then z



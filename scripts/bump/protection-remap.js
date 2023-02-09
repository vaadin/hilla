/* eslint-disable import/no-extraneous-dependencies,camelcase */
import meow from 'meow';

const {
  input: [response],
} = meow({ importMeta: import.meta });

const {
  required_status_checks,
  enforce_admins,
  required_pull_request_reviews,
  restrictions,
  required_linear_history,
  allow_force_pushes,
  allow_deletions,
  required_conversation_resolution,
} = JSON.parse(response);

const remapped = {
  required_status_checks: required_status_checks
    ? {
        enforcement_level: 'everyone',
        strict: required_status_checks.strict,
        contexts: required_status_checks.contexts,
      }
    : null,
  enforce_admins: enforce_admins?.enabled ?? null,
  required_pull_request_reviews: required_pull_request_reviews
    ? {
        dismissal_restrictions: required_pull_request_reviews.dismissal_restrictions
          ? {
              users: required_pull_request_reviews.dismissal_restrictions.users.map(({ login }) => login),
              teams: required_pull_request_reviews.dismissal_restrictions.teams.map(({ slug }) => slug),
            }
          : {},
        dismiss_stale_reviews: required_pull_request_reviews.dismiss_stale_reviews,
        require_code_owner_reviews: required_pull_request_reviews.require_code_owner_reviews,
        required_approving_review_count: required_pull_request_reviews.required_approving_review_count,
      }
    : null,
  restrictions: restrictions?.enabled ?? null,
  required_linear_history: required_linear_history?.enabled ?? false,
  allow_force_pushes: allow_force_pushes?.enabled ?? null,
  allow_deletions: allow_deletions?.enabled ?? false,
  required_conversation_resolution: required_conversation_resolution?.enabled ?? false,
};

process.stdout.write(JSON.stringify(remapped));

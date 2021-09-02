/* eslint-disable import/no-extraneous-dependencies,no-console,camelcase */
import { exec as _exec } from 'child_process';
import meow from 'meow';
import fetch from 'node-fetch';
import { promisify } from 'util';

const exec = promisify(_exec);

function buildProtectionBody(data) {
  const {
    required_status_checks,
    enforce_admins,
    required_pull_request_reviews,
    restrictions,
    required_linear_history,
    allow_force_pushes,
    allow_deletions,
    required_conversation_resolution,
  } = data;

  const body = {
    required_status_checks: null,
    enforce_admins: null,
    required_pull_request_reviews: null,
    restrictions: null,
    required_linear_history: false,
    allow_force_pushes: null,
    allow_deletions: false,
    required_conversation_resolution: false,
  };

  if (required_status_checks) {
    const { strict, contexts } = required_status_checks;
    body.required_status_checks = {
      strict,
      contexts,
    };
  }

  if (enforce_admins) {
    const { enabled } = enforce_admins;
    body.enforce_admins = enabled;
  }

  if (required_pull_request_reviews) {
    const {
      dismissal_restrictions,
      dismiss_stale_reviews,
      require_code_owner_reviews,
      required_approving_review_count,
    } = required_pull_request_reviews;

    body.required_pull_request_reviews = {
      dismissal_restrictions: {},
      dismiss_stale_reviews,
      require_code_owner_reviews,
      required_approving_review_count,
    };

    if (dismissal_restrictions) {
      const { users, teams } = dismissal_restrictions;
      body.required_pull_request_reviews.dismissal_restrictions = {
        users,
        teams,
      };
    }

    if (restrictions) {
      const { users, teams, apps } = restrictions;
      body.restrictions = { users, teams, apps };
    }

    if (required_linear_history) {
      const { enabled } = required_linear_history;
      body.required_linear_history = enabled;
    }

    if (allow_force_pushes) {
      const { enabled } = allow_force_pushes;
      body.allow_force_pushes = enabled;
    }

    if (allow_deletions) {
      const { enabled } = allow_deletions;
      body.allow_deletions = enabled;
    }

    if (required_conversation_resolution) {
      const { enabled } = required_conversation_resolution;
      body.required_conversation_resolution = enabled;
    }
  }

  return body;
}

const {
  input: [command],
} = meow({ importMeta: import.meta });

const { GITHUB_TOKEN, REPO, BRANCH } = process.env;

const headers = {
  Accept: 'application/vnd.github.loki-preview+json',
  Authorization: `token ${GITHUB_TOKEN}`,
};

const branchProtectionURL = `https://api.github.com/repos/${REPO}/branches/${BRANCH}/protection`;

const branchProtectionData = await fetch(branchProtectionURL, {
  method: 'GET',
  headers,
}).then((res) => res.json());

await fetch(branchProtectionURL, {
  method: 'DELETE',
  headers,
});

try {
  await exec(command);
} catch (e) {
  console.error(`The "${command}" execution failed`, e);
} finally {
  await fetch(branchProtectionURL, {
    method: 'PUT',
    headers,
    body: JSON.stringify(buildProtectionBody(branchProtectionData)),
  });
}

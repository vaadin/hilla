/* eslint-disable import/no-extraneous-dependencies,no-console,camelcase */
import { exec as _exec } from 'child_process';
import fetch from 'node-fetch';
import { promisify } from 'util';

const exec = promisify(_exec);

function remapBranchProtectionResponseToRequest({
  required_status_checks,
  enforce_admins,
  required_pull_request_reviews,
  restrictions,
  required_linear_history,
  allow_force_pushes,
  allow_deletions,
  required_conversation_resolution,
}) {
  return {
    required_status_checks: required_status_checks
      ? {
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
}

const { GITHUB_TOKEN, REPO } = process.env;

const headers = {
  Accept: 'application/vnd.github.v3+json',
  Authorization: `token ${GITHUB_TOKEN}`,
};

const branchProtectionURL = `https://api.github.com/repos/${REPO}/branches/main/protection`;

const branchProtectionData = await fetch(branchProtectionURL, {
  method: 'GET',
  headers,
}).then((res) => res.json());

const remappedBranchProtectionData = remapBranchProtectionResponseToRequest(branchProtectionData);

await fetch(branchProtectionURL, {
  method: 'PUT',
  headers,
  body: JSON.stringify({
    ...remappedBranchProtectionData,
    enforce_admins: false,
  }),
}).then((res) => res.json());

try {
  await exec(`git push origin HEAD:main`, { shell: false });
} catch (e) {
  console.error('The git push failed', e);
} finally {
  await fetch(branchProtectionURL, {
    method: 'PUT',
    headers,
    body: JSON.stringify(remappedBranchProtectionData),
  });
}

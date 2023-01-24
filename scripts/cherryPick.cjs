#!/usr/bin/env node
/**
 * This script is used for cherry-pick commits for hilla repo.
 * To run this script:
 * 1.collect committed-PRs marked with target/<branch-name>  labels
 * 2.cherry-pick the commit to the target branchs
 * 3.label the original commit PR with cherry-picked-<branch-name>
 * Exception handling:
 * - works for closed and merged PRs
 * - Commit PR labelled with {cherry-picked} or {need to pick manually} will be ignored.
 * - if cherry-pick cannot be done, the original PR will be labelled with need to pick manually
 *
 */

const axios = require('axios');
const https = require("https");
const exec = require('util').promisify(require('child_process').exec);
const { exe } = require('child_process');

let arrPR = [];
let arrTitle = [];
let arrURL = [];
let arrSHA = [];
let arrBranch = [];
let arrUser = [];
let arrMergedBy = [];

const repo = "vaadin/hilla";
const token = process.env['GITHUB_TOKEN'];
if (!token) {
  console.log(`GITHUB_TOKEN is not set, skipping PR creation`);
  process.exit(1);
}

async function getAllCommits(){
  let url = `https://api.github.com/repos/${repo}/pulls?state=closed&sort=updated&direction=desc&per_page=100`;
  try {
    const options = {
      headers:
      {
        'User-Agent': 'Vaadin Cherry Pick',
        'Authorization': `token ${token}`,
        'Content-Type': 'application/json',
      }
    };

    res = await axios.get(url, options);
    data = res.data;
    data = data.filter(da => da.labels.length > 0 && da.merged_at !== null);

    if (data.length === 0) {
      console.log("No commits needs to be picked.");
      process.exit(0);
    }
    return data;
  } catch (error) {
    console.error(`Cannot get the commits. ${error}`);
    process.exit(1);
  }
}

async function getCommit(commitURL){
  try {
    const options = {
      headers:
      {
        'User-Agent': 'Vaadin Cherry Pick',
        'Authorization': `token ${token}`,
        'Content-Type': 'application/json',
      }
    };

    res = await axios.get(commitURL, options);
    data = res.data;

    return data;
  } catch (error) {
    console.error(`Cannot get the commit. ${error}`);
    process.exit(1);
  }
}

async function filterCommits(commits){
  for (let commit of commits) {
    let target = false;
    let picked = false;
    for (let label of commit.labels){
      if(label.name.includes("target/")){
        target = true;
      }
      if(label.name.includes("cherry-picked") || label.name.includes("need to pick manually")){
        picked = true;
      }
    }
    if(target === true && picked === false){
      let singleCommit = await getCommit(commit.url);
      commit.labels.forEach(label => {
        let branch = /target\/(.*)/.exec(label.name);
        if (branch){
          console.log(commit.number, commit.user.login, commit.url, commit.merge_commit_sha, branch[1], singleCommit.merged_by.login);
          arrPR.push(commit.number);
          arrSHA.push(commit.merge_commit_sha);
          arrURL.push(commit.url);
          arrBranch.push(branch[1]);
          arrTitle.push(`${commit.title} (#${commit.number}) (CP: ${branch[1]})`);
          arrUser.push(`@${commit.user.login}`);
          arrMergedBy.push(`@${singleCommit.merged_by.login}`);
        }
      })
    }
  }
}

async function cherryPickCommits(){
  for(let i=arrPR.length-1; i>=0; i--){
    let branchName = `cherry-pick-${arrPR[i]}-to-${arrBranch[i]}-${Date.now()}`;

    await exec('git checkout main');
    await exec('git pull');
    await exec(`git checkout ${arrBranch[i]}`);
    await exec(`git reset --hard origin/${arrBranch[i]}`);

    try{
      await exec(`git checkout -b ${branchName}`);
    } catch (err) {
      console.error(`Cannot Create Branch, error : ${err}`);
      process.exit(1);
    }

    try{
      let {stdout, stderr} = await exec(`git cherry-pick ${arrSHA[i]}`);
    } catch (err) {
      console.error(`Cannot Pick the Commit:${arrSHA[i]} to ${arrBranch[i]}, error :${err}`);
      await labelCommit(arrURL[i], `need to pick manually ${arrBranch[i]}`);
      await postComment(arrURL[i], arrUser[i], arrMergedBy[i], arrBranch[i], err);
      await exec(`git cherry-pick --abort`);
      await exec(`git checkout main`);
      await exec(`git branch -D ${branchName}`);
      continue;
    }
    await exec(`git push origin HEAD:${branchName}`);

    await createPR(arrTitle[i], branchName, arrBranch[i]);
    await exec(`git checkout main`);
    await exec(`git branch -D ${branchName}`);
    await labelCommit(arrURL[i], `cherry-picked-${arrBranch[i]}`);
  }
}

async function labelCommit(url, label){
  let issueURL = url.replace("pulls", "issues") + "/labels";
  const options = {
    headers:{
      'User-Agent': 'Vaadin Cherry Pick',
      'Authorization': `token ${token}`,
    }
  };

  await axios.post(issueURL, {"labels":[label]}, options);
}

async function postComment(url, userName, mergedBy, branch, message){
  let issueURL = url.replace("pulls", "issues") + "/comments";
  const options = {
    headers:{
      'User-Agent': 'Vaadin Cherry Pick',
      'Authorization': `token ${token}`,
    }
  };

  await axios.post(issueURL, {"body":`Hi ${userName} and ${mergedBy}, when i performed cherry-pick to this commit to ${branch}, i have encountered the following issue. Can you take a look and pick it manually?\n Error Message:\n ${message}`}, options);
}


async function createPR(title, head, base){
  const payload = {title, head, base};

  return new Promise(resolve => {
    const content = JSON.stringify({ title, head, base }, null, 1)
    const req = https.request({
      method: 'POST',
      hostname: 'api.github.com',
      path: `/repos/${repo}/pulls`,
      headers: {
        'Authorization': `token ${token}`,
        'User-Agent': 'Vaadin Cherry Pick',
        'Content-Type': 'application/json',
        'Content-Length': content.length,
      },
      body: content
    }, res => {
      let body = "";
      res.on("data", data => {
        body += data;
      });
      res.on("end", () => {
        resolve(body);
      });
    });
    req.write(content)
  }).then(body => {
    const resp = JSON.parse(body);
    console.log(`Created PR '${title}' ${resp.url}`);
  });
}

async function main(){
  let allCommits = await getAllCommits();
  await filterCommits(allCommits);
  await cherryPickCommits();

}

main();

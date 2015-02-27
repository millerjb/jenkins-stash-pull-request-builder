# Jenkins Stash Pull Request Builder

A Jenkins plugin that enables building of Pull Requests from Atlassian Stash.

## Requirements

Requires Jenkins 1.509+ and Java 1.7+. Compatible with Jenkins Git plugin 2.x, may not work with other versions.

## Development

Run `mvn hpi:run` to see the plugin in action.

## Usage

Configure a single repository using Git SCM.
Set the name (e.g. origin). Do not leave blank
Set the refspec to `+refs/*:refs/remotes/origin/*`.

Configure a single branch to build.
Set it to `*/pull-requests/$STASH_PULL_REQUEST_ID/from`.

Configure a merge before build.
Set the name from above (e.g. origin). Do not leave blank
Set the branch to merge as `heads/$STASH_PULL_REQUEST_TO_REF`.

The result of the merge might create a new changeset. The commit hash of the `fromRef` is available as an environment variable
`$STASH_PULL_REQUEST_FROM_CHANGESET`.

### git-scm limitations

Unfortunately, the git-scm plugin doesn't support 1) multiple refspecs or 2) expanding environment variables with multiple branches and repositories.
The workaround for this is to shorten the refspec (so that it does not include `heads`). This means you have to have to reference the branches in a slightly more verbose manner.

### Pull Requests refspec

Stash exposes the pull request "from" branch under an additional refspec (`pull-requests`). It's unsupported, but it works.

https://answers.atlassian.com/questions/199925/creating-a-build-against-a-pull-request

## Stash Post-Receive hook

There's a Stash plugin for notifying Jenkins of commits.

https://github.com/Nerdwin15/stash-jenkins-postreceive-webhook

This triggers a POST to `git/notify?url=<url>`, which is registered by the git-scm plugin. The git-scm plugin supports registering an SCM Poller to trigger builds when that URL receives a request. However, since this plugin uses some environment variables to control what git-scm builds, it also adds support for building pull requests when that URL is hit.

The Stash plugin actually notifies Jenkins when Pull Requests are opened or rescoped (new commits). This plugin leverages that by, when it is notified by git-scm, queries Stash for a list of pull requests that have opened or been rescoped since the last build. It then triggers jobs for any pull requests that meet those criteria.

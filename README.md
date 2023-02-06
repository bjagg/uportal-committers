# uPortal-Committers -- Inactive?

## Overview

uPortal-Commiters is a small program that lists members of the 
_uPortal Committers_ that have not authored any commits in over a year.
We use this to remove them from the group in order to reduce "noise".
Any inactive committers retain their privileges and only need to request
that they be restored to the group.

GitHub API is used to check the main three GitHub organizations for
uPortal projects: _uPortal-project_, _uPortal-contrib_, and
_uPortal-attic_.

## Running uPortal-Committers

### Expectations

Note: the execution takes several minutes due to all the Github API calls.

### Set Up

Create a Github API token that has read access to the above orgs.

To make it available to this program (and others at runtime), add the token
to an alias in your user deps.edn,which is usually at `~/.clojure/deps.edn`:

```edn
{
  :aliases {
    :github-api-token {
      :jvm-opts ["-Dgithub-api-token=<your-api-token>"]}
  }
}
```

If you already have such a file with aliases, you would just add the
`github-api-token` entry into your existing `:aliases` map.

Currently the "since" date is hard-coded and should be updated as needed.

### Executing

Assuming you have configured an alias named `github-api-token` that adds
your token as a JVM option as above, run:

```sh
clj -X:github-api-token uportal-committers/run
```



# Bug reports / GitHub pull-requests

When reporting a bug or submitting a pull request, make sure you search
[JIRA](http://jira.qos.ch/browse/LOGBACK) and
[GitHub pull requests](https://github.com/qos-ch/logback/pulls)
for the same/similar issues. If you find one, feel free to add a `+1` comment
with any additional information that may help us solve the issue.

When creating a new bug report, be sure to state the following:

* Detailed steps to reproduce the bug
* The version of logback and SLF4J you are using
* Operating system and its version, any other relevant environment details


# Submitting a pull-request

## Before you begin
Is this the right place for the PR?

YES:
 * If it's a **bug fix** for logback proper (`logback-core`,
   `logback-classic`, `logback-access`)
 * If it's a **new feature** (such as an appender), and you're willing to
   submit a [signed CLA](http://logback.qos.ch/cla.txt)

NO:
 * If it's a **new feature**, but you prefer not to submit a signed CLA
 * If it's a **new feature**, and you wish to see it released sooner than
   logback's release schedule

If your PR falls into the *NO* category, please submit it to either
[`logback-extensions`](https://github.com/qos-ch/logback-extensions)
(no CLA required, shorter release cycles than logback, Apache License)
or [`logback-contrib`](https://github.com/qos-ch/logback-contrib)
(CLA required, shorter release cycles than logback, same license as logback).

**Please note that not every pull-request will be accepted even if it
meets all requirements outlined in this document.**

## Instructions
 1. [Fork](https://help.github.com/articles/fork-a-repo) the repo on GitHub.
 2. Make a [topic branch](https://github.com/dchelimsky/rspec/wiki/Topic-Branches#using-topic-branches-when-contributing-patches)
    and start hacking.
 3. If your branch becomes several commits behind master, be sure to rebase
    your change *on top* of master to avoid a merge conflict.
 3. Submit a pull-request based off your topic branch, following the patch
    rules below.
 4. If your patch is non-trivial and you haven't submitted a [signed CLA](http://logback.qos.ch/cla.txt),
    please email it to ceki@qos.ch and tony19@gmail.com with **\[logback]
    signed CLA** in the subject line. Trivial bug fixes (less than ~30 lines)
    do not require a CLA.

## Patch rules

 **P1.** The patch MUST follow the [general logback code style](#general-style-notes).
     Disable your IDE's auto-formatting for logback files (unless you're using
     the logback [`codeStyle.xml`](https://github.com/qos-ch/logback/blob/master/codeStyle.xml)
     file in Eclipse).

 **P2.** Small focused patches are preferred. The patch MUST NOT mix new features
     and bug fixes in the same pull-request. Also exclude reformatting from
     the pull-request (move that to its own commit or another pull-request).

 **P3.** Large changes to the code SHOULD be discussed with the core team first.
     Create an issue, explaining your plan, and see what we say.

 **P4.** The commit message SHOULD be formatted as described in http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html
     and indicate the appropriate JIRA key.

 **P5.** Pull-requests MUST NOT contain intermediate commits (e.g., bug fixes to
     your own patch, refactoring of your own patch), which should be [squashed and
     then force-pushed to your branch](https://github.com/edx/edx-platform/wiki/How-to-Rebase-a-Pull-Request).
     Generally, fewer commits are better. However, it's acceptable for a PR to have
     multiple commits, each implementing specific distinct changes. For example:
     
    * def5678 Add support for Foo
    * abc1234 Add unit tests for Foo
    * aaa5678 Update documentation for Foo

 **P6.** Unit tests MUST be included for behavioral changes (especially bug fixes) or new features. 

 **P7.** Pull-requests (especially bug fixes and new features) MUST include
     an update to the release notes that clearly describe your changes and
     the appropriate JIRA key. This will likely mimic your commit message.
     Don't modify the actual [release-notes file](https://github.com/qos-ch/logback/blob/master/logback-site/src/site/pages/news.html)
     since this potentially causes a merge conflict. Instead, copy your release
     notes (in raw HTML) into a comment in the PR, and a logback developer will
     take care of it.

 **P8.** Pull-requests MUST include updates to logback's documentation pages,
     where necessary. Examples where this would be required:
 * adding/removing a configuration flag to `SyslogAppender`
 * adding a new appender to `logback-classic`
 
# General style notes

Please note that most of the formatting rules are provided in
[codeStyle.xml](https://github.com/qos-ch/logback/blob/master/codeStyle.xml)
in the root directory of logback.

> **When in Rome, code like the Romans do**.

 **S1.** Use 2-space indents.

 ```java
// bad
class Foo {
   public static void main(String[] args) {
       System.out.println("hello world!");
   }
}

// good
class Foo {
  public static void main(String[] args) {
    System.out.println("hello world!");
  }
}
 ```

 **S2.** Closing-curly bracket should be on its own line. Opening-curly bracket
     should not.

 ```java
// bad
if (foo)
{
  ...
}

// bad
if (foo)
{ ... }

// bad
if (foo) {
  ... }

// good
if (foo) {
  ...
}
 ```

 **S3.** Delimit keywords and brackets with a space.

 ```java
// bad
try{
  ...
}catch(Exception e){
  ...
}

// good
try {
  ...
} catch (Exception e) {
  ...
}
 ```

 **S4.** Add logback's standard file-header to any new files.

 ```java
/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) <year>, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
  ```

The following command will automatically apply/update the license-header comments for *all* Java/Groovy files. Since this command touches more than only your modified files, be sure to exclude unrelated files from your PR.

```
mvn -P license license:format
```

 **S5.** Add javadoc for public functions (we won't fault you for skipping private
     functions unless comments are warranted).

 **S6.** Code for maintainability. We would rather a function be a couple of lines
     longer and have (for example) some [explaining variables](http://www.refactoring.com/catalog/extractVariable.html)
     to aid readability.

 **S7.** If you find that a file has two different styles in use, defer to the
     standard style notes here. You can submit a standalone PR to fix the formatting.

 **S8.** No extraneous new-lines. Only one line is necessary between elements.
 ```java
// bad
public foo() {
  ...
}



public bar() {
  ...
}



// good
public foo() {
  ...
}

public bar() {
  ...
}
```

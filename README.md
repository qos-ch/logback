
# About logback

Thank you for your interest in logback, the reliable, generic, fast
and flexible logging library for Java.

The Logback documentation can be found on the [project
web-site](https://logback.qos.ch/documentation.html) as well as under
the docs/ folder of the logback distribution.

# Java EE and Jakarta EE versions

Given that downstream users are likely to depend on either Java EE (in
the javax namespace) or on Jakarta EE (in the jakarta namespace) in
their projects, it was deemed important for logback to support both EE
alternatives.

**Version 1.3.x supports Java EE, while version 1.4.x supports Jakarta EE.** 
The two versions are feature identical.

Both 1.3.x and 1.4.x series require SLF4J 2.0.x or later.

The 1.3.x series requires Java 8 at runtime. If you wish to build
logback from source, you will need Java 9. 

The 1.4.x series requires Java 11 at build time and at runtime.

# Building logback

Version 1.3.x requires Java 9 to compile and build.

More details on building logback is documented at:

  https://logback.qos.ch/setup.html#ide

# In case of problems

In case of problems please do not hesitate to post an e-mail message
on the logback-user@qos.ch mailing list.  However, please do not
directly e-mail logback developers. The answer to your question might
be useful to other users. Moreover, there are many knowledgeable users
on the logback-user mailing lists who can quickly answer your
questions.

# Urgent issues

For urgent issues do not hesitate to [champion a
release](https://github.com/sponsors/qos-ch/sponsorships?tier_id=77436).
In principle, most championed issues are solved within 3 business days
ensued by a release.

# Pull requests

If you are interested in improving logback, great! The logback community
looks forward to your contribution. Please follow this process:

1. Please file a [bug
   report](https://logback.qos.ch/bugreport.html). Pull requests with
   an associated JIRA issue will get more attention.

   Optional: Start a discussion on the [logback-dev mailing
   list](https://logback.qos.ch/mailinglist.html) about your proposed
   change.

2. Fork qos-ch/logback. Ideally, create a new branch from your fork for
   your contribution to make it easier to merge your changes back.

3. Make your changes on the branch you hopefully created in Step 2. Be
   sure that your code passes existing unit tests.

4. Please add unit tests for your work if appropriate. It usually is.

5. Push your changes to your fork/branch in GitHub. Don't push it to
   your master! If you do it will make it harder to submit new changes
   later.

6. Submit a pull request to logback from your commit page on GitHub.


# Continous integration build status

| Branch | Last results |
| ------ | -------------|
| master | ![CI master](https://github.com/qos-ch/logback/actions/workflows/main.yml/badge.svg) |
| 1.3 branch | ![CI 1.3 branch](https://github.com/qos-ch/logback/actions/workflows/main.yml/badge.svg?branch=branch_1.3.x) |



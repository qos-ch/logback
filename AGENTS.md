
# Agent Rules

**Never commit or push changes.**

### Strict Rules:
- Do not run `git commit`, `git push`, `git add`, or any git command that modifies the repository history.
- You may edit files multiple times in one session.
- You may run tests, build commands, invoke linters to validate changes or check your work as needed.
- If you believe a commit or any other git action is needed, ask me first.

- The code is widely used. Thus, any changes must preserve backward
  compatibility, especially changes in interfaces or super classes.

Follow these rules at all times.

Maven can be located via the value of the $MAVEN_HOME environment variable. 
Java can ve located via the value of the $JAVA_HOME environment variable.

Indicate whether these values are accessible or not.

If valid, always use the values of these environment variables to
locate Maven and Java.
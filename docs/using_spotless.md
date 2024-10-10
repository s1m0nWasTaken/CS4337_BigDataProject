Using spotless (formatter)
```
user@machine repo % ./gradlew build
:spotlessJavaCheck FAILED
    The following files had format violations:
    src\main\java\com\diffplug\gradle\spotless\FormatExtension.java
        -\t\t····if·(targets.length·==·0)·{
        +\t\tif·(targets.length·==·0)·{
    Run './gradlew spotlessApply' to fix these violations.
user@machine repo % ./gradlew spotlessApply
:spotlessApply
BUILD SUCCESSFUL
user@machine repo % ./gradlew build
BUILD SUCCESSFUL
```
It automatically runs a check with gradle build
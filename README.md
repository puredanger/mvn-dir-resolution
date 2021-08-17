# mvn-dir-resolution
Repro of a maven bug in 3.8.2

# See the Repro.java source file here for the code

# Repro steps:

```shell
mvn clean compile
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
java -cp `cat cp.txt`:target/classes Repro
```

With Maven 3.8.1 libs (change the property in pom.xml to check):

```
Build.getSourceDirectory() javadoc says "The path given is relative to the project descriptor." but:
srcDir= ./src/main/java absolute=false
```

With Maven 3.8.2 libs:

```
Build.getSourceDirectory() javadoc says "The path given is relative to the project descriptor." but:
srcDir= /Users/alex.miller/code/mvn-dir-resolution/./src/main/java absolute=true
```

I suspect this is related to the change in https://issues.apache.org/jira/browse/MNG-7170

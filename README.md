# Building openBIS

## Requirements
JDK8

## Step By Step
```
git clone https://sissource.ethz.ch/sispub/openbis.git
cd installation/
./gradlew clean
./gradlew build -x test
```
## Output of the build can be found at
```
./installation/targets/gradle/distributions/openBIS-installation-standard-technologies-SNAPSHOT-rXXXXXXXXXX.tar.gz
```

## Why we disable tests to make the build?
They slowdown the time to obtain a build plus some tests could have additional environment requirements.
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.12.0/gradlew.bat" -OutFile "gradlew.bat"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.12.0/gradlew" -OutFile "gradlew"
New-Item -ItemType Directory -Force -Path "gradle\wrapper"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.12.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
$props = @"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"@
Set-Content -Path "gradle\wrapper\gradle-wrapper.properties" -Value $props

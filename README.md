# Introduction
The purpose of this project is to provide a RESTful API for the ConnectOne UX project.

[![Build status](https://dev.azure.com/rrdonnelley/shared/_apis/build/status/CustomPoint/C1UX-API%20CI)](https://dev.azure.com/rrdonnelley/shared/_build/latest?definitionId=2369)

# Prerequisites
- Java JDK 11+
- Install Maven 3.6.3+.  Simply download from [Maven Site](https://maven.apache.org/) and extract to C:\Program Files\apache-maven-3.6.3
- If using Eclipse or STS, be sure the Lombok plugin is installed (may have to do this off VPN).  See the [Project Lombok site](https://projectlombok.org/setup/eclipse).
- Azure Devops access to the private RRD Maven feed, ["shared-default"](https://dev.azure.com/rrdonnelley/shared/_packaging?_a=feed&feed=shared-default).  If you don't have an RRD Azure Devops login, use ServiceNow catalog item, "Enterprise Developer Tools" to request Stakeholder access to the "shared" project.
- Create a personal access token (PAT) from your [RRD Azure Devops profile menu](https://dev.azure.com/rrdonnelley/_usersSettings/tokens), and use it in the settings XML below.  The minimum scope required is "Packaging (Read)".
- Update or create a 'settings.xml' file in your user's home folder for '.m2', using the following:

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>shared-default</id>
      <username>rrdonnelley</username>
      <password>[personal access token here]</password>
    </server>
  </servers>
</settings>
```

# Running the Application in Eclipse
- Be sure the Spring Boot plugin is installed
- Right-click on the 'api' app in the Spring Boot Dashboard, choose "Open Config", then set the VM Arguments as follows, replacing the local CustomPoint (CP) repo path with yours:

```
-Dtt.arch.env.var=X4 
-DXS.SERVER.TOKEN=WSAD 
-DXS.SERVER.ID=1 
-Djxl.nogc=true 
-DXS.APP.LOCALE=en_US  
-Dfile.encoding=UTF-8 
-DCP_ENV=RAD 
-DENV_CODE=WSAD 
-Dcom.sun.tools.javac.main.largebranch=true
-Dtt.ParmCheckClass=com.wallace.atwinxs.framework.util.AtWinXSParmChecker 
-Dtt.PortalCookieClass=com.rrd.custompoint.framework.component.AuthServicesProcessorImpl 
-Dtt.SessionAuditClass=com.wallace.atwinxs.admin.dao.LoginAuditDAO 
-Dtt.SiteClass=com.wallace.atwinxs.framework.dao.LoginProcessDAO 
-Dtt.UserClass=com.wallace.atwinxs.framework.dao.LoginProcessDAO 
-Dorg.apache.commons.collections.enableUnsafeSerialization=true 
-DSERVER_LOG_ROOT="C:/xs2files/Logfiles" 
-Dtranslation="C:/xs2files/translation" 
-Dproperties=C:/git/custompoint_tc/custompoint/Properties 
-Dmqconnector=C:/git/custompoint_tc/custompoint/Servers/Runtimes/mq 
-Ddb2pwd_path=C:/git/custompoint_tc/custompoint/Servers/Runtimes/conf/db2_pwd.txt
```
- Then, while Config is open for 'api', go to the Classpath tab and add a User Entry that point to the CustomPoint repo's "Properties" folder using the "Advanced" button -> Add External Folder.
- Be sure to perform a Maven update by right-clicking on the project and choosing "Maven -> Update Project..."

# Running the Application via Command line
If maven is using java 8 (check with mvm -v from your maven bin folder), you must set the path to point to your Java 11 JDK.  To do that open command prompt and run 

```sh
set JAVA_HOME=C:\Program Files\Amazon Corretto\jdk11.0.3_7
```

To build the Spring Boot based web application, run the following command (or use the IDE's Maven command for 'package'):

```sh
mvn clean package "-Dspring.profiles.active=DEV"
```

To run the application:
- First, copy the "ws-startup-template.bat" file to a file named, "ws-startup.bat".
- Edit "ws-startup.bat", setting the CP_REPO_ROOT and APP_CLASSPATH variables' value to the path to your local custompoint git repo.
- Use the "ws-startup.bat" file located at the root of the repo folder to start the application.  To attach a debugger on port 5101, use the following command:

```sh
ws-startup.bat -debug 5101
```
- __NOTE:__ if you change the version in the pom.xml file, you will need to modify "ws-startup-template.bat" accordingly.

## Running the Application in Docker
To build the application Docker image, be sure Docker in WSL or Rancher Desktop (with Docker selected) is installed. To install Docker CE in WSL, ensure Ubuntu 20.04 LTS is installed in WSL, and follow [this](https://docs.docker.com/engine/install/ubuntu/) to install Docker CE.  First, build the application using;
```sh
./mvnw -DskipTests=true clean package
```
__NOTE:__ To build and run the Docker container while on VPN, you will need to set up and run the [wsl-vpnkit](https://github.com/sakai135/wsl-vpnkit).  Once downloaded, connect on VPN and then run 'wsl.exe -d wsl-vpnkit wsl-vpnkit' in a command prompt.  To use RRD DNS, you will likely need to edit the '/etc/resolv.conf' in your WSL distribution according to [this](https://github.com/microsoft/WSL/issues/1350#issuecomment-742454940).  You may also need to add the RRD root CA cert according to [this](https://ubuntu.com/server/docs/security-trust-store).

To build the Docker image using the [Maven plugin](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/#build-image), run:
```sh
./mvnw -DskipTests=true spring-boot:build-image "-Dspring-boot.build-image.imageName=c1ux-api:1.0.0-SNAPSHOT"
```
For customization options of the plugin, see [this page](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/#build-image.customization).

The CustomPoint libraries need translation property files to exist in a specific Docker volume as specified in the docker-compose.yml file.  For example:
```sh
scp rrlnx-co-apd01.rrd.com:/apps/c1ux/translation/translation-prelogin.properties /mnt/c/git/custompoint/Translation/translation-prelogin.properties
```

To run the app image in a Docker container, run:
```sh
docker-compose up -d
```

To stop the Docker container, run:
```sh
docker-compose down
```

__NOTE__: the following links detail issues that occur while on VPN when using WSL or local hyper-V linux VMs:
- https://github.com/rancher-sandbox/rancher-desktop/issues/722
- https://stackoverflow.com/questions/66444822/no-internet-connection-ubuntu-wsl-while-vpn
- https://github.com/microsoft/WSL/issues/1350#issuecomment-742454940

For more on hosting the application in Kubernetes, go [here](k8s.md).

# Security
## SAML SSO
To use SAML based SSO, be sure that the "saml2" section under "spring:security" is defined in "./src/main/resources/application.yml".

See https://www.yenlo.com/blogs/wso2is-spring-application-saml2/ and https://medium.com/digital-software-architecture/spring-boot-spring-security-with-saml-2-83d87df5b470 for reference on setting up a local SAML SSO Identity Provider (IdP).

For SAML to work in it's current state, be sure to map the 'organization' claim in the Service Provider setup.  The user must have the 'organization' set to a valid CustomPoint 'account'.

__TO-DO:__ Add code to look for CustomPoint session ID in a SAML attribute and set it in Spring session along with other SAML attributes for first name, last name, email, account, loging type - (u)ser, (p)rofile, (np) named profile - and "other params".

## Basic Auth
To use HTTP basic authentication to secure the web API/site, replace the 'oauth' section with the following Spring security settings in "./src/main/resources/application.yml":

```
spring:
  security:
    user:
      name: "currency:mstein"
      password: s0meth!ng_hard-toGue5s
```

Use the application's Swagger UI (while running), [Postman](https://www.postman.com/), or 'curl' to invoke the API.  For example, the following 'curl' command calls the health resource's "get" method:

```
curl -u username:password -X GET "https://localhost:8443/api/health" -H "accept: application/json"
```

## Open ID Connect / OAuth
To use Open ID Connect/OAuth2 to secure the web API/site, be sure that the both the 'oauthprovider' setting, and the spring security settings for 'oauth2' are specified in "./src/main/resources/application.yml".  Help for setting up Open ID Connect based security in WSO2 can be found here: https://is.docs.wso2.com/en/latest/develop/spring-boot/#register-application

Be sure to set the Service Provider (SP) client ID and secret accordingly in the application.yml file.  If running WSO2 locally, you will likely need to add the WSO2 site's SSL cert to the JDK cacerts store (THE ONE CORRESPONDING TO THE JRE USED FOR THE PROJECT).  For example:

```
"C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\keytool" -importcert -file localhost-wso2.cer -keystore "C:\Program Files\Amazon Corretto\jdk11.0.7_10\lib\security\cacerts" -alias "localhost-wso2"
```

If using the dev WSO2 site (https://dev.api.ingine.rrd.com:9443), you will likely need to add the RRD root, policy and intercept certificates to the JDK/JRE cacerts store.

Use the application's Swagger UI (while running), or [Postman](https://www.postman.com/) to invoke the API.

# To View API doc (JSON)
To view the self-generated API Swagger3 doc (JSON), start the application and navigate to:

```
https://localhost:8443/v3/api-docs
```

To view the auto-generated Swagger UI page, start the application and navigate to:

```
https://localhost:8443/swagger-ui/index.html
```

# Debugging a CustomPoint Library
## Debug using Eclipse
To debug a CustomPoint library in Eclipse, you have to import the dependency project into the workspace.  Open the java file you want to debug, set your breakpoint, right click in the editor and choose "Debug as...".  Choose "Debug configurations..." to open the Debug dialog.
 
## Debug using an IDE that attaches to debug port (e.g. VS Code)
To debug a CustomPoint library in VS Code, build and start the web app using the instructions from "Running the Application via Command line" above.  Then, open the project for the CP library and attach using port, 5101 (e.g. for VS Code, use the following 'launch.json' file):

```
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Attach to port 5101",
            "request": "attach",
            "hostName": "localhost",
            "port": 5101
        }
    ]
}
```

# SonarLint
## Installing SonarLint on your local workstation
We will be using SonarLint to see SonarQube's reports while developing in this environment.  To install SonarLint, you will first most likely have to be off of VPN, as most times, being on VPN will block access to Eclipse Marketplace.  So once you are off VPN, you can search for SonarLint in the search bar of Eclipse Marketplace (goto Help/Eclipse Marketplace on menubar).  You should see (as of 05/20/2022) SonarLint 7.4.  Click the Install and select default options.

Click accept for the Terms and Conditions and click Finish.

Click Restart Spring Tool Suite to complete installation.

Connect back to VPN.

## Installing SonarQube cert for Spring Tools Suite
Spring Tools Suite includes it's own java version to run.  We need to install the sonarqube cert to the cacerts store for Spring Tools Suite.  The cert can be found here.

[SonarQube Cert](https://drive.google.com/drive/folders/1ZVk3YrJqu3AF3t3ugonq8KADNQiKY7l2)

The cacerts file should be located in the following folder if you are using Spring Tools Suite 4.13.1 

C:\Program Files\sts-4.13.1.RELEASE\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_17.0.1.v20211116-1657\jre\lib\security

If you have a slightly new version of Spring Tools Suite, you may have to modify the path.  Copy the SonarQube Cert file to the folder listed above.  You can then open a Command Prompt as Administrator and goto the same folder listed above and run the following command:

```
..\..\bin\keytool -import -alias sonarqube -file sonar.cer -keystore cacerts
```

The password will be changeit, unless you changed it yourself.  This will install the cert.  You need to restart Spring Tools Suite (if running) once you install the cert.

## Setting up connection from SonarLint to SonarQube
You will need a token from SonarQube to connect your local SonarLint to SonarQube.  Login to SonarQube.  You should see your initials next to the Search box in SonarQube, and if you click your initials, you can select My Account.  Goto the Security tab.  You will be generating an token, so use C1UX API for the token name and click generate.  This will be the token you use to connect SonarLint to SonarQube.  You must save/remember this token.  If you forget it or need to reinstall and you didn't save the value somewhere, you will have to create a new token and replace the configuration.

Once you have the token, goto Spring Tools Suite and open the SonarLint Bindings view.  There should be one option to Setup the SonarQube connection.  Click it, and pick the SonarQube Server as the binding option.  It will then ask the URL for SonarQube, which is listed below.

```
https://sonarqube.rrd.com
```

Click next, and it will ask you to use a UserName/Password or a Token.  Select Token.  Use the token value that you setup in SonarQube and click next.

It will then ask you for the project to connect to.  Select ConnectOne UX API from the list and use the value it sends back.  Click next/finish, and you should be configured.
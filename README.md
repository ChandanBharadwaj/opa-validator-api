# opa-validator-api
Checks the syntax of openpolicyagent's policy files (.rego)

## Requirements
  * opa runtime ( opa commands should be accessable for the app runtime)
  * java 8
 
## Running the app
  * option 1 - mvn spring-boot:run
  * option 2 - mvn clean package and then java - jar *your jar in target folder*.jar

## Sample
### Success..
![Success](/Success.gif)

### Failure..
![Failure](/failure.gif)

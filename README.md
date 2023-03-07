# Create the application infrastructure 
In this exercise, you'll use Azure CLI to create the following resources:

An Azure resource group, that will contain all the resources for your application.
A PostgreSQL database server.
An Azure Spring Apps cluster, and a Spring Boot application running inside this cluster.
You need to provide some environment variables at the beginning of the script, that should be unique across Azure. We recommend you use your username and some random characters to avoid naming conflicts.

You also need to provide your local IP address to access the database from your local computer. This IP address should be an IPv4 Address. If you don't know your local IP address, you can go to the following website: https://www.whatismyip.com/

# Set the following environment variables:
```bash
	AZ_RESOURCE_GROUP=<YOUR_UNIQUE_RESOURCE_GROUP_NAME>
	AZ_DATABASE_USERNAME=<YOUR_POSTGRESQL_USERNAME>
	AZ_DATABASE_PASSWORD=<YOUR_POSTGRESQL_PASSWORD>
	AZ_LOCAL_IP_ADDRESS=<YOUR_LOCAL_IP_ADDRESS>
```
	
Once those environment variables are set, you can run the following command to create the resources:

```bash
	AZ_LOCATION=eastus
AZ_SPRING_CLOUD=spring-${AZ_RESOURCE_GROUP}
AZ_DATABASE_NAME=pgsql-${AZ_RESOURCE_GROUP}
AZ_DATABASE_USERNAME=${AZ_DATABASE_USERNAME}

az group create \
    --name $AZ_RESOURCE_GROUP \
    --location $AZ_LOCATION

az postgres server create \
    --resource-group $AZ_RESOURCE_GROUP \
    --name $AZ_DATABASE_NAME \
    --location $AZ_LOCATION \
    --sku-name B_Gen5_1 \
    --storage-size 5120 \
    --admin-user $AZ_DATABASE_USERNAME \
    --admin-password $AZ_DATABASE_PASSWORD
az postgres server firewall-rule create \
    --resource-group $AZ_RESOURCE_GROUP \
    --name $AZ_DATABASE_NAME-database-allow-local-ip \
    --server $AZ_DATABASE_NAME \
    --start-ip-address $AZ_LOCAL_IP_ADDRESS \
    --end-ip-address $AZ_LOCAL_IP_ADDRESS
az postgres server firewall-rule create \
    --resource-group $AZ_RESOURCE_GROUP \
    --name $AZ_DATABASE_NAME-database-allow-azure-ip \
    --server $AZ_DATABASE_NAME \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0
az postgres db create \
    --resource-group $AZ_RESOURCE_GROUP \
    --name demo \
    --server-name $AZ_DATABASE_NAME

az extension add --name spring-cloud
az spring-cloud create \
   --name $AZ_SPRING_CLOUD \
   --resource-group $AZ_RESOURCE_GROUP \
   --location $AZ_LOCATION \
   --sku Basic
az spring-cloud app create \
   --resource-group $AZ_RESOURCE_GROUP \
   --service $AZ_SPRING_CLOUD \
   --name application \
   --runtime-version Java_11 \
   --assign-endpoint true
```

This script will take some time to run, so you can keep it in the background and start coding the application in the meantime.

#Configure the Java application
Get the application skeleton from the https://github.com/Azure-Samples/manage-secrets-in-java-applications GitHub repository, using the git clone command:
```bash
git clone https://github.com/Azure-Samples/manage-secrets-in-java-applications.git
```


```properties
logging.level.org.springframework.jdbc.core=DEBUG

spring.datasource.url=jdbc:postgresql://${azureDatabaseName}.postgres.database.azure.com:5432/demo
spring.datasource.username=${azureDatabaseUsername}@${azureDatabaseName}
spring.datasource.password=${azureDatabasePassword}

spring.sql.init.mode=always
```

This configuration file has three variables that need to be configured:

${azureDatabaseName} is the name of the PostgreSQL database that was configured earlier in the AZ_DATABASE_NAME environment variable. Type echo $AZ_DATABASE_NAME to see it.
${azureDatabaseUsername} is the name of the database username that was configured earlier in the AZ_DATABASE_USERNAME environment variable. Type echo $AZ_DATABASE_USERNAME to see it.
${azureDatabasePassword} is the name of the database password that was configured earlier in the AZ_DATABASE_PASSWORD environment variable. Type echo $AZ_DATABASE_PASSWORD to see it.
As we've seen in the previous unit, it's a bad practice to hard-code those values in the application source code. But to test the application, you can write them temporarily and run the application:

#Test on local 
```bash
	./mvnw spring-boot:run
	curl http://localhost:8080
```

#Deploy the Java application to Azure
```bash	
	az spring-cloud app deploy \
   --resource-group $AZ_RESOURCE_GROUP \
   --service $AZ_SPRING_CLOUD \
   --name application \
   --jar-path target/*.jar
   
   curl https://$AZ_SPRING_CLOUD-application.azuremicroservices.io
   
```
Congratulations, you've successfully created a Java application that connects to a database! Now you'll need to secure the database credentials in the next units.